'use client';

import { useEffect, useRef } from 'react';
import { useReducedMotion } from '@/lib/hooks/useReducedMotion';

type Particle = {
  x: number;
  y: number;
  vx: number;
  vy: number;
  life: number;
  maxLife: number;
  color: string;
};

const PALETTE = ['rgba(227,192,129,0.9)', 'rgba(219,168,150,0.9)', 'rgba(90,140,124,0.85)', 'rgba(255,246,223,0.9)'];

/**
 * A brief, restrained firework flourish — three soft bursts, muted jewel
 * tones, gone in under three seconds. Fires once when `trigger` flips true.
 */
export function Fireworks({ trigger }: { trigger: boolean }) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const reducedMotion = useReducedMotion();
  const hasFired = useRef(false);

  useEffect(() => {
    if (!trigger || hasFired.current || reducedMotion) return;
    hasFired.current = true;

    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const width = window.innerWidth;
    const height = window.innerHeight;
    canvas.width = width * dpr;
    canvas.height = height * dpr;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    let particles: Particle[] = [];

    const burst = (x: number, y: number) => {
      const count = 26;
      for (let i = 0; i < count; i++) {
        const angle = (Math.PI * 2 * i) / count + Math.random() * 0.2;
        const speed = 1.2 + Math.random() * 2.2;
        particles.push({
          x,
          y,
          vx: Math.cos(angle) * speed,
          vy: Math.sin(angle) * speed,
          life: 0,
          maxLife: 70 + Math.random() * 30,
          color: PALETTE[Math.floor(Math.random() * PALETTE.length)] ?? PALETTE[0]!,
        });
      }
    };

    const bursts = [
      { x: width * 0.3, y: height * 0.32, delay: 0 },
      { x: width * 0.7, y: height * 0.25, delay: 350 },
      { x: width * 0.5, y: height * 0.4, delay: 700 },
    ];

    const timers = bursts.map((b) => window.setTimeout(() => burst(b.x, b.y), b.delay));

    let raf = 0;
    let running = true;
    const animate = () => {
      if (!running) return;
      ctx.clearRect(0, 0, width, height);
      particles = particles.filter((p) => p.life < p.maxLife);
      for (const p of particles) {
        p.life += 1;
        p.x += p.vx;
        p.y += p.vy;
        p.vy += 0.015;
        const alpha = 1 - p.life / p.maxLife;
        ctx.globalAlpha = Math.max(0, alpha);
        ctx.fillStyle = p.color;
        ctx.beginPath();
        ctx.arc(p.x, p.y, 1.8, 0, Math.PI * 2);
        ctx.fill();
      }
      ctx.globalAlpha = 1;
      raf = requestAnimationFrame(animate);
    };
    raf = requestAnimationFrame(animate);

    const stopTimer = window.setTimeout(() => {
      running = false;
      cancelAnimationFrame(raf);
      ctx.clearRect(0, 0, width, height);
    }, 3200);

    return () => {
      running = false;
      timers.forEach(clearTimeout);
      clearTimeout(stopTimer);
      cancelAnimationFrame(raf);
    };
  }, [trigger, reducedMotion]);

  if (reducedMotion) return null;

  return (
    <canvas
      ref={canvasRef}
      aria-hidden="true"
      className="pointer-events-none fixed inset-0 z-40 h-full w-full"
    />
  );
}
