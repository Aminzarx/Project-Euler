import { customAlphabet } from 'nanoid';
import type { User } from '@prisma/client';
import type { Repositories } from '../repositories/types';
import { WalletEngine } from './WalletEngine';
import { CampaignEngine } from './CampaignEngine';
import { normalizePhoneNumber } from '../utils/normalizePhoneNumber';

// Full A-Z0-9 alphabet, 8 characters — per the latest product spec. Deliberately includes the
// visually-ambiguous 0/O/1/I characters an earlier design excluded; that older tradeoff is
// superseded by the new spec.
const REFERRAL_CODE_ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
const REFERRAL_CODE_LENGTH = 8;
const generateCode = customAlphabet(REFERRAL_CODE_ALPHABET, REFERRAL_CODE_LENGTH);

const REWARD_TYPES = new Set(['REGISTRATION_REWARD', 'SUBSCRIPTION_REWARD', 'CAMPAIGN_REWARD']);

export interface RegisterUserInput {
  phoneNumber: string;
  referralCode: string;
  deviceFingerprint?: string;
  installationId?: string;
  ipAddress?: string;
  deviceModel?: string;
  operatingSystem?: string;
}

export interface ReferralStatistics {
  directReferrals: number;
  totalNetwork: number;
  lifetimeRewards: number;
  registrationRewards: number;
  subscriptionRewards: number;
}

/**
 * The referral tree and reward-eligibility engine. Deliberately never touches wallet balances
 * directly — every reward it decides on is handed to WalletEngine.credit so there is exactly one
 * place in the codebase that mutates a balance. Campaign overrides are resolved through
 * CampaignEngine, never read from ReferralSettings directly, so "does a campaign change this
 * reward right now" only has one answer in the whole system.
 */
export class ReferralEngine {
  constructor(
    private readonly repos: Repositories,
    private readonly wallet: WalletEngine,
    private readonly campaigns: CampaignEngine
  ) {}

  async generateUniqueReferralCode(): Promise<string> {
    for (let attempt = 0; attempt < 10; attempt++) {
      const code = generateCode();
      const existing = await this.repos.users.findByReferralCode(code);
      if (!existing) return code;
    }
    throw new Error('Could not generate a unique referral code after 10 attempts');
  }

  async validateReferralCode(code: string): Promise<User | null> {
    return this.repos.users.findByReferralCode(code.toUpperCase());
  }

  /**
   * Registers a new user. A referral code is mandatory — the application must not allow a new
   * user to register without one, that is the whole point of the feature (see BusinessRules.md).
   * The code is validated *before* the user record is created, so an invalid or ineligible code
   * never leaves behind an "active" account with nothing to show for it. The code, once applied,
   * is the user's one and only chance (see BusinessRules.md: "can never be changed"). The
   * *reward* for that referral, however, is only granted once the phone number is verified
   * (verifyPhone), matching the PRD precisely: "registers successfully using a referral code AND
   * verifies their phone number."
   *
   * The lone exception is the bootstrap/root account with nobody to be referred by — see
   * registerRootUser, which this method deliberately does not fall back to.
   */
  async registerUser(input: RegisterUserInput): Promise<User> {
    if (!input.referralCode || !input.referralCode.trim()) {
      throw new Error('Referral code is required');
    }

    const phoneNumber = normalizePhoneNumber(input.phoneNumber);
    const existingPhone = await this.repos.users.findByPhoneNumber(phoneNumber);
    if (existingPhone) throw new Error('Phone number already registered');

    // Validate (existence + eligibility) before creating anything.
    const referrer = await this.validateReferralCode(input.referralCode);
    if (!referrer) throw new Error('Invalid referral code');
    if (referrer.status !== 'ACTIVE') throw new Error('Referral code is not usable');

    const referralCode = await this.generateUniqueReferralCode();
    const user = await this.repos.users.create({
      phoneNumber,
      referralCode,
      deviceFingerprint: input.deviceFingerprint,
      installationId: input.installationId,
      ipAddress: input.ipAddress,
      deviceModel: input.deviceModel,
      operatingSystem: input.operatingSystem
    });

    // Analytics only, per the PRD's fraud philosophy — never used to block registration.
    await this.repos.referralLogs.create(user.id, 'REGISTRATION', {
      deviceFingerprint: input.deviceFingerprint,
      installationId: input.installationId,
      ipAddress: input.ipAddress,
      deviceModel: input.deviceModel,
      operatingSystem: input.operatingSystem
    });

    return this.applyReferral(user.id, input.referralCode);
  }

