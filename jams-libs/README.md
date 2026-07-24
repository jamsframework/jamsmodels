# jams-libs

Place `jams-api-*.jar` and `jams-main-*.jar` here — from a built `jams`
checkout (`jams-api/target/`, `jams-main/target/`) or from the `lib/`
directory of a downloaded [JAMS release bundle](https://github.com/jamsframework/jams/releases).
The exact version in the filename doesn't matter.

Then run `install-jams-libs.sh` (or `.ps1` on Windows) once to register them
in your local Maven repository. After that, build normally with `./mvnw`.
