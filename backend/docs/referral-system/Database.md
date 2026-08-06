# Database

Full schema: [`prisma/schema.prisma`](../../prisma/schema.prisma). PostgreSQL, normalized —
every table below maps 1:1 to a PRD "Database Design" entry.

## User

The referral tree, self-referencing via `referrerId`. `referralCode` is unique and independent of
`phoneNumber` — changing a phone number never breaks a referral link. Analytics-only fields
(`deviceFingerprint`, `installationId`, `ipAddress`, `deviceModel`, `operatingSystem`) are stored
here per the PRD's fraud philosophy, never used to gate anything. `phoneVerifiedAt` gates the
Registration Reward (null until `ReferralEngine.verifyPhone` runs).

## Wallet

One per user (`userId` unique). `balance` is a `Decimal(14,2)` — the domain only ever deals in
whole Toman amounts in practice, but the column keeps headroom for fractional currency if that
ever changes.

## WalletTransaction

The append-only ledger — "every credit movement must generate a transaction" is enforced by
`WalletEngine` never adjusting a balance without also inserting one of these in the same call.
`amount` is signed (positive credit, negative debit). `sourceUserId` + `level` record *who* caused
a referral reward and at what tier; `referenceType`/`referenceId` point at whatever caused a
Purchase/Refund (a subscription ID, a service key, ...).

## Subscription

One row per subscription purchase attempt. `status` starts `PENDING`, becomes `ACTIVE` once
`markPaid` runs (see `Wallet.md` for the purchase flow), and would move to `EXPIRED`/`CANCELLED`
via whatever renewal/cancellation job is added later (not built in this pass — see
`Architecture.md`).

## ReferralRelationship

A historical, append-only record of "this child applied this referral code from this parent at
this time" (`childId` unique — applying a code is a one-time event). Kept distinct from
`User.referrerId` (the live pointer used for tree traversal) purely for audit: if an admin ever
needed to correct `referrerId`, the original history wouldn't be lost.

## ReferralSettings

Singleton row (`id` is always `1`) holding every reward/pricing knob the PRD requires to be
admin-configurable: `subscriptionPrice`, `baseAmount`, `registrationRewardPercent`, `level1/2/
3Percent`, `maxReferralDepth`. Created lazily with sensible defaults the first time it's read
(`ReferralSettingsRepository.get` in `repositories/prisma/index.ts`) so there's no separate seed
step required before the API can serve requests.

## ServiceWalletLimit

One row per wallet-purchasable service (`serviceKey` unique — e.g. `subscription`,
`featured_listing`, `ai_credits`, `sms_package`). Nothing is hardcoded: adding a new
wallet-purchasable feature later means inserting a row here, not shipping new code.

## Campaign

A time-boxed override (`startsAt`/`endsAt`) of any subset of `ReferralSettings`' percentages.
Every override field is nullable — a campaign that only wants to boost `level1Percent` leaves the
rest `null`, and `CampaignEngine.getEffectiveSettings` falls back to the base settings for
anything a campaign didn't specify.

## CampaignReward

Links a `WalletTransaction` (unique — a transaction is either a normal reward or a campaign
reward, never both) back to the `Campaign` that produced it, purely for the admin dashboard's
"Campaign Statistics".

## ReferralLog

Append-only, analytics-only event stream (`REGISTRATION`, `REFERRAL_APPLIED`,
`REGISTRATION_REWARD_GRANTED`, `SUBSCRIPTION_REWARDS_GRANTED`, ...) with a free-form `metadata`
JSON column. Exists for future analysis, never read by any business-logic decision today.