  /**
   * Creates the referrer-less bootstrap/root account — the one legitimate exception to
   * registerUser's mandatory-referral-code rule, since the very first user in the tree has
   * nobody to be referred by. Internal/seed use only (e.g. a future admin script or one-time
   * bootstrap) — never called from any public HTTP route.
   */
  async registerRootUser(input: Omit<RegisterUserInput, 'referralCode'>): Promise<User> {
    const phoneNumber = normalizePhoneNumber(input.phoneNumber);
    const existingPhone = await this.repos.users.findByPhoneNumber(phoneNumber);
    if (existingPhone) throw new Error('Phone number already registered');

    const referralCode = await this.generateUniqueReferralCode();
    const user = await this.repos.users.create({
      phoneNumber,
      referralCode,
      deviceFingerprint: input.deviceFingerprint,
      installationId: input.installationId,
      ipAddress: input.ipAddress,
      deviceModel: input.deviceModel,
      operatingSystem: input.operatingSystem
    });

    // Analytics only, per the PRD's fraud philosophy — never used to block registration.
    await this.repos.referralLogs.create(user.id, 'REGISTRATION', {
      deviceFingerprint: input.deviceFingerprint,
      installationId: input.installationId,
      ipAddress: input.ipAddress,
      deviceModel: input.deviceModel,
      operatingSystem: input.operatingSystem
    });

    return user;
  }

  /** A user may enter a referral code exactly once, ever. Users with no referrer are root nodes. */
  async applyReferral(userId: string, code: string): Promise<User> {
    const alreadyApplied = await this.repos.referralRelationships.findByChildId(userId);
    if (alreadyApplied) throw new Error('A referral code has already been applied to this user');

    const referrer = await this.validateReferralCode(code);
    if (!referrer) throw new Error('Invalid referral code');
    if (referrer.id === userId) throw new Error('A user cannot refer themselves');
    if (referrer.status !== 'ACTIVE') throw new Error('Referral code is not usable');

    await this.repos.referralRelationships.create(userId, referrer.id, referrer.referralCode);
    const updated = await this.repos.users.setReferrer(userId, referrer.id);
    await this.repos.referralLogs.create(userId, 'REFERRAL_APPLIED', { referrerId: referrer.id });
    return updated;
  }

  /**
   * Marks the phone verified and — the first time only, and only if a referral was applied —
   * grants the direct referrer their Registration Reward (3% of Base Amount by default, level 1
   * only, no multi-level payout, no subscription purchase required).
   */
  async verifyPhone(userId: string): Promise<void> {
    const user = await this.repos.users.findById(userId);
    if (!user) throw new Error(`User ${userId} not found`);
    if (user.phoneVerifiedAt) return;

    await this.repos.users.markPhoneVerified(userId);
    if (!user.referrerId) return;

    const settings = await this.campaigns.getEffectiveSettings();
    const rewardAmount = Math.round((settings.baseAmount * settings.registrationRewardPercent) / 100);
    if (rewardAmount <= 0) return;

    const isCampaignOverride = settings.activeCampaign?.registrationRewardPercent != null;
    const transaction = await this.wallet.credit(
      user.referrerId,
      rewardAmount,
      isCampaignOverride ? 'CAMPAIGN_REWARD' : 'REGISTRATION_REWARD',
      `پاداش ثبت‌نام کاربر معرفی‌شده (${user.phoneNumber})`,
      { sourceUserId: userId, level: 1, referenceType: 'registration', referenceId: userId }
    );

    if (isCampaignOverride && settings.activeCampaign) {
      await this.repos.campaignRewards.create(settings.activeCampaign.id, transaction.id);
    }

    await this.repos.referralLogs.create(userId, 'REGISTRATION_REWARD_GRANTED', {
      referrerId: user.referrerId,
      amount: rewardAmount
    });
  }

