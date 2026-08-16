<?php
final class Validate
{
    public static function sanitizeText($value, int $maxLength): string
    {
        if (!is_string($value)) {
            return '';
        }
        $value = trim(preg_replace('/\s+/u', ' ', $value) ?? '');
        return mb_substr($value, 0, $maxLength);
    }
}
