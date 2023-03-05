package com.canvas.service.helperServices;
import com.canvas.exceptions.CanvasAPIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;


public class AESCryptoServiceTest {

    @Mock
    Environment env;

    @Before
    public void init(){
        env = mock(Environment.class);
        when(env.getProperty(any()))
                .thenReturn("someSecret");
    }

    @Test
    public void testEncryptAndDecrypt_to_ensure_entered_input_is_not_modified() throws CanvasAPIException {
        //Setup
        String originalString = "This is a test string";
        String secretKey = "This is a secret key";
        AESCryptoService aesCryptoService = new AESCryptoService(env);

        //Act
        String encryptedString = aesCryptoService.encrypt(originalString, secretKey);
        String decryptedString = aesCryptoService.decrypt(encryptedString, secretKey);

        //Assert
        assertEquals("The original and decrypted strings should match", originalString, decryptedString);
    }

    @Test
    public void testEncryptWithNullInput_method_should_throw_exception_without_input() {
        //Setup
        String originalString = null;
        String secretKey = "This is a secret key";
        AESCryptoService aesCryptoService = new AESCryptoService(env);

        //Assert
        assertThrows(CanvasAPIException.class, () -> aesCryptoService.encrypt(originalString, secretKey));
    }

    @Test
    public void testDecryptWithNullInput_method_should_throw_exception_without_input() {
        //Setup
        String originalString = "This is a test string";
        String secretKey = null;
        AESCryptoService aesCryptoService = new AESCryptoService(env);

        //Assert
        assertThrows(CanvasAPIException.class, () -> aesCryptoService.decrypt(originalString, secretKey));
    }
}
