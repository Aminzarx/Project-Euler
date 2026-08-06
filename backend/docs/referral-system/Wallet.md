# Wallet

## What it is, and isn't

A **credit wallet**, not a cash wallet. `WalletEngine` is the only code that ever changes a
balance, and it exposes exactly two operations:

- `credit(userId, amount, type, description, options)` — always adds to *that user's own*
  wallet, always paired with a `WalletTransaction` row. Used for every reward type
  (`REGISTRATION_REWARD`, `SUBSCRIPTION_REWARD`, `CAMPAIGN_REWARD`), plus `MANUAL_ADJUSTMENT` and
  `REFUND`.
- `purchase(userId, serviceKey, servicePrice)` — debits *that user's own* wallet against a listed
  service, capped at the service's configured percentage.

There is no third operation. In particular, there is no "transfer credits to another user"
function anywhere in the codebase — "credits cannot be transferred between users" is true because
the capability doesn't exist, not because of a check that could be forgotten.

## Per-service usage limits

Every wallet-purchasable service — subscription, featured listings, property promotion, premium
services, AI credits, SMS packages, anything added later — has its own `ServiceWalletLimit` row
(`serviceKey`, `label`, `maxWalletUsagePercent`). `WalletEngine.purchase`:

1. Looks up the limit for `serviceKey`. **No configured row means 0%** — a service must be
   explicitly enabled before the wallet can cover any of it.
2. Computes `maxWalletAmount = floor(servicePrice * maxUsagePercent / 100)`.
3. Uses `min(currentBalance, maxWalletAmount)` — the wallet never covers more than the service
   allows, and never more than the user actually has.
4. Debits that amount (if any) and returns `{ walletAmountUsed, cashAmountDue }`. The caller is
   responsible for actually collecting `cashAmountDue` through whatever payment method backs cash
   payments — this engine never touches cash.

`WalletEngine.previewPurchase` runs the exact same math without touching the balance, for a
"here's what this would cost you" preview (used by the user dashboard's "Wallet Usage Preview").

## Configuring limits

`PUT /api/admin/wallet-limits` (admin-only) upserts a limit:

```json
{ "serviceKey": "featured_listing", "label": "Featured Listing", "maxWalletUsagePercent": 50 }
```

Matches the PRD's example directly: Subscription 100%, Featured Listing 50%, AI Credits 30% — set
independently, changeable at any time, with no code deploy required.

## Transaction types

`REGISTRATION_REWARD`, `SUBSCRIPTION_REWARD`, `MANUAL_ADJUSTMENT`, `CAMPAIGN_REWARD`, `PURCHASE`,
`REFUND`, `EXPIRATION` — the exact list from the PRD. `EXPIRATION` exists as an enum value ready
for a future credit-expiry feature; nothing in this codebase currently produces one (the PRD
explicitly calls it out as "if enabled in future").
