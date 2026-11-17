#!/usr/bin/env python3
"""Publish a single model library on Zenodo, independent of the jamsmodels
releases.

Every library gets its own Zenodo concept record ("<library> - JAMS model
component library"); the first upload creates it, every later upload adds a
new version to it. The record is found again by its title among the uploads of
the token's account, so nothing has to be stored in the repository.

The record takes license, keywords and related identifiers from the root
.zenodo.json, adds "isPartOf" the jamsmodels concept DOI and lists the people
of the library in the order of its .zenodo.json. If the uploaded files contain
libraries it depends on, their people follow in alphabetical order (as in the
jamsmodels releases).

Without --publish the upload stays a draft that can be checked (and published
or deleted) on zenodo.org; a draft has no DOI yet. A later run reuses an open
draft of the library. The token is read from the environment variable
ZENODO_TOKEN.
"""
import argparse
import datetime
import html
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from zenodo_authors import (DEFAULT_TYPE, merge, module_people, read_json,  # noqa: E402
                            with_dependencies, with_module_section)

JAMSMODELS_CONCEPT_DOI = '10.5281/zenodo.23761918'
TITLE = '{} - JAMS model component library'


class Zenodo:
    def __init__(self, token):
        self.base = 'https://zenodo.org/api'
        self.token = token

    def call(self, method, url, data=None, raw=None):
        if not url.startswith('http'):
            url = self.base + url
        headers = {'Authorization': f'Bearer {self.token}'}
        body = None
        if raw is not None:
            headers['Content-Type'] = 'application/octet-stream'
            body = raw
        elif data is not None:
            headers['Content-Type'] = 'application/json'
            body = json.dumps(data).encode('utf-8')
        req = urllib.request.Request(url, method=method, headers=headers, data=body)
        try:
            with urllib.request.urlopen(req) as r:
                text = r.read()
                return json.loads(text) if text else None
        except urllib.error.HTTPError as e:
            sys.exit(f'{method} {url}: {e.code} {e.read().decode("utf-8", "replace")}')

    def find(self, title):
        """(latest published version, open draft) of the record with this title."""
        found = []
        page = 1
        while True:
            query = urllib.parse.urlencode({'q': f'title:"{title}"', 'size': 100, 'page': page})
            hits = self.call('GET', f'/deposit/depositions?{query}')
            found += [d for d in hits if d['metadata'].get('title') == title]
            if len(hits) < 100:
                break
            page += 1
        if len({d['conceptrecid'] for d in found}) > 1:
            sys.exit(f'more than one record titled "{title}" - clean up by hand on zenodo.org')
        published = [d for d in found if d.get('submitted')]
        drafts = [d for d in found if not d.get('submitted')]
        latest = max(published, key=lambda d: d['id']) if published else None
        draft = max(drafts, key=lambda d: d['id']) if drafts else None
        return latest, draft


def metadata(root, library, version, notes, tag_url):
    base = read_json(os.path.join(root, '.zenodo.json'))
    modules = with_dependencies(root, [library])
    per_module = {m: module_people(root, m) for m in modules}
    own_creators, own_contributors = per_module[library]
    seen = set()
    creators = merge(own_creators, [c for c, _ in per_module.values()], seen)
    contributors = merge(own_contributors, [c for _, c in per_module.values()], seen)
    for person in contributors:
        person['type'] = person.get('type') or DEFAULT_TYPE
    deps = [m for m in modules if m != library]
    text = (f'<p>{library}, a model component library for the JAMS framework from the '
            f'<a href="https://github.com/jamsframework/jamsmodels">jamsmodels</a> repository.</p>\n'
            f'<p>Unzip and copy the contents into the <code>components</code> folder of a JAMS '
            f'installation.')
    if deps:
        text += f' The archive also contains the libraries {library} needs: {", ".join(deps)}.'
    text += '</p>'
    if notes:
        text += f'\n<p>{html.escape(notes)}</p>'
    related = [r for r in base.get('related_identifiers', [])]
    related.append({'identifier': JAMSMODELS_CONCEPT_DOI, 'relation': 'isPartOf',
                    'resource_type': 'software', 'scheme': 'doi'})
    if tag_url:
        related.append({'identifier': tag_url, 'relation': 'isSupplementTo',
                        'resource_type': 'software', 'scheme': 'url'})
    meta = {
        'title': TITLE.format(library),
        'upload_type': 'software',
        'version': version,
        'publication_date': datetime.date.today().isoformat(),
        'description': with_module_section(text, per_module),
        'license': base.get('license'),
        'keywords': base.get('keywords', []) + [library],
        'related_identifiers': related,
        'creators': creators,
    }
    if contributors:
        meta['contributors'] = contributors
    return meta


def upload(args):
    token = os.environ.get('ZENODO_TOKEN')
    if not token:
        sys.exit('ZENODO_TOKEN is not set')
    z = Zenodo(token)
    meta = metadata(args.root, args.library, args.version, args.notes, args.tag_url)
    latest, draft = z.find(meta['title'])
    if latest and latest['metadata'].get('version') == args.version:
        sys.exit(f'{meta["title"]} {args.version} is already published: {latest["links"]["html"]}')
    if draft:
        draft = z.call('GET', f'/deposit/depositions/{draft["id"]}')
        print('reusing the open draft')
    elif latest:
        new = z.call('POST', f'/deposit/depositions/{latest["id"]}/actions/newversion')
        draft = z.call('GET', new['links']['latest_draft'])
        print(f'new version of {latest["links"]["html"]}')
    else:
        draft = z.call('POST', '/deposit/depositions', {})
        print('new record')
    for f in draft.get('files', []):
        z.call('DELETE', f'/deposit/depositions/{draft["id"]}/files/{f["id"]}')
    for path in args.files:
        with open(path, 'rb') as f:
            z.call('PUT', f'{draft["links"]["bucket"]}/{os.path.basename(path)}', raw=f.read())
    z.call('PUT', f'/deposit/depositions/{draft["id"]}', {'metadata': meta})
    if not args.publish:
        print(f'draft of {meta["title"]} {args.version} (not published, no DOI yet): '
              f'{draft["links"]["html"]}')
        return
    done = z.call('POST', f'/deposit/depositions/{draft["id"]}/actions/publish')
    print(f'published {meta["title"]} {args.version}: {done["links"]["html"]} (DOI {done["doi"]}, '
          f'concept DOI {done.get("conceptdoi")})')


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--root', default='.', help='jamsmodels directory')
    parser.add_argument('--library', required=True, help='library (module) to publish')
    parser.add_argument('--version', required=True, help='version string of the upload')
    parser.add_argument('--notes', help='optional release note (plain text)')
    parser.add_argument('--tag-url', help='URL of the source code of this upload (git tag or commit)')
    parser.add_argument('--publish', action='store_true', help='publish (default: leave a draft)')
    parser.add_argument('--dry-run', action='store_true', help='only print the metadata')
    parser.add_argument('files', nargs='*', help='files to upload')
    args = parser.parse_args()
    if args.dry_run:
        print(json.dumps(metadata(args.root, args.library, args.version, args.notes, args.tag_url),
                         indent=2, ensure_ascii=False))
        return
    if not args.files:
        sys.exit('no files to upload')
    upload(args)


if __name__ == '__main__':
    main()
