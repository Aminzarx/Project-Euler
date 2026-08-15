'use client';

import { AnimatePresence, motion } from 'framer-motion';
import { useCountdown } from '@/lib/hooks/useCountdown';
import { theme } from '@/lib/theme';
import { SectionReveal } from '@/components/ui/SectionReveal';

const UNITS = [
  { key: 'days', label: 'روز' },
  { key: 'hours', label: 'ساعت' },
  { key: 'minutes', label: 'دقیقه' },
  { key: 'seconds', label: 'ثانیه' },
] as const;

function pad(n: number) {
  return n.toString().padStart(2, '0');
}

function FlipDigit({ value }: { value: string }) {
  return (
    <span className="relative inline-block h-[1.2em] w-[0.75em] overflow-hidden text-center">
      <AnimatePresence mode="popLayout">
        <motion.span
          key={value}
          initial={{ y: '60%', opacity: 0 }}
          animate={{ y: '0%', opacity: 1 }}
          exit={{ y: '-60%', opacity: 0 }}
          transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
          className="absolute inset-0"
        >
          {value}
        </motion.span>
      </AnimatePresence>
    </span>
  );
}

export function Countdown() {
  const { days, hours, minutes, seconds, isPast } = useCountdown(theme.wedding.isoDateTime);
  const values = { days, hours, minutes, seconds };

  return (
    <SectionReveal className="mx-auto max-w-3xl px-6 py-16 text-center sm:py-20">
      <p className="mb-8 font-persian text-sm tracking-[0.25em] text-ink-400">
        {isPast ? 'جشن آغاز شده است' : 'تا شروع جشن'}
      </p>
      <div className="flex items-center justify-center gap-3 sm:gap-6" dir="ltr">
        {UNITS.map((unit) => (
          <div key={unit.key} className="flex flex-col items-center gap-2">
            <div className="glass-panel flex items-baseline gap-0.5 rounded-2xl px-3 py-4 font-serif text-3xl tabular-nums text-persiangold-500 shadow-glass sm:px-5 sm:py-5 sm:text-5xl">
              {pad(values[unit.key])
                .split('')
                .map((digit, i) => (
                  <FlipDigit key={i} value={digit} />
                ))}
            </div>
            <span className="font-persian text-xs text-ink-400 sm:text-sm">{unit.label}</span>
          </div>
        ))}
      </div>
    </SectionReveal>
  );
}
