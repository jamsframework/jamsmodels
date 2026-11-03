#!/usr/bin/env python3
"""Maintain Zenodo creator lists from per-module author files.

Every model library directory keeps an ``authors.json`` with the people who
contributed to it. Release workflows merge these lists into the ``creators``
and ``contributors`` of a ``.zenodo.json`` file, so the Zenodo record of a
release names the people behind exactly the libraries that are part of it.

authors.json is a JSON array of Zenodo creator objects ("name" as
"Family, Given", optional "affiliation" and "orcid"). The lists are
maintained by hand. A person is an author of a module if they are named as
author of a class or component in it, or if they substantially changed one
of its files within the module (code, not comments or annotations); people
with smaller changes get "role": "contributor" (optional "type", default
"Other"). The optional "aliases" list holds git author names, e-mail
addresses or source code tags that differ from the name; it is only used by
the check and never written to .zenodo.json.

  build   merge base list(s) and module lists into the creators and
          contributors of a .zenodo.json file and list the authors of each
          module at the end of its description (all other fields are kept);
          with --modules-as contributor (used for the jams record) only the
          base lists provide creators and all module authors become
          contributors of type "Researcher"
  check   warn about commit authors that are missing from the authors.json
          of a module they changed
"""
import html
import argparse
import json
import os
import re
import subprocess
import sys
import unicodedata
import xml.etree.ElementTree as ET

POM_NS = {'m': 'http://maven.apache.org/POM/4.0.0'}


def read_json(path):
    with open(path, encoding='utf-8') as f:
        return json.load(f)


def write_json(path, data):
    text = json.dumps(data, indent=2, ensure_ascii=False) + '\n'
    if os.path.exists(path):
        with open(path, encoding='utf-8') as f:
            if f.read() == text:
                return False
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(text)
    return True


def normalize(name):
    """Lower-case, accent-free 'given family' form of a person name."""
    if ',' in name:
        family, given = [s.strip() for s in name.split(',', 1)]
        name = f'{given} {family}'
    name = unicodedata.normalize('NFKD', name).encode('ascii', 'ignore').decode()
    return ' '.join(name.lower().split())


def all_modules(root):
    pom = ET.parse(os.path.join(root, 'pom.xml')).getroot()
    return [m.text.strip() for m in pom.findall('m:modules/m:module', POM_NS)]


def with_dependencies(root, modules):
    """Add the sibling modules each module depends on, dependencies first."""
    known = set(all_modules(root))
    ordered = []

    def visit(module):
        if module in ordered:
            return
        pom = ET.parse(os.path.join(root, module, 'pom.xml')).getroot()
        for dep in pom.findall('m:dependencies/m:dependency/m:artifactId', POM_NS):
            if dep.text.strip() in known:
                visit(dep.text.strip())
        ordered.append(module)

    for module in modules:
        visit(module)
    return ordered


def module_authors(root, module):
    path = os.path.join(root, module, 'authors.json')
    if not os.path.exists(path):
        print(f'warning: {path} not found', file=sys.stderr)
        return []
    return read_json(path)


def build(args):
    modules = split(args.modules) or all_modules(args.root)
    modules = with_dependencies(args.root, modules)
    base = [read_json(b) for b in args.base or []]
    per_module = {m: module_authors(args.root, m) for m in modules}

    if args.modules_as == 'contributor':
        # module authors are credited as researchers, not as creators of the
        # framework; the base contributors keep their own type
        module_people = [[dict(p, role='contributor', type='Researcher') for p in people]
                         for people in per_module.values()]
    else:
        module_people = list(per_module.values())
    lists = base + module_people

    # creators first: anyone who is a creator in one list is not repeated as
    # contributor from another list
    creators = merge(lists, lambda p: p.get('role', 'creator') == 'creator', set())
    seen = {key for p in creators for key in keys(p)}
    contributors = merge(lists, lambda p: p.get('role') == 'contributor', seen)
    for person in contributors:
        person['type'] = person.pop('type', None) or 'Other'

    meta = read_json(args.zenodo)
    meta['creators'] = creators
    if contributors:
        meta['contributors'] = contributors
    else:
        meta.pop('contributors', None)
    meta['description'] = with_module_section(meta.get('description', ''), per_module)
    changed = write_json(args.zenodo, meta)
    print(f'{len(creators)} creators, {len(contributors)} contributors from {", ".join(modules)}'
          f'{" (updated)" if changed else " (unchanged)"}')


