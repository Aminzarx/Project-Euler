# Folder Structure

```
wedding/
├── app/
│   ├── layout.tsx          # fonts, <html dir="rtl">, metadata
│   ├── page.tsx             # renders <WeddingExperience />
│   ├── globals.css          # Tailwind layers + .glass-panel/.gold-hairline utilities
│   ├── icon.svg              # favicon
│   └── api/
│       ├── guestbook/route.ts
│       └── rsvp/route.ts
├── components/
│   ├── WeddingExperience.tsx # top-level orchestrator (state: envelope opened?)
│   ├── Footer.tsx
│   ├── ui/                   # GlassCard, GlowButton, SectionReveal
│   ├── background/           # AmbientBackground (canvas)
│   ├── effects/               # CursorGlow, Fireworks (canvas)
│   ├── intro/                 # Envelope
│   ├── invitation/            # NamesHero, FloralOrnament, InvitationText
│   ├── greeting/               # PersonalGreeting (?name=)
│   ├── info/                   # InfoCards, Countdown
│   ├── navigation/             # MapCards
│   ├── guestbook/               # Guestbook
│   ├── rsvp/                     # RSVPSection, RSVPModal
│   └── audio/                    # MusicPlayer
├── lib/
│   ├── theme.ts               # non-Tailwind design tokens + wedding data (names, date, links)
│   ├── motion.ts               # shared Framer Motion variants/springs
│   ├── config.ts                # BASE_PATH, apiUrl() helper
│   ├── hooks/                    # useReducedMotion, useCountdown, useInView
│   ├── utils/                     # cn(), validate()
│   ├── store/                      # GuestbookRepository / RsvpRepository + file-based impl
│   └── sms/                         # provider-agnostic SMS sending
│       └── providers/                # kavenegar, melipayamak, farazsms, none
├── data/                        # JSON-file persistence (git-ignored, dev/self-host only)
├── public/audio/                 # wedding-song.mp3 goes here (not committed)
├── docs/                          # you are here
└── ...config files (tailwind, next, tsconfig, postcss)
```

## Conventions

- **One component, one file, named export** (not default) — makes barrel-free imports
  explicit and greppable (`import { GlassCard } from '@/components/ui/GlassCard'`).
- **`lib/` never imports from `components/`** — keep the dependency direction one-way.
- **API routes are thin**: validate input, call a `lib/store` or `lib/sms` function,
  return JSON. No business logic inline in `route.ts`.
- **Repository pattern for persistence** (`lib/store/types.ts` interfaces +
  `guestbookStore.ts`/`rsvpStore.ts` implementations) mirrors the pattern already used
  in this monorepo's `backend/src/repositories`, so swapping the file-based store for a
  real database later is a drop-in change — see `/docs/future-extension.md`.
