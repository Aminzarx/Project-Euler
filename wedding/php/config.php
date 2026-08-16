<?php
/**
 * SMS + recipient configuration for the FTP/static deployment.
 *
 * There is no build pipeline on this path — you edit this file directly on the
 * server after uploading. Keep a local backup before ever re-uploading a fresh
 * copy of the site so you don't overwrite real credentials with these defaults.
 */
return [
    // 'kavenegar' | 'melipayamak' | 'farazsms' | 'none'
    // 'none' just logs to the PHP error log instead of sending — safe default.
    'sms_provider' => 'none',

    'recipients' => [
        'bride' => '09152034261',
        'groom' => '09159205387',
    ],

    'kavenegar' => [
        'api_key' => '',
        'sender' => '',
    ],

    'melipayamak' => [
        'username' => '',
        'password' => '',
        'sender' => '',
    ],

    'farazsms' => [
        'api_key' => '',
        'sender' => '',
    ],
];
