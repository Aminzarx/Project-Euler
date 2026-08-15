import type { SmsMessage, SmsProvider } from '@/lib/sms/types';

/** https://kavenegar.com/rest.html — simple GET-based send endpoint. */
export function createKavenegarProvider(): SmsProvider {
  const apiKey = process.env.KAVENEGAR_API_KEY;
  const sender = process.env.KAVENEGAR_SENDER;

  return {
    name: 'kavenegar',
    async send({ to, text }: SmsMessage) {
      if (!apiKey) throw new Error('KAVENEGAR_API_KEY is not configured.');

      const url = new URL(`https://api.kavenegar.com/v1/${apiKey}/sms/send.json`);
      url.searchParams.set('receptor', to);
      url.searchParams.set('message', text);
      if (sender) url.searchParams.set('sender', sender);

      const res = await fetch(url.toString(), { method: 'GET' });
      if (!res.ok) {
        const body = await res.text().catch(() => '');
        throw new Error(`Kavenegar send failed (${res.status}): ${body}`);
      }
    },
  };
}
