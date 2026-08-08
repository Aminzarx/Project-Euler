/** Canonical phone-number form used for storage and lookup: Persian/Arabic-Indic digits
 *  mapped to ASCII, whitespace/dashes/parens stripped, then trimmed. Not a full E.164
 *  normalizer (no country-code inference) — just enough that formatting variations of the
 *  same number can never collide-bypass the unique constraint. */
export function normalizePhoneNumber(raw: string): string {
  const PERSIAN_ARABIC_DIGITS: Record<string, string> = {
    '۰': '0', '۱': '1', '۲': '2', '۳': '3', '۴': '4', '۵': '5', '۶': '6', '۷': '7', '۸': '8', '۹': '9',
    '٠': '0', '١': '1', '٢': '2', '٣': '3', '٤': '4', '٥': '5', '٦': '6', '٧': '7', '٨': '8', '٩': '9'
  };
  return raw
    .trim()
    .replace(/[۰-۹٠-٩]/g, (d) => PERSIAN_ARABIC_DIGITS[d] ?? d)
    .replace(/[\s\-()]/g, '');
}
