# Architecture

## Why this exists

The real estate app's PRD asked for a referral economy connecting many users' accounts — a
referral tree spanning different people's devices, an admin panel, cross-user analytics. The
Android app this backend serves is a local-only, single-device, no-backend app (Room database,
one agent's own data). A referral system that rewards *other* people for referring *you* cannot
be built without something that both sides of a referral can reach — hence this backend: a
separate, deployable service that owns the referral tree, the wallet ledger, and the reward rules
across every user.

## Layering

```
routes/        Express routers — parse+validate the request, call exactly one engine method, shape the response.
  |
  v
engines/       WalletEngine, ReferralEngine, CampaignEngine — all business logic. Zero Express
  |            imports, zero Prisma imports. Depend only on the Repositories interface.
  v
repositories/  Repositories interface (types.ts) + two implementations:
                 prisma/  — the real one, used by the running server (src/container.ts)
                 memory/  — an in-memory one, used only by the engine unit tests
```

This is the "Separate Business Logic from UI" / "Separate Wallet Engine from Referral Engine" /
"Separate Campaign Engine from Wallet Engine" principle from the PRD, made mechanical: an engine
can only reach the outside world through the `Repositories` interface, so swapping Prisma for an
in-memory Map in tests requires changing zero engine code — see `src/__tests__/*.test.ts`, which
exercise the real reward math, the 3-level cap, and campaign overrides without a database.

## The three engines

- **WalletEngine** — the only code in the entire system that ever mutates a wallet balance.
  `credit` (always this user's own wallet, always paired with a transaction row) and `purchase`
  (debit against a listed service, capped at that service's `maxWalletUsagePercent`) are its only
  two ways to move credits — there is no generic "transfer" operation, which is what makes "credits
  cannot be transferred between users" structurally true rather than merely a rule someone has to
  remember to enforce.
- **ReferralEngine** — owns the tree (`registerUser`, `applyReferral`), the once-only rule, and
  reward *eligibility* (who gets rewarded and how much) for both reward types. It never touches a
  balance directly; every reward it decides on is handed to `WalletEngine.credit`.
- **CampaignEngine** — resolves the reward settings actually in effect right now
  (`getEffectiveSettings`), merging the base `ReferralSettings` with whichever fields an active
  `Campaign` overrides. `ReferralEngine` asks this before granting any reward, so a campaign never
  needs a code change to take effect — it needs a database row.

## Performance

The PRD's Performance section asks for reward calculation to stay effectively O(1), with at most
three database lookups for ancestor traversal, at a scale of millions of users. `UserRepository.
getAncestors` (the Prisma implementation, `src/repositories/prisma/index.ts`) uses a single
three-deep nested `include` (`referrer.referrer.referrer`), which Prisma compiles into one SQL
query with joins — regardless of how deep the actual tree goes, finding a user's first three
ancestors is always exactly one query. Every other per-request repository call
(`findByReferralCode`, `adjustBalance`, `create`) is a single indexed lookup or write.

One acknowledged simplification: `ReferralEngine.getStatistics` sums a user's own reward history
by paging through `listByUserId` rather than a dedicated SQL aggregate. A single user's own
transaction history is always a modest list in practice, so this is fine today — but if a "power
user" ever accumulates an unusually large history, this is the one spot that would need a real
`SUM(...) WHERE userId = ... AND type = ...` query instead. Everything else already aggregates in
the database (`WalletTransactionRepository.sumByType`/`sumAllRewards`, used by the admin
dashboard).

## What's intentionally out of scope for this pass

- **A built admin web UI.** `GET /api/admin/dashboard` and the settings/campaign CRUD endpoints
  return the exact data an admin dashboard needs (see `API.md`); rendering that as a web page is a
  separate frontend project, not part of this backend.
- **A real payment gateway integration.** `POST /api/subscriptions/purchase` treats a purchase as
  succeeding the moment it's called — a production deployment should only call it from a payment
  gateway's confirmed-payment webhook. See `Security.md`.
- **Fraud detection.** Per the PRD's own fraud philosophy, this system stores the analytics data
  (`ReferralLog`, device/IP fields on `User`) but never blocks on it. Building actual detection on
  top of that data is explicitly deferred to the future, by the PRD itself.
