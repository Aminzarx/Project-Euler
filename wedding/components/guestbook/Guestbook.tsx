'use client';

import { useEffect, useState, type FormEvent } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { GlassCard } from '@/components/ui/GlassCard';
import { GlowButton } from '@/components/ui/GlowButton';
import { SectionReveal } from '@/components/ui/SectionReveal';
import type { GuestbookEntry } from '@/lib/store/types';
import { apiUrl } from '@/lib/config';

type Status = 'idle' | 'loading' | 'submitting' | 'success' | 'error';

export function Guestbook() {
  const [entries, setEntries] = useState<GuestbookEntry[]>([]);
  const [name, setName] = useState('');
  const [message, setMessage] = useState('');
  const [status, setStatus] = useState<Status>('loading');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch(apiUrl('guestbook'))
      .then((res) => res.json())
      .then((data: { entries: GuestbookEntry[] }) => {
        setEntries(data.entries ?? []);
        setStatus('idle');
      })
      .catch(() => setStatus('idle'));
  }, []);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!name.trim() || !message.trim()) return;
    setStatus('submitting');
    setError(null);
    try {
      const res = await fetch(apiUrl('guestbook'), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, message }),
      });
      if (!res.ok) throw new Error();
      const data: { entry: GuestbookEntry } = await res.json();
      setEntries((prev) => [data.entry, ...prev]);
      setName('');
      setMessage('');
      setStatus('success');
      window.setTimeout(() => setStatus('idle'), 2200);
    } catch {
      setStatus('error');
      setError('ثبت پیام با خطا مواجه شد. دوباره تلاش کنید.');
    }
  };

  return (
    <SectionReveal className="mx-auto max-w-3xl px-6 py-16 sm:py-20" id="guestbook">
      <p className="mb-2 text-center font-persian text-sm tracking-[0.25em] text-ink-400">دفتر مهمانان</p>
      <h2 className="mb-8 text-center font-calligraphy text-3xl text-persiangold-500 sm:text-4xl">
        پیام‌های شما برای علی و یگانه
      </h2>

      <GlassCard className="mb-10 px-6 py-7 sm:px-8 sm:py-9">
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label htmlFor="guest-name" className="mb-1.5 block font-persian text-sm text-ink-500">
              نام شما
            </label>
            <input
              id="guest-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              maxLength={60}
              required
              className="w-full rounded-xl border border-champagne-200 bg-ivory-50/70 px-4 py-2.5 font-persian text-ink-700 outline-none transition-colors focus:border-persiangold-400"
              placeholder="مثلاً: سارا محمدی"
            />
          </div>
          <div>
            <label htmlFor="guest-message" className="mb-1.5 block font-persian text-sm text-ink-500">
              پیام شما
            </label>
            <textarea
              id="guest-message"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              maxLength={400}
              required
              rows={3}
              className="w-full resize-none rounded-xl border border-champagne-200 bg-ivory-50/70 px-4 py-2.5 font-persian text-ink-700 outline-none transition-colors focus:border-persiangold-400"
              placeholder="آرزوی خوشبختی خود را بنویسید..."
            />
          </div>
          {error && <p className="font-persian text-sm text-rosegold-500">{error}</p>}
          <div className="flex items-center justify-between gap-4">
            <GlowButton type="submit" disabled={status === 'submitting'}>
              {status === 'submitting' ? 'در حال ارسال...' : 'ثبت پیام'}
            </GlowButton>
            <AnimatePresence>
              {status === 'success' && (
                <motion.span
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0 }}
                  className="font-persian text-sm text-emerald-500"
                >
                  ✓ پیام شما ثبت شد
                </motion.span>
              )}
            </AnimatePresence>
          </div>
        </form>
      </GlassCard>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <AnimatePresence initial={false}>
          {entries.map((entry) => (
            <motion.div
              key={entry.id}
              layout
              initial={{ opacity: 0, y: 16, scale: 0.97 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
            >
              <GlassCard className="h-full px-5 py-5">
                <p className="font-persian text-sm leading-relaxed text-ink-600">{entry.message}</p>
                <p className="mt-3 font-calligraphy text-lg text-persiangold-500">— {entry.name}</p>
              </GlassCard>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </SectionReveal>
  );
}
