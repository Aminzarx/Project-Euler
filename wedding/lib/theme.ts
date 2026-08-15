/**
 * Central design tokens. Mirrors tailwind.config.ts so non-Tailwind
 * consumers (canvas effects, inline styles) share the same palette.
 * See /docs/color-system.md for usage guidance.
 */
export const theme = {
  color: {
    ivory: '#fffdf8',
    champagne: '#e9d6b4',
    roseGold: '#dba896',
    persianGold: '#cda054',
    persianGoldBright: '#e3c081',
    emerald: '#3a6f5e',
    ink: '#3d382f',
  },
  gradient: {
    silk: 'linear-gradient(160deg, #fffdf8 0%, #fbf6ea 35%, #f4e9d8 65%, #e9d6b4 100%)',
    radialGlow: 'radial-gradient(60% 60% at 50% 40%, rgba(227,192,129,0.28) 0%, rgba(227,192,129,0) 70%)',
  },
  wedding: {
    groomName: 'علی',
    brideName: 'یگانه',
    dateFa: '۸ شهریور ۱۴۰۵',
    // Gregorian equivalent used for the machine-readable countdown target.
    // 8 Shahrivar 1405 = 30 August 2026.
    isoDateTime: '2026-08-30T19:00:00+03:30',
    timeFa: '۱۹:۰۰',
    venueName: 'سرای خاطره پینود',
    venueAddress: 'جاده باغرود',
    links: {
      balad: 'https://balad.ir/p/587omoL1kiveCI',
      neshan: 'https://nshn.ir/02_b1LJkGJgeSN',
      googleMaps: 'https://maps.app.goo.gl/SJ4LN5naMFcPZwRy5',
    },
  },
} as const;

export type Theme = typeof theme;
