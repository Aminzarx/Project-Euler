/**
 * One-off / rerunnable safety net: assigns a referral code to any user row that doesn't already
 * have one, and never touches a row that does. `User.referralCode` is a required, unique column
 * in the current schema, so under normal operation every row already has a code by the time it's
 * created (see ReferralEngine.registerUser/registerRootUser) — this script exists for the
 * migration scenario the referral-system spec calls out explicitly: importing/backfilling a
 * pre-existing user table (e.g. from a system that predates this referral feature, or a bulk
 * import that bypassed the engine) where some rows may not have a code yet.
 *
 * Usage: npm run backfill:referral-codes  (requires DATABASE_URL to be set; safe to run multiple
 * times — already-coded rows are always skipped).
 */
import { PrismaClient } from '@prisma/client';
import { customAlphabet } from 'nanoid';

const REFERRAL_CODE_ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
const REFERRAL_CODE_LENGTH = 8;
const generateCode = customAlphabet(REFERRAL_CODE_ALPHABET, REFERRAL_CODE_LENGTH);

const prisma = new PrismaClient();

async function generateUniqueCode(): Promise<string> {
  for (let attempt = 0; attempt < 10; attempt += 1) {
    const code = generateCode();
    const existing = await prisma.user.findUnique({ where: { referralCode: code } });
    if (!existing) return code;
  }
  throw new Error('Could not generate a unique referral code after 10 attempts');
}

async function main(): Promise<void> {
  const users = await prisma.user.findMany();
  const missing = users.filter((user) => !user.referralCode || user.referralCode.trim().length === 0);

  console.log(`Found ${users.length} user(s); ${missing.length} missing a referral code.`);

  for (const user of missing) {
    const code = await generateUniqueCode();
    await prisma.user.update({ where: { id: user.id }, data: { referralCode: code } });
    console.log(`  ${user.id} (${user.phoneNumber}) -> ${code}`);
  }

  console.log('Backfill complete. Users that already had a referral code were left untouched.');
}

main()
  .catch((error) => {
    console.error(error);
    process.exitCode = 1;
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
