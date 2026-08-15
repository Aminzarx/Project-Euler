'use client';

import { useEffect, useRef } from 'react';
import { useReducedMotion } from '@/lib/hooks/useReducedMotion';

type Petal = {
  x: number;
  y: number;
  size: number;
  speed: number;
  drift: number;
  angle: number;
  spin: number;
  opacity: number;
};

type Bokeh = {
  x: number;
  y: number;
  radius: number;
  driftPhase: number;
  opacity: number;
  hue: 'gold' | 'rose' | 'emerald';
};

const HUES: Record<Bokeh['hue'], string> = {
  gold: '227,192,129',
  rose: '219,168,150',
  emerald: '90,140,124',
};

/**
 * A single canvas that layers three GPU-cheap ambient motions:
 * slow glowing bokeh, drifting rose petals, and a faint light-ray sweep.
 * Everything runs on one rAF loop and freezes on prefers-reduced-motion.
 */
export function AmbientBackground() {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const reducedMotion = useReducedMotion();

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = window.innerWidth;
    let height = window.innerHeight;
    let dpr = Math.min(window.devicePixelRatio || 1, 2);

    const resize = () => {
      width = window.innerWidth;
      height = window.innerHeight;
      dpr = Math.min(window.devicePixelRatio || 1, 2);
      canvas.width = width * dpr;
      canvas.height = height * dpr;
      canvas.style.width = `${width}px`;
      canvas.style.height = `${height}px`;
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    };
    resize();
    window.addEventListener('resize', resize);

    const petalCount = width < 640 ? 10 : 18;
    const petals: Petal[] = Array.from({ length: petalCount }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      size: 6 + Math.random() * 8,
      speed: 0.25 + Math.random() * 0.4,
      drift: Math.random() * Math.PI * 2,
      angle: Math.random() * Math.PI * 2,
      spin: (Math.random() - 0.5) * 0.02,
      opacity: 0.25 + Math.random() * 0.35,
    }));

    const bokehCount = width < 640 ? 6 : 10;
    const bokehHues: Bokeh['hue'][] = ['gold', 'rose', 'emerald'];
    const bokeh: Bokeh[] = Array.from({ length: bokehCount }, () => ({
      x: Math.random() * width,
      y: Math.random() * height,
      radius: 60 + Math.random() * 120,
      driftPhase: Math.random() * Math.PI * 2,
      opacity: 0.05 + Math.random() * 0.08,
      hue: bokehHues[Math.floor(Math.random() * bokehHues.length)] ?? 'gold',
    }));

    let frame = 0;
    let raf = 0;

    const drawStatic = () => {
      ctx.clearRect(0, 0, width, height);
      for (const b of bokeh) {
        const gradient = ctx.createRadialGradient(b.x, b.y, 0, b.x, b.y, b.radius);
        gradient.addColorStop(0, `rgba(${HUES[b.hue]}, ${b.opacity})`);
        gradient.addColorStop(1, `rgba(${HUES[b.hue]}, 0)`);
        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.arc(b.x, b.y, b.radius, 0, Math.PI * 2);
        ctx.fill();
      }
    };

    const drawPetal = (p: Petal) => {
      ctx.save();
      ctx.translate(p.x, p.y);
      ctx.rotate(p.angle);
      ctx.globalAlpha = p.opacity;
      const grad = ctx.createLinearGradient(-p.size, 0, p.size, 0);
      grad.addColorStop(0, 'rgba(219,168,150,0.05)');
      grad.addColorStop(0.5, 'rgba(219,168,150,0.55)');
      grad.addColorStop(1, 'rgba(227,192,129,0.15)');
      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.ellipse(0, 0, p.size, p.size * 0.6, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    };

    const animate = () => {
      frame += 1;
      ctx.clearRect(0, 0, width, height);

      for (const b of bokeh) {
        const bx = b.x + Math.sin(frame * 0.0025 + b.driftPhase) * 18;
        const by = b.y + Math.cos(frame * 0.002 + b.driftPhase) * 14;
        const gradient = ctx.createRadialGradient(bx, by, 0, bx, by, b.radius);
        gradient.addColorStop(0, `rgba(${HUES[b.hue]}, ${b.opacity})`);
        gradient.addColorStop(1, `rgba(${HUES[b.hue]}, 0)`);
        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.arc(bx, by, b.radius, 0, Math.PI * 2);
        ctx.fill();
      }

      for (const p of petals) {
        p.y += p.speed;
        p.x += Math.sin(frame * 0.01 + p.drift) * 0.5;
        p.angle += p.spin;
        if (p.y > height + 20) {
          p.y = -20;
          p.x = Math.random() * width;
        }
        drawPetal(p);
      }

      // soft diagonal light sweep
      const sweepX = (Math.sin(frame * 0.0008) * 0.5 + 0.5) * width;
      const rayGrad = ctx.createLinearGradient(sweepX - 200, 0, sweepX + 200, height);
      rayGrad.addColorStop(0, 'rgba(255,246,223,0)');
      rayGrad.addColorStop(0.5, 'rgba(255,246,223,0.06)');
      rayGrad.addColorStop(1, 'rgba(255,246,223,0)');
      ctx.fillStyle = rayGrad;
      ctx.fillRect(0, 0, width, height);

      raf = requestAnimationFrame(animate);
    };

    if (reducedMotion) {
      drawStatic();
    } else {
      raf = requestAnimationFrame(animate);
    }

    return () => {
      window.removeEventListener('resize', resize);
      cancelAnimationFrame(raf);
    };
  }, [reducedMotion]);

  return (
    <canvas
      ref={canvasRef}
      aria-hidden="true"
      className="pointer-events-none fixed inset-0 z-0 h-full w-full"
    />
  );
}
