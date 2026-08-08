# Referral Economy & Credit Wallet System — Backend

A clean-architecture Node.js/TypeScript API implementing the real estate app's internal growth
engine: a 3-level referral tree, a non-cash credit wallet, and an admin-configurable campaign
engine. See [`docs/referral-system/`](./docs/referral-system) for the full design documentation
(architecture, business rules, database, API reference, security model, flow/sequence diagrams,
admin guide).

## Stack

Node.js 22 + TypeScript, Express, Prisma ORM + PostgreSQL, Zod for input validation, Jest for
tests. No framework magic beyond that — see `docs/referral-system/Architecture.md` for why.

## Local development

```bash
npm install
cp .env.example .env        # then edit DATABASE_URL / ADMIN_API_KEY as needed
docker compose up -d postgres   # or point DATABASE_URL at any Postgres you already have
npx prisma migrate dev --name init
npm run dev                 # starts the API on http://localhost:4000 with hot reload
```

Run the test suite (pure unit tests against an in-memory repository layer — no database
required):

```bash
npm test
```

## Running everything in Docker

```bash
cp .env.example .env        # set a real ADMIN_API_KEY before doing this for anything but local dev
docker compose up --build
```

This starts Postgres and the API together; the API container applies pending Prisma migrations
on every boot before starting the server.

## Deploying

The `Dockerfile` builds a self-contained production image (`docker build .`) that any
container-hosting platform can run directly — this repo doesn't lock you into one host. Two
common, low-friction options:

**Railway / Render (git-based deploy):**
1. Create a new service from this repository, pointing it at the `backend/` directory.
2. Add a managed PostgreSQL database from the same platform; copy its connection string into the
   service's `DATABASE_URL` environment variable.
3. Set `ADMIN_API_KEY` to a long random value (`openssl rand -hex 32` works well) — never reuse
   the placeholder from `.env.example`.
4. Deploy. The platform builds the `Dockerfile` and runs `prisma migrate deploy` automatically on
   every boot (baked into the image's `CMD`), so there's no separate migration step to remember.

**Any other Docker host (Fly.io, a VPS, ECS, ...):** build and push the image, then run it with
`DATABASE_URL`, `PORT`, and `ADMIN_API_KEY` set as environment variables. Point it at any reachable
PostgreSQL 14+ instance.

**Your own VPS, via GitHub Actions (`.github/workflows/deploy-backend.yml`):** the workflow SSHes
into your server (from GitHub's runners, not from any sandboxed agent session — that's the whole
reason this path exists) on every push to `main` that touches `backend/`, or on demand from the
Actions tab. It expects:

| Secret/Variable | Where | Purpose |
| --- | --- | --- |
| `VPS_HOST` | Secret | The server's IP or hostname |
| `VPS_PORT` | Secret | SSH port (defaults to 22 if unset) |
| `VPS_USER` | Secret | The SSH user to deploy as |
| `VPS_SSH_PRIVATE_KEY` | Secret | Private half of a key pair whose public half is in that user's `~/.ssh/authorized_keys` — **use a dedicated deploy key, not your personal one** |
| `ADMIN_API_KEY` | Secret, optional | If set, the workflow writes it into the server's `backend/.env` on every deploy; if unset, whatever's already in `.env` on the server is left alone |
| `BACKEND_PORT` | Variable, optional | Local port the app listens on inside the server/container; defaults to `4000` |

Two deployment shapes are supported, depending on what the target actually has:

**A) A real VPS with Docker** — use `docker-compose.yml` (Postgres + API + a
[Caddy](https://caddyserver.com/) reverse proxy that gets a real, auto-renewing Let's Encrypt
certificate for `api.zarandix.ir`). Needs Docker + the Compose plugin installed and ports 80/443
open. `docker compose up -d --build` is the whole deploy.

**B) An unprivileged sandbox container with no Docker socket** (this project's actual deploy
target — see your own container's onboarding docs for exactly what it does/doesn't allow) — this
is what `deploy-backend.yml` is written for. No Docker is available *inside* the container, so it
installs PostgreSQL directly via `apt`, builds the app with plain `npm`/`tsc`, and runs it as a
Node process supervised by [pm2](https://pm2.keymetrics.io/) (auto-restart on crash, `pm2 save` so
it can be resurrected, `pm2 logs referral-backend` / `pm2 status` for troubleshooting). A random
Postgres password is generated once on first deploy and reused after that — it's never printed to
the workflow log or stored as a GitHub secret.

**HTTPS in shape B is *not* handled by this workflow** — the container has no way to bind a
host-level port 80/443 itself, so there is no ACME challenge this workflow can complete. Getting
`https://api.zarandix.ir/` actually working over the internet needs one manual, one-time step from
whoever administers the host: a reverse-proxy vhost (with a real cert, e.g. via `certbot --nginx`)
forwarding `api.zarandix.ir` to wherever the host already routes traffic into this container, on
the app's internal port (`4000` by default — see `BACKEND_PORT` above). For example, on the host:

```nginx
server {
    listen 80;
    server_name api.zarandix.ir;
    location / {
        proxy_pass http://<however-the-host-reaches-this-container>:4000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
followed by `certbot --nginx -d api.zarandix.ir` to add TLS. A DNS **A record** for
`api.zarandix.ir` pointing at that host's public IP needs to exist first. Everything else
(cloning, installing dependencies, migrations, building, starting/restarting the app) is fully
automated by the workflow — this proxy step is the one piece no workflow running inside the
container can reach.

> This backend was designed and built in an environment with no hosting credentials, deploy
> connector, or raw network egress available (only an allowlisted HTTPS proxy — no SSH, no
> arbitrary TCP), so it has been verified to install, type-check, build, and pass its full test
> suite here, and the workflow above has been written to actually deploy it — but as of this
> commit it has **not yet run against a live server**, since that requires secrets only the
> repository owner can add.

## API surface

See [`docs/referral-system/API.md`](./docs/referral-system/API.md) for every endpoint, its
request/response shape, and which ones require the admin API key.

## Project layout

```
src/
  engines/       WalletEngine, ReferralEngine, CampaignEngine — all business logic, zero HTTP/ORM awareness
  repositories/  Repository interfaces + two implementations: prisma/ (real) and memory/ (tests)
  routes/        Express routers — thin, validate input then call an engine
  middleware/    asyncHandler, errorHandler, adminAuth, validate
  container.ts   Wires the Prisma-backed repositories + engines used by the running server
  app.ts         Express app assembly
  server.ts      Process entrypoint
  __tests__/     Engine unit tests, run against the in-memory repositories
prisma/
  schema.prisma  The full data model (see docs/referral-system/Database.md)
docs/
  referral-system/  Design documentation (see below)
```
