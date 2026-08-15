import { createFarazSmsProvider } from '@/lib/sms/providers/farazsms';
import { createKavenegarProvider } from '@/lib/sms/providers/kavenegar';
import { createMelipayamakProvider } from '@/lib/sms/providers/melipayamak';
import { createNoopProvider } from '@/lib/sms/providers/none';
import type { RsvpSide } from '@/lib/store/types';

/**
 * Provider registry keyed by SMS_PROVIDER. Swapping providers is a one-line
 * env change — nothing else in the RSVP flow needs to know which one is
 * active. See /docs/future-extension.md for adding a new provider.
 */
function resolveProvider() {
  switch (process.env.SMS_PROVIDER) {
    case 'kavenegar':
      return createKavenegarProvider();
    case 'melipayamak':
      return createMelipayamakProvider();
    case 'farazsms':
      return createFarazSmsProvider();
    default:
      return createNoopProvider();
  }
}

function recipientForSide(side: RsvpSide): string {
  const number = side === 'bride' ? process.env.SMS_RECIPIENT_BRIDE : process.env.SMS_RECIPIENT_GROOM;
  if (!number) {
    throw new Error(`No SMS recipient configured for side "${side}".`);
  }
  return number;
}

export async function notifyRsvpBySms(params: { side: RsvpSide; text: string }) {
  const provider = resolveProvider();
  const to = recipientForSide(params.side);
  await provider.send({ to, text: params.text });
}
