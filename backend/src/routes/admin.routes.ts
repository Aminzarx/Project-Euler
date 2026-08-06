import { Router } from 'express';
import { z } from 'zod';
import { engines, repositories } from '../container';
import { adminAuth } from '../middleware/adminAuth';
import { asyncHandler } from '../middleware/asyncHandler';
import { validateBody } from '../middleware/validate';

export const adminRouter = Router();
adminRouter.use(adminAuth);

// ---------- Reward settings ----------

const settingsPatchSchema = z
  .object({
    subscriptionPrice: z.number().positive().optional(),
    baseAmount: z.number().positive().optional(),
    registrationRewardPercent: z.number().min(0).max(100).optional(),
    level1Percent: z.number().min(0).max(100).optional(),
    level2Percent: z.number().min(0).max(100).optional(),
    level3Percent: z.number().min(0).max(100).optional(),
    maxReferralDepth: z.number().int().min(0).max(3).optional()
  })
  .strict();

/** GET /api/admin/settings — every reward/pricing knob the PRD requires to be admin-configurable
 *  without a code change. */
adminRouter.get(
  '/settings',
  asyncHandler(async (_req, res) => {
    res.json(await repositories.referralSettings.get());
  })
);

/** PUT /api/admin/settings — partial update; only the fields provided are changed. */
adminRouter.put(
  '/settings',
  validateBody(settingsPatchSchema),
  asyncHandler(async (req, res) => {
    res.json(await repositories.referralSettings.update(req.body));
  })
);

// ---------- Per-service wallet usage limits ----------

const walletLimitSchema = z.object({
  serviceKey: z.string().min(1),
  label: z.string().min(1),
  maxWalletUsagePercent: z.number().min(0).max(100)
});

/** GET /api/admin/wallet-limits — every wallet-purchasable service and how much of its price the
 *  wallet may cover (Subscription 100%, Featured Listing 50%, AI Credits 30%, ... in the PRD's
 *  example — all of it configurable here, nothing hardcoded). */
adminRouter.get(
  '/wallet-limits',
  asyncHandler(async (_req, res) => {
    res.json(await repositories.serviceWalletLimits.list());
  })
);

adminRouter.put(
  '/wallet-limits',
  validateBody(walletLimitSchema),
  asyncHandler(async (req, res) => {
    const { serviceKey, label, maxWalletUsagePercent } = req.body;
    res.json(await repositories.serviceWalletLimits.upsert(serviceKey, label, maxWalletUsagePercent));
  })
);

// ---------- Campaigns ----------

const campaignSchema = z.object({
  name: z.string().min(1),
  startsAt: z.coerce.date(),
  endsAt: z.coerce.date(),
  active: z.boolean().optional(),
  registrationRewardPercent: z.number().min(0).max(100).nullable().optional(),
  level1Percent: z.number().min(0).max(100).nullable().optional(),
  level2Percent: z.number().min(0).max(100).nullable().optional(),
  level3Percent: z.number().min(0).max(100).nullable().optional()
});

adminRouter.get(
  '/campaigns',
  asyncHandler(async (_req, res) => {
    res.json(await engines.campaigns.listCampaigns());
  })
);

adminRouter.post(
  '/campaigns',
  validateBody(campaignSchema),
  asyncHandler(async (req, res) => {
    res.status(201).json(await engines.campaigns.createCampaign(req.body));
  })
);

adminRouter.put(
  '/campaigns/:id',
  validateBody(campaignSchema.partial()),
  asyncHandler(async (req, res) => {
    res.json(await engines.campaigns.updateCampaign(req.params.id, req.body));
  })
);

// ---------- Dashboard ----------

/**
 * GET /api/admin/dashboard — the metrics the PRD's "Admin Dashboard" section lists. A network
 * *graph* (the actual node/edge visualization) is a frontend concern once an admin UI exists;
 * this endpoint returns the numbers that graph — and every other tile — would be built from.
 */
adminRouter.get(
  '/dashboard',
  asyncHandler(async (_req, res) => {
    const since24h = new Date(Date.now() - 24 * 60 * 60 * 1000);
    const since30d = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);

    const [
      totalUsers,
      totalReferrals,
      topReferrers,
      registrationRewardTotal,
      subscriptionRewardTotal,
      campaignRewardTotal,
      totalWalletBalance,
      activeSubscriptions,
      pendingSubscriptions,
      newUsersLast30Days,
      dailyRewardCost,
      monthlyRewardCost,
      campaigns
    ] = await Promise.all([
      repositories.users.count(),
      repositories.users.countReferred(),
      repositories.users.listTopReferrers(10),
      repositories.walletTransactions.sumByType('REGISTRATION_REWARD'),
      repositories.walletTransactions.sumByType('SUBSCRIPTION_REWARD'),
      repositories.walletTransactions.sumByType('CAMPAIGN_REWARD'),
      repositories.wallets.sumAllBalances(),
      repositories.subscriptions.countByStatus('ACTIVE'),
      repositories.subscriptions.countByStatus('PENDING'),
      repositories.users.countCreatedSince(since30d),
      repositories.walletTransactions.sumAllRewards(since24h),
      repositories.walletTransactions.sumAllRewards(since30d),
      engines.campaigns.listCampaigns()
    ]);

    res.json({
      totalUsers,
      totalReferrals,
      registrationConversionRate: totalUsers > 0 ? totalReferrals / totalUsers : 0,
      topReferrers: topReferrers.map((r) => ({
        userId: r.user.id,
        phoneNumber: r.user.phoneNumber,
        referralCode: r.user.referralCode,
        directReferrals: r.directReferrals
      })),
      rewardDistribution: {
        registration: registrationRewardTotal,
        subscription: subscriptionRewardTotal,
        campaign: campaignRewardTotal
      },
      walletStatistics: { totalOutstandingBalance: totalWalletBalance },
      subscriptionConversion: {
        active: activeSubscriptions,
        pending: pendingSubscriptions
      },
      growthRate: { newUsersLast30Days },
      dailyRewardCost,
      monthlyRewardCost,
      campaignStatistics: campaigns.map((c) => ({
        id: c.id,
        name: c.name,
        active: c.active,
        startsAt: c.startsAt,
        endsAt: c.endsAt
      }))
    });
  })
);