MODULE_SECTION = '<h3>Authors of the model component libraries</h3>'


def display_name(person):
    family, _, given = person['name'].partition(',')
    return html.escape(f'{given.strip()} {family.strip()}'.strip())


def with_module_section(description, per_module):
    """Replace the generated per-module author list at the end of the description."""
    intro = description.split(MODULE_SECTION, 1)[0].rstrip()
    items = []
    for module, people in per_module.items():
        authors = [display_name(p) for p in people if p.get('role', 'creator') == 'creator']
        others = [display_name(p) for p in people if p.get('role') == 'contributor']
        line = f'<li><strong>{html.escape(module)}</strong>: {", ".join(authors)}'
        if others:
            line += f' (contributors: {", ".join(others)})'
        items.append(line + '</li>')
    if not items:
        return intro
    return f'{intro}\n{MODULE_SECTION}\n<ul>\n' + '\n'.join(items) + '\n</ul>'


def keys(person):
    return {person.get('orcid') or normalize(person['name']), normalize(person['name'])}


def merge(lists, accept, seen):
    """Persons accepted by the filter, in list order, without duplicates."""
    result = []
    for people in lists:
        for person in people:
            if not accept(person) or keys(person) & seen:
                continue
            seen.update(keys(person))
            result.append({k: v for k, v in person.items() if k not in ('aliases', 'role')})
    return result


def check(args):
    modules = all_modules(args.root)
    log = subprocess.run(
        ['git', '-C', args.root, 'log', '--no-merges', '--format=@%h|%aN|%aE', '--name-only', args.range],
        capture_output=True, text=True, encoding='utf-8', check=True).stdout
    missing = {}
    commit = None
    for line in log.splitlines():
        if line.startswith('@'):
            commit = line[1:].split('|')
            continue
        module = line.split('/', 1)[0]
        if not line or module not in modules or line == f'{module}/authors.json':
            continue
        sha, name, mail = commit
        known = set()
        for person in module_authors(args.root, module):
            known.add(normalize(person['name']))
            known.update(normalize(a) for a in person.get('aliases', []))
        if normalize(name) not in known and mail.lower() not in known:
            missing.setdefault((module, name), sha)
    for (module, name), sha in sorted(missing.items()):
        # GitHub Actions annotation; shows up as warning in the run summary
        print(f'::warning file={module}/authors.json::{name} ({sha}) changed {module} '
              f'but is not listed in {module}/authors.json')
    if not missing:
        print('all commit authors are listed')


def split(value):
    return [v for v in re.split(r'[,\s]+', value or '') if v]


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest='cmd', required=True)
    b = sub.add_parser('build', help='write the creators of a .zenodo.json file')
    b.add_argument('--root', default='.', help='jamsmodels directory')
    b.add_argument('--modules', help='modules to include (default: all modules in pom.xml)')
    b.add_argument('--base', action='append', help='authors.json listed before the module authors')
    b.add_argument('--zenodo', default='.zenodo.json', help='.zenodo.json to update')
    b.add_argument('--modules-as', choices=['creator', 'contributor'], default='creator',
                   help='how module authors are listed (default: creator)')
    b.set_defaults(func=build)
    c = sub.add_parser('check', help='warn about commit authors missing in authors.json')
    c.add_argument('--root', default='.', help='jamsmodels directory')
    c.add_argument('--range', required=True, help='git revision range, e.g. origin/main..HEAD')
    c.set_defaults(func=check)
    args = parser.parse_args()
    args.func(args)


if __name__ == '__main__':
    main()
