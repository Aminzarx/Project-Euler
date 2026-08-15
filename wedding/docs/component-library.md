# Component Library

## UI primitives (`components/ui/`)

- **`GlassCard`** — the base frosted-glass surface (`.glass-panel` + rounded corners +
  `shadow-glass`). Pass `glow` for a gold shadow variant. Used by info cards, nav cards,
  RSVP choices, guestbook cards.
- **`GlowButton`** — the primary button: spring hover/press/focus, three variants
  (`solid`, `outline`, `ghost`).
- **`SectionReveal`** — scroll-triggered `fadeUp` wrapper; every top-level `<section>`
  on the page uses it.

## Background & effects (`components/background/`, `components/effects/`)

- **`AmbientBackground`** — the always-on canvas layer (bokeh + drifting petals + a
  slow light sweep). Mounted once at the top of `WeddingExperience`.
- **`CursorGlow`** — desktop: soft golden light tracking the pointer. Touch: expanding
  ripple at each tap. Pure `pointerType` branching, one listener set.
- **`Fireworks`** — fires once (`trigger` prop flips `false → true`), three staggered
  bursts, auto-clears after ~3.2s.

## Intro (`components/intro/`)

- **`Envelope`** <a name="envelope"></a> — the full-screen intro. Internal stage machine
  `idle → cracking → opening → done`. Calls `onOpen()` once, at the start of the `done`
  transition, so the parent can start music/fireworks in sync with the visual. Unmounts
  itself after exit animation completes (`AnimatePresence`).

## Invitation (`components/invitation/`)

- **`NamesHero`** — the "Ali ❤ Yegane" centerpiece. See `/docs/animation-system.md` for
  how the calligraphy reveal works.
- **`FloralOrnament`** — decorative SVG line-art flourish behind the names, mirrored
  left/right, no image assets.
- **`InvitationText`** — the emotional copy block + Quranic verse.

## Info & navigation

- **`InfoCards`** — date/time/venue, 3-up grid of `GlassCard`.
- **`Countdown`** — live countdown to `theme.wedding.isoDateTime`, digit-flip animation
  via `AnimatePresence mode="popLayout"` per character.
- **`MapCards`** — Balad / Neshan / Google Maps links, `cardHover` variants.

## Guestbook & RSVP

- **`Guestbook`** — form + animated card grid, backed by `/api/guestbook`
  (`GuestbookRepository`, file-based by default).
- **`RSVPSection`** — three status cards (attending / maybe / declined), each opens
  **`RSVPModal`** with full name, side (bride/groom), optional message. Submits to
  `/api/rsvp`, which persists the response and fires an SMS via the provider configured
  in `lib/sms/`.

## Audio

- **`MusicPlayer`** — floating pill with play/pause/mute. Exposes an imperative
  `start()` via `forwardRef` + `useImperativeHandle`, called by `WeddingExperience` the
  moment the envelope opens (the only guaranteed user gesture, required for autoplay).
  Position persists to `localStorage`; volume changes always fade over `FADE_MS` (1.2s).
