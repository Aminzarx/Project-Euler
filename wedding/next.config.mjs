// STATIC_EXPORT=true switches this to a static HTML/JS/CSS build (no Node server needed —
// for FTP-only hosting). See scripts/build-static.sh, which sets this and temporarily moves
// app/api out of the way (Route Handlers that do file I/O aren't exportable to static; the
// PHP equivalents under php/api/ take over instead). The default (unset) build keeps the
// Next.js API routes for the pm2/VPS deployment path — see docs/README.md for both.
const isStaticExport = process.env.STATIC_EXPORT === 'true';

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  basePath: '/Wedding',
  ...(isStaticExport ? { output: 'export' } : {}),
  images: {
    formats: ['image/avif', 'image/webp'],
    unoptimized: isStaticExport,
  },
  experimental: {
    optimizePackageImports: ['framer-motion'],
  },
};

export default nextConfig;
