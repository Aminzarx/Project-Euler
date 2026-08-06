import type {
  User,
  Wallet,
  WalletTransaction,
  TransactionType,
  ReferralRelationship,
  ReferralSettings,
  ServiceWalletLimit,
  Campaign,
  CampaignReward,
  ReferralLog,
  Subscription,
  SubscriptionStatus
} from '@prisma/client';

/**
 * Repository interfaces the engines (WalletEngine/ReferralEngine/CampaignEngine) depend on.
 * This is the seam that keeps business logic testable without a database: unit tests inject an
 * in-memory implementation (src/repositories/memory), while the running server injects the
 * Prisma-backed one (src/repositories/prisma). Every monetary amount and percentage crossing this
 * boundary is a plain `number` — all figures in this domain (Toman prices, integer-percent
 * reward rates) are whole numbers, so there is no floating-point rounding concern in practice,
 * and it keeps engine code free of any ORM-specific decimal type.
 */

export interface CreateUserInput {
  phoneNumber: string;
  referralCode: string;
  deviceFingerprint?: string;
  installationId?: string;
  ipAddress?: string;
  deviceModel?: string;
  operatingSystem?: string;
}

export interface UserRepository {
  findById(id: string): Promise<User | null>;
  findByPhoneNumber(phoneNumber: string): Promise<User | null>;
  findByReferralCode(referralCode: string): Promise<User | null>;
  create(input: CreateUserInput): Promise<User>;
  setReferrer(userId: string, referrerId: string): Promise<User>;
  markPhoneVerified(userId: string): Promise<User>;
  /** Up to `maxLevels` ancestors, nearest-first (index 0 = direct parent = level 1). Stops early
   *  if the tree runs out of ancestors before reaching maxLevels. */
  getAncestors(userId: string, maxLevels: number): Promise<User[]>;
  countDirectReferrals(userId: string): Promise<number>;
  countTotalNetwork(userId: string): Promise<number>;
  listTopReferrers(limit: number): Promise<Array<{ user: User; directReferrals: number }>>;
  count(): Promise<number>;
  /** Users that have a referrer at all — the admin dashboard's "Total Referrals". */
  countReferred(): Promise<number>;
  countCreatedSince(since: Date): Promise<number>;
}

export interface WalletRepository {
  findByUserId(userId: string): Promise<Wallet | null>;
  create(userId: string): Promise<Wallet>;
  /** Atomically adjusts balance by delta (positive credits, negative debits) and returns the
   *  updated wallet. Implementations must reject a debit that would take balance below zero. */
  adjustBalance(walletId: string, delta: number): Promise<Wallet>;
  /** Total credits currently outstanding across every wallet — the admin dashboard's "Wallet
   *  Statistics" (this is the business's total future-payment liability, not cash on hand). */
  sumAllBalances(): Promise<number>;
}

export interface CreateTransactionInput {
  walletId: string;
  userId: string;
  amount: number;
  type: TransactionType;
  sourceUserId?: string;
  level?: number;
  referenceType?: string;
  referenceId?: string;
  description: string;
}

export interface WalletTransactionRepository {
  create(input: CreateTransactionInput): Promise<WalletTransaction>;
  listByUserId(userId: string, limit?: number, offset?: number): Promise<WalletTransaction[]>;
  sumByType(type: TransactionType, since?: Date): Promise<number>;
  sumAllRewards(since?: Date): Promise<number>;
}

export interface ReferralRelationshipRepository {
  create(childId: string, parentId: string, referralCode: string): Promise<ReferralRelationship>;
  findByChildId(childId: string): Promise<ReferralRelationship | null>;
}

export type ReferralSettingsPatch = Partial<
  Omit<ReferralSettings, 'id' | 'updatedAt' | 'subscriptionPrice' | 'baseAmount' | 'registrationRewardPercent' | 'level1Percent' | 'level2Percent' | 'level3Percent'>
> & {
  subscriptionPrice?: number;
  baseAmount?: number;
  registrationRewardPercent?: number;
  level1Percent?: number;
  level2Percent?: number;
  level3Percent?: number;
};

export interface ReferralSettingsRepository {
  get(): Promise<ReferralSettings>;
  update(patch: ReferralSettingsPatch): Promise<ReferralSettings>;
}

export interface ServiceWalletLimitRepository {
  get(serviceKey: string): Promise<ServiceWalletLimit | null>;
  list(): Promise<ServiceWalletLimit[]>;
  upsert(serviceKey: string, label: string, maxWalletUsagePercent: number): Promise<ServiceWalletLimit>;
}

export interface CreateCampaignInput {
  name: string;
  startsAt: Date;
  endsAt: Date;
  active?: boolean;
  registrationRewardPercent?: number | null;
  level1Percent?: number | null;
  level2Percent?: number | null;
  level3Percent?: number | null;
}

export interface CampaignRepository {
  findActiveAt(date: Date): Promise<Campaign | null>;
  list(): Promise<Campaign[]>;
  findById(id: string): Promise<Campaign | null>;
  create(input: CreateCampaignInput): Promise<Campaign>;
  update(id: string, patch: Partial<CreateCampaignInput>): Promise<Campaign>;
}

export interface CampaignRewardRepository {
  create(campaignId: string, walletTransactionId: string): Promise<CampaignReward>;
  countByCampaign(campaignId: string): Promise<number>;
}

export interface ReferralLogRepository {
  create(userId: string, event: string, metadata?: Record<string, unknown>): Promise<ReferralLog>;
}

export interface SubscriptionRepository {
  create(userId: string, priceAtPurchase: number): Promise<Subscription>;
  markPaid(id: string, walletAmountUsed: number, cashAmountPaid: number, expiresAt: Date): Promise<Subscription>;
  countByStatus(status: SubscriptionStatus): Promise<number>;
  findLatestActive(userId: string): Promise<Subscription | null>;
}

export interface Repositories {
  users: UserRepository;
  wallets: WalletRepository;
  walletTransactions: WalletTransactionRepository;
  referralRelationships: ReferralRelationshipRepository;
  referralSettings: ReferralSettingsRepository;
  serviceWalletLimits: ServiceWalletLimitRepository;
  campaigns: CampaignRepository;
  campaignRewards: CampaignRewardRepository;
  referralLogs: ReferralLogRepository;
  subscriptions: SubscriptionRepository;
}
