import type { Wallet, WalletTransaction, TransactionType } from '@prisma/client';
import type { Repositories } from '../repositories/types';

export interface CreditOptions {
  sourceUserId?: string;
  level?: number;
  referenceType?: string;
  referenceId?: string;
}

export interface PurchaseResult {
  walletAmountUsed: number;
  cashAmountDue: number;
  transaction: WalletTransaction | null;
}

/**
 * The credit wallet engine. This is deliberately the only place balances are ever mutated, so
 * every movement is guaranteed to produce a matching WalletTransaction row (the PRD's "every
 * credit movement must generate a transaction" rule). Kept fully separate from ReferralEngine —
 * the referral engine decides *that* a reward is owed and for how much; this engine is the only
 * thing that actually moves credits.
 *
 * These are internal, non-cash credits: nothing in this module ever pays out, transfers between
 * users, or exposes a withdrawal path — that's enforced structurally by only ever offering
 * `credit` (always this user's own wallet) and `purchase` (always a debit against a listed
 * service), never a generic "transfer" operation.
 */
export class WalletEngine {
  constructor(private readonly repos: Repositories) {}

  async getOrCreateWallet(userId: string): Promise<Wallet> {
    const existing = await this.repos.wallets.findByUserId(userId);
    if (existing) return existing;
    return this.repos.wallets.create(userId);
  }

  async getBalance(userId: string): Promise<number> {
    const wallet = await this.getOrCreateWallet(userId);
    return Number(wallet.balance);
  }

  /** Adds credits to a user's wallet and records why. `amount` must be positive — use the
   *  dedicated `type` (REFUND, MANUAL_ADJUSTMENT, ...) to describe the reason, rather than a
   *  negative credit. */
  async credit(
    userId: string,
    amount: number,
    type: TransactionType,
    description: string,
    options: CreditOptions = {}
  ): Promise<WalletTransaction> {
    if (amount <= 0) throw new Error('Credit amount must be positive');
    const wallet = await this.getOrCreateWallet(userId);
    await this.repos.wallets.adjustBalance(wallet.id, amount);
    return this.repos.walletTransactions.create({
      walletId: wallet.id,
      userId,
      amount,
      type,
      description,
      ...options
    });
  }

  /** Same math as `purchase`, without ever touching the balance — used to show the user a
   *  "here's what this would cost you" preview (e.g. the Dashboard's "Wallet Usage Preview"). */
  async previewPurchase(userId: string, serviceKey: string, servicePrice: number): Promise<PurchaseResult> {
    const limit = await this.repos.serviceWalletLimits.get(serviceKey);
    const maxUsagePercent = limit ? Number(limit.maxWalletUsagePercent) : 0;
    const maxWalletAmount = Math.floor((servicePrice * maxUsagePercent) / 100);
    const wallet = await this.getOrCreateWallet(userId);
    const walletAmountUsed = Math.min(Number(wallet.balance), maxWalletAmount);
    return { walletAmountUsed, cashAmountDue: servicePrice - walletAmountUsed, transaction: null };
  }

  /**
   * Applies a wallet-funded purchase for `serviceKey`, capped at that service's configured
   * maxWalletUsagePercent (services with no configured limit default to 0% — a service must be
   * explicitly opted in before the wallet can pay for it). Returns how much the wallet covered
   * and how much cash is still due; the caller (an API route, in practice) is responsible for
   * actually collecting `cashAmountDue` through whatever payment method backs cash payments.
   */
  async purchase(userId: string, serviceKey: string, servicePrice: number): Promise<PurchaseResult> {
    if (servicePrice < 0) throw new Error('servicePrice cannot be negative');

    const limit = await this.repos.serviceWalletLimits.get(serviceKey);
    const maxUsagePercent = limit ? Number(limit.maxWalletUsagePercent) : 0;
    const maxWalletAmount = Math.floor((servicePrice * maxUsagePercent) / 100);

    const wallet = await this.getOrCreateWallet(userId);
    const walletAmountUsed = Math.min(Number(wallet.balance), maxWalletAmount);
    const cashAmountDue = servicePrice - walletAmountUsed;

    if (walletAmountUsed <= 0) {
      return { walletAmountUsed: 0, cashAmountDue: servicePrice, transaction: null };
    }

    await this.repos.wallets.adjustBalance(wallet.id, -walletAmountUsed);
    const transaction = await this.repos.walletTransactions.create({
      walletId: wallet.id,
      userId,
      amount: -walletAmountUsed,
      type: 'PURCHASE',
      referenceType: serviceKey,
      description: `${walletAmountUsed} تومان از کیف‌پول برای «${serviceKey}» استفاده شد`
    });

    return { walletAmountUsed, cashAmountDue, transaction };
  }

  async refund(
    userId: string,
    amount: number,
    description: string,
    options: CreditOptions = {}
  ): Promise<WalletTransaction> {
    return this.credit(userId, amount, 'REFUND', description, options);
  }

  async getHistory(userId: string, limit = 50, offset = 0): Promise<WalletTransaction[]> {
    return this.repos.walletTransactions.listByUserId(userId, limit, offset);
  }
}
