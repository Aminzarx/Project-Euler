# Motion Guidelines

Principles that every animation in this project should satisfy:

1. **Spring over linear.** Use `type: 'spring'` (see `lib/motion.ts`) for anything that
   represents a physical object moving (cards, the envelope, the modal). Reserve
   `ease`-based tweens for opacity/color fades where "physicality" doesn't apply.
2. **Nothing pops in instantly.** Every element entering the viewport gets at least an
   opacity transition; most also get a small `y` or `scale` offset. Minimum duration
   ~0.4s, hero-level moments ~1–1.5s.
3. **Motion has a purpose.** It should either (a) direct attention to what just became
   interactive, (b) communicate state change (success, error, open/closed), or (c) add
   ambient life to otherwise static backgrounds. If an animation doesn't do one of
   these, cut it.
4. **Respect `prefers-reduced-motion`.** `lib/hooks/useReducedMotion.ts` is checked by
   every non-trivial animated component. Reduced motion should still deliver the same
   *information* (an opened envelope, a revealed name) — just via a fade/instant swap
   instead of a spring/canvas animation.
5. **60fps or don't ship it.** Prefer `transform`/`opacity` (compositor-only) over
   properties that trigger layout (`width`, `top`, `left`) for anything that runs every
   frame. The one intentional exception is the calligraphy name reveal, which animates
   `width` on a masking element — acceptable because it's a single small element, runs
   once, and finishes in ~1.3s.
6. **Stagger, don't synchronize.** Groups of similar elements (info cards, nav cards,
   guestbook entries) reveal with a small stagger (`staggerChildren: 0.09` via
   `staggerContainer`) rather than appearing all at once — it reads as more intentional
   and less mechanical.
7. **One big moment at a time.** The envelope open, the calligraphy reveal, and the
   fireworks are sequenced (not simultaneous) so the guest's attention is never split.

## Timing budget

- Micro-interactions (button press, focus): 150–300ms
- Card reveals, hover lifts: 300–500ms
- Section entrances: 600–900ms
- Hero/envelope choreography: 1.5–3s total, broken into named stages
