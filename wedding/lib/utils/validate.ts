/** Trims, collapses whitespace, and enforces a max length for user text input. */
export function sanitizeText(value: unknown, maxLength: number): string {
  if (typeof value !== 'string') return '';
  return value.trim().replace(/\s+/g, ' ').slice(0, maxLength);
}

export function isNonEmpty(value: string): boolean {
  return value.length > 0;
}
