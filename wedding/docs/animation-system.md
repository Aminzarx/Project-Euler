# Animation System

Shared primitives live in `lib/motion.ts`. Prefer these over inventing new transitions
inline — consistency is what makes the experience feel designed, not assembled.

## Springs (`lib/motion.ts`)

| Name | stiffness/damping | Use |
|---|---|---|
| `springSilk` | 90 / 20 | Large, slow, weighted movement (envelope, hero) |
| `springSnappy` | 260 / 22 | Card hover/press, buttons, quick feedback |
| `springGentle` | 60 / 18 | Ambient, looping motion |

## Variants

- `fadeUp` — the default scroll-reveal (opacity 0→1, y 28→0). Accepts a `custom` index for stagger delay.
- `fadeIn` — opacity-only, for large background elements where a position shift would be distracting.
- `scaleIn` — for elements that should feel like they "pop" in (badges, icons).
- `staggerContainer` — wrap a `motion.div` around a list of `fadeUp`/`scaleIn` children.
- `cardHover` — `rest` / `hover` / `press` states for any clickable card (nav cards, RSVP choices).

## Canvas-based effects

`AmbientBackground`, `Fireworks`, and the touch-ripple layer in `CursorGlow` use raw
`<canvas>` + `requestAnimationFrame` rather than Framer Motion, because they render
dozens of independent particles — doing that with DOM nodes would cost far more in
layout/paint. Rules for any new canvas effect:

- Single `rAF` loop per canvas, cancelled on unmount.
- Resize listener recalculates `width`/`height`/`dpr`, capped at `devicePixelRatio` ≤ 2.
- Respect `useReducedMotion()` — either skip entirely (`Fireworks`, `CursorGlow`) or
  render one static frame instead of animating (`AmbientBackground`).

## Section reveals

`components/ui/SectionReveal.tsx` wraps a section in `whileInView` + `fadeUp`, triggered
once (`viewport={{ once: true }}`) so re-scrolling past a section doesn't re-fire it.
Every top-level section on the page uses this wrapper for a consistent "float up as you
scroll" rhythm.

## The envelope sequence

`components/intro/Envelope.tsx` is the one bespoke, multi-stage animation in the app
(`idle → cracking → opening → done`). It's kept self-contained and heavily commented
inline rather than generalized, because it's genuinely a one-off — see
`/docs/component-library.md#envelope`.
