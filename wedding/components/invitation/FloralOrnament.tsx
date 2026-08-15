'use client';

import { motion } from 'framer-motion';

/**
 * Hand-drawn-style floral flourish rendered as line art, mirrored on both
 * sides of the names. Pure vector, no image assets — keeps the hero
 * lightweight and crisp at any resolution.
 */
export function FloralOrnament() {
  return (
    <div aria-hidden="true" className="pointer-events-none absolute inset-0 flex items-center justify-center">
      <motion.svg
        viewBox="0 0 800 400"
        className="h-[60vh] w-full max-w-4xl opacity-[0.28]"
        initial={{ opacity: 0 }}
        animate={{ opacity: 0.28 }}
        transition={{ duration: 1.6, delay: 0.3 }}
      >
        <g stroke="#cda054" strokeWidth="1.2" fill="none" strokeLinecap="round">
          <Branch transform="translate(60,200) scale(1)" />
          <Branch transform="translate(740,200) scale(-1,1)" />
        </g>
      </motion.svg>
    </div>
  );
}

function Branch({ transform }: { transform: string }) {
  return (
    <g transform={transform}>
      <path d="M0,0 C40,-30 90,-40 150,-20 C190,-6 210,20 240,10" />
      <path d="M40,-15 C50,-45 40,-70 15,-90" />
      <path d="M85,-28 C95,-58 88,-82 62,-100" />
      <path d="M130,-24 C145,-50 145,-72 125,-95" />
      <circle cx="15" cy="-92" r="6" fill="#e3c081" stroke="none" opacity="0.8" />
      <circle cx="62" cy="-102" r="7" fill="#dba896" stroke="none" opacity="0.75" />
      <circle cx="125" cy="-97" r="5" fill="#e3c081" stroke="none" opacity="0.8" />
      <path d="M0,0 C30,20 70,28 110,18" />
      <path d="M35,10 C40,30 32,46 12,58" />
      <path d="M75,15 C82,34 76,50 55,62" />
    </g>
  );
}
