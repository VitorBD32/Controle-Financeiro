<?php
// syncjava2.php - Endpoint de sincronização com banco de dados MySQL (cópia de syncjava.php)
// Para XAMPP local: copiar para C:\xampp\htdocs\syncjava2.php
// Para servidor remoto: ajustar credenciais DB e fazer upload para https://www.datse.com.br/dev/syncjava2.php

header('Content-Type: application/json; charset=utf-8');

// ========== CONFIGURAÇÃO DO BANCO DE DADOS ==========
// Conecta ao MySQL Workbench (porta 3306, banco PROVA1)
$DB_HOST = '127.0.0.1';
$DB_USER = 'root';
$DB_PASS = '';
$DB_NAME = 'PROVA1';

// Secret used by the desktop client to derive AES key for encrypted payloads.
// In production, override with environment variable or edit this value.
$SYNC_SECRET = getenv('API_SYNC_SECRET') ?: 'default-secret';

// Para servidor remoto (production - www.datse.com.br):
// Descomentar e ajustar as credenciais do servidor:
// $DB_HOST = 'localhost'; // ou IP do servidor MySQL
// $DB_USER = 'seu_usuario_mysql';
// $DB_PASS = 'sua_senha_mysql';
// $DB_NAME = 'prova1'; // ou nome do banco no servidor

// ========== FUNÇÕES AUXILIARES ==========
function pw_match($provided, $stored_hash) {
    if ($provided === null || $stored_hash === null) return false;
    // Tenta match direto (plain text - não recomendado em produção)
    if ($provided === $stored_hash) return true;
    // Tenta MD5
    if (md5($provided) === $stored_hash) return true;
    // Tenta bcrypt (password_verify)
    if (password_verify($provided, $stored_hash)) return true;
    return false;
}

function db_connect() {
    global $DB_HOST, $DB_USER, $DB_PASS, $DB_NAME;
    $conn = new mysqli($DB_HOST, $DB_USER, $DB_PASS, $DB_NAME);
    if ($conn->connect_error) {
        return null;
    }
    $conn->set_charset('utf8mb4');
    return $conn;
}

// ========== LÓGICA DE AUTENTICAÇÃO ==========
$userKeys = array('username','user','login','email','nome','usuario');
$passKeys = array('password','pass','senha');

$auth_ok = false;
$user_id = null;
$user_nome = null;
$user_email = null;

$conn = db_connect();
if (!$conn) {
    http_response_code(500);
    echo json_encode(array(
        'ok' => false,
        'message' => 'Conexão perdida',
        'error' => 'Database connection failed'
    ));
    exit;
}

// 1) Basic Auth
$postedUser = null;
$postedPass = null;

if (isset($_SERVER['PHP_AUTH_USER'])) {
    $postedUser = $_SERVER['PHP_AUTH_USER'];
    $postedPass = isset($_SERVER['PHP_AUTH_PW']) ? $_SERVER['PHP_AUTH_PW'] : null;
}

// 2) Form fields (vários nomes de campo)
if (!$postedUser && $_SERVER['REQUEST_METHOD'] === 'POST') {
    foreach ($userKeys as $k) {
        if (isset($_POST[$k]) && !empty($_POST[$k])) { $postedUser = $_POST[$k]; break; }
    }
    foreach ($passKeys as $k) {
        if (isset($_POST[$k]) && !empty($_POST[$k])) { $postedPass = $_POST[$k]; break; }
    }
}

// Validar contra o banco de dados
if ($postedUser && $postedPass) {
    // Busca por nome ou email
    $stmt = $conn->prepare("SELECT id, nome, email, senha FROM usuarios WHERE nome = ? OR email = ? LIMIT 1");
    $stmt->bind_param("ss", $postedUser, $postedUser);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($row = $result->fetch_assoc()) {
        if (pw_match($postedPass, $row['senha'])) {
            $auth_ok = true;
            $user_id = $row['id'];
            $user_nome = $row['nome'];
            $user_email = $row['email'];
        }
    }
    $stmt->close();
}

