import type { Metadata, Viewport } from 'next';
import { Noto_Nastaliq_Urdu, Vazirmatn, Cormorant_Garamond } from 'next/font/google';
import './globals.css';

const calligraphy = Noto_Nastaliq_Urdu({
  subsets: ['arabic'],
  weight: ['400', '700'],
  variable: '--font-calligraphy',
  display: 'swap',
});

const persian = Vazirmatn({
  subsets: ['arabic', 'latin'],
  weight: ['300', '400', '500', '600', '700'],
  variable: '--font-persian',
  display: 'swap',
});

const serif = Cormorant_Garamond({
  subsets: ['latin'],
  weight: ['400', '500', '600', '700'],
  style: ['normal', 'italic'],
  variable: '--font-serif',
  display: 'swap',
});

const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? 'https://zarandix.ir/Wedding';

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: 'علی و یگانه | دعوت‌نامه عروسی',
  description: 'با شکوه و عشق، شما را به جشن ازدواج علی و یگانه دعوت می‌کنیم.',
  openGraph: {
    title: 'علی ❤ یگانه',
    description: 'دعوت‌نامه‌ی دیجیتال عروسی علی و یگانه',
    url: siteUrl,
    siteName: 'Ali & Yegane',
    locale: 'fa_IR',
    type: 'website',
  },
  robots: {
    index: true,
    follow: true,
  },
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  themeColor: '#fbf6ea',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fa" dir="rtl" className={`${calligraphy.variable} ${persian.variable} ${serif.variable}`}>
      <body className="font-persian">
        <a
          href="#main-content"
          className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:start-4 focus:z-[100] focus:rounded-full focus:bg-ink-700 focus:px-5 focus:py-2.5 focus:text-ivory-50 focus:shadow-glass-lg"
        >
          رفتن به محتوای اصلی
        </a>
        {children}
      </body>
    </html>
  );
}
