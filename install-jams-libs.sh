#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

install_one() {
  local artifact="$1"
  local jar
  jar=$(find jams-libs -maxdepth 1 -name "${artifact}-*.jar" | sort | head -n 1)
  if [ -z "$jar" ]; then
    echo "Error: no ${artifact}-*.jar found in jams-libs/" >&2
    exit 1
  fi
  echo "Installing $jar as org.jams:${artifact}:local"
  ./mvnw -q org.apache.maven.plugins:maven-install-plugin:3.1.2:install-file \
    -Dfile="$jar" -DgroupId=org.jams -DartifactId="$artifact" \
    -Dversion=local -Dpackaging=jar -DgeneratePom=true
}

install_one jams-api
install_one jams-main
