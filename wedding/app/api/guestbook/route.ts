import { NextResponse } from 'next/server';
import { guestbookStore } from '@/lib/store/guestbookStore';
import { isNonEmpty, sanitizeText } from '@/lib/utils/validate';

export async function GET() {
  const entries = await guestbookStore.list();
  return NextResponse.json({ entries });
}

export async function POST(request: Request) {
  const body = await request.json().catch(() => null);
  if (!body) {
    return NextResponse.json({ error: 'بدنه‌ی درخواست نامعتبر است.' }, { status: 400 });
  }

  const name = sanitizeText(body.name, 60);
  const message = sanitizeText(body.message, 400);

  if (!isNonEmpty(name) || !isNonEmpty(message)) {
    return NextResponse.json({ error: 'نام و پیام الزامی هستند.' }, { status: 422 });
  }

  const entry = await guestbookStore.add({ name, message });
  return NextResponse.json({ entry }, { status: 201 });
}
