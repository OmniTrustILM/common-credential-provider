package czertainly.common.credential.provider.service;

import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.model.client.attribute.RequestAttribute;
import com.czertainly.api.model.client.attribute.RequestAttributeV2;
import com.czertainly.api.model.common.attribute.common.BaseAttribute;
import com.czertainly.api.model.common.attribute.common.content.data.FileAttributeContentData;
import com.czertainly.api.model.common.attribute.common.content.data.SecretAttributeContentData;
import com.czertainly.api.model.common.attribute.v2.content.FileAttributeContentV2;
import com.czertainly.api.model.common.attribute.v2.content.SecretAttributeContentV2;
import com.czertainly.api.model.common.attribute.v2.content.StringAttributeContentV2;
import czertainly.common.credential.provider.BuildInfoTestConfig;
import czertainly.common.credential.provider.KeyStoreTest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Import(BuildInfoTestConfig.class)
class AttributeServiceTest {

    private static final String KEYSTORE_PASSWORD = "123456";
    private static final String KEYSTORE_TYPE = "JKS";

    @Autowired
    private AttributeService attributeService;

    private String validKeyStoreBase64;
    private List<RequestAttribute> attributesBasic;
    private List<RequestAttribute> attributesApiKey;

    @BeforeEach
    void setup() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());

        RequestAttributeV2 username = new RequestAttributeV2();
        username.setUuid(UUID.fromString("1b6c48ad-c1c7-4c82-91ef-3e61bc9f52ac"));
        username.setContent(List.of(new StringAttributeContentV2("admin")));
        username.setName("username");

        RequestAttributeV2 password = new RequestAttributeV2();
        password.setUuid(UUID.fromString("9379ca2c-aa51-42c8-8afd-2a2d16c99c56"));
        password.setContent(List.of(new SecretAttributeContentV2("", new SecretAttributeContentData("admin"))));
        password.setName("password");

        attributesBasic = Arrays.asList(username, password);

        RequestAttributeV2 apiKey = new RequestAttributeV2();
        apiKey.setUuid(UUID.fromString("9379ca2c-aa51-42c8-8afd-2a2d16c99c56"));
        apiKey.setContent(List.of(new SecretAttributeContentV2("", new SecretAttributeContentData("ASufvjhFUtydFDFA"))));
        apiKey.setName("apiKey");

        attributesApiKey = List.of(apiKey);

        Security.addProvider(new BouncyCastleProvider());

        // Load keystore for testing
        InputStream is = KeyStoreTest.class.getClassLoader().getResourceAsStream("trustStore.jks");
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(is, KEYSTORE_PASSWORD.toCharArray());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        keyStore.store(baos, KEYSTORE_PASSWORD.toCharArray());
        validKeyStoreBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    @Test
    void testSoftkeyAttributeResponse() {
        List<BaseAttribute> attributes = attributeService.getAttributes("SoftKeyStore");
        Assertions.assertEquals(6, attributes.size());
    }

    @Test
    void testBasicAttributeResponse() {
        List<BaseAttribute> attributes = attributeService.getAttributes("Basic");
        Assertions.assertEquals(2, attributes.size());
    }

    @Test
    void testApiKeyAttributeResponse() {
        List<BaseAttribute> attributes = attributeService.getAttributes("ApiKey");
        Assertions.assertEquals(1, attributes.size());
    }

    @Test
    void testValidateAttributesBasic() {
        Assertions.assertTrue(attributeService.validateAttributes("Basic", attributesBasic));
    }

    @Test
    void testValidateAttributesApiKey() {
        Assertions.assertTrue(attributeService.validateAttributes("ApiKey", attributesApiKey));
    }

    @Test
    void testValidateAttributes_Fail() {
        Assertions.assertThrows(ValidationException.class, () -> attributeService.validateAttributes("default",null));
    }

    @Test
    void testValidateSoftKeyStoreAttributes_InvalidKeyStoreContent_ThrowsValidationException() {
        List<RequestAttribute> attributes = createKeyStoreAttributesWithInvalidContent();

        Assertions.assertThrows(ValidationException.class, () ->
                attributeService.validateAttributes("SoftKeyStore", attributes));
    }

    @Test
    void testValidateSoftKeyStoreAttributes_WrongPassword_ThrowsValidationException() {
        List<RequestAttribute> attributes = createKeyStoreAttributesWithWrongPassword();

        Assertions.assertThrows(ValidationException.class, () ->
                attributeService.validateAttributes("SoftKeyStore", attributes));
    }

    @Test
    void testValidateSoftKeyStoreAttributes_InvalidKeyStoreType_ThrowsValidationException() {
        List<RequestAttribute> attributes = createKeyStoreAttributesWithInvalidType();

        Assertions.assertThrows(ValidationException.class, () ->
                attributeService.validateAttributes("SoftKeyStore", attributes));
    }

    @Test
    void testValidateSoftKeyStoreAttributes_PartialTrustStoreAttributes_ThrowsValidationException() {
        List<RequestAttribute> attributes = createKeyStoreWithPartialTrustStoreAttributes();

        Assertions.assertThrows(ValidationException.class, () ->
                attributeService.validateAttributes("SoftKeyStore", attributes));
    }

    private List<RequestAttribute> createValidKeyStoreAttributes() {
        RequestAttributeV2 keyStoreType = new RequestAttributeV2();
        keyStoreType.setUuid(UUID.fromString("e334e055-900e-43f1-aedc-54e837028de0"));
        keyStoreType.setName("keyStoreType");
        keyStoreType.setContent(List.of(new StringAttributeContentV2(KEYSTORE_TYPE)));

        RequestAttributeV2 keyStore = new RequestAttributeV2();
        keyStore.setUuid(UUID.fromString("6df7ace9-c501-4d58-953c-f8d53d4fb378"));
        keyStore.setName("keyStore");

        FileAttributeContentData fileAttributeContentData = new FileAttributeContentData();
        fileAttributeContentData.setContent(validKeyStoreBase64);
        fileAttributeContentData.setFileName(KEYSTORE_TYPE);
        fileAttributeContentData.setMimeType("application/octet-stream");

        FileAttributeContentV2 fileContent = new FileAttributeContentV2();
        fileContent.setData(fileAttributeContentData);
        keyStore.setContent(List.of(fileContent));

        RequestAttributeV2 keyStorePassword = new RequestAttributeV2();
        keyStorePassword.setUuid(UUID.fromString("d975fe42-9d09-4740-a362-fc26f98e55ea"));
        keyStorePassword.setName("keyStorePassword");
        keyStorePassword.setContent(List.of(new SecretAttributeContentV2("", new SecretAttributeContentData(KEYSTORE_PASSWORD))));

        return Arrays.asList(keyStoreType, keyStore, keyStorePassword);
    }

    private List<RequestAttribute> createKeyStoreAttributesWithInvalidContent() {
        RequestAttributeV2 keyStoreType = new RequestAttributeV2();
        keyStoreType.setUuid(UUID.fromString("e334e055-900e-43f1-aedc-54e837028de0"));
        keyStoreType.setName("keyStoreType");
        keyStoreType.setContent(List.of(new StringAttributeContentV2(KEYSTORE_TYPE)));

        RequestAttributeV2 keyStore = new RequestAttributeV2();
        keyStore.setUuid(UUID.fromString("6df7ace9-c501-4d58-953c-f8d53d4fb378"));
        keyStore.setName("keyStore");

        FileAttributeContentData fileAttributeContentData = new FileAttributeContentData();
        fileAttributeContentData.setContent(Base64.getEncoder().encodeToString("invalid".getBytes()));
        fileAttributeContentData.setFileName(KEYSTORE_TYPE);
        fileAttributeContentData.setMimeType("application/octet-stream");

        FileAttributeContentV2 fileContent = new FileAttributeContentV2();
        fileContent.setData(fileAttributeContentData);
        keyStore.setContent(List.of(fileContent));

        RequestAttributeV2 keyStorePassword = new RequestAttributeV2();
        keyStorePassword.setUuid(UUID.fromString("d975fe42-9d09-4740-a362-fc26f98e55ea"));
        keyStorePassword.setName("keyStorePassword");
        keyStorePassword.setContent(List.of(new SecretAttributeContentV2("", new SecretAttributeContentData(KEYSTORE_PASSWORD))));

        return Arrays.asList(keyStoreType, keyStore, keyStorePassword);
    }

    private List<RequestAttribute> createKeyStoreAttributesWithWrongPassword() {
        RequestAttributeV2 keyStoreType = new RequestAttributeV2();
        keyStoreType.setUuid(UUID.fromString("e334e055-900e-43f1-aedc-54e837028de0"));
        keyStoreType.setName("keyStoreType");
        keyStoreType.setContent(List.of(new StringAttributeContentV2(KEYSTORE_TYPE)));

        RequestAttributeV2 keyStore = new RequestAttributeV2();
        keyStore.setUuid(UUID.fromString("6df7ace9-c501-4d58-953c-f8d53d4fb378"));
        keyStore.setName("keyStore");
        FileAttributeContentV2 fileContent = new FileAttributeContentV2();
        fileContent.setReference(validKeyStoreBase64);
        keyStore.setContent(List.of(fileContent));

        RequestAttributeV2 keyStorePassword = new RequestAttributeV2();
        keyStorePassword.setUuid(UUID.fromString("d975fe42-9d09-4740-a362-fc26f98e55ea"));
        keyStorePassword.setName("keyStorePassword");
        keyStorePassword.setContent(List.of(new SecretAttributeContentV2("", new SecretAttributeContentData("wrongpassword"))));

        return Arrays.asList(keyStoreType, keyStore, keyStorePassword);
    }

    private List<RequestAttribute> createKeyStoreAttributesWithInvalidType() {
        RequestAttributeV2 keyStoreType = new RequestAttributeV2();
        keyStoreType.setUuid(UUID.fromString("e334e055-900e-43f1-aedc-54e837028de0"));
        keyStoreType.setName("keyStoreType");
        keyStoreType.setContent(List.of(new StringAttributeContentV2("INVALID_TYPE")));

        RequestAttributeV2 keyStore = new RequestAttributeV2();
        keyStore.setUuid(UUID.fromString("6df7ace9-c501-4d58-953c-f8d53d4fb378"));
        keyStore.setName("keyStore");
        FileAttributeContentV2 fileContent = new FileAttributeContentV2();
        fileContent.setReference(validKeyStoreBase64);
        keyStore.setContent(List.of(fileContent));

        RequestAttributeV2 keyStorePassword = new RequestAttributeV2();
        keyStorePassword.setUuid(UUID.fromString("d975fe42-9d09-4740-a362-fc26f98e55ea"));
        keyStorePassword.setName("keyStorePassword");
        keyStorePassword.setContent(List.of(new SecretAttributeContentV2("", new SecretAttributeContentData(KEYSTORE_PASSWORD))));

        return Arrays.asList(keyStoreType, keyStore, keyStorePassword);
    }

    private List<RequestAttribute> createKeyStoreWithPartialTrustStoreAttributes() {
        List<RequestAttribute> attrs = new java.util.ArrayList<>(createValidKeyStoreAttributes());

        RequestAttributeV2 trustStoreType = new RequestAttributeV2();
        trustStoreType.setUuid(UUID.fromString("c4454807-805a-44e2-81d1-94b56e993786"));
        trustStoreType.setName("trustStoreType");
        trustStoreType.setContent(List.of(new StringAttributeContentV2(KEYSTORE_TYPE)));

        attrs.add(trustStoreType);
        return attrs;
    }
}