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

> This backend was designed and built in an environment with no hosting credentials or deploy
> connector available, so it has been verified to install, type-check, build, and pass its full
> test suite here — but it has **not** been deployed to a live, internet-reachable server as part
> of this work. If you'd like it actually pushed live, either share API access to a host you
> control (e.g. a Railway or Render API token) so it can be deployed directly, or follow the steps
> above yourself — either way takes about five minutes from here.

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
