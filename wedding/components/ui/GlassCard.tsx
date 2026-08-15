'use client';

import { motion, type HTMLMotionProps } from 'framer-motion';
import type { ReactNode } from 'react';
import { cn } from '@/lib/utils/cn';

type GlassCardProps = Omit<HTMLMotionProps<'div'>, 'children'> & {
  glow?: boolean;
  children?: ReactNode;
};

/** Reusable frosted-glass surface used across info, nav and guestbook cards. */
export function GlassCard({ className, glow = false, children, ...props }: GlassCardProps) {
  return (
    <motion.div
      className={cn(
        'glass-panel relative overflow-hidden rounded-[28px] shadow-glass',
        glow && 'shadow-gold',
        className,
      )}
      {...props}
    >
      <div className="pointer-events-none absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-persiangold-300/70 to-transparent" />
      {children}
    </motion.div>
  );
}
