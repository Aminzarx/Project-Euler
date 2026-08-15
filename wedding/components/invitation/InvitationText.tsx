import { SectionReveal } from '@/components/ui/SectionReveal';

export function InvitationText() {
  return (
    <SectionReveal className="relative mx-auto max-w-2xl px-6 py-16 text-center sm:py-24">
      <p className="font-serif text-lg italic leading-relaxed text-ink-500 sm:text-xl">
        &ldquo;و از نشانه‌های او این است که همسرانی از جنس خودتان برایتان آفرید تا در کنار آنان آرامش یابید&rdquo;
      </p>
      <div className="gold-hairline mx-auto my-8 w-24" />
      <p className="text-balance font-persian text-xl leading-[2.1] text-ink-600 sm:text-2xl">
        با شکوهی از عشق و سپاسی از خداوند، دستان یکدیگر را برای همیشه در دست می‌گیریم.
        <br />
        حضور گرم و صمیمانه‌ی شما، این جشن را برای ما به یادماندنی‌تر خواهد کرد.
      </p>
    </SectionReveal>
  );
}
