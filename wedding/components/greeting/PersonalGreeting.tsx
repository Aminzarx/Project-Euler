'use client';

import { useSearchParams } from 'next/navigation';
import { motion } from 'framer-motion';

/**
 * Reads ?name=... and, when present, greets the guest by name.
 * Renders nothing when the parameter is absent — the invitation still
 * reads perfectly without it.
 */
export function PersonalGreeting() {
  const searchParams = useSearchParams();
  const name = searchParams.get('name')?.trim();

  if (!name) return null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
      className="mx-auto mb-4 max-w-md px-6 text-center"
    >
      <p className="font-calligraphy text-2xl text-emerald-500 sm:text-3xl">{name} عزیز</p>
      <p className="mt-2 font-persian text-sm text-ink-500 sm:text-base">
        حضور گرم شما باعث افتخار ماست
      </p>
    </motion.div>
  );
}
