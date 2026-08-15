'use client';

import { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { theme } from '@/lib/theme';
import { useReducedMotion } from '@/lib/hooks/useReducedMotion';

type Stage = 'idle' | 'cracking' | 'opening' | 'done';

type EnvelopeProps = {
  onOpen: () => void;
};

/**
 * The cinematic entry screen: a sealed Persian envelope the guest taps
 * to reveal the invitation. Stages: idle -> cracking (wax seal breaks) ->
 * opening (flap lifts, card slides out, camera zooms) -> done (unmounts).
 */
export function Envelope({ onOpen }: EnvelopeProps) {
  const [stage, setStage] = useState<Stage>('idle');
  const reducedMotion = useReducedMotion();

  const handleTap = () => {
    if (stage !== 'idle') return;
    setStage('cracking');
    window.setTimeout(() => setStage('opening'), reducedMotion ? 50 : 650);
    window.setTimeout(
      () => {
        setStage('done');
        onOpen();
      },
      reducedMotion ? 200 : 2200,
    );
  };

  return (
    <AnimatePresence>
      {stage !== 'done' && (
        <motion.div
          role="button"
          tabIndex={0}
          aria-label="برای باز کردن دعوت‌نامه لمس یا کلیک کنید"
          onClick={handleTap}
          onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && handleTap()}
          className="fixed inset-0 z-50 flex cursor-pointer flex-col items-center justify-center overflow-hidden bg-silk-gradient"
          exit={{ opacity: 0, transition: { duration: 0.8, ease: [0.22, 1, 0.36, 1], delay: 0.2 } }}
        >
          <AmbientGlow />
          <FloatingSpecks reducedMotion={reducedMotion} />

          <motion.div
            className="relative flex flex-col items-center"
            animate={
              stage === 'opening'
                ? { scale: reducedMotion ? 1 : 6, opacity: 0 }
                : { scale: 1, opacity: 1 }
            }
            transition={{ duration: reducedMotion ? 0.2 : 1.5, ease: [0.76, 0, 0.24, 1] }}
            style={{ perspective: 1200 }}
          >
            <EnvelopeBody stage={stage} />
          </motion.div>

          <motion.p
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: stage === 'idle' ? 1 : 0, y: stage === 'idle' ? 0 : 10 }}
            transition={{ duration: 0.6 }}
            className="absolute bottom-12 px-6 text-center font-persian text-sm tracking-wide text-ink-500/80 sm:bottom-16 sm:text-base"
          >
            <motion.span
              animate={reducedMotion ? {} : { opacity: [0.6, 1, 0.6] }}
              transition={{ duration: 2.4, repeat: Infinity, ease: 'easeInOut' }}
              className="inline-block"
            >
              برای باز کردن دعوت‌نامه لمس کنید
            </motion.span>
          </motion.p>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

function EnvelopeBody({ stage }: { stage: Stage }) {
  return (
    <div className="relative h-[220px] w-[300px] sm:h-[260px] sm:w-[360px]">
      {/* envelope base */}
      <div className="absolute inset-0 rounded-[10px] border border-persiangold-300/50 bg-gradient-to-b from-ivory-50 to-champagne-100 shadow-glass-lg" />

      {/* inner card peeking / sliding out */}
      <motion.div
        className="absolute left-1/2 top-1/2 h-[70%] w-[84%] rounded-[6px] border border-persiangold-300/40 bg-ivory-50 shadow-inner"
        style={{ x: '-50%' }}
        animate={
          stage === 'opening'
            ? { y: '-58%', opacity: 1 }
            : { y: '-50%', opacity: stage === 'idle' ? 0 : 1 }
        }
        transition={{ duration: 1.1, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className="flex h-full flex-col items-center justify-center gap-2 px-4">
          <span className="font-calligraphy text-2xl text-persiangold-500 sm:text-3xl">
            {theme.wedding.groomName} <span className="text-rosegold-400">❤</span> {theme.wedding.brideName}
          </span>
        </div>
      </motion.div>

      {/* envelope flap (triangle) */}
      <motion.div
        className="absolute inset-x-0 top-0 origin-top"
        style={{ transformStyle: 'preserve-3d' }}
        animate={{ rotateX: stage === 'opening' || stage === 'cracking' ? -170 : 0 }}
        transition={{ duration: 0.9, ease: [0.65, 0, 0.35, 1], delay: stage === 'opening' ? 0.05 : 0 }}
      >
        <div
          className="h-[130px] w-full sm:h-[150px]"
          style={{
            background: 'linear-gradient(135deg, #f4e9d8 0%, #e9d6b4 60%, #ddc191 100%)',
            clipPath: 'polygon(0 0, 100% 0, 50% 100%)',
            boxShadow: '0 6px 20px rgba(93,72,42,0.1)',
          }}
        />
      </motion.div>

      {/* wax seal */}
      <motion.div
        className="absolute left-1/2 top-[38%] z-10"
        style={{ x: '-50%', y: '-50%' }}
        animate={
          stage === 'cracking' || stage === 'opening'
            ? { scale: [1, 1.15, 0], opacity: [1, 1, 0], rotate: [0, -8, 20] }
            : { scale: 1, opacity: 1, rotate: 0 }
        }
        transition={{ duration: 0.7, ease: 'easeInOut' }}
      >
        <div
          className="flex h-16 w-16 items-center justify-center rounded-full border-2 border-persiangold-300/60 text-ivory-50 shadow-gold sm:h-20 sm:w-20"
          style={{ background: 'radial-gradient(circle at 35% 30%, #d68f6f, #a84f39 70%)' }}
        >
          <span className="text-lg sm:text-xl">❤</span>
        </div>
        {(stage === 'cracking' || stage === 'opening') && (
          <motion.span
            className="absolute inset-0 rounded-full border border-ivory-50/70"
            initial={{ clipPath: 'inset(0 50% 0 0)' }}
            animate={{ rotate: 8 }}
            transition={{ duration: 0.4 }}
          />
        )}
      </motion.div>
    </div>
  );
}

function AmbientGlow() {
  return (
    <div className="pointer-events-none absolute inset-0">
      <div className="absolute left-1/2 top-1/2 h-[70vmax] w-[70vmax] -translate-x-1/2 -translate-y-1/2 rounded-full bg-radial-glow" />
    </div>
  );
}

function FloatingSpecks({ reducedMotion }: { reducedMotion: boolean }) {
  if (reducedMotion) return null;
  const specks = Array.from({ length: 14 });
  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden">
      {specks.map((_, i) => (
        <motion.span
          key={i}
          className="absolute h-1 w-1 rounded-full bg-persiangold-300/70"
          style={{ left: `${(i * 37) % 100}%`, top: `${(i * 53) % 100}%` }}
          animate={{ y: [0, -18, 0], opacity: [0.2, 0.8, 0.2] }}
          transition={{
            duration: 5 + (i % 5),
            repeat: Infinity,
            ease: 'easeInOut',
            delay: i * 0.3,
          }}
        />
      ))}
    </div>
  );
}
