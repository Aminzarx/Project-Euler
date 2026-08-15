# Color System

All tokens live in two places that must stay in sync:

- `tailwind.config.ts` — `theme.extend.colors` (for className usage)
- `lib/theme.ts` — plain JS values (for canvas effects and inline styles that can't use Tailwind classes)

## Palette

| Token | Hex | Use |
|---|---|---|
| `ivory-50` | `#fffdf8` | Page background base |
| `ivory-100` | `#fbf6ea` | Secondary background, card fallback |
| `champagne-100/200/300` | `#f4e9d8` / `#e9d6b4` / `#ddc191` | Borders, subtle fills, gradients |
| `rosegold-200..500` | `#e8c3b9` → `#b8735f` | Warm accents, petals, hearts |
| `persiangold-300..600` | `#e3c081` → `#8f6b2f` | Primary accent — CTAs, headings, glow, focus states |
| `emerald-400..600` | `#4d8f7b` → `#2c5548` | Secondary accent — nav icons, success states |
| `ink-400..700` | `#7a7266` → `#2a2620` | Text hierarchy (400 = muted, 700 = headings) |

## Rules

1. **No pure black or pure white.** Text uses `ink-*`, backgrounds use `ivory-*`/`champagne-*`.
2. **Gradients stay subtle.** Only two gradients are defined (`bg-radial-glow`, `bg-silk-gradient`);
   reuse them rather than inventing new ones. Never use a gradient with more than ~15% lightness delta
   between stops.
3. **Gold is the accent, not the base.** `persiangold` drives emphasis (names, active states, focus
   rings, glow shadows) — it should never be a large flat background fill.
4. **Glass surfaces** use the `.glass-panel` utility (`app/globals.css`), not ad-hoc `bg-white/50`.
5. **Contrast**: body text (`ink-600` on `ivory-50`) is ~9.5:1 — keep any new text color at AA (4.5:1)
   or better against its background.

## Shadows

- `shadow-glass` — resting card elevation
- `shadow-glass-lg` — modals, the envelope
- `shadow-gold` — focus/emphasis glow, used sparingly (RSVP cards, active nav)
