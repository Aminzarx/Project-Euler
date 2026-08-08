import { normalizePhoneNumber } from '../utils/normalizePhoneNumber';

describe('normalizePhoneNumber', () => {
  it('converts Persian digits to ASCII', () => {
    expect(normalizePhoneNumber('۰۹۱۲۱۲۳۴۵۶۷')).toBe('09121234567');
  });

  it('converts Arabic-Indic digits to ASCII', () => {
    expect(normalizePhoneNumber('٠٩١٢١٢٣٤٥٦٧')).toBe('09121234567');
  });

  it('trims leading and trailing whitespace', () => {
    expect(normalizePhoneNumber('  09121234567  ')).toBe('09121234567');
  });

  it('strips dashes, spaces, and parentheses', () => {
    expect(normalizePhoneNumber('(091) 212-34567')).toBe('09121234567');
  });

  it('normalizes two differently-formatted inputs representing the same number identically', () => {
    const a = normalizePhoneNumber('۰۹۱۲۱۲۳۴۵۶۷');
    const b = normalizePhoneNumber('09121234567');
    expect(a).toBe(b);
  });
});
