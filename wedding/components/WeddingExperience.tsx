'use client';

import { Suspense, useEffect, useRef, useState } from 'react';
import { AmbientBackground } from '@/components/background/AmbientBackground';
import { CursorGlow } from '@/components/effects/CursorGlow';
import { Fireworks } from '@/components/effects/Fireworks';
import { Envelope } from '@/components/intro/Envelope';
import { NamesHero } from '@/components/invitation/NamesHero';
import { InvitationText } from '@/components/invitation/InvitationText';
import { PersonalGreeting } from '@/components/greeting/PersonalGreeting';
import { InfoCards } from '@/components/info/InfoCards';
import { Countdown } from '@/components/info/Countdown';
import { MapCards } from '@/components/navigation/MapCards';
import { Guestbook } from '@/components/guestbook/Guestbook';
import { RSVPSection } from '@/components/rsvp/RSVPSection';
import { MusicPlayer, type MusicPlayerHandle } from '@/components/audio/MusicPlayer';
import { Footer } from '@/components/Footer';

/**
 * Orchestrates the whole guest journey: locks scroll behind the sealed
 * envelope, then on open starts music, fires the fireworks flourish once,
 * and unlocks the invitation underneath.
 */
export function WeddingExperience() {
  const [opened, setOpened] = useState(false);
  const musicRef = useRef<MusicPlayerHandle>(null);

  useEffect(() => {
    document.body.style.overflow = opened ? '' : 'hidden';
    return () => {
      document.body.style.overflow = '';
    };
  }, [opened]);

  const handleOpen = () => {
    setOpened(true);
    musicRef.current?.start();
  };

  return (
    <>
      <AmbientBackground />
      <CursorGlow />
      <Envelope onOpen={handleOpen} />
      <Fireworks trigger={opened} />

      <main id="main-content" className="relative z-20">
        <Suspense fallback={null}>
          <PersonalGreeting />
        </Suspense>
        <NamesHero />
        <InvitationText />
        <InfoCards />
        <Countdown />
        <MapCards />
        <Guestbook />
        <RSVPSection />
        <Footer />
      </main>

      <MusicPlayer ref={musicRef} />
    </>
  );
}
