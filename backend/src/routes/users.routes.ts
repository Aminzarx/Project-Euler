import { Router } from 'express';
import { z } from 'zod';
import { engines, repositories } from '../container';
import { asyncHandler } from '../middleware/asyncHandler';
import { validateBody } from '../middleware/validate';

export const usersRouter = Router();

const registerSchema = z.object({
  phoneNumber: z.string().min(3),
  referralCode: z
    .string()
    .trim()
    .toUpperCase()
    .length(8, 'Referral code must be 8 characters')
    .regex(/^[A-Z0-9]{8}$/, 'Referral code must be 8 uppercase letters/digits'),
  deviceFingerprint: z.string().optional(),
  installationId: z.string().optional(),
  ipAddress: z.string().optional(),
  deviceModel: z.string().optional(),
  operatingSystem: z.string().optional()
});

/** POST /api/users/register — a referral code is mandatory (see BusinessRules.md) and is applied
 *  immediately on success. This does NOT grant the registration reward yet — see /verify-phone. */
usersRouter.post(
  '/register',
  validateBody(registerSchema),
  asyncHandler(async (req, res) => {
    const user = await engines.referral.registerUser(req.body);
    await engines.wallet.getOrCreateWallet(user.id);
    res.status(201).json({
      id: user.id,
      phoneNumber: user.phoneNumber,
      referralCode: user.referralCode,
      referrerId: user.referrerId
    });
  })
);

/** POST /api/users/:id/verify-phone — grants the direct referrer's Registration Reward the first
 *  time this is called for a given user; a no-op on every call after that. */
usersRouter.post(
  '/:id/verify-phone',
  asyncHandler(async (req, res) => {
    await engines.referral.verifyPhone(req.params.id);
    res.json({ success: true });
  })
);

/** GET /api/users/:id/dashboard — everything the "User Dashboard" section of the PRD asks for,
 *  in one call: wallet balance, lifetime/registration/subscription rewards, referral link data,
 *  network size, reward history, upcoming subscription cost, and a wallet-usage preview for it. */
usersRouter.get(
  '/:id/dashboard',
  asyncHandler(async (req, res) => {
    const userId = req.params.id;
    const user = await repositories.users.findById(userId);
    if (!user) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    const [balance, stats, tree, history, settings] = await Promise.all([
      engines.wallet.getBalance(userId),
      engines.referral.getStatistics(userId),
      engines.referral.getReferralTree(userId),
      engines.wallet.getHistory(userId, 20, 0),
      engines.campaigns.getEffectiveSettings()
    ]);
    const walletUsagePreview = await engines.wallet.previewPurchase(userId, 'subscription', settings.subscriptionPrice);

    res.json({
      walletBalance: balance,
      lifetimeRewards: stats.lifetimeRewards,
      registrationRewards: stats.registrationRewards,
      subscriptionRewards: stats.subscriptionRewards,
      referralCode: user.referralCode,
      referralLink: `https://app.example.com/r/${user.referralCode}`,
      directReferrals: tree.directReferrals,
      totalNetwork: tree.totalNetwork,
      rewardHistory: history,
      upcomingSubscriptionCost: settings.subscriptionPrice,
      walletUsagePreview
    });
  })
);
