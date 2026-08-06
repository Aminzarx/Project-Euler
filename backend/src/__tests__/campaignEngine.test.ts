import { createMemoryRepositories } from '../repositories/memory';
import { CampaignEngine } from '../engines/CampaignEngine';

describe('CampaignEngine', () => {
  function setup() {
    const repos = createMemoryRepositories();
    const campaigns = new CampaignEngine(repos);
    return { repos, campaigns };
  }

  it('falls back to base ReferralSettings when no campaign is active', async () => {
    const { campaigns } = setup();
    const settings = await campaigns.getEffectiveSettings(new Date('2026-01-15'));
    expect(settings.level1Percent).toBe(10);
    expect(settings.level2Percent).toBe(5);
    expect(settings.level3Percent).toBe(2);
    expect(settings.registrationRewardPercent).toBe(3);
    expect(settings.activeCampaign).toBeNull();
  });

  it('an active campaign overrides only the fields it sets, leaving the rest at default', async () => {
    const { campaigns } = setup();
    await campaigns.createCampaign({
      name: 'Weekend Boost',
      startsAt: new Date('2026-01-01'),
      endsAt: new Date('2026-01-31'),
      level1Percent: 15
    });

    const settings = await campaigns.getEffectiveSettings(new Date('2026-01-15'));
    expect(settings.level1Percent).toBe(15); // overridden
    expect(settings.level2Percent).toBe(5); // untouched, falls back to default
    expect(settings.level3Percent).toBe(2); // untouched, falls back to default
    expect(settings.activeCampaign?.name).toBe('Weekend Boost');
  });

  it('a campaign outside its date range is not active', async () => {
    const { campaigns } = setup();
    await campaigns.createCampaign({
      name: 'New Year Campaign',
      startsAt: new Date('2026-01-01'),
      endsAt: new Date('2026-01-10'),
      registrationRewardPercent: 10
    });

    const settings = await campaigns.getEffectiveSettings(new Date('2026-02-01'));
    expect(settings.activeCampaign).toBeNull();
    expect(settings.registrationRewardPercent).toBe(3); // default, campaign already ended
  });

  it('rejects a campaign whose end date is not after its start date', async () => {
    const { campaigns } = setup();
    await expect(
      campaigns.createCampaign({
        name: 'Broken Campaign',
        startsAt: new Date('2026-01-10'),
        endsAt: new Date('2026-01-01')
      })
    ).rejects.toThrow();
  });
});