// 3) Fallback: encrypted payload (aceitar presença de encrypted_data como alternativa para testes)
$encrypted_data = isset($_POST['encrypted_data']) ? $_POST['encrypted_data'] : null;
$salt = isset($_POST['salt']) ? $_POST['salt'] : null;
$client_id = isset($_POST['client_id']) ? $_POST['client_id'] : 'unknown';

if (!$auth_ok && $encrypted_data && strlen($encrypted_data) > 0) {
    try {
        if (!$salt) {
            throw new Exception('Missing salt for encrypted payload');
        }
        $salt_raw = base64_decode($salt);
        $combined = base64_decode($encrypted_data);
        if ($salt_raw === false || $combined === false) {
            throw new Exception('Invalid base64 input');
        }
        if (strlen($combined) <= 16) {
            throw new Exception('Encrypted payload too short');
        }
        $iv = substr($combined, 0, 16);
        $ciphertext = substr($combined, 16);
        $iterations = 20000;
        $keyLen = 32;
        if (function_exists('hash_pbkdf2')) {
            $key = hash_pbkdf2('sha256', $SYNC_SECRET, $salt_raw, $iterations, $keyLen, true);
        } else if (function_exists('openssl_pbkdf2')) {
            $key = openssl_pbkdf2($SYNC_SECRET, $salt_raw, $keyLen, $iterations, 'sha256');
        } else {
            throw new Exception('No PBKDF2 implementation available in PHP');
        }
        $plain = openssl_decrypt($ciphertext, 'AES-256-CBC', $key, OPENSSL_RAW_DATA, $iv);
        if ($plain === false) {
            throw new Exception('Decryption failed (bad key/iv or corrupted payload)');
        }
        $payload = json_decode($plain, true);
        if (!is_array($payload)) {
            throw new Exception('Decrypted payload is not valid JSON');
        }
        $postedUser = null;
        $postedPass = null;
        foreach (array('nome','login','user','username','email','usuario') as $k) {
            if (isset($payload[$k]) && !empty($payload[$k])) { $postedUser = $payload[$k]; break; }
        }
        foreach (array('senha','password','pass') as $k) {
            if (isset($payload[$k]) && !empty($payload[$k])) { $postedPass = $payload[$k]; break; }
        }
        if ($postedUser && $postedPass) {
            $stmt = $conn->prepare("SELECT id, nome, email, senha FROM usuarios WHERE nome = ? OR email = ? LIMIT 1");
            if ($stmt) {
                $stmt->bind_param("ss", $postedUser, $postedUser);
                $stmt->execute();
                $result = $stmt->get_result();
                if ($row = $result->fetch_assoc()) {
                    if (pw_match($postedPass, $row['senha'])) {
                        $auth_ok = true;
                        $user_id = $row['id'];
                        $user_nome = $row['nome'];
                        $user_email = $row['email'];
                    }
                }
                $stmt->close();
            }
        }
    } catch (Exception $e) {
        $errorMsg = $e->getMessage();
        if (!isset($response_debug)) $response_debug = array();
        $response_debug['decrypt_error'] = $errorMsg;
    }
}

$conn->close();

// ========== RESPOSTA JSON ==========
$response = array(
    'ok' => $auth_ok,
    'auth_ok' => $auth_ok,
    'endpoint' => isset($_SERVER['REQUEST_URI']) ? $_SERVER['REQUEST_URI'] : '',
    'user' => $auth_ok ? array(
        'id' => $user_id,
        'nome' => $user_nome,
        'email' => $user_email
    ) : null,
    'received' => array(
        'encrypted_data_length' => $encrypted_data ? strlen($encrypted_data) : 0,
        'salt' => $salt,
        'client_id' => $client_id
    ),
    'message' => $auth_ok ? 'Sincronização aceita' : 'Login invalido'
);

http_response_code($auth_ok ? 200 : 401);
echo json_encode($response);

if (isset($response_debug) && is_array($response_debug)) {
    // If debug info was collected, append it to the JSON output for local testing.
    // Note: remove or disable in production to avoid leaking sensitive diagnostics.
    // (We can't modify the already-sent JSON easily here without buffering; include header to help)
    // For convenience, also write debug to Apache error log.
    error_log('syncjava2 debug: ' . json_encode($response_debug));
}

?>
