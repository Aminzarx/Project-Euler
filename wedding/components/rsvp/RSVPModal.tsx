'use client';

import { useState, type FormEvent } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { GlowButton } from '@/components/ui/GlowButton';
import { apiUrl } from '@/lib/config';
import type { RsvpSide, RsvpStatus } from '@/lib/store/types';

type RSVPModalProps = {
  status: RsvpStatus;
  onClose: () => void;
};

const TITLES: Record<RsvpStatus, string> = {
  attending: 'خوشحالیم که در کنارمان خواهید بود',
  maybe: 'امیدواریم بتوانید همراهمان باشید',
  declined: 'حضور شما برایمان عزیز است',
};

const MESSAGE_PLACEHOLDER: Record<RsvpStatus, string> = {
  attending: 'اگر دوست دارید پیامی برایمان بنویسید (اختیاری)',
  maybe: 'اگر دوست دارید پیامی برایمان بنویسید (اختیاری)',
  declined: 'دلمان برای حضورتان تنگ می‌شود؛ اگر پیامی دارید بنویسید',
};

export function RSVPModal({ status, onClose }: RSVPModalProps) {
  const [fullName, setFullName] = useState('');
  const [message, setMessage] = useState('');
  const [side, setSide] = useState<RsvpSide | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!fullName.trim() || !side) return;
    setSubmitting(true);
    setError(null);
    try {
      const res = await fetch(apiUrl('rsvp'), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, fullName, message, side }),
      });
      if (!res.ok) throw new Error();
      setSuccess(true);
      window.setTimeout(onClose, 1800);
    } catch {
      setError('ثبت پاسخ با خطا مواجه شد. لطفاً دوباره تلاش کنید.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AnimatePresence>
      <motion.div
        className="fixed inset-0 z-[60] flex items-end justify-center bg-ink-700/40 backdrop-blur-sm sm:items-center"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
        role="presentation"
      >
        <motion.div
          role="dialog"
          aria-modal="true"
          aria-labelledby="rsvp-modal-title"
          onClick={(e) => e.stopPropagation()}
          initial={{ y: 60, opacity: 0, scale: 0.96 }}
          animate={{ y: 0, opacity: 1, scale: 1 }}
          exit={{ y: 40, opacity: 0, scale: 0.97 }}
          transition={{ type: 'spring', stiffness: 220, damping: 26 }}
          className="glass-panel relative w-full max-w-md rounded-t-[32px] rounded-b-none bg-ivory-50 px-6 py-8 shadow-glass-lg sm:rounded-b-[32px] sm:px-8 sm:py-9"
        >
          <button
            type="button"
            onClick={onClose}
            aria-label="بستن"
            className="absolute end-5 top-5 flex h-9 w-9 items-center justify-center rounded-full text-ink-400 transition-colors hover:bg-champagne-100 hover:text-ink-600"
          >
            ✕
          </button>

          {success ? (
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              className="flex flex-col items-center gap-3 py-8 text-center"
            >
              <span className="text-4xl">✓</span>
              <p className="font-persian text-ink-600">پاسخ شما با سپاس ثبت شد</p>
            </motion.div>
          ) : (
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              <h3 id="rsvp-modal-title" className="font-calligraphy text-2xl text-persiangold-500">
                {TITLES[status]}
              </h3>

              <div>
                <label htmlFor="rsvp-name" className="mb-1.5 block font-persian text-sm text-ink-500">
                  نام و نام خانوادگی
                </label>
                <input
                  id="rsvp-name"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  maxLength={80}
                  className="w-full rounded-xl border border-champagne-200 bg-ivory-50/70 px-4 py-2.5 font-persian text-ink-700 outline-none transition-colors focus:border-persiangold-400"
                />
              </div>

              <fieldset>
                <legend className="mb-1.5 font-persian text-sm text-ink-500">مهمان کدام طرف هستید؟</legend>
                <div className="flex gap-3">
                  {(['bride', 'groom'] as const).map((option) => (
                    <button
                      key={option}
                      type="button"
                      onClick={() => setSide(option)}
                      aria-pressed={side === option}
                      className={`flex-1 rounded-xl border px-4 py-2.5 font-persian text-sm transition-colors ${
                        side === option
                          ? 'border-persiangold-400 bg-persiangold-300/15 text-ink-700'
                          : 'border-champagne-200 text-ink-500 hover:border-persiangold-300'
                      }`}
                    >
                      {option === 'bride' ? 'سمت عروس' : 'سمت داماد'}
                    </button>
                  ))}
                </div>
              </fieldset>

              <div>
                <label htmlFor="rsvp-message" className="mb-1.5 block font-persian text-sm text-ink-500">
                  پیام
                </label>
                <textarea
                  id="rsvp-message"
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  maxLength={400}
                  rows={3}
                  placeholder={MESSAGE_PLACEHOLDER[status]}
                  className="w-full resize-none rounded-xl border border-champagne-200 bg-ivory-50/70 px-4 py-2.5 font-persian text-ink-700 outline-none transition-colors focus:border-persiangold-400"
                />
              </div>

              {error && <p className="font-persian text-sm text-rosegold-500">{error}</p>}

              <GlowButton type="submit" disabled={submitting || !fullName.trim() || !side} className="mt-1 w-full">
                {submitting ? 'در حال ارسال...' : 'ارسال پاسخ'}
              </GlowButton>
            </form>
          )}
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
