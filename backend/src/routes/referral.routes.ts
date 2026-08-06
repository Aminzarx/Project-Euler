import { Router } from 'express';
import { z } from 'zod';
import { engines } from '../container';
import { asyncHandler } from '../middleware/asyncHandler';
import { validateBody } from '../middleware/validate';

export const referralRouter = Router();

/** GET /api/referral/validate/:code — lets the client check a code before submitting it, e.g.
 *  while the user is still typing it in at registration. */
referralRouter.get(
  '/validate/:code',
  asyncHandler(async (req, res) => {
    const owner = await engines.referral.validateReferralCode(req.params.code);
    res.json({ valid: Boolean(owner), ownerId: owner?.id ?? null });
  })
);

const applySchema = z.object({
  userId: z.string().min(1),
  code: z.string().min(4).max(12)
});

/** POST /api/referral/apply — for the (rarer) case a user enters a code after registering
 *  rather than during it. Still only allowed once, ever, per user. */
referralRouter.post(
  '/apply',
  validateBody(applySchema),
  asyncHandler(async (req, res) => {
    const user = await engines.referral.applyReferral(req.body.userId, req.body.code);
    res.json({ id: user.id, referrerId: user.referrerId });
  })
);

/** GET /api/referral/:userId/tree */
referralRouter.get(
  '/:userId/tree',
  asyncHandler(async (req, res) => {
    const tree = await engines.referral.getReferralTree(req.params.userId);
    res.json(tree);
  })
);

/** GET /api/referral/:userId/statistics */
referralRouter.get(
  '/:userId/statistics',
  asyncHandler(async (req, res) => {
    const stats = await engines.referral.getStatistics(req.params.userId);
    res.json(stats);
  })
);
