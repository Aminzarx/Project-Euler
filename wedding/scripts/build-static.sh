#!/usr/bin/env bash
# Produces a fully static build (no Node server required) in wedding/out/, ready to
# upload via FTP into a `/Wedding` folder on any PHP-capable shared host.
#
# Route Handlers that do file I/O (app/api/rsvp, app/api/guestbook) aren't exportable
# to a static build, so this temporarily moves app/api out of the way for the build —
# the PHP equivalents under php/api/ take over at runtime instead, and get copied into
# the output alongside the static HTML/JS/CSS.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ -d app/api ]; then
  mv app/api app/__api_disabled_for_static_export
fi
restore_api() {
  if [ -d app/__api_disabled_for_static_export ]; then
    mv app/__api_disabled_for_static_export app/api
  fi
}
trap restore_api EXIT

STATIC_EXPORT=true npx next build

mkdir -p out/api out/lib out/data
cp -r php/api/. out/api/
cp -r php/lib/. out/lib/
cp php/data/.htaccess out/data/.htaccess
cp php/config.php out/config.php

echo ""
echo "Static build ready: wedding/out/"
echo "Upload the entire contents of that folder into a '/Wedding' directory on your host."
echo "Then edit out-uploaded config.php on the server to set your SMS provider + keys."
