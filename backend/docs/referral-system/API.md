# API Reference

Base URL: whatever host/port the server is deployed to (`PORT`, default `4000`). All request and
response bodies are JSON. All admin (`/api/admin/*`) routes additionally require an
`X-Admin-Api-Key` header matching the server's `ADMIN_API_KEY` environment variable — see
`Security.md`.

Every error response has the shape `{ "error": string, "details"?: object }` and a `4xx` status.

---

## Users

### `POST /api/users/register`

Registers a new user; applies a referral code immediately if one is supplied (does **not** yet
grant the registration reward — see `/verify-phone`).

```json
// Request
{
  "phoneNumber": "+989120000000",
  "referralCode": "AMN4KD",       // optional
  "deviceFingerprint": "...",     // optional, analytics only
  "installationId": "...",        // optional
  "ipAddress": "...",              // optional
  "deviceModel": "...",            // optional
  "operatingSystem": "..."         // optional
}
// 201 Response
{ "id": "uuid", "phoneNumber": "+989120000000", "referralCode": "X8PLQ2", "referrerId": "uuid|null" }
```

### `POST /api/users/:id/verify-phone`

Marks the phone verified; grants the direct referrer's Registration Reward the first time this is
called for a given user, a no-op every time after. `{ "success": true }`

### `GET /api/users/:id/dashboard`

Everything the PRD's "User Dashboard" section lists, in one call:

```json
{
  "walletBalance": 13000,
  "lifetimeRewards": 13000,
  "registrationRewards": 3000,
  "subscriptionRewards": 10000,
  "referralCode": "X8PLQ2",
  "referralLink": "https://app.example.com/r/X8PLQ2",
  "directReferrals": 4,
  "totalNetwork": 9,
  "rewardHistory": [ /* up to 20 most recent WalletTransaction rows */ ],
  "upcomingSubscriptionCost": 200000,
  "walletUsagePreview": { "walletAmountUsed": 13000, "cashAmountDue": 187000, "transaction": null }
}
```

`referralLink` uses a placeholder domain — substitute the app's real deep-link domain before
shipping. Referral **QR code** generation is a client-side concern (encode `referralLink` or
`referralCode` into a QR image on-device); this API only returns the data to encode.

---

## Referral

### `GET /api/referral/validate/:code`

`{ "valid": true }` or `{ "valid": false }`. `valid` reflects both existence and eligibility — a
code belonging to a BLOCKED/DELETED user reads as `valid: false`. The owning user's internal ID
is intentionally never exposed here (avoids ID enumeration via this public endpoint).

### `POST /api/referral/apply`

For applying a code after registration rather than during it — still allowed only once, ever.

```json
{ "userId": "uuid", "code": "AMN4KD" }
```

### `GET /api/referral/:userId/tree`

`{ "directReferrals": 4, "totalNetwork": 9 }`

### `GET /api/referral/:userId/statistics`

`{ "directReferrals": 4, "totalNetwork": 9, "lifetimeRewards": 13000, "registrationRewards": 3000, "subscriptionRewards": 10000 }`

---

## Wallet

### `GET /api/wallet/:userId/balance` → `{ "balance": 13000 }`

### `GET /api/wallet/:userId/history?limit=50&offset=0` → array of `WalletTransaction`

### `POST /api/wallet/purchase`

Applies the wallet-funded portion of a purchase for any listed service, capped at that service's
configured percentage. Never moves cash — the caller collects `cashAmountDue` however cash
payments are handled.

```json
// Request
{ "userId": "uuid", "serviceKey": "featured_listing", "servicePrice": 200000 }
// Response
{ "walletAmountUsed": 100000, "cashAmountDue": 100000, "transaction": { /* WalletTransaction */ } }
```

### `POST /api/wallet/transactions` — **admin only**

Manual credit adjustment (positive amounts only — see `BusinessRules.md`).

```json
{ "userId": "uuid", "amount": 50000, "description": "Customer support goodwill credit" }
```

---

## Subscriptions

### `POST /api/subscriptions/purchase`

Charges the configured subscription price against the wallet (up to the `subscription` service's
limit), marks the subscription active, and — only now — grants subscription-level referral
rewards up the tree. `{ "userId": "uuid" }` →

```json
{ "subscriptionId": "uuid", "walletAmountUsed": 100000, "cashAmountDue": 100000, "status": "ACTIVE", "expiresAt": "2026-09-06T..." }
```

> In this MVP the purchase is treated as succeeding as soon as this endpoint is called. A
> production deployment should only call it from a payment gateway's confirmed-payment webhook —
> see `Security.md`.

---

## Admin — all require `X-Admin-Api-Key`

### `GET` / `PUT /api/admin/settings`

Reads/patches `ReferralSettings` (subscription price, base amount, all three level percentages,
registration reward percentage, max referral depth). `PUT` accepts any subset of fields.

### `GET /api/admin/wallet-limits` / `PUT /api/admin/wallet-limits`

Lists / upserts a `{ serviceKey, label, maxWalletUsagePercent }` row.

### `GET` / `POST /api/admin/campaigns`, `PUT /api/admin/campaigns/:id`

CRUD for time-boxed reward overrides — see `Campaigns.md`.

### `GET /api/admin/dashboard`

Every metric the PRD's "Admin Dashboard" section lists: total users/referrals, top referrers,
reward distribution by type, total outstanding wallet balance, subscription conversion, 30-day new
user growth, daily/monthly reward cost, and per-campaign summaries. A network *graph* visualization
is a frontend concern built on top of this data, not part of this API.
