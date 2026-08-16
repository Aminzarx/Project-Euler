<?php
/**
 * Minimal JSON-file list persistence — the PHP-side twin of
 * lib/store/jsonFileStore.ts on the Node deployment path. Same shape,
 * same file names, so RSVP/guestbook entries look identical either way.
 */
final class Store
{
    public static function readList(string $file): array
    {
        $path = self::path($file);
        if (!is_file($path)) {
            return [];
        }
        $raw = file_get_contents($path);
        $data = json_decode((string) $raw, true);
        return is_array($data) ? $data : [];
    }

    public static function append(string $file, array $item): array
    {
        $list = self::readList($file);
        $list[] = $item;
        $dir = dirname(self::path($file));
        if (!is_dir($dir)) {
            mkdir($dir, 0775, true);
        }
        file_put_contents(
            self::path($file),
            json_encode($list, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE)
        );
        return $item;
    }

    private static function path(string $file): string
    {
        return __DIR__ . '/../data/' . $file;
    }
}
