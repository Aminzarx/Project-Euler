import type { SmsMessage, SmsProvider } from '@/lib/sms/types';

/** https://www.melipayamak.com/api/ — REST SendSMS endpoint (username/password auth). */
export function createMelipayamakProvider(): SmsProvider {
  const username = process.env.MELIPAYAMAK_USERNAME;
  const password = process.env.MELIPAYAMAK_PASSWORD;
  const sender = process.env.MELIPAYAMAK_SENDER;

  return {
    name: 'melipayamak',
    async send({ to, text }: SmsMessage) {
      if (!username || !password || !sender) {
        throw new Error('Melipayamak credentials are not fully configured.');
      }

      const res = await fetch('https://rest.payamak-panel.com/api/SendSMS/SendSMS', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username,
          password,
          to,
          from: sender,
          text,
          isFlash: false,
        }),
      });
      if (!res.ok) {
        const body = await res.text().catch(() => '');
        throw new Error(`Melipayamak send failed (${res.status}): ${body}`);
      }
    },
  };
}
