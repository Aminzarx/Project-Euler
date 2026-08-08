import express from 'express';
import { usersRouter } from './routes/users.routes';
import { referralRouter } from './routes/referral.routes';
import { walletRouter } from './routes/wallet.routes';
import { subscriptionsRouter } from './routes/subscriptions.routes';
import { adminRouter } from './routes/admin.routes';
import { errorHandler } from './middleware/errorHandler';

export function createApp() {
  const app = express();
  app.use(express.json());

  app.get('/health', (_req, res) => res.json({ status: 'ok' }));

  app.use('/api/users', usersRouter);
  app.use('/api/referral', referralRouter);
  app.use('/api/wallet', walletRouter);
  app.use('/api/subscriptions', subscriptionsRouter);
  app.use('/api/admin', adminRouter);

  app.use((_req, res) => res.status(404).json({ error: 'Not found' }));
  app.use(errorHandler);

  return app;
}
