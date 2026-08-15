'use client';

import { forwardRef } from 'react';
import { motion, type HTMLMotionProps } from 'framer-motion';
import { cn } from '@/lib/utils/cn';

type GlowButtonProps = HTMLMotionProps<'button'> & {
  variant?: 'solid' | 'outline' | 'ghost';
};

/** Premium micro-interaction button: hover lift, press squeeze, focus ring. */
export const GlowButton = forwardRef<HTMLButtonElement, GlowButtonProps>(
  ({ className, variant = 'solid', children, ...props }, ref) => {
    const variants = {
      solid:
        'bg-gradient-to-b from-persiangold-400 to-persiangold-500 text-ivory-50 shadow-gold hover:shadow-lg',
      outline: 'border border-persiangold-400/60 text-ink-600 hover:bg-persiangold-300/10',
      ghost: 'text-ink-500 hover:bg-champagne-100/60',
    } as const;

    return (
      <motion.button
        ref={ref}
        whileHover={{ scale: 1.035, y: -2 }}
        whileTap={{ scale: 0.96, y: 0 }}
        whileFocus={{ scale: 1.02 }}
        transition={{ type: 'spring', stiffness: 320, damping: 22 }}
        className={cn(
          'relative inline-flex items-center justify-center gap-2 rounded-full px-6 py-3 text-sm font-medium tracking-wide transition-colors duration-300 ease-silk disabled:cursor-not-allowed disabled:opacity-50',
          variants[variant],
          className,
        )}
        {...props}
      >
        {children}
      </motion.button>
    );
  },
);
GlowButton.displayName = 'GlowButton';
