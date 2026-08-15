import { NextResponse } from 'next/server';
import { notifyRsvpBySms } from '@/lib/sms';
import { rsvpStore } from '@/lib/store/rsvpStore';
import type { RsvpSide, RsvpStatus } from '@/lib/store/types';
import { isNonEmpty, sanitizeText } from '@/lib/utils/validate';

const VALID_STATUSES: RsvpStatus[] = ['attending', 'maybe', 'declined'];
const VALID_SIDES: RsvpSide[] = ['bride', 'groom'];

const STATUS_LABEL_FA: Record<RsvpStatus, string> = {
  attending: 'حضور می‌یابد',
  maybe: 'شاید حضور یابد',
  declined: 'متأسفانه نمی‌تواند حضور یابد',
};

export async function POST(request: Request) {
  const body = await request.json().catch(() => null);
  if (!body) {
    return NextResponse.json({ error: 'بدنه‌ی درخواست نامعتبر است.' }, { status: 400 });
  }

  const status = body.status as RsvpStatus;
  const fullName = sanitizeText(body.fullName, 80);
  const message = sanitizeText(body.message, 400);
  const side = body.side as RsvpSide;

  if (!VALID_STATUSES.includes(status)) {
    return NextResponse.json({ error: 'وضعیت حضور نامعتبر است.' }, { status: 422 });
  }
  if (!isNonEmpty(fullName)) {
    return NextResponse.json({ error: 'نام و نام خانوادگی الزامی است.' }, { status: 422 });
  }
  if (!VALID_SIDES.includes(side)) {
    return NextResponse.json({ error: 'انتخاب طرف عروس یا داماد الزامی است.' }, { status: 422 });
  }

  const entry = await rsvpStore.add({
    status,
    fullName,
    side,
    message: message || undefined,
  });

  try {
    const text = [
      `پاسخ دعوت‌نامه (${side === 'bride' ? 'سمت عروس' : 'سمت داماد'})`,
      `${fullName} — ${STATUS_LABEL_FA[status]}`,
      message ? `پیام: ${message}` : null,
    ]
      .filter(Boolean)
      .join('\n');

    await notifyRsvpBySms({ side, text });
  } catch (error) {
    // The RSVP itself is already persisted — an SMS delivery failure
    // (missing provider credentials, network) must not fail the request.
    console.error('[rsvp] SMS notification failed:', error);
  }

  return NextResponse.json({ entry }, { status: 201 });
}
