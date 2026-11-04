#!/usr/bin/env python3
"""Merge the Zenodo author lists of the model libraries into a release record.

Every model library directory keeps a ``.zenodo.json`` that only holds its
``creators`` and ``contributors`` in Zenodo format ("name" as "Family, Given",
optional "orcid" and "affiliation"; contributors need a "type", e.g.
"Researcher"). The lists are maintained by hand following these rules, which
also apply to the jams people in the jams .zenodo.json (see also README.md):

  creator      named as author in a file the module ships, where the author
               tag was added in this module, or at least 50 own lines of
               code in one file of the module
  contributor  any other commit to code the module ships: smaller code
               changes, checking in code of others, translations,
               documentation
  not listed   trivial changes (one or two lines), code that is no longer
               shipped, only commits that touch more than 20 files

The author attribute of the component description wins over the @author
tag; a tag taken over from a file copied or derived from another module (or
from jams) counts for that module. Only code lines count: comments, license
headers, annotations, imports, blank lines and whitespace changes do not.
Lines are summed per person and file, so reverted changes cancel out. Only
commits made in the module (including its former directory names) count;
work done on a file in another module before it was copied counts there.
Moving or re-adding a file does not count, a derived file counts only its
new lines, and adding a file whose author tag names someone else counts no
lines. People are ordered by their own work in the module.

The optional "aliases" list of a person holds git author names, e-mail
addresses or source code tags that differ from the name; it is only used by
the check and never written to a release record.

The ``.zenodo.json`` of a repository root is the release record Zenodo reads
from the tagged commit. Its own creators and contributors are kept; the
release workflows add the module people and commit the result only on the
tagged release commit, so the maintained file on the branch stays unchanged.

  build   add the creators and contributors of the module lists to a
          .zenodo.json file and list the people of each module at the end of
          its description (all other fields are kept). The people already
          in the file keep their order, the module people follow in
          alphabetical order; a creator of any list is a creator of the
          release.
  check   warn about commit authors that are missing from the .zenodo.json
          of a module they changed
"""
import argparse
import html
import json
import os
import re
import subprocess
import sys
import unicodedata
import xml.etree.ElementTree as ET

POM_NS = {'m': 'http://maven.apache.org/POM/4.0.0'}
DEFAULT_TYPE = 'Researcher'
MODULE_SECTION = '<h3>Authors of the model component libraries</h3>'


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


def module_people(root, module):
    """(creators, contributors) of a module."""
    path = os.path.join(root, module, '.zenodo.json')
    if not os.path.exists(path):
        print(f'warning: {path} not found', file=sys.stderr)
        return [], []
    meta = read_json(path)
    return meta.get('creators', []), meta.get('contributors', [])


def keys(person):
    return {person.get('orcid') or normalize(person['name']), normalize(person['name'])}


def clean(person):
    return {k: v for k, v in person.items() if k != 'aliases'}


def sort_key(person):
    """Family name first, accents ignored."""
    name = unicodedata.normalize('NFKD', person['name']).encode('ascii', 'ignore').decode()
    return ' '.join(name.lower().split())


def merge(own, lists, seen):
    """The record's own persons in their order, then the persons of the lists
    in alphabetical order, without duplicates."""
    result = []
    for person in own + sorted((p for people in lists for p in people), key=sort_key):
        if keys(person) & seen:
            continue
        seen.update(keys(person))
        result.append(clean(person))
    return result


def build(args):
    modules = split(args.modules) or all_modules(args.root)
    modules = with_dependencies(args.root, modules)
    per_module = {m: module_people(args.root, m) for m in modules}
    meta = read_json(args.zenodo)

    # creators first: anyone who is a creator in one list is not repeated as
    # contributor from another list
    seen = set()
    creators = merge(meta.get('creators', []), [c for c, _ in per_module.values()], seen)
    contributors = merge(meta.get('contributors', []), [c for _, c in per_module.values()], seen)
    for person in contributors:
        person['type'] = person.get('type') or DEFAULT_TYPE

    meta['creators'] = creators
    if contributors:
        meta['contributors'] = contributors
    else:
        meta.pop('contributors', None)
    meta['description'] = with_module_section(meta.get('description', ''), per_module)
    output = args.output or args.zenodo
    changed = write_json(output, meta)
    print(f'{len(creators)} creators, {len(contributors)} contributors '
          f'from {", ".join(modules)} -> {output}{" (updated)" if changed else " (unchanged)"}')


def display_name(person):
    family, _, given = person['name'].partition(',')
    return html.escape(f'{given.strip()} {family.strip()}'.strip())


def with_module_section(description, per_module):
    """Replace the generated per-module author list at the end of the description."""
    intro = description.split(MODULE_SECTION, 1)[0].rstrip()
    items = []
    for module, (module_creators, module_contributors) in per_module.items():
        line = f'<li><strong>{html.escape(module)}</strong>: {", ".join(map(display_name, module_creators))}'
        if module_contributors:
            line += f' (contributors: {", ".join(map(display_name, module_contributors))})'
        items.append(line + '</li>')
    if not items:
        return intro
    return f'{intro}\n{MODULE_SECTION}\n<ul>\n' + '\n'.join(items) + '\n</ul>'


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
        if not line or module not in modules or line == f'{module}/.zenodo.json':
            continue
        sha, name, mail = commit
        known = set()
        for people in module_people(args.root, module):
            for person in people:
                known.add(normalize(person['name']))
                known.update(normalize(a) for a in person.get('aliases', []))
        if normalize(name) not in known and mail.lower() not in known:
            missing.setdefault((module, name), sha)
    for (module, name), sha in sorted(missing.items()):
        # GitHub Actions annotation; shows up as warning in the run summary
        print(f'::warning file={module}/.zenodo.json::{name} ({sha}) changed {module} '
              f'but is not listed in {module}/.zenodo.json')
    if not missing:
        print('all commit authors are listed')


def split(value):
    return [v for v in re.split(r'[,\s]+', value or '') if v]


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest='cmd', required=True)
    b = sub.add_parser('build', help='add the module people to a .zenodo.json file')
    b.add_argument('--root', default='.', help='jamsmodels directory')
    b.add_argument('--modules', help='modules to include (default: all modules in pom.xml)')
    b.add_argument('--zenodo', default='.zenodo.json', help='release record to read')
    b.add_argument('--output', help='file to write (default: the --zenodo file)')
    b.set_defaults(func=build)
    c = sub.add_parser('check', help='warn about commit authors missing in the module lists')
    c.add_argument('--root', default='.', help='jamsmodels directory')
    c.add_argument('--range', required=True, help='git revision range, e.g. origin/main..HEAD')
    c.set_defaults(func=check)
    args = parser.parse_args()
    args.func(args)


if __name__ == '__main__':
    main()
