import { theme } from '@/lib/theme';

export function Footer() {
  return (
    <footer className="relative z-10 flex flex-col items-center gap-3 px-6 py-16 text-center">
      <div className="gold-hairline w-24" />
      <p className="font-calligraphy text-2xl text-persiangold-400">
        {theme.wedding.groomName} <span className="text-rosegold-400">❤</span> {theme.wedding.brideName}
      </p>
      <p className="font-persian text-xs tracking-widest text-ink-400">با عشق، منتظر شما هستیم</p>
    </footer>
  );
}
