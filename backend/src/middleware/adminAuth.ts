import type { RequestHandler } from 'express';

const ADMIN_HEADER = 'x-admin-api-key';

/**
 * Guards every /api/admin/* route with a shared-secret header (ADMIN_API_KEY). This is a service
 * credential for whatever internal admin tool calls these endpoints, not a per-admin user login —
 * see docs/referral-system/Security.md for what a multi-admin production rollout should add
 * (per-admin accounts, audit log of who changed what) on top of this.
 */
export const adminAuth: RequestHandler = (req, res, next) => {
  const expected = process.env.ADMIN_API_KEY;
  const provided = req.header(ADMIN_HEADER);
  if (!expected || provided !== expected) {
    res.status(401).json({ error: 'Invalid or missing admin API key' });
    return;
  }
  next();
};
