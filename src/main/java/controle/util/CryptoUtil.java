package controle.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utilitário de criptografia AES-256 para dados sensíveis
 * Usado para criptografar números de cartão, CVV, etc.
 */
public class CryptoUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int IV_LENGTH = 16;
    
    // Chave mestra - em produção, deve vir de variável de ambiente ou HSM
    private static final String MASTER_KEY = "ControleFinanceiro2025!SecureKey";
    private static final String SALT = "CF_SALT_2025_SECURE";

    /**
     * Criptografa um texto usando AES-256
     * @param plainText Texto a ser criptografado
     * @return Texto criptografado em Base64 (IV + ciphertext)
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        try {
            // Gera IV aleatório
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Deriva a chave
            SecretKey secretKey = deriveKey();

            // Criptografa
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatena IV + ciphertext e codifica em Base64
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            System.err.println("[CryptoUtil] Erro ao criptografar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Descriptografa um texto criptografado com AES-256
     * @param encryptedText Texto criptografado em Base64
     * @return Texto original
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return null;
        }
        try {
            // Decodifica Base64
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Extrai IV e ciphertext
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Deriva a chave
            SecretKey secretKey = deriveKey();

            // Descriptografa
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[CryptoUtil] Erro ao descriptografar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deriva uma chave AES-256 a partir da chave mestra usando PBKDF2
     */
    private static SecretKey deriveKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(MASTER_KEY.toCharArray(), SALT.getBytes(), ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Mascara um número de cartão para exibição (mostra apenas últimos 4 dígitos)
     * @param cardNumber Número do cartão
     * @return Número mascarado (ex: **** **** **** 1234)
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() < 4) {
            return "****";
        }
        String last4 = digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }

    /**
     * Valida número de cartão usando algoritmo de Luhn
     * @param cardNumber Número do cartão
     * @return true se válido
     */
    public static boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null) return false;
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() < 13 || digits.length() > 19) return false;

        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(digits.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
     * Detecta a bandeira do cartão pelo número
     * @param cardNumber Número do cartão
     * @return Nome da bandeira
     */
    public static String detectCardBrand(String cardNumber) {
        if (cardNumber == null) return "Desconhecida";
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.isEmpty()) return "Desconhecida";

        if (digits.startsWith("4")) return "Visa";
        if (digits.startsWith("5") || digits.startsWith("2")) return "Mastercard";
        if (digits.startsWith("34") || digits.startsWith("37")) return "American Express";
        if (digits.startsWith("6011") || digits.startsWith("65")) return "Discover";
        if (digits.startsWith("36") || digits.startsWith("38")) return "Diners Club";
        if (digits.startsWith("35")) return "JCB";
        if (digits.startsWith("606282") || digits.startsWith("3841")) return "Hipercard";
        if (digits.startsWith("636368") || digits.startsWith("438935")) return "Elo";

        return "Outra";
    }

    /**
     * Gera um token seguro para identificação de cartão
     * @return Token aleatório de 32 caracteres
     */
    public static String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
