# Elena & Daniel — Wedding Invitation

A premium, single-page digital wedding invitation. Static HTML/CSS/JS —
no backend, no build step, no framework. Deploy the folder as-is to any
static host (GitHub Pages, Netlify, Vercel, S3, etc.).

## Folder structure

```
wedding-invitation/
├── index.html            All markup / sections
├── css/
│   └── style.css         Full design system (light + dark theme, animations)
├── js/
│   └── main.js            All behavior (see module list below)
├── assets/
│   ├── icons/
│   │   └── favicon.svg
│   └── audio/
│       └── README.md      How to add your own background track
└── README.md
```

## What's inside

- **Hero** — animated name reveal, canvas particle field, parallax glow.
- **Love Story** — animated vertical timeline.
- **Wedding Details** — date, time, venue, dress code, contact, RSVP deadline.
- **Countdown** — live days/hours/minutes/seconds to the ceremony.
- **Gallery** — responsive grid + keyboard-navigable lightbox.
- **Order of the Evening** — arrival through farewell.
- **Location** — embedded Google Map + "Open in Maps" / "Navigate" buttons.
- **RSVP** — validated form, success state, confetti celebration.
- **Gift Registry** — bank details, copy-to-clipboard IBAN, QR code.
- **Footer** — thank-you note, quote (English + Persian), share/copy/download,
  invitation QR code, social links.
- **Utilities** — dark/light theme toggle (persisted), scroll progress bar,
  back-to-top, Web Share API with clipboard fallback, "Add to Calendar" (.ics
  download), "Download as Image" (canvas-generated keepsake card), background
  music toggle (persisted, silent until you add a track).

## Customizing for your own wedding

1. **Names, date, copy** — edit the text directly in `index.html`. The
   countdown and "Add to Calendar" both read from `WEDDING_DATE` /
   `WEDDING_END` at the top of `js/main.js` — update those to match.
2. **Colors** — all palette values are CSS custom properties at the top of
   `css/style.css` (`:root` for light, `:root[data-theme="dark"]` for dark).
3. **Photos** — the gallery currently uses elegant gradient placeholders
   (`GALLERY` array in `js/main.js`) instead of stock/lorem-ipsum imagery.
   Swap the `tone` gradients for real photos by changing
   `background-image:${g.tone}` to `url('assets/images/your-photo.jpg')` in
   `initGallery()`, and add your images under `assets/images/`.
4. **Map** — replace the `Lake Como, Italy` query in the map `iframe` and the
   "Open in Google Maps" / "Navigate" links with your venue.
5. **Music** — see `assets/audio/README.md`.
6. **RSVP submissions** — the form is client-side only (no backend). Wire
   `initRsvp()`'s submit handler in `js/main.js` to a form service (Formspree,
   Getform, a serverless function, etc.) to actually receive responses.
7. **Gift QR / IBAN** — update the IBAN/BIC in `index.html` and the string
   passed to `QRCode.toCanvas` in `initQrCodes()`.

## Tech

- Vanilla HTML/CSS/JS — no build tooling required.
- [GSAP](https://gsap.com/) + ScrollTrigger for motion (CDN).
- [qrcode](https://github.com/soldair/node-qrcode) for QR generation (CDN).
- Google Fonts: Cormorant Garamond & Cinzel (display), Jost (UI), Vazirmatn
  (Persian text).

## Accessibility & performance notes

- Respects `prefers-reduced-motion` (disables particle field, confetti, and
  shortens/removes transitions).
- Respects `prefers-color-scheme` with a manual override persisted in
  `localStorage`.
- Semantic form labels, focus-visible states, keyboard-navigable lightbox
  (Esc / Arrow keys), `aria-label`s on icon-only controls.
- Map iframe is lazy-loaded; no large images are shipped by default.
