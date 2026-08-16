<?php
/**
 * Provider-agnostic SMS sender — the PHP-side twin of lib/sms/ on the Node
 * deployment path. Swapping providers is a one-line change in config.php;
 * nothing else needs to know which one is active.
 */
final class Sms
{
    public static function notifyRsvp(array $config, string $side, string $text): void
    {
        $to = $config['recipients'][$side] ?? null;
        if (!$to) {
            throw new RuntimeException("No SMS recipient configured for side \"$side\".");
        }

        switch ($config['sms_provider']) {
            case 'kavenegar':
                self::sendKavenegar($config['kavenegar'], $to, $text);
                break;
            case 'melipayamak':
                self::sendMelipayamak($config['melipayamak'], $to, $text);
                break;
            case 'farazsms':
                self::sendFarazSms($config['farazsms'], $to, $text);
                break;
            default:
                error_log("[sms:none] would send to $to: $text");
        }
    }

    /** https://kavenegar.com/rest.html */
    private static function sendKavenegar(array $cfg, string $to, string $text): void
    {
        if (empty($cfg['api_key'])) {
            throw new RuntimeException('Kavenegar api_key is not configured.');
        }
        $params = array_filter([
            'receptor' => $to,
            'message' => $text,
            'sender' => $cfg['sender'] ?: null,
        ]);
        $url = 'https://api.kavenegar.com/v1/' . rawurlencode($cfg['api_key'])
            . '/sms/send.json?' . http_build_query($params);
        self::request('GET', $url);
    }

    /** https://www.melipayamak.com/api/ */
    private static function sendMelipayamak(array $cfg, string $to, string $text): void
    {
        if (empty($cfg['username']) || empty($cfg['password']) || empty($cfg['sender'])) {
            throw new RuntimeException('Melipayamak credentials are not fully configured.');
        }
        self::request('POST', 'https://rest.payamak-panel.com/api/SendSMS/SendSMS', [
            'username' => $cfg['username'],
            'password' => $cfg['password'],
            'to' => $to,
            'from' => $cfg['sender'],
            'text' => $text,
            'isFlash' => false,
        ]);
    }

    /** FarazSMS / ippanel — https://ippanel.com/ */
    private static function sendFarazSms(array $cfg, string $to, string $text): void
    {
        if (empty($cfg['api_key']) || empty($cfg['sender'])) {
            throw new RuntimeException('FarazSMS credentials are not fully configured.');
        }
        self::request('POST', 'https://edge.ippanel.com/v1/api/send', [
            'sending_type' => 'webservice',
            'from_number' => $cfg['sender'],
            'message' => $text,
            'params' => ['recipients' => [$to]],
        ], ['Authorization: AccessKey ' . $cfg['api_key']]);
    }

    private static function request(string $method, string $url, ?array $jsonBody = null, array $extraHeaders = []): void
    {
        $headers = $jsonBody !== null ? array_merge(['Content-Type: application/json'], $extraHeaders) : $extraHeaders;
        $ctx = stream_context_create([
            'http' => [
                'method' => $method,
                'header' => implode("\r\n", $headers),
                'content' => $jsonBody !== null ? json_encode($jsonBody) : null,
                'timeout' => 10,
                'ignore_errors' => true,
            ],
        ]);
        $result = @file_get_contents($url, false, $ctx);
        if ($result === false) {
            throw new RuntimeException("SMS request failed: $url");
        }
    }
}
