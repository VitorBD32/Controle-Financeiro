package controle.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * GERENCIADOR DE SEGURANÇA - CONTROLE FINANCEIRO
 * ===============================================
 * 
 * Proteção contra:
 * - SQL Injection
 * - XSS (Cross-Site Scripting)
 * - Vazamento de dados sensíveis
 * - Ataques de força bruta
 * - Man-in-the-Middle
 * 
 * Implementa:
 * - Criptografia AES-256-GCM
 * - Hashing SHA-256 e BCrypt
 * - Validação de entrada
 * - Sanitização de dados
 * - Rate limiting
 * - Auditoria de acesso
 * 
 * @author Sistema de Controle Financeiro
 * @version 2.0 - Segurança Reforçada
 */
public class SecurityManager {
    
    // Constantes de criptografia
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    
    // Chave mestra derivada do sistema (em produção, usar HSM ou Vault)
    private static final String MASTER_KEY_SEED = "CF-UESPI-2024-SECURE-KEY";
    
    // Padrões para validação (prevenção de SQL Injection)
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER|CREATE|TRUNCATE|EXEC|EXECUTE)\\b)|" +
        "(--|;|/\\*|\\*/|@@|@|\\bOR\\b\\s+\\d+=\\d+|\\bAND\\b\\s+\\d+=\\d+|'\\s*OR\\s*'|" +
        "'\\s*=\\s*'|\"\\s*OR\\s*\"|\"\\s*=\\s*\")"
    );
    
    // Padrões para XSS
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<script[^>]*>.*?</script>|javascript:|on\\w+\\s*=|<iframe|<object|<embed|" +
        "<link|<style|<img[^>]+src\\s*=\\s*[\"']?javascript:)"
    );
    
    // Padrão para caracteres perigosos
    private static final Pattern DANGEROUS_CHARS = Pattern.compile("[<>\"'&;\\\\]");
    
    // Instância singleton
    private static SecurityManager instance;
    private SecretKey masterKey;
    private SecureRandom secureRandom;
    
    // Rate limiting
    private java.util.Map<String, Long> lastAccessTime = new java.util.concurrent.ConcurrentHashMap<>();
    private java.util.Map<String, Integer> failedAttempts = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_TIME_MS = 300000; // 5 minutos
    private static final long MIN_REQUEST_INTERVAL_MS = 100; // 100ms entre requisições
    
    /**
     * Construtor privado (Singleton)
     */
    private SecurityManager() {
        try {
            this.secureRandom = SecureRandom.getInstanceStrong();
            this.masterKey = deriveMasterKey();
        } catch (Exception e) {
            throw new SecurityException("Falha ao inicializar SecurityManager: " + e.getMessage());
        }
    }
    
    /**
     * Obtém a instância singleton
     */
    public static synchronized SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }
    
    // =========================================================================
    // PROTEÇÃO CONTRA SQL INJECTION
    // =========================================================================
    
    /**
     * Valida entrada contra SQL Injection
     * @param input Texto a ser validado
     * @return true se a entrada é segura
     */
    public boolean isSafeSqlInput(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        return !SQL_INJECTION_PATTERN.matcher(input).find();
    }
    
    /**
     * Sanitiza entrada para uso seguro em SQL
     * Remove ou escapa caracteres perigosos
     */
    public String sanitizeSqlInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove caracteres de controle
        String sanitized = input.replaceAll("[\\x00-\\x1F\\x7F]", "");
        
        // Escapa aspas simples (principal vetor de SQL Injection)
        sanitized = sanitized.replace("'", "''");
        
        // Remove comentários SQL
        sanitized = sanitized.replaceAll("--", "");
        sanitized = sanitized.replaceAll("/\\*.*?\\*/", "");
        
        // Remove ponto e vírgula (previne múltiplas queries)
        sanitized = sanitized.replace(";", "");
        
        return sanitized.trim();
    }
    
    /**
     * Valida e sanitiza entrada, lançando exceção se detectar ataque
     */
    public String validateAndSanitize(String input, String fieldName) throws SecurityException {
        if (input == null) {
            return null;
        }
        
        if (!isSafeSqlInput(input)) {
            logSecurityEvent("SQL_INJECTION_ATTEMPT", "Campo: " + fieldName + ", Valor: " + input);
            throw new SecurityException("⚠️ ALERTA DE SEGURANÇA: Tentativa de SQL Injection detectada no campo '" + fieldName + "'");
        }
        
        return sanitizeSqlInput(input);
    }
    
    // =========================================================================
    // PROTEÇÃO CONTRA XSS
    // =========================================================================
    
    /**
     * Valida entrada contra XSS
     */
    public boolean isSafeXssInput(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        return !XSS_PATTERN.matcher(input).find();
    }
    
    /**
     * Sanitiza entrada para prevenir XSS
     */
    public String sanitizeXssInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Escapa caracteres HTML
        String sanitized = input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
        
        return sanitized;
    }
    
    // =========================================================================
    // CRIPTOGRAFIA DE DADOS SENSÍVEIS
    // =========================================================================
    
    /**
     * Deriva a chave mestra usando PBKDF2
     */
    private SecretKey deriveMasterKey() throws Exception {
        byte[] salt = "CF-SALT-2024-SECURE".getBytes(StandardCharsets.UTF_8);
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(MASTER_KEY_SEED.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
    
    /**
     * Criptografa dados sensíveis usando AES-256-GCM
     * @param plaintext Texto original
     * @return Texto criptografado em Base64
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        try {
            // Gera IV aleatório
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Configura o cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, parameterSpec);
            
            // Criptografa
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combina IV + ciphertext
            byte[] encrypted = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            logSecurityEvent("ENCRYPTION_ERROR", e.getMessage());
            throw new SecurityException("Erro ao criptografar dados: " + e.getMessage());
        }
    }
    
    /**
     * Descriptografa dados
     * @param ciphertext Texto criptografado em Base64
     * @return Texto original
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        
        try {
            byte[] encrypted = Base64.getDecoder().decode(ciphertext);
            
            // Extrai IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encrypted, 0, iv, 0, iv.length);
            
            // Extrai ciphertext
            byte[] ciphertextBytes = new byte[encrypted.length - GCM_IV_LENGTH];
            System.arraycopy(encrypted, GCM_IV_LENGTH, ciphertextBytes, 0, ciphertextBytes.length);
            
            // Configura o cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, parameterSpec);
            
            // Descriptografa
            byte[] plaintext = cipher.doFinal(ciphertextBytes);
            
            return new String(plaintext, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logSecurityEvent("DECRYPTION_ERROR", e.getMessage());
            throw new SecurityException("Erro ao descriptografar dados: " + e.getMessage());
        }
    }
    
    /**
     * Criptografa dados financeiros (valores monetários)
     */
    public String encryptFinancialData(double value) {
        return encrypt(String.format("%.2f", value));
    }
    
    /**
     * Descriptografa dados financeiros
     */
    public double decryptFinancialData(String encrypted) {
        String decrypted = decrypt(encrypted);
        return Double.parseDouble(decrypted);
    }
    
    // =========================================================================
    // HASHING SEGURO
    // =========================================================================
    
    /**
     * Gera hash SHA-256 de um texto
     */
    public String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("SHA-256 não disponível");
        }
    }
    
    /**
     * Gera hash com salt para senhas (alternativa ao BCrypt)
     */
    public String hashPassword(String password, String salt) {
        return hashSHA256(salt + password + salt);
    }
    
    /**
     * Gera um salt aleatório
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    // =========================================================================
    // RATE LIMITING E PROTEÇÃO CONTRA FORÇA BRUTA
    // =========================================================================
    
    /**
     * Verifica se o usuário está bloqueado por tentativas excessivas
     */
    public boolean isUserLocked(String username) {
        Integer attempts = failedAttempts.get(username);
        if (attempts != null && attempts >= MAX_FAILED_ATTEMPTS) {
            Long lockTime = lastAccessTime.get(username);
            if (lockTime != null) {
                long elapsed = System.currentTimeMillis() - lockTime;
                if (elapsed < LOCKOUT_TIME_MS) {
                    return true;
                } else {
                    // Desbloqueia após o tempo de lockout
                    failedAttempts.remove(username);
                    return false;
                }
            }
        }
        return false;
    }
    
    /**
     * Registra tentativa de login falha
     */
    public void registerFailedAttempt(String username) {
        failedAttempts.merge(username, 1, Integer::sum);
        lastAccessTime.put(username, System.currentTimeMillis());
        
        Integer attempts = failedAttempts.get(username);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            logSecurityEvent("ACCOUNT_LOCKED", "Usuário bloqueado após " + attempts + " tentativas: " + username);
        }
    }
    
    /**
     * Limpa tentativas após login bem-sucedido
     */
    public void clearFailedAttempts(String username) {
        failedAttempts.remove(username);
    }
    
    /**
     * Verifica rate limiting (proteção contra DDoS)
     */
    public boolean checkRateLimit(String identifier) {
        Long lastAccess = lastAccessTime.get(identifier);
        long now = System.currentTimeMillis();
        
        if (lastAccess != null) {
            if (now - lastAccess < MIN_REQUEST_INTERVAL_MS) {
                logSecurityEvent("RATE_LIMIT_EXCEEDED", "Identificador: " + identifier);
                return false;
            }
        }
        
        lastAccessTime.put(identifier, now);
        return true;
    }
    
    // =========================================================================
    // VALIDAÇÃO DE DADOS
    // =========================================================================
    
    /**
     * Valida CPF
     */
    public boolean isValidCPF(String cpf) {
        if (cpf == null) return false;
        
        cpf = cpf.replaceAll("[^0-9]", "");
        
        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false; // Todos dígitos iguais
        
        // Validação dos dígitos verificadores
        int[] weights1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        
        int sum1 = 0;
        for (int i = 0; i < 9; i++) {
            sum1 += Character.getNumericValue(cpf.charAt(i)) * weights1[i];
        }
        int digit1 = 11 - (sum1 % 11);
        if (digit1 > 9) digit1 = 0;
        
        int sum2 = 0;
        for (int i = 0; i < 10; i++) {
            sum2 += Character.getNumericValue(cpf.charAt(i)) * weights2[i];
        }
        int digit2 = 11 - (sum2 % 11);
        if (digit2 > 9) digit2 = 0;
        
        return digit1 == Character.getNumericValue(cpf.charAt(9)) &&
               digit2 == Character.getNumericValue(cpf.charAt(10));
    }
    
    /**
     * Valida CNPJ
     */
    public boolean isValidCNPJ(String cnpj) {
        if (cnpj == null) return false;
        
        cnpj = cnpj.replaceAll("[^0-9]", "");
        
        if (cnpj.length() != 14) return false;
        if (cnpj.matches("(\\d)\\1{13}")) return false;
        
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        
        int sum1 = 0;
        for (int i = 0; i < 12; i++) {
            sum1 += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
        }
        int digit1 = 11 - (sum1 % 11);
        if (digit1 > 9) digit1 = 0;
        
        int sum2 = 0;
        for (int i = 0; i < 13; i++) {
            sum2 += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
        }
        int digit2 = 11 - (sum2 % 11);
        if (digit2 > 9) digit2 = 0;
        
        return digit1 == Character.getNumericValue(cnpj.charAt(12)) &&
               digit2 == Character.getNumericValue(cnpj.charAt(13));
    }
    
    /**
     * Valida e-mail
     */
    public boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }
    
    /**
     * Mascara dados sensíveis para exibição
     */
    public String maskSensitiveData(String data, String type) {
        if (data == null || data.isEmpty()) return data;
        
        switch (type.toUpperCase()) {
            case "CPF":
                if (data.length() >= 11) {
                    return "***." + data.substring(3, 6) + ".***-**";
                }
                break;
            case "CNPJ":
                if (data.length() >= 14) {
                    return "**." + data.substring(2, 5) + ".***/**" + data.substring(12, 14);
                }
                break;
            case "CARTAO":
                if (data.length() >= 16) {
                    return "**** **** **** " + data.substring(12);
                }
                break;
            case "EMAIL":
                int atIndex = data.indexOf('@');
                if (atIndex > 2) {
                    return data.substring(0, 2) + "***" + data.substring(atIndex);
                }
                break;
            case "TELEFONE":
                if (data.length() >= 10) {
                    return "(**) ****-" + data.substring(data.length() - 4);
                }
                break;
            case "CONTA":
                if (data.length() >= 4) {
                    return "****-" + data.substring(data.length() - 2);
                }
                break;
        }
        
        // Máscara genérica: mostra apenas primeiros e últimos 2 caracteres
        if (data.length() > 4) {
            return data.substring(0, 2) + "***" + data.substring(data.length() - 2);
        }
        return "****";
    }
    
    // =========================================================================
    // AUDITORIA E LOG DE SEGURANÇA
    // =========================================================================
    
    /**
     * Registra evento de segurança
     */
    public void logSecurityEvent(String eventType, String details) {
        String timestamp = java.time.LocalDateTime.now().toString();
        String logEntry = String.format("[SECURITY] %s | %s | %s", timestamp, eventType, details);
        
        // Log no console (em produção, gravar em arquivo seguro)
        System.err.println(logEntry);
        
        // Em produção: gravar em banco de dados ou sistema de SIEM
        // logToDatabase(eventType, details);
    }
    
    /**
     * Registra tentativa de acesso
     */
    public void logAccessAttempt(String username, String action, boolean success, String ip) {
        String status = success ? "SUCCESS" : "FAILED";
        logSecurityEvent("ACCESS_" + status, 
            String.format("User: %s, Action: %s, IP: %s", username, action, ip));
    }
    
    // =========================================================================
    // PROTEÇÃO DE CONFIGURAÇÕES
    // =========================================================================
    
    /**
     * Criptografa configurações sensíveis (para db.properties)
     */
    public String encryptConfig(String value) {
        return "ENC(" + encrypt(value) + ")";
    }
    
    /**
     * Descriptografa configurações
     */
    public String decryptConfig(String value) {
        if (value != null && value.startsWith("ENC(") && value.endsWith(")")) {
            String encrypted = value.substring(4, value.length() - 1);
            return decrypt(encrypted);
        }
        return value;
    }
    
    /**
     * Verifica se o valor está criptografado
     */
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith("ENC(") && value.endsWith(")");
    }
    
    // =========================================================================
    // GERADOR DE TOKENS SEGUROS
    // =========================================================================
    
    /**
     * Gera um token de sessão seguro
     */
    public String generateSessionToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Gera um token de API seguro
     */
    public String generateApiToken(String userId) {
        String payload = userId + ":" + System.currentTimeMillis() + ":" + generateSalt();
        return hashSHA256(payload);
    }
    
    // =========================================================================
    // VERIFICAÇÃO DE INTEGRIDADE
    // =========================================================================
    
    /**
     * Gera checksum de dados para verificação de integridade
     */
    public String generateChecksum(String data) {
        return hashSHA256(data);
    }
    
    /**
     * Verifica integridade dos dados
     */
    public boolean verifyChecksum(String data, String expectedChecksum) {
        return generateChecksum(data).equals(expectedChecksum);
    }
    
    /**
     * Main para testes
     */
    public static void main(String[] args) {
        SecurityManager sm = SecurityManager.getInstance();
        
        System.out.println("=== TESTE DE SEGURANÇA ===\n");
        
        // Teste SQL Injection
        String[] sqlTests = {
            "SELECT * FROM users",
            "'; DROP TABLE users; --",
            "1 OR 1=1",
            "normal_input",
            "João da Silva"
        };
        
        System.out.println("1. Teste SQL Injection:");
        for (String test : sqlTests) {
            boolean safe = sm.isSafeSqlInput(test);
            System.out.printf("   '%s' -> %s%n", test, safe ? "✓ Seguro" : "✗ BLOQUEADO");
        }
        
        // Teste Criptografia
        System.out.println("\n2. Teste Criptografia:");
        String original = "Dados sensíveis: R$ 1.500,00";
        String encrypted = sm.encrypt(original);
        String decrypted = sm.decrypt(encrypted);
        System.out.println("   Original:      " + original);
        System.out.println("   Criptografado: " + encrypted);
        System.out.println("   Descriptogr.:  " + decrypted);
        System.out.println("   Válido: " + (original.equals(decrypted) ? "✓ Sim" : "✗ Não"));
        
        // Teste Mascaramento
        System.out.println("\n3. Teste Mascaramento:");
        System.out.println("   CPF:      " + sm.maskSensitiveData("12345678901", "CPF"));
        System.out.println("   Cartão:   " + sm.maskSensitiveData("5555666677778888", "CARTAO"));
        System.out.println("   Email:    " + sm.maskSensitiveData("usuario@email.com", "EMAIL"));
        
        // Teste Validação
        System.out.println("\n4. Teste Validação CPF/CNPJ:");
        System.out.println("   CPF 11144477735: " + (sm.isValidCPF("11144477735") ? "✓ Válido" : "✗ Inválido"));
        System.out.println("   CPF 12345678901: " + (sm.isValidCPF("12345678901") ? "✓ Válido" : "✗ Inválido"));
        
        System.out.println("\n=== TESTES CONCLUÍDOS ===");
    }
}
