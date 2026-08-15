'use client';

import { motion } from 'framer-motion';
import { theme } from '@/lib/theme';
import { useReducedMotion } from '@/lib/hooks/useReducedMotion';
import { FloralOrnament } from '@/components/invitation/FloralOrnament';

const REVEAL_EASE: [number, number, number, number] = [0.65, 0, 0.35, 1];

/**
 * "Written by hand" reveal: rather than splitting Persian glyphs (which
 * would break Arabic-script letter joining), each name is masked by a
 * shrinking cover synced to a moving ink-glow edge — so the word appears
 * stroke-by-stroke in natural RTL reading order while staying one text run.
 */
function CalligraphyName({ text, delay }: { text: string; delay: number }) {
  const reducedMotion = useReducedMotion();
  const duration = reducedMotion ? 0.01 : 1.35;

  return (
    <span className="relative inline-block">
      <span className="font-calligraphy text-5xl leading-none text-persiangold-500 sm:text-7xl md:text-8xl">
        {text}
      </span>
      <motion.span
        aria-hidden="true"
        className="absolute inset-y-0 start-0 bg-ivory-50"
        initial={{ width: '100%' }}
        animate={{ width: '0%' }}
        transition={{ duration, delay, ease: REVEAL_EASE }}
      />
      <motion.span
        aria-hidden="true"
        className="absolute inset-y-0 w-[3px] rounded-full bg-persiangold-300 shadow-gold"
        style={{ filter: 'blur(1px)' }}
        initial={{ insetInlineStart: '100%', opacity: 1 }}
        animate={{ insetInlineStart: '0%', opacity: [1, 1, 0] }}
        transition={{ duration, delay, ease: REVEAL_EASE, times: [0, 0.85, 1] }}
      />
    </span>
  );
}

export function NamesHero() {
  const reducedMotion = useReducedMotion();

  return (
    <div className="relative flex min-h-[70vh] flex-col items-center justify-center px-6 py-24 text-center sm:min-h-[80vh]">
      <FloralOrnament />

      <motion.div
        aria-hidden="true"
        className="absolute left-1/2 top-1/2 h-[42vmax] w-[42vmax] rounded-full bg-radial-glow"
        style={{ x: '-50%', y: '-50%' }}
        animate={reducedMotion ? {} : { opacity: [0.6, 1, 0.6] }}
        transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut' }}
      />

      <motion.p
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2, duration: 0.8 }}
        className="relative z-10 mb-4 font-serif text-sm italic tracking-[0.3em] text-ink-400 sm:text-base"
      >
        WE ARE GETTING MARRIED
      </motion.p>

      <div className="relative z-10 flex flex-col items-center gap-3 sm:flex-row sm:gap-6">
        <CalligraphyName text={theme.wedding.groomName} delay={0.5} />

        <motion.span
          className="text-3xl text-rosegold-400 sm:text-5xl"
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: [0, 1.3, 1], opacity: 1 }}
          transition={{ delay: reducedMotion ? 0.1 : 2.1, duration: 0.7, ease: 'easeOut' }}
        >
          <motion.span
            animate={reducedMotion ? {} : { scale: [1, 1.15, 1] }}
            transition={{ duration: 1.8, repeat: Infinity, ease: 'easeInOut', delay: 2.8 }}
            className="inline-block"
          >
            ❤
          </motion.span>
        </motion.span>

        <CalligraphyName text={theme.wedding.brideName} delay={reducedMotion ? 0.6 : 1.3} />
      </div>

      <motion.div
        initial={{ opacity: 0, scaleX: 0 }}
        animate={{ opacity: 1, scaleX: 1 }}
        transition={{ delay: reducedMotion ? 0.7 : 2.6, duration: 0.9, ease: REVEAL_EASE }}
        className="gold-hairline relative z-10 mt-8 w-40 sm:w-56"
      />

      <motion.p
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: reducedMotion ? 0.8 : 2.9, duration: 0.8 }}
        className="relative z-10 mt-5 font-persian text-base text-ink-500 sm:text-lg"
      >
        {theme.wedding.dateFa} — {theme.wedding.venueName}
      </motion.p>
    </div>
  );
}
