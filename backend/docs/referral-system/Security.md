# Security

## What's actually implemented today

- **Admin routes** (`/api/admin/*`) require a shared-secret header, `X-Admin-Api-Key`, checked
  against the `ADMIN_API_KEY` environment variable (`src/middleware/adminAuth.ts`). This is a
  service credential for whatever internal admin tool calls these endpoints — not a per-admin
  login. There is no concept of multiple admin accounts or an audit trail of *which* admin changed
  a setting; every request with the right key is equally trusted.
- **Input validation** on every mutating endpoint via Zod schemas (`src/middleware/validate.ts`) —
  malformed request bodies are rejected with field-level detail before touching any engine.
- **PIN-equivalent secrets are never stored in this system** — there's no password/PIN here at
  all; user identity is assumed to come from whatever the Android app's own auth layer already
  handles (out of scope for this backend).
- **Analytics data is collected but never used to gate anything** (device fingerprint,
  installation ID, IP, device model, OS), matching the PRD's explicit fraud philosophy: growth
  over strict anti-fraud, for now.

## The gap that matters most before real deployment

**There is currently no verification that a caller is who it claims to be.** Endpoints like
`POST /api/users/:id/verify-phone`, `POST /api/wallet/purchase`, and
`POST /api/subscriptions/purchase` take a `userId` in the request and act on it — nothing stops
one client from passing a different user's ID. This is a deliberate simplification for this
architecture-design pass (there is no user-facing authentication system specified anywhere in the
PRD, and building one is a substantial project of its own), but it means: **do not expose these
endpoints directly to untrusted clients as-is.**

Before a real rollout, add one of:

- A user-facing auth layer (session token or JWT) issued by whatever already authenticates users
  in the Android app, verified by middleware that extracts the caller's own user ID — routes
  would then use *that* ID instead of trusting a URL/body parameter.
- Or, if this API is only ever called from a trusted backend-for-frontend (not directly from the
  mobile app), restrict network access to that intermediary and let it own end-user
  authentication.

## Payment integrity

`POST /api/subscriptions/purchase` treats the purchase as succeeded the instant it's called — see
`Architecture.md`. In production this endpoint must only be invoked after a real payment gateway
confirms payment (ideally from that gateway's signed webhook, not from the client redirecting
back), otherwise a user could grant themselves — and their whole upstream referral chain — a paid
subscription's rewards for free.

## Data handled

`User.ipAddress`, `deviceFingerprint`, `installationId`, `deviceModel`, `operatingSystem` are
personally-identifying-adjacent data collected for future fraud analytics. Treat the database
accordingly: encrypt at rest (most managed Postgres hosts do this by default), restrict direct
database access to operators who need it, and consider a data-retention policy for `ReferralLog`
if this ever needs to comply with a privacy regulation the app is subject to — none of that is
implemented here; it's an operational decision for whoever runs the deployed database.

## Concurrency

`WalletRepository.adjustBalance` (Prisma implementation) wraps its read-check-write in a
`$transaction` to prevent a balance going negative under a single request, but does not use an
explicit row lock (`SELECT ... FOR UPDATE`). Under default Postgres isolation this is adequate for
the volume this MVP targets; a system processing many concurrent purchases for the *same* user at
the same instant should revisit this with an explicit pessimistic lock or Postgres's `SERIALIZABLE`
isolation level.
