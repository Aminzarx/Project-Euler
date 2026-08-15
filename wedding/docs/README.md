# Ali & Yegane — Wedding Invitation Docs

A cinematic, interactive Persian wedding invitation built with Next.js (App Router),
TypeScript, Tailwind CSS and Framer Motion, deployed at `zarandix.ir/Wedding`.

## Contents

- [Folder Structure](./folder-structure.md)
- [Color System](./color-system.md)
- [Typography](./typography.md)
- [Animation System](./animation-system.md)
- [Motion Guidelines](./motion-guidelines.md)
- [Component Library](./component-library.md)
- [Future Extension Guide](./future-extension.md)

## Quick start

```bash
cd wedding
npm install
cp .env.example .env.local   # configure SMS provider + recipients
npm run dev
```

Open `http://localhost:3000/Wedding`. The app is served under the `/Wedding` base path
in every environment (see `next.config.mjs`) to match the production URL.

## Deploying

**Your own VPS, via GitHub Actions (`.github/workflows/deploy-wedding.yml`):** the workflow
SSHes into your server (from GitHub's runners, not from any sandboxed agent session — that's
the whole reason this path exists) on every push to `main`/this branch that touches `wedding/`,
or on demand from the Actions tab. It reuses the same four VPS secrets as
`deploy-backend.yml`:

| Secret/Variable | Where | Purpose |
| --- | --- | --- |
| `VPS_HOST` | Secret | The server's IP or hostname |
| `VPS_PORT` | Secret | SSH port (defaults to 22 if unset) |
| `VPS_USER` | Secret | The SSH user to deploy as |
| `VPS_SSH_PRIVATE_KEY` | Secret | Private half of a key pair whose public half is in that user's `~/.ssh/authorized_keys` |
| `WEDDING_PORT` | Repo variable, optional | Internal port the app listens on (defaults to `4100`) |
| `SMS_PROVIDER` | Repo variable, optional | `kavenegar` \| `melipayamak` \| `farazsms` — defaults to `none` (logs instead of sending) until you're ready to go live |
| `KAVENEGAR_API_KEY` / `KAVENEGAR_SENDER` | Secrets, optional | Only needed once `SMS_PROVIDER=kavenegar` |
| `MELIPAYAMAK_USERNAME` / `MELIPAYAMAK_PASSWORD` / `MELIPAYAMAK_SENDER` | Secrets, optional | Only needed once `SMS_PROVIDER=melipayamak` |
| `FARAZSMS_API_KEY` / `FARAZSMS_SENDER` | Secrets, optional | Only needed once `SMS_PROVIDER=farazsms` |

The app runs under `pm2` as `wedding-invitation`, built with `next build`/`next start` (not a
static export — the RSVP and guestbook API routes need a running Node process). **This workflow
does not, and cannot, make the site reachable at `https://zarandix.ir/Wedding`** — the sandboxed
container it deploys into has no way to bind host ports 80/443 or edit the real vhost. Once
deployed, hand the internal port (`WEDDING_PORT`, default `4100`) to whoever manages that host so
they can add a reverse-proxy rule from `zarandix.ir/Wedding` to `127.0.0.1:<port>` inside the
container.

## Keep this folder current

Whenever a section, animation, or system-level convention changes, update the
relevant doc in the same change. These docs are the source of truth for anyone
extending the experience later — including a future AI assistant.
