import type { Campaign } from '@prisma/client';
import type { CreateCampaignInput, Repositories } from '../repositories/types';

/** The reward percentages that actually apply right now — either the admin-configured defaults,
 *  or an active campaign's override for whichever fields it defines. */
export interface EffectiveRewardSettings {
  subscriptionPrice: number;
  baseAmount: number;
  registrationRewardPercent: number;
  level1Percent: number;
  level2Percent: number;
  level3Percent: number;
  maxReferralDepth: number;
  activeCampaign: Campaign | null;
}

/**
 * Resolves the reward settings actually in effect at a given moment, letting a time-boxed
 * Campaign override any subset of ReferralSettings' percentages without touching application
 * code — exactly the "Future Campaign Engine" requirement from the PRD. Kept as its own module,
 * separate from WalletEngine and ReferralEngine, per the PRD's "Development Principles".
 */
export class CampaignEngine {
  constructor(private readonly repos: Repositories) {}

  async getActiveCampaign(at: Date = new Date()): Promise<Campaign | null> {
    return this.repos.campaigns.findActiveAt(at);
  }

  async getEffectiveSettings(at: Date = new Date()): Promise<EffectiveRewardSettings> {
    const base = await this.repos.referralSettings.get();
    const activeCampaign = await this.getActiveCampaign(at);

    return {
      subscriptionPrice: Number(base.subscriptionPrice),
      baseAmount: Number(base.baseAmount),
      registrationRewardPercent: this.overrideOrDefault(
        activeCampaign?.registrationRewardPercent,
        Number(base.registrationRewardPercent)
      ),
      level1Percent: this.overrideOrDefault(activeCampaign?.level1Percent, Number(base.level1Percent)),
      level2Percent: this.overrideOrDefault(activeCampaign?.level2Percent, Number(base.level2Percent)),
      level3Percent: this.overrideOrDefault(activeCampaign?.level3Percent, Number(base.level3Percent)),
      maxReferralDepth: base.maxReferralDepth,
      activeCampaign
    };
  }

  async listCampaigns(): Promise<Campaign[]> {
    return this.repos.campaigns.list();
  }

  async createCampaign(input: CreateCampaignInput): Promise<Campaign> {
    if (input.endsAt <= input.startsAt) {
      throw new Error('Campaign endsAt must be after startsAt');
    }
    return this.repos.campaigns.create(input);
  }

  async updateCampaign(id: string, patch: Partial<CreateCampaignInput>): Promise<Campaign> {
    return this.repos.campaigns.update(id, patch);
  }

  private overrideOrDefault(override: unknown, fallback: number): number {
    return override === null || override === undefined ? fallback : Number(override);
  }
}
