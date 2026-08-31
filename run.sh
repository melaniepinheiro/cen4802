#!/usr/bin/env bash
set -euo pipefail

if ! command -v javac >/dev/null 2>&1; then
  echo "Java compiler not found. Install JDK 17 or newer." >&2
  exit 1
fi

mkdir -p out
javac --add-modules jdk.httpserver -d out src/main/java/com/melanie/cen4802/App.java
echo "Open http://localhost:8080 in your browser."
java --add-modules jdk.httpserver -cp out com.melanie.cen4802.App
