
import org.junit.Test;
import static org.junit.Assert.*;
import controle.util.AESUtil;
import javax.crypto.spec.SecretKeySpec;

public class AESUtilAdditionalTest {

    @Test
    public void testDifferentIVOrSaltProducesDifferentCiphertext() throws Exception {
        char[] password = "test-password".toCharArray();
        byte[] salt1 = new byte[16];
        new java.security.SecureRandom().nextBytes(salt1);
        byte[] salt2 = new byte[16];
        new java.security.SecureRandom().nextBytes(salt2);
        SecretKeySpec key1 = AESUtil.deriveKey(password, salt1, 20000, 256);
        SecretKeySpec key2 = AESUtil.deriveKey(password, salt2, 20000, 256);

        String plain = "Same message";
        String c1 = AESUtil.encrypt(plain, key1);
        String c2 = AESUtil.encrypt(plain, key2);

        assertNotEquals("Ciphertexts should differ when salt/IV differ", c1, c2);
    }

    @Test(expected = RuntimeException.class)
    public void testTamperDetected() throws Exception {
        char[] password = "test-password".toCharArray();
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        SecretKeySpec key = AESUtil.deriveKey(password, salt, 20000, 256);

        String plain = "Important";
        String encrypted = AESUtil.encrypt(plain, key);

        // tamper with the base64 payload (flip a byte)
        byte[] raw = java.util.Base64.getDecoder().decode(encrypted);
        raw[raw.length - 1] ^= 0x01; // flip last bit
        String tampered = java.util.Base64.getEncoder().encodeToString(raw);

        // should throw (authentication failure) or return an error -> expect exception
        AESUtil.decrypt(tampered, key);
    }
}
