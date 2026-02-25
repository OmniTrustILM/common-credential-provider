package czertainly.common.credential.provider.util;

import czertainly.common.credential.provider.BuildInfoTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(BuildInfoTestConfig.class)
class SecretsUtilTest {
    private static final String SECRET = "This is my secret value I want to protect";

    @Autowired
    private SecretsUtil secretsUtil;

    @Test
    void testEncodeSecret_ok() {
        String encodedSecret = secretsUtil.encryptAndEncodeSecretString(SECRET, SecretEncodingVersion.V1);

        Assertions.assertEquals(SecretEncodingVersion.V1.getVersion(), encodedSecret.substring(0, 2));
    }

    @Test
    void testEncryptDecrypt_ok() {
        String encodedSecret = secretsUtil.encryptAndEncodeSecretString(SECRET, SecretEncodingVersion.V1);
        String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, SecretEncodingVersion.V1);

        Assertions.assertEquals(SECRET, decodedSecret);
    }

}
