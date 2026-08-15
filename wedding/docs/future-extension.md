# Future Extension Guide

## Swap the data store for a real database

`lib/store/guestbookStore.ts` and `lib/store/rsvpStore.ts` implement the
`GuestbookRepository`/`RsvpRepository` interfaces from `lib/store/types.ts` using
JSON files (fine for local dev / a self-hosted VPS with a persistent disk, **not**
safe on serverless platforms with ephemeral/read-only filesystems, e.g. Vercel).

To move to Postgres/Prisma/Supabase/etc.:

1. Write a new module (e.g. `lib/store/guestbookStore.prisma.ts`) implementing the same
   interface.
2. Swap the export in the two files above — nothing in `app/api/*` or the components
   needs to change, since they only depend on the interface shape.

## Add a new SMS provider

1. Create `lib/sms/providers/<name>.ts` exporting `create<Name>Provider(): SmsProvider`
   (see the existing three for the shape).
2. Add a `case '<name>':` branch in `resolveProvider()` in `lib/sms/index.ts`.
3. Add the required env vars to `.env.example` and document them here.
4. Set `SMS_PROVIDER=<name>` in the deployment environment.

No other file needs to change — `notifyRsvpBySms()` is provider-agnostic.

## Add a new section to the invitation

1. Build the component under the appropriate `components/<domain>/` folder (create a
   new domain folder if it doesn't fit an existing one).
2. Wrap its root element in `<SectionReveal>` for the standard scroll-reveal.
3. Import and place it in `components/WeddingExperience.tsx`, inside `<main>`.
4. If it introduces new design tokens (a color, a spacing value), add them to
   `tailwind.config.ts` **and** `lib/theme.ts`, and note them in
   `/docs/color-system.md`.

## Real wedding photo / video背景

The background is currently pure vector/canvas (no photography) by design — it keeps
the bundle tiny and avoids a mismatched photo style. If a couple's photo is added
later (e.g. as a hero background), use `next/image` with `priority` only on the first
viewport image, and keep the same soft-glow/blur treatment already used elsewhere so it
doesn't visually clash with the vector ornament.

## Internationalization

Everything is hardcoded Persian/RTL by design (per the brief). If an English/LTR
version is ever needed, the cleanest path is a `[locale]` route segment with the
existing components made locale-aware via a small `dictionary.ts`, rather than
retrofitting `dir`/`lang` toggling into the current single-locale layout.
