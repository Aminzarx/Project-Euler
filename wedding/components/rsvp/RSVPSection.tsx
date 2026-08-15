'use client';

import { useState } from 'react';
import { motion } from 'framer-motion';
import { GlassCard } from '@/components/ui/GlassCard';
import { SectionReveal } from '@/components/ui/SectionReveal';
import { RSVPModal } from '@/components/rsvp/RSVPModal';
import { cardHover, fadeUp, staggerContainer } from '@/lib/motion';
import type { RsvpStatus } from '@/lib/store/types';

const CHOICES: { status: RsvpStatus; label: string; icon: string }[] = [
  { status: 'attending', label: 'حضور می‌یابم', icon: '✓' },
  { status: 'maybe', label: 'شاید', icon: '?' },
  { status: 'declined', label: 'متأسفانه نمی‌توانم حضور یابم', icon: '✕' },
];

export function RSVPSection() {
  const [activeStatus, setActiveStatus] = useState<RsvpStatus | null>(null);

  return (
    <SectionReveal className="mx-auto max-w-3xl px-6 py-16 sm:py-20" id="rsvp">
      <p className="mb-2 text-center font-persian text-sm tracking-[0.25em] text-ink-400">اعلام حضور</p>
      <h2 className="mb-10 text-center font-calligraphy text-3xl text-persiangold-500 sm:text-4xl">
        آیا در جشن ما همراه ما خواهید بود؟
      </h2>

      <motion.div
        variants={staggerContainer}
        initial="hidden"
        whileInView="visible"
        viewport={{ once: true, margin: '-10%' }}
        className="grid grid-cols-1 gap-4 sm:grid-cols-3"
      >
        {CHOICES.map((choice, i) => (
          <motion.div key={choice.status} variants={fadeUp} custom={i}>
            <motion.button
              type="button"
              onClick={() => setActiveStatus(choice.status)}
              initial="rest"
              whileHover="hover"
              whileTap="press"
              animate="rest"
              variants={cardHover}
              className="block w-full text-start"
            >
              <GlassCard className="flex h-full flex-col items-center gap-3 px-5 py-8 text-center" glow>
                <span className="flex h-11 w-11 items-center justify-center rounded-full bg-emerald-500/10 text-lg text-emerald-500">
                  {choice.icon}
                </span>
                <span className="font-persian text-base font-medium text-ink-600">{choice.label}</span>
              </GlassCard>
            </motion.button>
          </motion.div>
        ))}
      </motion.div>

      {activeStatus && <RSVPModal status={activeStatus} onClose={() => setActiveStatus(null)} />}
    </SectionReveal>
  );
}
