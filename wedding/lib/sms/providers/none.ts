import type { SmsMessage, SmsProvider } from '@/lib/sms/types';

/** Safe default: logs instead of sending. Used when SMS_PROVIDER is unset. */
export function createNoopProvider(): SmsProvider {
  return {
    name: 'none',
    async send({ to, text }: SmsMessage) {
      // eslint-disable-next-line no-console
      console.info(`[sms:none] would send to ${to}: ${text}`);
    },
  };
}
