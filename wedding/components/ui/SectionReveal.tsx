'use client';

import { motion } from 'framer-motion';
import type { ReactNode } from 'react';
import { fadeUp } from '@/lib/motion';
import { cn } from '@/lib/utils/cn';

type SectionRevealProps = {
  children: ReactNode;
  className?: string;
  delay?: number;
  as?: 'section' | 'div';
  id?: string;
};

/** Scroll-triggered fade+rise wrapper used to reveal each section on entry. */
export function SectionReveal({ children, className, delay = 0, as = 'section', id }: SectionRevealProps) {
  const Component = motion[as];
  return (
    <Component
      id={id}
      className={cn(className)}
      initial="hidden"
      whileInView="visible"
      viewport={{ once: true, margin: '-10% 0px -10% 0px' }}
      custom={delay}
      variants={fadeUp}
    >
      {children}
    </Component>
  );
}
