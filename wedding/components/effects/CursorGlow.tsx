'use client';

import { useEffect, useRef, useState } from 'react';
import { motion, useMotionValue, useSpring } from 'framer-motion';
import { useReducedMotion } from '@/lib/hooks/useReducedMotion';

type Ripple = { id: number; x: number; y: number };

/**
 * Desktop: a soft golden light that trails the cursor (GPU transform-only).
 * Touch: a gentle expanding ripple at each tap point.
 */
export function CursorGlow() {
  const reducedMotion = useReducedMotion();
  const x = useMotionValue(-200);
  const y = useMotionValue(-200);
  const springX = useSpring(x, { stiffness: 120, damping: 24, mass: 0.6 });
  const springY = useSpring(y, { stiffness: 120, damping: 24, mass: 0.6 });
  const [visible, setVisible] = useState(false);
  const [ripples, setRipples] = useState<Ripple[]>([]);
  const rippleId = useRef(0);

  useEffect(() => {
    if (reducedMotion) return;

    const handleMove = (event: PointerEvent) => {
      if (event.pointerType !== 'mouse') return;
      x.set(event.clientX);
      y.set(event.clientY);
      setVisible(true);
    };
    const handleLeave = () => setVisible(false);

    const handleTouch = (event: PointerEvent) => {
      if (event.pointerType !== 'touch') return;
      const id = rippleId.current++;
      setRipples((prev) => [...prev, { id, x: event.clientX, y: event.clientY }]);
      window.setTimeout(() => {
        setRipples((prev) => prev.filter((r) => r.id !== id));
      }, 900);
    };

    window.addEventListener('pointermove', handleMove);
    window.addEventListener('pointerdown', handleTouch);
    window.addEventListener('pointerleave', handleLeave);
    return () => {
      window.removeEventListener('pointermove', handleMove);
      window.removeEventListener('pointerdown', handleTouch);
      window.removeEventListener('pointerleave', handleLeave);
    };
  }, [reducedMotion, x, y]);

  if (reducedMotion) return null;

  return (
    <div aria-hidden="true" className="pointer-events-none fixed inset-0 z-10 overflow-hidden">
      <motion.div
        className="absolute h-[420px] w-[420px] rounded-full mix-blend-multiply"
        style={{
          x: springX,
          y: springY,
          translateX: '-50%',
          translateY: '-50%',
          opacity: visible ? 1 : 0,
          background:
            'radial-gradient(circle, rgba(227,192,129,0.18) 0%, rgba(227,192,129,0.06) 45%, rgba(227,192,129,0) 70%)',
          transition: 'opacity 0.6s ease',
        }}
      />
      {ripples.map((r) => (
        <motion.span
          key={r.id}
          className="absolute rounded-full border border-persiangold-300/60"
          style={{ left: r.x, top: r.y, translateX: '-50%', translateY: '-50%' }}
          initial={{ width: 0, height: 0, opacity: 0.6 }}
          animate={{ width: 140, height: 140, opacity: 0 }}
          transition={{ duration: 0.9, ease: [0.22, 1, 0.36, 1] }}
        />
      ))}
    </div>
  );
}
