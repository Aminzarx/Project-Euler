import type { Transition, Variants } from 'framer-motion';

/**
 * Shared motion language for the whole experience.
 * Everything leans on spring physics rather than linear easings so
 * transitions feel tactile instead of mechanical. See /docs/motion-guidelines.md.
 */
export const springSilk: Transition = {
  type: 'spring',
  stiffness: 90,
  damping: 20,
  mass: 1,
};

export const springSnappy: Transition = {
  type: 'spring',
  stiffness: 260,
  damping: 22,
  mass: 0.9,
};

export const springGentle: Transition = {
  type: 'spring',
  stiffness: 60,
  damping: 18,
  mass: 1.1,
};

export const fadeUp: Variants = {
  hidden: { opacity: 0, y: 28 },
  visible: (i = 0) => ({
    opacity: 1,
    y: 0,
    transition: { ...springSilk, delay: i * 0.08 },
  }),
};

export const fadeIn: Variants = {
  hidden: { opacity: 0 },
  visible: (i = 0) => ({
    opacity: 1,
    transition: { duration: 0.9, ease: [0.22, 1, 0.36, 1], delay: i * 0.08 },
  }),
};

export const scaleIn: Variants = {
  hidden: { opacity: 0, scale: 0.92 },
  visible: (i = 0) => ({
    opacity: 1,
    scale: 1,
    transition: { ...springSnappy, delay: i * 0.06 },
  }),
};

export const staggerContainer: Variants = {
  hidden: {},
  visible: {
    transition: { staggerChildren: 0.09, delayChildren: 0.05 },
  },
};

export const cardHover = {
  rest: { scale: 1, y: 0 },
  hover: { scale: 1.02, y: -4, transition: springSnappy },
  press: { scale: 0.97, y: -1, transition: { type: 'spring', stiffness: 400, damping: 25 } },
} satisfies Record<string, unknown>;
