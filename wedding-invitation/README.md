# یگانه و علی — Wedding Invitation

A premium, single-page Persian (RTL) wedding invitation. Static HTML/CSS/JS —
no backend, no build step, no framework. Deploy the folder as-is to any
static host (GitHub Pages, Netlify, Vercel, S3, etc.).

## Folder structure

```
wedding-invitation/
├── index.html            All markup / sections (lang="fa", dir="rtl")
├── css/
│   └── style.css         Full design system (light + dark theme, animations)
├── js/
│   └── main.js            All behavior (see module list below)
├── assets/
│   └── icons/
│       └── favicon.svg
└── README.md
```

## What's inside

- **Hero** — shimmering gradient name reveal (یگانه و علی), animated aurora
  gradient blobs + canvas particle field, cursor-follow spotlight (mouse
  devices), parallax glow.
- **Wedding Details** — date, time, venue, dress code, contact number, and a
  card telling guests to notify by phone 3 days ahead if they can't attend.
- **Countdown** — live days/hours/minutes/seconds to the event (Persian
  numerals), slowly drifting gradient backdrop.
- **Location** — a real embedded Google Maps iframe (no API key needed, plain
  `?q=...&output=embed` search) plus "Open in Google Maps" / "Navigate"
  buttons pointing at the address.
- **RSVP** — validated form, success state, confetti celebration, plus a note
  asking guests to call/text if they can't come.
- **Footer** — thank-you note, a Hafez couplet, share/copy-link/download-as-image,
  invitation QR code.
- **Utilities** — dark/light theme toggle (persisted), scroll progress bar,
  back-to-top, Web Share API with clipboard fallback, "Add to Calendar" (.ics
  download with a 3-day-before reminder alarm baked in), "Download as Image"
  (canvas-generated Persian keepsake card), background music toggle (persisted).

The Love Story timeline, day-of-event timeline, photo gallery, and gift
registry sections from the original template were intentionally left out for
this invitation.

## Customizing

1. **Names, date, copy** — edit the text directly in `index.html`. The
   countdown and "Add to Calendar" both read from `WEDDING_DATE` /
   `WEDDING_END` at the top of `js/main.js` (Jalali 8 Shahrivar 1405 →
   Gregorian `2026-08-30T19:00:00+03:30`) — update those if the date changes.
2. **Colors** — all palette values are CSS custom properties at the top of
   `css/style.css` (`:root` for light, `:root[data-theme="dark"]` for dark).
3. **Fonts** — Noto Nastaliq Urdu (names/titles/quotes), Vazirmatn (UI/body) —
   both loaded from Google Fonts, full Persian coverage.
4. **Map** — the `iframe` in `#location` uses a plain text-search embed
   (`google.com/maps?q=...&output=embed`, no API key). Update the `q=`
   query (and the two button `href`s below it) if the address changes.
5. **Music** — `#bgAudio` currently points at an externally-hosted MP3 URL
   (not bundled in this repo). Replace the `<source src="...">` in
   `index.html` with your own licensed/royalty-free track's URL, or a local
   file if you have the rights to redistribute it.
6. **RSVP submissions** — the form is client-side only (no backend). Wire
   `initRsvp()`'s submit handler in `js/main.js` to a form service (Formspree,
   Getform, a serverless function, etc.) to actually receive responses.
7. **Reminder window** — the "notify 3 days before" phone number and the
   calendar `VALARM:-P3D` trigger both live in `index.html` / `js/main.js`
   (`CONTACT_PHONE`, `initCalendar()`) — search for `۳ روز` to find every spot.

## Tech

- Vanilla HTML/CSS/JS — no build tooling required.
- [GSAP](https://gsap.com/) + ScrollTrigger for motion (CDN), with an
  IntersectionObserver + CSS-transition fallback if the CDN is unreachable.
- [qrcode](https://github.com/soldair/node-qrcode) for QR generation (CDN).
- Google Fonts: Noto Nastaliq Urdu, Vazirmatn.

## Accessibility & performance notes

- Respects `prefers-reduced-motion` (disables particle field, confetti, and
  shortens/removes transitions).
- Respects `prefers-color-scheme` with a manual override persisted in
  `localStorage`.
- Semantic form labels, focus-visible states, `aria-label`s on icon-only
  controls, `dir="rtl"`/`lang="fa"` set at the document root.
