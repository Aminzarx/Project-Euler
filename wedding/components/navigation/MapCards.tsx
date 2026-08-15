'use client';

import { motion } from 'framer-motion';
import { GlassCard } from '@/components/ui/GlassCard';
import { theme } from '@/lib/theme';
import { cardHover, fadeUp, staggerContainer } from '@/lib/motion';
import { SectionReveal } from '@/components/ui/SectionReveal';

const providers = [
  { name: 'بلد', href: theme.wedding.links.balad, icon: <BaladIcon /> },
  { name: 'نشان', href: theme.wedding.links.neshan, icon: <NeshanIcon /> },
  { name: 'گوگل مپ', href: theme.wedding.links.googleMaps, icon: <GoogleIcon /> },
];

export function MapCards() {
  return (
    <SectionReveal className="mx-auto max-w-4xl px-6 py-16 sm:py-20">
      <p className="mb-8 text-center font-persian text-sm tracking-[0.25em] text-ink-400">مسیریابی</p>
      <motion.div
        variants={staggerContainer}
        initial="hidden"
        whileInView="visible"
        viewport={{ once: true, margin: '-10%' }}
        className="grid grid-cols-1 gap-5 sm:grid-cols-3"
      >
        {providers.map((p, i) => (
          <motion.div key={p.name} variants={fadeUp} custom={i}>
            <motion.a
              href={p.href}
              target="_blank"
              rel="noreferrer"
              initial="rest"
              whileHover="hover"
              whileTap="press"
              animate="rest"
              variants={cardHover}
              className="block"
            >
              <GlassCard className="flex flex-col items-center gap-3 px-6 py-8 text-center">
                <div className="flex h-14 w-14 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-500">
                  {p.icon}
                </div>
                <span className="font-persian text-base font-medium text-ink-600">باز کردن در {p.name}</span>
              </GlassCard>
            </motion.a>
          </motion.div>
        ))}
      </motion.div>
    </SectionReveal>
  );
}

function BaladIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M12 21s7-6.2 7-11.5A7 7 0 0 0 5 9.5C5 14.8 12 21 12 21z" />
      <circle cx="12" cy="9.5" r="2.5" />
    </svg>
  );
}
function NeshanIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <path d="M3 8.5 12 4l9 4.5-9 4.5-9-4.5z" />
      <path d="M3 8.5V16l9 4.5 9-4.5V8.5" />
    </svg>
  );
}
function GoogleIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 3.5c2.5 2.4 3.9 5.4 3.9 8.5s-1.4 6.1-3.9 8.5c-2.5-2.4-3.9-5.4-3.9-8.5s1.4-6.1 3.9-8.5z" />
      <path d="M3.7 9.5h16.6M3.7 14.5h16.6" />
    </svg>
  );
}
