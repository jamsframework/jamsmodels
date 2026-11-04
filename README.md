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
| creator | named as author in a file the library ships, where the author tag was added in this library, **or** at least 50 own lines of code in one file of the library |
| contributor | any other commit to code the library ships: smaller code changes, checking in code of others, translations, documentation |
| not listed | trivial changes (one or two lines), code that is no longer shipped, only commits that touch more than 20 files (API changes, renames, formatting) |

How author tags are read:

- The `author` attribute of the component description wins over the
  `@author` javadoc tag.
- A tag taken over from a file that was copied or derived from another
  library (or from jams) counts for that library, not for the copy.

How lines are counted:

- Only code lines count: comments, license headers, annotations, imports,
  blank lines and whitespace changes do not.
- Lines are summed per person and file over all commits, so changes that
  were reverted later cancel out.
- Only commits made in the library (including its former directory names)
  count. Work done on a file in another library before it was copied counts
  for that other library.
- Moving or re-adding a file does not count. For a file derived from an
  existing one only the new lines count; adding a file whose author tag
  names someone else (a commit of someone else's work) counts no lines.

Within each list people are ordered by their own work in the library.

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
- The release lists the library people in alphabetical order (in the jams
  release after the jams people, who keep their order), since work in
  libraries maintained independently cannot be compared.
- The description gets a generated list of the people of each library, in
  the order of the library's `.zenodo.json`.
- The merged file is committed only on the tagged release commit, so the
  maintained files on `main` stay unchanged.

A single library can also be published as a Zenodo record of its own with
the [Library release](.github/workflows/library-release.yml) workflow and
[tools/zenodo_upload.py](tools/zenodo_upload.py): every library gets its own
concept DOI, each upload a new version. The archive also contains the
libraries it depends on, whose people follow in alphabetical order. By
default the upload stays a draft on zenodo.org, to be checked and published
(or deleted) there.

The [Authors](.github/workflows/authors.yml) workflow warns when a commit
changes a library whose `.zenodo.json` does not list the commit author.
