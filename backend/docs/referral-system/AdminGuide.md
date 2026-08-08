# Admin Guide

Everything below is called with the `X-Admin-Api-Key` header set to the server's `ADMIN_API_KEY`.
There's no built admin web UI yet (see `Architecture.md`) — these are the raw API calls whatever
internal tool or script you use should make. Examples use `curl`.

## Changing reward percentages / pricing

```bash
curl -X PUT https://your-api-host/api/admin/settings \
  -H "X-Admin-Api-Key: $ADMIN_API_KEY" -H "Content-Type: application/json" \
  -d '{ "level1Percent": 12, "baseAmount": 120000 }'
```

Only the fields you send are changed — omit anything you don't want to touch. Valid fields:
`subscriptionPrice`, `baseAmount`, `registrationRewardPercent`, `level1Percent`, `level2Percent`,
`level3Percent`, `maxReferralDepth` (0–3).

## Enabling a service for wallet payments

A service you haven't configured defaults to 0% wallet coverage — nothing is purchasable with
credits until you explicitly enable it:

```bash
curl -X PUT https://your-api-host/api/admin/wallet-limits \
  -H "X-Admin-Api-Key: $ADMIN_API_KEY" -H "Content-Type: application/json" \
  -d '{ "serviceKey": "ai_credits", "label": "AI Credits", "maxWalletUsagePercent": 30 }'
```

Call this again with the same `serviceKey` any time to change the percentage.

## Running a campaign

```bash
curl -X POST https://your-api-host/api/admin/campaigns \
  -H "X-Admin-Api-Key: $ADMIN_API_KEY" -H "Content-Type: application/json" \
  -d '{
    "name": "Double Rewards Weekend",
    "startsAt": "2026-08-08T00:00:00Z",
    "endsAt": "2026-08-10T00:00:00Z",
    "level1Percent": 15,
    "level2Percent": 10,
    "level3Percent": 5
  }'
```

Leave any of the four percent fields out entirely (or send `null`) to leave that reward tier at
its normal default while the campaign is running. The campaign takes effect automatically once
`startsAt` arrives — no separate "go live" step — and stops automatically at `endsAt`. To end one
early, `PUT /api/admin/campaigns/:id` with `{ "active": false }`.

Keep campaign windows non-overlapping — the system only resolves one active campaign at a time
(see `Campaigns.md`).

## Reading the dashboard

```bash
curl https://your-api-host/api/admin/dashboard -H "X-Admin-Api-Key: $ADMIN_API_KEY"
```

Returns total users, total referred users, top 10 referrers, reward totals by type, total
outstanding wallet balance across every user, active/pending subscription counts, new users in the
last 30 days, reward cost in the last 24 hours and 30 days, and a summary of every campaign. See
`API.md` for the exact response shape.

## Rotating the admin key

`ADMIN_API_KEY` is a single shared secret, not a per-admin login (see `Security.md`). Rotate it by
changing the environment variable and redeploying — every caller using the old value will need the
new one. There's no in-place "list of valid keys" to manage; if you need multiple admins with
individually revocable access, that's the real per-admin-auth system flagged in `Security.md`, not
something layered onto this single-key scheme.
