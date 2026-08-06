# Business Rules

## Referral tree

- Every user has at most one referrer (`User.referrerId`), set at most once, ever. Attempting to
  apply a second referral code fails (`ReferralEngine.applyReferral` checks
  `ReferralRelationship.findByChildId` first).
- A user cannot refer themselves.
- A user with no referrer is a root node — this is normal, not an error.
- The tree itself has unlimited width and depth. **Reward distribution** is capped at three
  ancestor levels regardless of how deep the tree actually is — a fourth-level ancestor (and
  beyond) is never rewarded for anything a descendant does.

## Referral codes

- Six characters, from `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` (uppercase letters and digits, excluding
  the visually ambiguous `0/O` and `1/I`) — see `ReferralEngine.generateUniqueReferralCode`.
- Tied to the user record, not their phone number — changing a phone number never invalidates a
  referral code.
- Regenerated on collision (checked against the database up to 10 attempts before giving up; at
  32 possible characters and 6 positions, a collision is astronomically unlikely long before
  "millions of users" is reached).

## Registration Reward

- 3% of the configured Base Amount (default: 100,000 Toman → 3,000 Toman), granted to the
  **direct referrer only** — never multi-level.
- Granted the moment the new user's phone number is verified (`ReferralEngine.verifyPhone`), *not*
  at registration itself, and *not* conditional on a subscription purchase.
- Granted at most once per referred user — calling `verifyPhone` again on an already-verified user
  is a no-op.

## Subscription Reward

- Triggered only after a subscription purchase actually succeeds
  (`SubscriptionRepository.markPaid` happens before `ReferralEngine.grantSubscriptionRewards` is
  called — see `routes/subscriptions.routes.ts`).
- Distributed to up to three ancestor levels: the direct referrer (level 1, default 10%), their
  referrer (level 2, default 5%), and *their* referrer (level 3, default 2%) — all percentages of
  the Base Amount, not the subscription price.
- Stops the moment the tree runs out of ancestors; a root-node payer, or one with only one or two
  ancestors, simply generates fewer reward transactions.

## Wallet

- Credits are internal-only: no cash withdrawal, no transfer between users. The `WalletEngine`
  API surface only offers `credit` (a user's own wallet) and `purchase` (debit against a listed
  service) — there is no operation that could move credits from one user to another even by
  accident.
- Every wallet-purchasable service (subscription, featured listing, AI credits, SMS packages, ...)
  independently caps how much of its price the wallet may cover, via `ServiceWalletLimit.
  maxWalletUsagePercent`. A service with no configured limit defaults to 0% — it must be
  explicitly enabled before the wallet can pay for any part of it.
- A wallet-funded purchase never uses more than the current balance, even if the service's
  percentage limit would technically allow more.

## Fraud philosophy (as specified by the PRD, not softened)

The business deliberately favors growth over strict anti-fraud. Analytics fields (device
fingerprint, installation ID, IP address, device model, OS) are stored on every `User` row and
never used to block registration, referral application, or reward payout. They exist so that
*future* fraud detection can be layered on top without a schema change — see `Security.md`.
