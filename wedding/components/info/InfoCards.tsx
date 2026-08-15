'use client';

import { motion } from 'framer-motion';
import { GlassCard } from '@/components/ui/GlassCard';
import { theme } from '@/lib/theme';
import { fadeUp, staggerContainer } from '@/lib/motion';
import { SectionReveal } from '@/components/ui/SectionReveal';

const cards = [
  {
    title: 'تاریخ',
    value: theme.wedding.dateFa,
    icon: <CalendarIcon />,
  },
  {
    title: 'ساعت',
    value: theme.wedding.timeFa,
    icon: <ClockIcon />,
  },
  {
    title: 'مکان',
    value: theme.wedding.venueName,
    subvalue: theme.wedding.venueAddress,
    icon: <PinIcon />,
  },
];

export function InfoCards() {
  return (
    <SectionReveal className="mx-auto max-w-5xl px-6 py-16 sm:py-20">
      <motion.div
        variants={staggerContainer}
        initial="hidden"
        whileInView="visible"
        viewport={{ once: true, margin: '-10%' }}
        className="grid grid-cols-1 gap-5 sm:grid-cols-3"
      >
        {cards.map((card, i) => (
          <motion.div key={card.title} variants={fadeUp} custom={i}>
            <GlassCard className="flex h-full flex-col items-center gap-3 px-6 py-9 text-center" glow>
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-persiangold-300/15 text-persiangold-500">
                {card.icon}
              </div>
              <p className="font-persian text-sm tracking-wide text-ink-400">{card.title}</p>
              <p className="font-calligraphy text-2xl text-ink-700 sm:text-3xl">{card.value}</p>
              {card.subvalue && <p className="font-persian text-sm text-ink-400">{card.subvalue}</p>}
            </GlassCard>
          </motion.div>
        ))}
      </motion.div>
    </SectionReveal>
  );
}

function CalendarIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <rect x="3.5" y="5" width="17" height="15" rx="2.5" />
      <path d="M3.5 9.5h17M8 3v4M16 3v4" strokeLinecap="round" />
    </svg>
  );
}
function ClockIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}
function PinIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M12 21s7-6.2 7-11.5A7 7 0 0 0 5 9.5C5 14.8 12 21 12 21z" />
      <circle cx="12" cy="9.5" r="2.5" />
    </svg>
  );
}
