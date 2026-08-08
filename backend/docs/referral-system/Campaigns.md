# Campaigns

## Purpose

Lets an admin temporarily change reward percentages — "double rewards this weekend", a "New Year"
registration-reward boost — without a code deploy. This is `CampaignEngine`, kept deliberately
separate from both `WalletEngine` (which only knows how to move credits, not why) and
`ReferralEngine` (which decides *that* a reward is owed, then asks `CampaignEngine` what
percentage to use).

## Model

A `Campaign` has a name, a `startsAt`/`endsAt` window, an `active` flag, and four **nullable**
override fields: `registrationRewardPercent`, `level1Percent`, `level2Percent`, `level3Percent`.
Nullable is the whole mechanism — a campaign that only wants to change `level1Percent` leaves
everything else `null`, and those fields simply fall back to the base `ReferralSettings`.

## Resolution

`CampaignEngine.getEffectiveSettings(at?)`:

1. Reads the base `ReferralSettings` (the admin-configured defaults).
2. Finds the campaign, if any, where `active` is true and `startsAt <= at <= endsAt`
   (`getActiveCampaign`). At most one campaign is expected to be active at a time in this MVP —
   nothing currently resolves a conflict between two overlapping active campaigns, so keep
   campaign windows non-overlapping when scheduling them.
3. For each of the four override-able fields, uses the campaign's value if it's non-null,
   otherwise the base setting's value.

`ReferralEngine` calls this once per reward-granting operation (`verifyPhone`,
`grantSubscriptionRewards`) — a campaign's effect is visible on the very next reward calculated
after it becomes active, with no cache to invalidate.

## Reward tagging

When a reward was actually influenced by an active campaign's override for that specific level,
the resulting `WalletTransaction.type` is `CAMPAIGN_REWARD` instead of the normal
`REGISTRATION_REWARD`/`SUBSCRIPTION_REWARD`, and a `CampaignReward` row links it back to the
campaign — this is what feeds the admin dashboard's "Campaign Statistics" and lets a user's
transaction history show *why* a reward was larger than usual.

If a campaign is active but didn't override the specific level being rewarded (e.g. it only
touches `level1Percent` and this is a level-2 reward), that reward is tagged normally — "was this
reward's amount actually changed by a campaign" is what the tag means, not "was some campaign
merely active at the time."

## Managing campaigns

All via `/api/admin/campaigns` (admin-only, see `API.md`):

- `GET` — list every campaign.
- `POST` — create one (`endsAt` must be after `startsAt`).
- `PUT /:id` — patch any subset of fields.

There is no separate "activate"/"deactivate" endpoint beyond `PUT`ing `active: false`, or letting
a campaign's own `endsAt` pass.
