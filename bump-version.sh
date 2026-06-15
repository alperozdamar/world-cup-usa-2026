#!/bin/bash

set -e

INPUT=$1

if [[ -z "$INPUT" ]]; then
  echo "Usage: ./bump-version.sh [major|minor|patch|<explicit-version>]"
  exit 1
fi

CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
BASE_VERSION="${CURRENT_VERSION%%-*}"
SUFFIX=""
if [[ "$CURRENT_VERSION" == *-* ]]; then
  SUFFIX="-${CURRENT_VERSION#*-}"
fi

IFS='.' read -r MAJOR MINOR PATCH <<<"$BASE_VERSION"

if [[ "$INPUT" == "major" || "$INPUT" == "minor" || "$INPUT" == "patch" ]]; then
  case "$INPUT" in
    major)
      NEW_VERSION="$((MAJOR + 1)).0.0${SUFFIX}"
      ;;
    minor)
      NEW_VERSION="$MAJOR.$((MINOR + 1)).0${SUFFIX}"
      ;;
    patch)
      NEW_VERSION="$MAJOR.$MINOR.$((PATCH + 1))${SUFFIX}"
      ;;
  esac
else
  if [[ "$INPUT" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9.-]+)?$ ]]; then
    NEW_VERSION="$INPUT"
  else
    echo "Invalid version format: $INPUT"
    exit 1
  fi
fi

echo "Bumping version: $CURRENT_VERSION → $NEW_VERSION"

mvn versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false

git add pom.xml
git commit -m "Release version $NEW_VERSION"
git tag "v$NEW_VERSION"
git push origin main
git push origin "v$NEW_VERSION"
