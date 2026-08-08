import { randomUUID } from 'crypto';
import type {
  User,
  Wallet,
  WalletTransaction,
  ReferralRelationship,
  ReferralSettings,
  ServiceWalletLimit,
  Campaign,
  CampaignReward,
  ReferralLog,
  Subscription,
  TransactionType,
  SubscriptionStatus,
  UserStatus
} from '@prisma/client';
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

/**
 * Pure in-memory implementation of every repository interface, used by the engine unit tests
 * (src/__tests__/*) so business logic can be verified without a running Postgres instance. Not
 * used by the actual server — see src/repositories/prisma for that.
 */
/** Test-only escape hatch beyond the UserRepository interface — lets unit tests flip a user's
 *  status (e.g. to BLOCKED) without adding a status-mutation method to the shared repository
 *  contract, which would otherwise force the Prisma-backed repo to implement it too even though
 *  status changes are a future admin action, not a caller-supplied field. */
export interface MemoryRepositories extends Repositories {
  __setUserStatusForTesting(userId: string, status: UserStatus): void;
}

export function createMemoryRepositories(): MemoryRepositories {
  const users = new Map<string, User>();
  const wallets = new Map<string, Wallet>();
  const walletsByUser = new Map<string, string>();
  const transactions: WalletTransaction[] = [];
  const relationships = new Map<string, ReferralRelationship>();
  const serviceLimits = new Map<string, ServiceWalletLimit>();
  const campaigns = new Map<string, Campaign>();
  const campaignRewards: CampaignReward[] = [];
  const referralLogs: ReferralLog[] = [];
  const subscriptions: Subscription[] = [];

  let settings: ReferralSettings = {
    id: 1,
    subscriptionPrice: 200_000 as unknown as ReferralSettings['subscriptionPrice'],
    baseAmount: 100_000 as unknown as ReferralSettings['baseAmount'],
    registrationRewardPercent: 3 as unknown as ReferralSettings['registrationRewardPercent'],
    level1Percent: 10 as unknown as ReferralSettings['level1Percent'],
    level2Percent: 5 as unknown as ReferralSettings['level2Percent'],
    level3Percent: 2 as unknown as ReferralSettings['level3Percent'],
    maxReferralDepth: 3,
    updatedAt: new Date()
  };

  const userRepo: UserRepository = {
    async findById(id) {
      return users.get(id) ?? null;
    },
    async findByPhoneNumber(phoneNumber) {
      return [...users.values()].find((u) => u.phoneNumber === phoneNumber) ?? null;
    },
    async findByReferralCode(referralCode) {
      return [...users.values()].find((u) => u.referralCode === referralCode) ?? null;
    },
    async create(input: CreateUserInput) {
      const now = new Date();
      const user: User = {
        id: randomUUID(),
        phoneNumber: input.phoneNumber,
        referralCode: input.referralCode,
        referrerId: null,
        deviceFingerprint: input.deviceFingerprint ?? null,
        installationId: input.installationId ?? null,
        ipAddress: input.ipAddress ?? null,
        deviceModel: input.deviceModel ?? null,
        operatingSystem: input.operatingSystem ?? null,
        status: 'ACTIVE',
        phoneVerifiedAt: null,
        createdAt: now,
        updatedAt: now
      };
      users.set(user.id, user);
      return user;
    },
    async setReferrer(userId, referrerId) {
      const user = users.get(userId);
      if (!user) throw new Error(`User ${userId} not found`);
      const updated = { ...user, referrerId, updatedAt: new Date() };
      users.set(userId, updated);
      return updated;
    },
    async markPhoneVerified(userId) {
      const user = users.get(userId);
      if (!user) throw new Error(`User ${userId} not found`);
      const updated = { ...user, phoneVerifiedAt: new Date(), updatedAt: new Date() };
      users.set(userId, updated);
      return updated;
    },
    async getAncestors(userId, maxLevels) {
      const ancestors: User[] = [];
      let current = users.get(userId) ?? null;
      while (current?.referrerId && ancestors.length < maxLevels) {
        const parent = users.get(current.referrerId) ?? null;
        if (!parent) break;
        ancestors.push(parent);
        current = parent;
      }
      return ancestors;
    },
    async countDirectReferrals(userId) {
      return [...users.values()].filter((u) => u.referrerId === userId).length;
    },
    async countTotalNetwork(userId) {
      let count = 0;
      const queue = [userId];
      while (queue.length > 0) {
        const current = queue.pop()!;
        const children = [...users.values()].filter((u) => u.referrerId === current);
        count += children.length;
        queue.push(...children.map((c) => c.id));
      }
      return count;
    },
    async listTopReferrers(limit) {
      const counts = new Map<string, number>();
      for (const u of users.values()) {
        if (u.referrerId) counts.set(u.referrerId, (counts.get(u.referrerId) ?? 0) + 1);
      }
      return [...counts.entries()]
        .sort((a, b) => b[1] - a[1])
        .slice(0, limit)
        .map(([userId, directReferrals]) => ({ user: users.get(userId)!, directReferrals }));
    },
    async count() {
      return users.size;
    },
    async countReferred() {
      return [...users.values()].filter((u) => u.referrerId).length;
    },
    async countCreatedSince(since) {
      return [...users.values()].filter((u) => u.createdAt >= since).length;
    }
  };

  const walletRepo: WalletRepository = {
    async findByUserId(userId) {
      const walletId = walletsByUser.get(userId);
      return walletId ? wallets.get(walletId) ?? null : null;
    },
    async create(userId) {
      const wallet: Wallet = {
        id: randomUUID(),
        userId,
        balance: 0 as unknown as Wallet['balance'],
        createdAt: new Date(),
        updatedAt: new Date()
      };
      wallets.set(wallet.id, wallet);
      walletsByUser.set(userId, wallet.id);
      return wallet;
    },
    async adjustBalance(walletId, delta) {
      const wallet = wallets.get(walletId);
      if (!wallet) throw new Error(`Wallet ${walletId} not found`);
      const currentBalance = Number(wallet.balance);
      const nextBalance = currentBalance + delta;
      if (nextBalance < 0) throw new Error('Insufficient wallet balance');
      const updated = { ...wallet, balance: nextBalance as unknown as Wallet['balance'], updatedAt: new Date() };
      wallets.set(walletId, updated);
      return updated;
    },
    async sumAllBalances() {
      return [...wallets.values()].reduce((sum, w) => sum + Number(w.balance), 0);
    }
  };

  const walletTransactionRepo: WalletTransactionRepository = {
    async create(input: CreateTransactionInput) {
      const tx: WalletTransaction = {
        id: randomUUID(),
        walletId: input.walletId,
        userId: input.userId,
        amount: input.amount as unknown as WalletTransaction['amount'],
        type: input.type,
        sourceUserId: input.sourceUserId ?? null,
        level: input.level ?? null,
        referenceType: input.referenceType ?? null,
        referenceId: input.referenceId ?? null,
        description: input.description,
        createdAt: new Date()
      };
      transactions.push(tx);
      return tx;
    },
    async listByUserId(userId, limit = 50, offset = 0) {
      // Reversed before the (stable) sort so transactions created within the same millisecond —
      // easy to hit in fast-running tests — still come back most-recently-inserted-first, same
      // as a real "ORDER BY createdAt DESC" would read in practice.
      return [...transactions]
        .filter((t) => t.userId === userId)
        .reverse()
        .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime())
        .slice(offset, offset + limit);
    },
    async sumByType(type: TransactionType, since?: Date) {
      return transactions
        .filter((t) => t.type === type && (!since || t.createdAt >= since))
        .reduce((sum, t) => sum + Number(t.amount), 0);
    },
    async sumAllRewards(since?: Date) {
      const rewardTypes: TransactionType[] = ['REGISTRATION_REWARD', 'SUBSCRIPTION_REWARD', 'CAMPAIGN_REWARD'];
      return transactions
        .filter((t) => rewardTypes.includes(t.type) && (!since || t.createdAt >= since))
        .reduce((sum, t) => sum + Number(t.amount), 0);
    }
  };

  const referralRelationshipRepo: ReferralRelationshipRepository = {
    async create(childId, parentId, referralCode) {
      const rel: ReferralRelationship = {
        id: randomUUID(),
        childId,
        parentId,
        referralCode,
        appliedAt: new Date()
      };
      relationships.set(childId, rel);
      return rel;
    },
    async findByChildId(childId) {
      return relationships.get(childId) ?? null;
    }
  };

  const referralSettingsRepo: ReferralSettingsRepository = {
    async get() {
      return settings;
    },
    async update(patch: ReferralSettingsPatch) {
      settings = { ...settings, ...(patch as Partial<ReferralSettings>), updatedAt: new Date() };
      return settings;
    }
  };

  const serviceWalletLimitRepo: ServiceWalletLimitRepository = {
    async get(serviceKey) {
      return serviceLimits.get(serviceKey) ?? null;
    },
    async list() {
      return [...serviceLimits.values()];
    },
    async upsert(serviceKey, label, maxWalletUsagePercent) {
      const existing = serviceLimits.get(serviceKey);
      const entry: ServiceWalletLimit = {
        id: existing?.id ?? randomUUID(),
        serviceKey,
        label,
        maxWalletUsagePercent: maxWalletUsagePercent as unknown as ServiceWalletLimit['maxWalletUsagePercent'],
        updatedAt: new Date()
      };
      serviceLimits.set(serviceKey, entry);
      return entry;
    }
  };

  const campaignRepo: CampaignRepository = {
    async findActiveAt(date) {
      return (
        [...campaigns.values()].find((c) => c.active && c.startsAt <= date && c.endsAt >= date) ?? null
      );
    },
    async list() {
      return [...campaigns.values()];
    },
    async findById(id) {
      return campaigns.get(id) ?? null;
    },
    async create(input: CreateCampaignInput) {
      const campaign: Campaign = {
        id: randomUUID(),
        name: input.name,
        startsAt: input.startsAt,
        endsAt: input.endsAt,
        active: input.active ?? true,
        registrationRewardPercent: (input.registrationRewardPercent ?? null) as unknown as Campaign['registrationRewardPercent'],
        level1Percent: (input.level1Percent ?? null) as unknown as Campaign['level1Percent'],
        level2Percent: (input.level2Percent ?? null) as unknown as Campaign['level2Percent'],
        level3Percent: (input.level3Percent ?? null) as unknown as Campaign['level3Percent'],
        createdAt: new Date(),
        updatedAt: new Date()
      };
      campaigns.set(campaign.id, campaign);
      return campaign;
    },
    async update(id, patch) {
      const existing = campaigns.get(id);
      if (!existing) throw new Error(`Campaign ${id} not found`);
      const updated = { ...existing, ...patch, updatedAt: new Date() } as Campaign;
      campaigns.set(id, updated);
      return updated;
    }
  };

  const campaignRewardRepo: CampaignRewardRepository = {
    async create(campaignId, walletTransactionId) {
      const reward: CampaignReward = {
        id: randomUUID(),
        campaignId,
        walletTransactionId,
        createdAt: new Date()
      };
      campaignRewards.push(reward);
      return reward;
    },
    async countByCampaign(campaignId) {
      return campaignRewards.filter((r) => r.campaignId === campaignId).length;
    }
  };

  const referralLogRepo: ReferralLogRepository = {
    async create(userId, event, metadata) {
      const log: ReferralLog = {
        id: randomUUID(),
        userId,
        event,
        metadata: (metadata ?? null) as ReferralLog['metadata'],
        createdAt: new Date()
      };
      referralLogs.push(log);
      return log;
    }
  };

  const subscriptionRepo: SubscriptionRepository = {
    async create(userId, priceAtPurchase) {
      const sub: Subscription = {
        id: randomUUID(),
        userId,
        status: 'PENDING',
        priceAtPurchase: priceAtPurchase as unknown as Subscription['priceAtPurchase'],
        walletAmountUsed: 0 as unknown as Subscription['walletAmountUsed'],
        cashAmountPaid: 0 as unknown as Subscription['cashAmountPaid'],
        startedAt: null,
        expiresAt: null,
        createdAt: new Date()
      };
      subscriptions.push(sub);
      return sub;
    },
    async markPaid(id, walletAmountUsed, cashAmountPaid, expiresAt) {
      const index = subscriptions.findIndex((s) => s.id === id);
      if (index === -1) throw new Error(`Subscription ${id} not found`);
      const updated: Subscription = {
        ...subscriptions[index],
        status: 'ACTIVE' as SubscriptionStatus,
        walletAmountUsed: walletAmountUsed as unknown as Subscription['walletAmountUsed'],
        cashAmountPaid: cashAmountPaid as unknown as Subscription['cashAmountPaid'],
        startedAt: new Date(),
        expiresAt
      };
      subscriptions[index] = updated;
      return updated;
    },
    async countByStatus(status) {
      return subscriptions.filter((s) => s.status === status).length;
    },
    async findLatestActive(userId) {
      return (
        subscriptions
          .filter((s) => s.userId === userId && s.status === 'ACTIVE')
          .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime())[0] ?? null
      );
    }
  };

  return {
    users: userRepo,
    wallets: walletRepo,
    walletTransactions: walletTransactionRepo,
    referralRelationships: referralRelationshipRepo,
    referralSettings: referralSettingsRepo,
    serviceWalletLimits: serviceWalletLimitRepo,
    campaigns: campaignRepo,
    campaignRewards: campaignRewardRepo,
    referralLogs: referralLogRepo,
    subscriptions: subscriptionRepo,
    __setUserStatusForTesting(userId: string, status: UserStatus) {
      const user = users.get(userId);
      if (!user) throw new Error(`User ${userId} not found`);
      users.set(userId, { ...user, status, updatedAt: new Date() });
    }
  };
}
