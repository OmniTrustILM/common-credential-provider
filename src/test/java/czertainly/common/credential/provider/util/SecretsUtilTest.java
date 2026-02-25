package czertainly.common.credential.provider.util;

import czertainly.common.credential.provider.BuildInfoTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(BuildInfoTestConfig.class)
public class SecretsUtilTest {
    private static final String secret = "This is my secret value I want to protect";

    @Autowired
    private SecretsUtil secretsUtil;

    @Test
    public void testEncodeSecret_ok() {
        String encodedSecret = secretsUtil.encryptAndEncodeSecretString(secret, SecretEncodingVersion.V1);

        Assertions.assertEquals(encodedSecret.substring(0, 2), SecretEncodingVersion.V1.getVersion());
    }

    @Test
    public void testEncryptDecrypt_ok() {
        String encodedSecret = secretsUtil.encryptAndEncodeSecretString(secret, SecretEncodingVersion.V1);
        String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, SecretEncodingVersion.V1);

        Assertions.assertEquals(secret, decodedSecret);
    }

}
