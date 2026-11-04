# jamsmodels

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.23761918.svg)](https://doi.org/10.5281/zenodo.23761918)

Model component libraries for the [JAMS](https://github.com/jamsframework/jams)
framework (J2000 model family and related models). Each library is a Maven
module in its own directory (see the `<modules>` list in [pom.xml](pom.xml)).

## Authors and Zenodo metadata

Every library directory has a `.zenodo.json` with the people behind that
library, in Zenodo format:

```json
{
  "creators": [
    {"name": "Family, Given", "orcid": "0000-0000-0000-0000"}
  ],
  "contributors": [
    {"name": "Family, Given", "type": "Researcher", "aliases": ["login"]}
  ]
}
```

- `creators` have no `type`; `contributors` need one. The default is
  `Researcher`; other Zenodo types (e.g. `ProjectLeader`, `Supervisor`) can be
  set by hand.
- `aliases` (optional) lists git author names, e-mail addresses or source code
  tags (e.g. a university login) that differ from the name. It is only used
  by the author check and never written to a release record.

The lists are maintained by hand. Whether someone is a creator or a
contributor is decided per library, with these fixed rules (the same rules
apply to the jams people in the jams `.zenodo.json`):

| Role | Criterion |
|---|---|
| creator | named as author (`@author` tag or `author` attribute of the component description) in a file the library ships, **or** at least 50 changed lines of code in one file of the library, summed over all commits of that person |
| contributor | smaller code changes, translations, documentation |
| not listed | trivial changes (one or two lines), code that is no longer shipped, only commits that touch more than 20 files (API changes, renames, formatting) |

How lines are counted:

- Only code lines count: comments, license headers, annotations, imports,
  blank lines and whitespace changes do not.
- Only commits made in the library (including its former directory names)
  count. Work done on a file in another library before it was copied counts
  for that other library.
- Adding a file whose author tag names someone else (a copy or a commit of
  someone else's work) does not count, nor do changes that were reverted
  later.

### Releases

The release record is the `.zenodo.json` in the repository root (title,
description, license, related identifiers, keywords). The release workflows
merge the library lists into it with
[tools/zenodo_authors.py](tools/zenodo_authors.py):

- jamsmodels releases include all libraries; the jams release includes the
  libraries it bundles and the jams people from the jams `.zenodo.json`.
- A creator of any included library (or of jams) is a creator of the
  release; contributors stay contributors with the type from their file.
  Adding more libraries to a release adds people, it never changes anyone's
  role.
- The description gets a generated list of the people of each library.
- The merged file is committed only on the tagged release commit, so the
  maintained files on `main` stay unchanged.

The [Authors](.github/workflows/authors.yml) workflow warns when a commit
changes a library whose `.zenodo.json` does not list the commit author.
