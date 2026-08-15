'use client';

import { useEffect, useState } from 'react';

export type CountdownParts = {
  days: number;
  hours: number;
  minutes: number;
  seconds: number;
  isPast: boolean;
};

function computeParts(targetIso: string): CountdownParts {
  const diff = new Date(targetIso).getTime() - Date.now();
  if (diff <= 0) {
    return { days: 0, hours: 0, minutes: 0, seconds: 0, isPast: true };
  }
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
  const minutes = Math.floor((diff / (1000 * 60)) % 60);
  const seconds = Math.floor((diff / 1000) % 60);
  return { days, hours, minutes, seconds, isPast: false };
}

const ZERO_PARTS: CountdownParts = { days: 0, hours: 0, minutes: 0, seconds: 0, isPast: false };

/**
 * Live countdown to an ISO target date. Ticks every second, client-only.
 * Starts from a static zero state (matching SSR output exactly) and only
 * computes the real, time-dependent value inside an effect — computing it
 * during the initial render would diverge between server and client and
 * trigger a hydration mismatch.
 */
export function useCountdown(targetIso: string): CountdownParts {
  const [parts, setParts] = useState<CountdownParts>(ZERO_PARTS);

  useEffect(() => {
    setParts(computeParts(targetIso));
    const interval = setInterval(() => setParts(computeParts(targetIso)), 1000);
    return () => clearInterval(interval);
  }, [targetIso]);

  return parts;
}
