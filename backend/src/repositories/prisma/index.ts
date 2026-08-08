import type { PrismaClient, User, TransactionType, SubscriptionStatus } from '@prisma/client';
import type {
  CampaignRepository,
  CampaignRewardRepository,
  CreateCampaignInput,
  CreateTransactionInput,
  CreateUserInput,
  ReferralLogRepository,
  ReferralRelationshipRepository,
  ReferralSettingsPatch,
  ReferralSettingsRepository,
  Repositories,
  ServiceWalletLimitRepository,
  SubscriptionRepository,
  UserRepository,
  WalletRepository,
  WalletTransactionRepository
} from '../types';

const REWARD_TYPES: TransactionType[] = ['REGISTRATION_REWARD', 'SUBSCRIPTION_REWARD', 'CAMPAIGN_REWARD'];

/** Default row for the ReferralSettings singleton, used the very first time it's read. */
const DEFAULT_SETTINGS = {
  subscriptionPrice: 200_000,
  baseAmount: 100_000,
  registrationRewardPercent: 3,
  level1Percent: 10,
  level2Percent: 5,
  level3Percent: 2,
  maxReferralDepth: 3
};

export function createPrismaRepositories(prisma: PrismaClient): Repositories {
  const users: UserRepository = {
    findById: (id) => prisma.user.findUnique({ where: { id } }),
    findByPhoneNumber: (phoneNumber) => prisma.user.findUnique({ where: { phoneNumber } }),
    findByReferralCode: (referralCode) => prisma.user.findUnique({ where: { referralCode } }),
    create: (input: CreateUserInput) => prisma.user.create({ data: input }),
    setReferrer: (userId, referrerId) => prisma.user.update({ where: { id: userId }, data: { referrerId } }),
    markPhoneVerified: (userId) =>
      prisma.user.update({ where: { id: userId }, data: { phoneVerifiedAt: new Date() } }),
    async getAncestors(userId, maxLevels) {
      // Reward distribution is structurally capped at 3 ancestor levels (see BusinessRules.md),
      // so a fixed 3-deep nested `include` is enough to cover every case in a single query —
      // this is the "maximum three database lookups" the PRD's Performance section asks for.
      const user = await prisma.user.findUnique({
        where: { id: userId },
        include: {
          referrer: {
            include: {
              referrer: {
                include: { referrer: true }
              }
            }
          }
        }
      });

      const ancestors: User[] = [];
      let node: (User & { referrer?: unknown }) | null | undefined = user?.referrer as User | undefined;
      while (node && ancestors.length < maxLevels) {
        const { referrer, ...plain } = node as User & { referrer?: unknown };
        ancestors.push(plain as User);
        node = referrer as typeof node;
      }
      return ancestors;
    },
    countDirectReferrals: (userId) => prisma.user.count({ where: { referrerId: userId } }),
    async countTotalNetwork(userId) {
      let total = 0;
      let frontier = [userId];
      while (frontier.length > 0) {
        const children = await prisma.user.findMany({
          where: { referrerId: { in: frontier } },
          select: { id: true }
        });
        total += children.length;
        frontier = children.map((c) => c.id);
      }
      return total;
    },
    async listTopReferrers(limit) {
      const grouped = await prisma.user.groupBy({
        by: ['referrerId'],
        where: { referrerId: { not: null } },
        _count: { referrerId: true },
        orderBy: { _count: { referrerId: 'desc' } },
        take: limit
      });
      const results = [];
      for (const g of grouped) {
        const user = await prisma.user.findUnique({ where: { id: g.referrerId! } });
        if (user) results.push({ user, directReferrals: g._count.referrerId });
      }
      return results;
    },
    count: () => prisma.user.count(),
    countReferred: () => prisma.user.count({ where: { referrerId: { not: null } } }),
    countCreatedSince: (since) => prisma.user.count({ where: { createdAt: { gte: since } } })
  };

  const wallets: WalletRepository = {
    findByUserId: (userId) => prisma.wallet.findUnique({ where: { userId } }),
    create: (userId) => prisma.wallet.create({ data: { userId, balance: 0 } }),
    adjustBalance: (walletId, delta) =>
      prisma.$transaction(async (tx) => {
        const wallet = await tx.wallet.findUnique({ where: { id: walletId } });
        if (!wallet) throw new Error(`Wallet ${walletId} not found`);
        const nextBalance = Number(wallet.balance) + delta;
        if (nextBalance < 0) throw new Error('Insufficient wallet balance');
        return tx.wallet.update({ where: { id: walletId }, data: { balance: nextBalance } });
      }),
    async sumAllBalances() {
      const result = await prisma.wallet.aggregate({ _sum: { balance: true } });
      return Number(result._sum.balance ?? 0);
    }
  };

  const walletTransactions: WalletTransactionRepository = {
    create: (input: CreateTransactionInput) => prisma.walletTransaction.create({ data: input }),
    listByUserId: (userId, limit = 50, offset = 0) =>
      prisma.walletTransaction.findMany({
        where: { userId },
        orderBy: { createdAt: 'desc' },
        take: limit,
        skip: offset
      }),
    async sumByType(type, since) {
      const result = await prisma.walletTransaction.aggregate({
        where: { type, ...(since ? { createdAt: { gte: since } } : {}) },
        _sum: { amount: true }
      });
      return Number(result._sum.amount ?? 0);
    },
    async sumAllRewards(since) {
      const result = await prisma.walletTransaction.aggregate({
        where: { type: { in: REWARD_TYPES }, ...(since ? { createdAt: { gte: since } } : {}) },
        _sum: { amount: true }
      });
      return Number(result._sum.amount ?? 0);
    }
  };

  const referralRelationships: ReferralRelationshipRepository = {
    create: (childId, parentId, referralCode) =>
      prisma.referralRelationship.create({ data: { childId, parentId, referralCode } }),
    findByChildId: (childId) => prisma.referralRelationship.findUnique({ where: { childId } })
  };

  const referralSettings: ReferralSettingsRepository = {
    async get() {
      const existing = await prisma.referralSettings.findUnique({ where: { id: 1 } });
      if (existing) return existing;
      return prisma.referralSettings.create({ data: { id: 1, ...DEFAULT_SETTINGS } });
    },
    update: (patch: ReferralSettingsPatch) =>
      prisma.referralSettings.upsert({
        where: { id: 1 },
        create: { id: 1, ...DEFAULT_SETTINGS, ...patch },
        update: patch
      })
  };

  const serviceWalletLimits: ServiceWalletLimitRepository = {
    get: (serviceKey) => prisma.serviceWalletLimit.findUnique({ where: { serviceKey } }),
    list: () => prisma.serviceWalletLimit.findMany(),
    upsert: (serviceKey, label, maxWalletUsagePercent) =>
      prisma.serviceWalletLimit.upsert({
        where: { serviceKey },
        create: { serviceKey, label, maxWalletUsagePercent },
        update: { label, maxWalletUsagePercent }
      })
  };

  const campaigns: CampaignRepository = {
    findActiveAt: (date) =>
      prisma.campaign.findFirst({ where: { active: true, startsAt: { lte: date }, endsAt: { gte: date } } }),
    list: () => prisma.campaign.findMany({ orderBy: { startsAt: 'desc' } }),
    findById: (id) => prisma.campaign.findUnique({ where: { id } }),
    create: (input: CreateCampaignInput) => prisma.campaign.create({ data: input }),
    update: (id, patch) => prisma.campaign.update({ where: { id }, data: patch })
  };

  const campaignRewards: CampaignRewardRepository = {
    create: (campaignId, walletTransactionId) =>
      prisma.campaignReward.create({ data: { campaignId, walletTransactionId } }),
    countByCampaign: (campaignId) => prisma.campaignReward.count({ where: { campaignId } })
  };

  const referralLogs: ReferralLogRepository = {
    create: (userId, event, metadata) =>
      prisma.referralLog.create({ data: { userId, event, metadata: metadata as object | undefined } })
  };

  const subscriptions: SubscriptionRepository = {
    create: (userId, priceAtPurchase) =>
      prisma.subscription.create({ data: { userId, priceAtPurchase, status: 'PENDING' } }),
    markPaid: (id, walletAmountUsed, cashAmountPaid, expiresAt) =>
      prisma.subscription.update({
        where: { id },
        data: { status: 'ACTIVE', walletAmountUsed, cashAmountPaid, startedAt: new Date(), expiresAt }
      }),
    countByStatus: (status: SubscriptionStatus) => prisma.subscription.count({ where: { status } }),
    findLatestActive: (userId) =>
      prisma.subscription.findFirst({ where: { userId, status: 'ACTIVE' }, orderBy: { createdAt: 'desc' } })
  };

  return {
    users,
    wallets,
    walletTransactions,
    referralRelationships,
    referralSettings,
    serviceWalletLimits,
    campaigns,
    campaignRewards,
    referralLogs,
    subscriptions
  };
}
