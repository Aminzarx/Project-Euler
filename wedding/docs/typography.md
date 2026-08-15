# Typography

Three font families, loaded via `next/font/google` in `app/layout.tsx` and exposed as
CSS variables consumed by `tailwind.config.ts` (`fontFamily.calligraphy/persian/serif`).

| Role | Font | Tailwind class | Where |
|---|---|---|---|
| Persian display / calligraphy | Noto Nastaliq Urdu | `font-calligraphy` | Names hero, section titles, modal headings |
| Persian body | Vazirmatn | `font-persian` (default on `<body>`) | All UI copy, forms, cards |
| English / Latin accents | Cormorant Garamond (italic) | `font-serif` | Small English labels ("WE ARE GETTING MARRIED"), invitation verse |

## Hierarchy

- **Hero names**: `text-5xl` → `text-8xl` (responsive), calligraphy, `persiangold-500`
- **Section titles**: `text-3xl` → `text-4xl`, calligraphy
- **Eyebrow labels**: `text-sm`, `tracking-[0.25em]`, `ink-400`, uppercase-feel via letter-spacing (Persian has no case)
- **Body copy**: `text-base`/`text-lg`, `leading-relaxed` to `leading-[2.1]` for the invitation verse
- **Micro copy** (form labels, captions): `text-sm`, `ink-500`

## Rules

1. RTL is the default document direction (`<html dir="rtl">`). Use logical Tailwind utilities
   (`ms-`, `me-`, `ps-`, `pe-`, `start-`, `end-`) instead of `ml-`/`mr-`/`left-`/`right-` so the
   layout doesn't need per-direction overrides.
2. Never set `font-calligraphy` below `text-xl` — Nastaliq loses legibility at small sizes.
3. Line height on any paragraph longer than one line should be `leading-relaxed` or looser.
4. Numbers (countdown, dates) use `tabular-nums` so digits don't jitter horizontally when they change.
