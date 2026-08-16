<?php
declare(strict_types=1);

require __DIR__ . '/../lib/Store.php';
require __DIR__ . '/../lib/Validate.php';

header('Content-Type: application/json; charset=utf-8');

$method = $_SERVER['REQUEST_METHOD'] ?? '';

if ($method === 'GET') {
    $entries = Store::readList('guestbook.json');
    usort($entries, fn($a, $b) => strcmp($b['createdAt'] ?? '', $a['createdAt'] ?? ''));
    echo json_encode(['entries' => $entries], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($method === 'POST') {
    $body = json_decode((string) file_get_contents('php://input'), true);
    if (!is_array($body)) {
        http_response_code(400);
        echo json_encode(['error' => 'بدنه‌ی درخواست نامعتبر است.'], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $name = Validate::sanitizeText($body['name'] ?? null, 60);
    $message = Validate::sanitizeText($body['message'] ?? null, 400);

    if ($name === '' || $message === '') {
        http_response_code(422);
        echo json_encode(['error' => 'نام و پیام الزامی هستند.'], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $entry = [
        'id' => bin2hex(random_bytes(8)),
        'name' => $name,
        'message' => $message,
        'createdAt' => gmdate('c'),
    ];
    Store::append('guestbook.json', $entry);
    http_response_code(201);
    echo json_encode(['entry' => $entry], JSON_UNESCAPED_UNICODE);
    exit;
}

http_response_code(405);
echo json_encode(['error' => 'Method not allowed'], JSON_UNESCAPED_UNICODE);
