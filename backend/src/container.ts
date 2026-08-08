import { prisma } from './db/prismaClient';
import { createPrismaRepositories } from './repositories/prisma';
import { WalletEngine } from './engines/WalletEngine';
import { CampaignEngine } from './engines/CampaignEngine';
import { ReferralEngine } from './engines/ReferralEngine';
import type { Repositories } from './repositories/types';

export interface Engines {
  wallet: WalletEngine;
  campaigns: CampaignEngine;
  referral: ReferralEngine;
}

export function createEngines(repos: Repositories): Engines {
  const wallet = new WalletEngine(repos);
  const campaigns = new CampaignEngine(repos);
  const referral = new ReferralEngine(repos, wallet, campaigns);
  return { wallet, campaigns, referral };
}

/** The process-wide, Prisma-backed wiring the running server actually uses. Routes only ever
 *  import `engines`/`repositories` from here — never `PrismaClient` directly — which is what
 *  keeps them swappable for the in-memory repositories in tests. */
export const repositories: Repositories = createPrismaRepositories(prisma);
export const engines: Engines = createEngines(repositories);
