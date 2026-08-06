import { Router } from 'express';
import { z } from 'zod';
import { engines } from '../container';
import { adminAuth } from '../middleware/adminAuth';
import { asyncHandler } from '../middleware/asyncHandler';
import { validateBody } from '../middleware/validate';

export const walletRouter = Router();

/** GET /api/wallet/:userId/balance */
walletRouter.get(
  '/:userId/balance',
  asyncHandler(async (req, res) => {
    const balance = await engines.wallet.getBalance(req.params.userId);
    res.json({ balance });
  })
);

/** GET /api/wallet/:userId/history?limit=&offset= */
walletRouter.get(
  '/:userId/history',
  asyncHandler(async (req, res) => {
    const limit = Number(req.query.limit ?? 50);
    const offset = Number(req.query.offset ?? 0);
    const history = await engines.wallet.getHistory(req.params.userId, limit, offset);
    res.json(history);
  })
);

const purchaseSchema = z.object({
  userId: z.string().min(1),
  serviceKey: z.string().min(1),
  servicePrice: z.number().nonnegative()
});

/** POST /api/wallet/purchase — applies the wallet-funded portion of a purchase (subscription,
 *  featured listing, AI credits, ...), capped at that service's configured percentage. The
 *  caller is responsible for collecting `cashAmountDue` through whatever payment method backs
 *  cash payments — this endpoint only ever moves credits, never cash. */
walletRouter.post(
  '/purchase',
  validateBody(purchaseSchema),
  asyncHandler(async (req, res) => {
    const result = await engines.wallet.purchase(req.body.userId, req.body.serviceKey, req.body.servicePrice);
    res.json(result);
  })
);

const adjustmentSchema = z.object({
  userId: z.string().min(1),
  amount: z.number(),
  description: z.string().min(1)
});

/**
 * POST /api/wallet/transactions — admin-only manual credit adjustment (the "Manual Adjustment"
 * transaction type from the PRD). A positive amount credits the wallet; there is no debit path
 * here on purpose — reducing a user's balance without a matching purchase/refund reason isn't a
 * scenario the PRD calls for, and this MVP does not invent one.
 */
walletRouter.post(
  '/transactions',
  adminAuth,
  validateBody(adjustmentSchema),
  asyncHandler(async (req, res) => {
    const transaction = await engines.wallet.credit(
      req.body.userId,
      req.body.amount,
      'MANUAL_ADJUSTMENT',
      req.body.description
    );
    res.status(201).json(transaction);
  })
);
