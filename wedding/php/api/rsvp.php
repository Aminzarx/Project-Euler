<?php
declare(strict_types=1);

require __DIR__ . '/../lib/Store.php';
require __DIR__ . '/../lib/Sms.php';
require __DIR__ . '/../lib/Validate.php';

header('Content-Type: application/json; charset=utf-8');

if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed'], JSON_UNESCAPED_UNICODE);
    exit;
}

$body = json_decode((string) file_get_contents('php://input'), true);
if (!is_array($body)) {
    http_response_code(400);
    echo json_encode(['error' => 'بدنه‌ی درخواست نامعتبر است.'], JSON_UNESCAPED_UNICODE);
    exit;
}

$status = is_string($body['status'] ?? null) ? $body['status'] : '';
$fullName = Validate::sanitizeText($body['fullName'] ?? null, 80);
$message = Validate::sanitizeText($body['message'] ?? null, 400);
$side = is_string($body['side'] ?? null) ? $body['side'] : '';

$validStatuses = ['attending', 'maybe', 'declined'];
$validSides = ['bride', 'groom'];

if (!in_array($status, $validStatuses, true)) {
    http_response_code(422);
    echo json_encode(['error' => 'وضعیت حضور نامعتبر است.'], JSON_UNESCAPED_UNICODE);
    exit;
}
if ($fullName === '') {
    http_response_code(422);
    echo json_encode(['error' => 'نام و نام خانوادگی الزامی است.'], JSON_UNESCAPED_UNICODE);
    exit;
}
if (!in_array($side, $validSides, true)) {
    http_response_code(422);
    echo json_encode(['error' => 'انتخاب طرف عروس یا داماد الزامی است.'], JSON_UNESCAPED_UNICODE);
    exit;
}

$entry = [
    'id' => bin2hex(random_bytes(8)),
    'status' => $status,
    'fullName' => $fullName,
    'side' => $side,
    'message' => $message !== '' ? $message : null,
    'createdAt' => gmdate('c'),
];
Store::append('rsvp.json', $entry);

$config = require __DIR__ . '/../config.php';
$statusLabels = [
    'attending' => 'حضور می‌یابد',
    'maybe' => 'شاید حضور یابد',
    'declined' => 'متأسفانه نمی‌تواند حضور یابد',
];
$sideLabel = $side === 'bride' ? 'سمت عروس' : 'سمت داماد';
$text = "پاسخ دعوت‌نامه ($sideLabel)\n{$fullName} — {$statusLabels[$status]}"
    . ($message !== '' ? "\nپیام: {$message}" : '');

try {
    Sms::notifyRsvp($config, $side, $text);
} catch (Throwable $e) {
    // The RSVP itself is already persisted — an SMS delivery failure (missing
    // provider credentials, network) must not fail the request.
    error_log('[rsvp] SMS notification failed: ' . $e->getMessage());
}

http_response_code(201);
echo json_encode(['entry' => $entry], JSON_UNESCAPED_UNICODE);
