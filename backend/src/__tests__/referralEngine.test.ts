import { createMemoryRepositories } from '../repositories/memory';
import { WalletEngine } from '../engines/WalletEngine';
import { CampaignEngine } from '../engines/CampaignEngine';
import { ReferralEngine } from '../engines/ReferralEngine';

describe('ReferralEngine', () => {
  function setup() {
    const repos = createMemoryRepositories();
    const wallet = new WalletEngine(repos);
    const campaigns = new CampaignEngine(repos);
    const referral = new ReferralEngine(repos, wallet, campaigns);
    return { repos, wallet, campaigns, referral };
  }

  it('generates an 8-character referral code from the expected alphabet', async () => {
    const { referral } = setup();
    const code = await referral.generateUniqueReferralCode();
    expect(code).toHaveLength(8);
    expect(code).toMatch(/^[ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789]{8}$/);
  });

  it('registerRootUser creates a referrer-less, ACTIVE, 8-char-coded user', async () => {
    const { referral } = setup();
    const user = await referral.registerRootUser({ phoneNumber: '+98-A' });
    expect(user.referrerId).toBeNull();
    expect(user.status).toBe('ACTIVE');
    expect(user.referralCode).toHaveLength(8);
    expect(user.referralCode).toMatch(/^[A-Z0-9]{8}$/);
  });

  it('rejects registerUser when no referral code is supplied', async () => {
    const { referral } = setup();
    await expect(referral.registerUser({ phoneNumber: '+98-no-code' } as any)).rejects.toThrow();
  });

  it('rejects registerUser with a nonexistent referral code, and creates no user row for that phone number', async () => {
    const { repos, referral } = setup();
    await expect(
      referral.registerUser({ phoneNumber: '+98-ghost', referralCode: 'ZZZZZZZZ' })
    ).rejects.toThrow();
    expect(await repos.users.findByPhoneNumber('+98-ghost')).toBeNull();
  });

  it('rejects registerUser with a referral code belonging to a BLOCKED-status user', async () => {
    const { repos, referral } = setup();
    const parent = await referral.registerRootUser({ phoneNumber: '+98-blocked-parent' });
    repos.__setUserStatusForTesting(parent.id, 'BLOCKED');

    await expect(
      referral.registerUser({ phoneNumber: '+98-blocked-child', referralCode: parent.referralCode })
    ).rejects.toThrow();
    expect(await repos.users.findByPhoneNumber('+98-blocked-child')).toBeNull();
  });

  it('referral code lookups are case-insensitive', async () => {
    const { referral } = setup();
    const parent = await referral.registerRootUser({ phoneNumber: '+98-case' });
    const found = await referral.validateReferralCode(parent.referralCode.toLowerCase());
    expect(found?.id).toBe(parent.id);
  });

  it('rejects registering the same phone number twice', async () => {
    const { referral } = setup();
    const parent = await referral.registerRootUser({ phoneNumber: '+98-dup-parent' });
    await referral.registerUser({ phoneNumber: '+98-dup', referralCode: parent.referralCode });
    await expect(
      referral.registerUser({ phoneNumber: '+98-dup', referralCode: parent.referralCode })
    ).rejects.toThrow();
  });

  it('normalizes phone numbers so Persian-digit and ASCII-digit forms of the same number cannot both register', async () => {
    const { referral } = setup();
    const parent = await referral.registerRootUser({ phoneNumber: '+98-norm-parent' });
    await referral.registerUser({ phoneNumber: '۰۹۱۲۱۲۳۴۵۶۷', referralCode: parent.referralCode });
    await expect(
      referral.registerUser({ phoneNumber: '09121234567', referralCode: parent.referralCode })
    ).rejects.toThrow();
  });

  it('applies a referral code at registration and rejects reusing/forging one afterwards', async () => {
    const { referral } = setup();
    const parent = await referral.registerRootUser({ phoneNumber: '+98-parent' });
    const child = await referral.registerUser({ phoneNumber: '+98-child', referralCode: parent.referralCode });
    expect(child.referrerId).toBe(parent.id);

    await expect(referral.applyReferral(child.id, parent.referralCode)).rejects.toThrow();
  });

  it('rejects an invalid referral code and self-referral', async () => {
    const { referral } = setup();
    const root = await referral.registerRootUser({ phoneNumber: '+98-solo-root' });
    const user = await referral.registerUser({ phoneNumber: '+98-solo', referralCode: root.referralCode });
    await expect(referral.applyReferral(user.id, 'ZZZZZZZZ')).rejects.toThrow();
    await expect(referral.applyReferral(user.id, user.referralCode)).rejects.toThrow();
  });

  it('grants the registration reward to the direct referrer only, once phone is verified, and never twice', async () => {
    const { referral, wallet } = setup();
    const parent = await referral.registerRootUser({ phoneNumber: '+98-p2' });
    const child = await referral.registerUser({ phoneNumber: '+98-c2', referralCode: parent.referralCode });

    expect(await wallet.getBalance(parent.id)).toBe(0);
    await referral.verifyPhone(child.id);
    // default settings: baseAmount 100,000 * 3% = 3,000
    expect(await wallet.getBalance(parent.id)).toBe(3_000);

    await referral.verifyPhone(child.id); // idempotent — already verified
    expect(await wallet.getBalance(parent.id)).toBe(3_000);
  });

  it('distributes subscription rewards to exactly three ancestor levels, matching the PRD example', async () => {
    const { referral, wallet } = setup();
    const a = await referral.registerRootUser({ phoneNumber: '+98-A-tree' });
    const b = await referral.registerUser({ phoneNumber: '+98-B', referralCode: a.referralCode });
    const c = await referral.registerUser({ phoneNumber: '+98-C', referralCode: b.referralCode });
    const d = await referral.registerUser({ phoneNumber: '+98-D', referralCode: c.referralCode });

    await referral.grantSubscriptionRewards(d.id, 'sub-1');

    // default settings: baseAmount 100,000; level1=10% -> 10,000; level2=5% -> 5,000; level3=2% -> 2,000
    expect(await wallet.getBalance(c.id)).toBe(10_000); // level 1
    expect(await wallet.getBalance(b.id)).toBe(5_000); // level 2
    expect(await wallet.getBalance(a.id)).toBe(2_000); // level 3
  });

  it('never rewards a fourth ancestor level, even when the tree is deeper', async () => {
    const { referral, wallet } = setup();
    const a = await referral.registerRootUser({ phoneNumber: '+98-A2' });
    const b = await referral.registerUser({ phoneNumber: '+98-B2', referralCode: a.referralCode });
    const c = await referral.registerUser({ phoneNumber: '+98-C2', referralCode: b.referralCode });
    const d = await referral.registerUser({ phoneNumber: '+98-D2', referralCode: c.referralCode });
    const e = await referral.registerUser({ phoneNumber: '+98-E2', referralCode: d.referralCode });

    await referral.grantSubscriptionRewards(e.id, 'sub-2');

    expect(await wallet.getBalance(d.id)).toBe(10_000); // level 1
    expect(await wallet.getBalance(c.id)).toBe(5_000); // level 2
    expect(await wallet.getBalance(b.id)).toBe(2_000); // level 3
    expect(await wallet.getBalance(a.id)).toBe(0); // level 4 — never rewarded
  });

  it('a root node (no referrer) generates no subscription rewards for anyone', async () => {
    const { referral, wallet } = setup();
    const solo = await referral.registerRootUser({ phoneNumber: '+98-solo2' });
    await referral.grantSubscriptionRewards(solo.id, 'sub-3');
    expect(await wallet.getBalance(solo.id)).toBe(0);
  });

  it('tags rewards granted under an active campaign override as CAMPAIGN_REWARD', async () => {
    const { referral, wallet, campaigns } = setup();
    const a = await referral.registerRootUser({ phoneNumber: '+98-A3' });
    const b = await referral.registerUser({ phoneNumber: '+98-B3', referralCode: a.referralCode });

    await campaigns.createCampaign({
      name: 'Double Weekend',
      startsAt: new Date(Date.now() - 1000),
      endsAt: new Date(Date.now() + 1000 * 60 * 60),
      level1Percent: 20
    });

    await referral.grantSubscriptionRewards(b.id, 'sub-4');
    const history = await wallet.getHistory(a.id);
    expect(history[0].type).toBe('CAMPAIGN_REWARD');
    expect(history[0].amount).toBe(20_000); // 20% of 100,000 base amount
  });

  it('reports referral tree size and statistics', async () => {
    const { referral } = setup();
    const a = await referral.registerRootUser({ phoneNumber: '+98-A4' });
    const b = await referral.registerUser({ phoneNumber: '+98-B4', referralCode: a.referralCode });
    await referral.registerUser({ phoneNumber: '+98-C4', referralCode: b.referralCode });
    await referral.registerUser({ phoneNumber: '+98-D4', referralCode: a.referralCode });

    const tree = await referral.getReferralTree(a.id);
    expect(tree.directReferrals).toBe(2); // B and D
    expect(tree.totalNetwork).toBe(3); // B, D, and C (B's child)
  });
});
