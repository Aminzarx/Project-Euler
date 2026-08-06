import { createMemoryRepositories } from '../repositories/memory';
import { WalletEngine } from '../engines/WalletEngine';

describe('WalletEngine', () => {
  function setup() {
    const repos = createMemoryRepositories();
    const wallet = new WalletEngine(repos);
    return { repos, wallet };
  }

  it('starts a new user at a zero balance', async () => {
    const { wallet } = setup();
    expect(await wallet.getBalance('user-1')).toBe(0);
  });

  it('credit increases balance and records a transaction', async () => {
    const { wallet } = setup();
    const tx = await wallet.credit('user-1', 10_000, 'MANUAL_ADJUSTMENT', 'test credit');
    expect(await wallet.getBalance('user-1')).toBe(10_000);
    expect(tx.amount).toBe(10_000);
    expect(tx.type).toBe('MANUAL_ADJUSTMENT');
  });

  it('rejects a non-positive credit amount', async () => {
    const { wallet } = setup();
    await expect(wallet.credit('user-1', 0, 'MANUAL_ADJUSTMENT', 'x')).rejects.toThrow();
    await expect(wallet.credit('user-1', -5, 'MANUAL_ADJUSTMENT', 'x')).rejects.toThrow();
  });

  it('a service with no configured wallet limit covers 0% by default', async () => {
    const { wallet } = setup();
    await wallet.credit('user-1', 100_000, 'MANUAL_ADJUSTMENT', 'seed');
    const result = await wallet.purchase('user-1', 'unlisted_service', 50_000);
    expect(result.walletAmountUsed).toBe(0);
    expect(result.cashAmountDue).toBe(50_000);
    expect(await wallet.getBalance('user-1')).toBe(100_000);
  });

  it('purchase caps wallet usage at the service percentage, not the full price', async () => {
    const { repos, wallet } = setup();
    await repos.serviceWalletLimits.upsert('subscription', 'Subscription', 100);
    await repos.serviceWalletLimits.upsert('featured_listing', 'Featured Listing', 50);
    await wallet.credit('user-1', 1_000_000, 'MANUAL_ADJUSTMENT', 'seed');

    const featuredResult = await wallet.purchase('user-1', 'featured_listing', 200_000);
    expect(featuredResult.walletAmountUsed).toBe(100_000); // 50% of 200,000
    expect(featuredResult.cashAmountDue).toBe(100_000);
    expect(await wallet.getBalance('user-1')).toBe(900_000);
  });

  it('purchase never uses more than the current balance, even under a generous limit', async () => {
    const { repos, wallet } = setup();
    await repos.serviceWalletLimits.upsert('subscription', 'Subscription', 100);
    await wallet.credit('user-1', 50_000, 'MANUAL_ADJUSTMENT', 'seed');

    const result = await wallet.purchase('user-1', 'subscription', 200_000);
    expect(result.walletAmountUsed).toBe(50_000);
    expect(result.cashAmountDue).toBe(150_000);
    expect(await wallet.getBalance('user-1')).toBe(0);
  });

  it('refund credits the wallet with a REFUND transaction', async () => {
    const { wallet } = setup();
    await wallet.refund('user-1', 20_000, 'refund test');
    expect(await wallet.getBalance('user-1')).toBe(20_000);
    const history = await wallet.getHistory('user-1');
    expect(history[0].type).toBe('REFUND');
  });

  it('getHistory returns transactions newest-first', async () => {
    const { wallet } = setup();
    await wallet.credit('user-1', 1_000, 'MANUAL_ADJUSTMENT', 'first');
    await wallet.credit('user-1', 2_000, 'MANUAL_ADJUSTMENT', 'second');
    const history = await wallet.getHistory('user-1');
    expect(history).toHaveLength(2);
    expect(history[0].description).toBe('second');
  });
});
