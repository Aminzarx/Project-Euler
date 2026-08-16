#!/usr/bin/env bash
# Runs entirely on the target server as a single script (invoked via `bash deploy.sh`
# over SSH from .github/workflows/deploy-wedding.yml). Deliberately NOT inlined into the
# workflow's `script:` block: appleboy/ssh-action's `script_stop: true` mode injects an
# exit-code check after every newline in that block, which breaks multi-line `if/fi`
# constructs (the check fires on the test itself). A real script file has no such problem.
set -euo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$APP_DIR"

# .env is untracked (gitignored) so it survives every redeploy (the workflow does a
# git reset --hard, never a git clean). Only created once, from .env.example, and never
# blindly overwritten afterward.
if [ ! -f .env ]; then
  cp .env.example .env
fi

# Each secret/variable only ever overwrites its line when the workflow was actually given
# a non-empty value — never blanks out something set by hand on the server.
set_env_var() {
  local key="$1" value="$2"
  [ -z "$value" ] && return 0
  if grep -q "^${key}=" .env; then
    sed -i "s|^${key}=.*|${key}=${value}|" .env
  else
    echo "${key}=${value}" >> .env
  fi
}

set_env_var "SMS_PROVIDER" "${SMS_PROVIDER_VALUE:-}"
set_env_var "KAVENEGAR_API_KEY" "${KAVENEGAR_API_KEY_SECRET:-}"
set_env_var "KAVENEGAR_SENDER" "${KAVENEGAR_SENDER_SECRET:-}"
set_env_var "MELIPAYAMAK_USERNAME" "${MELIPAYAMAK_USERNAME_SECRET:-}"
set_env_var "MELIPAYAMAK_PASSWORD" "${MELIPAYAMAK_PASSWORD_SECRET:-}"
set_env_var "MELIPAYAMAK_SENDER" "${MELIPAYAMAK_SENDER_SECRET:-}"
set_env_var "FARAZSMS_API_KEY" "${FARAZSMS_API_KEY_SECRET:-}"
set_env_var "FARAZSMS_SENDER" "${FARAZSMS_SENDER_SECRET:-}"

npm ci
NODE_OPTIONS="--max-old-space-size=768" npm run build

command -v pm2 >/dev/null 2>&1 || sudo npm install -g pm2

APP_PORT="${APP_PORT:-4100}"
pm2 restart wedding-invitation --update-env 2>/dev/null || \
  pm2 start npm --name wedding-invitation --cwd "$APP_DIR" -- run start -- -p "$APP_PORT"
pm2 save
