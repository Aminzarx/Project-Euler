# Ali & Yegane — Wedding Invitation Docs

A cinematic, interactive Persian wedding invitation built with Next.js (App Router),
TypeScript, Tailwind CSS and Framer Motion, deployed at `zarandix.ir/Wedding`.

## Contents

- [Folder Structure](./folder-structure.md)
- [Color System](./color-system.md)
- [Typography](./typography.md)
- [Animation System](./animation-system.md)
- [Motion Guidelines](./motion-guidelines.md)
- [Component Library](./component-library.md)
- [Future Extension Guide](./future-extension.md)

## Quick start

```bash
cd wedding
npm install
cp .env.example .env.local   # configure SMS provider + recipients
npm run dev
```

Open `http://localhost:3000/Wedding`. The app is served under the `/Wedding` base path
in every environment (see `next.config.mjs`) to match the production URL.

## Keep this folder current

Whenever a section, animation, or system-level convention changes, update the
relevant doc in the same change. These docs are the source of truth for anyone
extending the experience later — including a future AI assistant.
