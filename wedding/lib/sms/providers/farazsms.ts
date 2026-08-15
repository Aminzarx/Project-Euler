import type { SmsMessage, SmsProvider } from '@/lib/sms/types';

/** FarazSMS / ippanel — https://ippanel.com/ REST API (bearer token auth). */
export function createFarazSmsProvider(): SmsProvider {
  const apiKey = process.env.FARAZSMS_API_KEY;
  const sender = process.env.FARAZSMS_SENDER;

  return {
    name: 'farazsms',
    async send({ to, text }: SmsMessage) {
      if (!apiKey || !sender) throw new Error('FarazSMS credentials are not fully configured.');

      const res = await fetch('https://edge.ippanel.com/v1/api/send', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `AccessKey ${apiKey}`,
        },
        body: JSON.stringify({
          sending_type: 'webservice',
          from_number: sender,
          message: text,
          params: { recipients: [to] },
        }),
      });
      if (!res.ok) {
        const body = await res.text().catch(() => '');
        throw new Error(`FarazSMS send failed (${res.status}): ${body}`);
      }
    },
  };
}
