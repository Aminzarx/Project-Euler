'use client';

import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { motion } from 'framer-motion';
import { GlassCard } from '@/components/ui/GlassCard';
import { BASE_PATH } from '@/lib/config';

export type MusicPlayerHandle = {
  /** Starts playback with a fade-in. Safe to call multiple times. */
  start: () => void;
};

const STORAGE_KEY = 'wedding:audio-position';
const TARGET_VOLUME = 0.55;
const FADE_MS = 1200;

/**
 * Floating music control. Playback only ever starts from a user gesture
 * (the envelope tap, or these buttons) to respect autoplay policies.
 * Volume fades smoothly on every play/pause and position survives reloads.
 */
export const MusicPlayer = forwardRef<MusicPlayerHandle>(function MusicPlayer(_props, ref) {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const fadeRaf = useRef<number | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const stored = Number(window.localStorage.getItem(STORAGE_KEY) ?? '0');
    if (audioRef.current && Number.isFinite(stored)) {
      audioRef.current.currentTime = stored;
    }
    const audio = audioRef.current;
    const handleCanPlay = () => setReady(true);
    audio?.addEventListener('canplaythrough', handleCanPlay);
    return () => audio?.removeEventListener('canplaythrough', handleCanPlay);
  }, []);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;
    const persist = () => window.localStorage.setItem(STORAGE_KEY, String(audio.currentTime));
    const interval = window.setInterval(persist, 3000);
    audio.addEventListener('pause', persist);
    return () => {
      window.clearInterval(interval);
      audio.removeEventListener('pause', persist);
      persist();
    };
  }, []);

  const fadeTo = (target: number, onDone?: () => void) => {
    const audio = audioRef.current;
    if (!audio) return;
    if (fadeRaf.current) cancelAnimationFrame(fadeRaf.current);
    const start = audio.volume;
    const startTime = performance.now();

    const step = (now: number) => {
      const t = Math.min(1, (now - startTime) / FADE_MS);
      audio.volume = start + (target - start) * t;
      if (t < 1) {
        fadeRaf.current = requestAnimationFrame(step);
      } else {
        onDone?.();
      }
    };
    fadeRaf.current = requestAnimationFrame(step);
  };

  const play = () => {
    const audio = audioRef.current;
    if (!audio) return;
    audio.volume = 0;
    audio
      .play()
      .then(() => {
        setIsPlaying(true);
        fadeTo(isMuted ? 0 : TARGET_VOLUME);
      })
      .catch(() => {
        // Autoplay blocked or asset missing — fail silently, UI stays interactive.
        setIsPlaying(false);
      });
  };

  const pause = () => {
    const audio = audioRef.current;
    if (!audio) return;
    fadeTo(0, () => {
      audio.pause();
      setIsPlaying(false);
    });
  };

  useImperativeHandle(ref, () => ({
    start: () => {
      if (!isPlaying) play();
    },
  }));

  const toggleMute = () => {
    setIsMuted((prev) => {
      const next = !prev;
      fadeTo(next ? 0 : TARGET_VOLUME);
      return next;
    });
  };

  return (
    <>
      <audio ref={audioRef} src={`${BASE_PATH}/audio/wedding-song.mp3`} loop preload="auto" />
      <GlassCard
        className="fixed bottom-5 left-1/2 z-40 flex items-center gap-1 rounded-full px-2 py-2"
        style={{ x: '-50%' }}
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 1.2, duration: 0.8 }}
      >
        <ControlButton
          label={isPlaying ? 'توقف موسیقی' : 'پخش موسیقی'}
          onClick={() => (isPlaying ? pause() : play())}
          disabled={!ready && !isPlaying}
        >
          {isPlaying ? <PauseIcon /> : <PlayIcon />}
        </ControlButton>
        <ControlButton label={isMuted ? 'باز کردن صدا' : 'قطع صدا'} onClick={toggleMute}>
          {isMuted ? <MuteIcon /> : <VolumeIcon />}
        </ControlButton>
        <motion.div
          className="me-2 h-2 w-2 rounded-full bg-emerald-500"
          animate={isPlaying ? { opacity: [0.4, 1, 0.4] } : { opacity: 0.25 }}
          transition={{ duration: 1.8, repeat: isPlaying ? Infinity : 0 }}
          aria-hidden="true"
        />
      </GlassCard>
    </>
  );
});

function ControlButton({
  children,
  label,
  onClick,
  disabled,
}: {
  children: React.ReactNode;
  label: string;
  onClick: () => void;
  disabled?: boolean;
}) {
  return (
    <motion.button
      type="button"
      aria-label={label}
      title={label}
      onClick={onClick}
      disabled={disabled}
      whileHover={{ scale: 1.08 }}
      whileTap={{ scale: 0.92 }}
      className="flex h-10 w-10 items-center justify-center rounded-full text-ink-600 transition-colors hover:bg-persiangold-300/15 disabled:opacity-40"
    >
      {children}
    </motion.button>
  );
}

function PlayIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
      <path d="M4 2.5v11l9-5.5-9-5.5z" />
    </svg>
  );
}
function PauseIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
      <rect x="3.5" y="2.5" width="3" height="11" rx="1" />
      <rect x="9.5" y="2.5" width="3" height="11" rx="1" />
    </svg>
  );
}
function VolumeIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.4" aria-hidden="true">
      <path d="M2 6h2.5L8 3v10L4.5 10H2V6z" fill="currentColor" stroke="none" />
      <path d="M10.5 5.5c1 .8 1 4.2 0 5" strokeLinecap="round" />
      <path d="M12.3 3.7c2 1.8 2 6.8 0 8.6" strokeLinecap="round" />
    </svg>
  );
}
function MuteIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.4" aria-hidden="true">
      <path d="M2 6h2.5L8 3v10L4.5 10H2V6z" fill="currentColor" stroke="none" />
      <path d="M10.5 6l3.5 4M14 6l-3.5 4" strokeLinecap="round" />
    </svg>
  );
}