  /**
   * Walks up to three ancestor levels from the paying user and grants each one their
   * subscription-referral share, stopping the moment the tree runs out of ancestors. Never more
   * than three levels regardless of how deep the actual tree is — the Performance section's
   * "maximum three database lookups" requirement (see UserRepository.getAncestors).
   */
  async grantSubscriptionRewards(payingUserId: string, subscriptionId: string): Promise<void> {
    const settings = await this.campaigns.getEffectiveSettings();
    const maxLevels = Math.min(settings.maxReferralDepth, 3);
    const ancestors = await this.repos.users.getAncestors(payingUserId, maxLevels);

    const levelPercents = [settings.level1Percent, settings.level2Percent, settings.level3Percent];
    const levelOverridden = [
      settings.activeCampaign?.level1Percent != null,
      settings.activeCampaign?.level2Percent != null,
      settings.activeCampaign?.level3Percent != null
    ];

    for (let i = 0; i < ancestors.length; i++) {
      const ancestor = ancestors[i];
      const level = i + 1;
      const rewardAmount = Math.round((settings.baseAmount * levelPercents[i]) / 100);
      if (rewardAmount <= 0) continue;

      const isOverride = levelOverridden[i];
      const transaction = await this.wallet.credit(
        ancestor.id,
        rewardAmount,
        isOverride ? 'CAMPAIGN_REWARD' : 'SUBSCRIPTION_REWARD',
        `پاداش اشتراک، سطح ${level} شبکه معرفی`,
        { sourceUserId: payingUserId, level, referenceType: 'subscription', referenceId: subscriptionId }
      );

      if (isOverride && settings.activeCampaign) {
        await this.repos.campaignRewards.create(settings.activeCampaign.id, transaction.id);
      }
    }

    await this.repos.referralLogs.create(payingUserId, 'SUBSCRIPTION_REWARDS_GRANTED', {
      subscriptionId,
      levelsRewarded: ancestors.length
    });
  }

  async getReferralTree(userId: string): Promise<{ directReferrals: number; totalNetwork: number }> {
    const [directReferrals, totalNetwork] = await Promise.all([
      this.repos.users.countDirectReferrals(userId),
      this.repos.users.countTotalNetwork(userId)
    ]);
    return { directReferrals, totalNetwork };
  }

  async getStatistics(userId: string): Promise<ReferralStatistics> {
    const [directReferrals, totalNetwork] = await Promise.all([
      this.repos.users.countDirectReferrals(userId),
      this.repos.users.countTotalNetwork(userId)
    ]);

    // A single user's own reward history is always a modest list in practice, so summarizing it
    // by paging through listByUserId (rather than a dedicated per-user SQL aggregate) is a
    // reasonable simplification here — see Architecture.md for where this would need to change
    // to stay O(1) for a "power user" with an unusually large transaction history.
    const history = await this.repos.walletTransactions.listByUserId(userId, 100_000, 0);
    const registrationRewards = this.sumByType(history, 'REGISTRATION_REWARD');
    const subscriptionRewards = this.sumByType(history, 'SUBSCRIPTION_REWARD');
    const lifetimeRewards = history
      .filter((t) => REWARD_TYPES.has(t.type))
      .reduce((sum, t) => sum + Number(t.amount), 0);

    return { directReferrals, totalNetwork, registrationRewards, subscriptionRewards, lifetimeRewards };
  }

  private sumByType(history: Array<{ type: string; amount: unknown }>, type: string): number {
    return history.filter((t) => t.type === type).reduce((sum, t) => sum + Number(t.amount), 0);
  }
}
