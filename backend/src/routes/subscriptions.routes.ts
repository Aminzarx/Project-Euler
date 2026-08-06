import { Router } from 'express';
import { z } from 'zod';
import { engines, repositories } from '../container';
import { asyncHandler } from '../middleware/asyncHandler';
import { validateBody } from '../middleware/validate';

export const subscriptionsRouter = Router();

const purchaseSchema = z.object({ userId: z.string().min(1) });

/**
 * POST /api/subscriptions/purchase — the one and only place subscription-level referral rewards
 * are granted, and only after the purchase actually succeeds (see BusinessRules.md: "Rewards are
 * created only after successful payment"). This MVP treats the purchase as succeeding as soon as
 * this endpoint is called — a real deployment would call this only from a payment gateway's
 * confirmed-payment webhook, not directly from the client.
 */
subscriptionsRouter.post(
  '/purchase',
  validateBody(purchaseSchema),
  asyncHandler(async (req, res) => {
    const userId = req.body.userId as string;
    const user = await repositories.users.findById(userId);
    if (!user) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    const settings = await engines.campaigns.getEffectiveSettings();
    const subscription = await repositories.subscriptions.create(userId, settings.subscriptionPrice);

    const { walletAmountUsed, cashAmountDue } = await engines.wallet.purchase(
      userId,
      'subscription',
      settings.subscriptionPrice
    );

    const oneMonthFromNow = new Date();
    oneMonthFromNow.setMonth(oneMonthFromNow.getMonth() + 1);
    await repositories.subscriptions.markPaid(subscription.id, walletAmountUsed, cashAmountDue, oneMonthFromNow);

    await engines.referral.grantSubscriptionRewards(userId, subscription.id);

    res.status(201).json({
      subscriptionId: subscription.id,
      walletAmountUsed,
      cashAmountDue,
      status: 'ACTIVE',
      expiresAt: oneMonthFromNow
    });
  })
);
