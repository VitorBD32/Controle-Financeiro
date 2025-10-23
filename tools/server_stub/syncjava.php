<?php
// syncjava.php - Test stub for desktop client integration
// Place this file in your Apache DocumentRoot (e.g. C:\xampp\htdocs\syncjava.php)
// This stub accepts form-urlencoded POSTs and Basic Auth (for testing only).

header('Content-Type: application/json; charset=utf-8');

// Simple auth check: accept JOAO/1234 via Basic Auth or form fields username/password
$auth_ok = false;
if (isset($_SERVER['PHP_AUTH_USER'])) {
    $user = $_SERVER['PHP_AUTH_USER'];
    $pass = $_SERVER['PHP_AUTH_PW'];
    if ($user === 'JOAO' && $pass === '1234') {
        $auth_ok = true;
    }
}

if (!$auth_ok && isset($_POST['username']) && isset($_POST['password'])) {
    if ($_POST['username'] === 'JOAO' && $_POST['password'] === '1234') {
        $auth_ok = true;
    }
}

$encrypted_data = isset($_POST['encrypted_data']) ? $_POST['encrypted_data'] : null;
$salt = isset($_POST['salt']) ? $_POST['salt'] : null;
$client_id = isset($_POST['client_id']) ? $_POST['client_id'] : 'unknown';

// Basic response with counts/echo for debugging
$response = array(
    'ok' => $auth_ok,
    'auth_ok' => $auth_ok,
    'received' => array(
        'encrypted_data_length' => $encrypted_data ? strlen($encrypted_data) : 0,
        'salt' => $salt,
        'client_id' => $client_id
    ),
    'message' => $auth_ok ? 'sync accepted (test stub)' : 'authentication failed (test stub)'
);

http_response_code($auth_ok ? 200 : 401);
echo json_encode($response);
