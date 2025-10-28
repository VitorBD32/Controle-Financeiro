
import org.junit.Test;
import static org.junit.Assert.*;
import controle.util.AESUtil;
import javax.crypto.spec.SecretKeySpec;

public class AESUtilTest {

    @Test
    public void testEncryptDecrypt() throws Exception {
        char[] password = "test-password".toCharArray();
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        SecretKeySpec key = AESUtil.deriveKey(password, salt, 10000, 256);

        String plain = "Criptografia simétrica aplicada!";
        String encrypted = AESUtil.encrypt(plain, key);
        String decrypted = AESUtil.decrypt(encrypted, key);

        assertEquals(plain, decrypted);
    }
}
