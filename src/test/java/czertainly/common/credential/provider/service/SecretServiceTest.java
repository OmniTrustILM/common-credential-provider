package czertainly.common.credential.provider.service;

import com.czertainly.api.exception.AlreadyExistException;
import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.model.common.attribute.common.BaseAttribute;
import com.czertainly.api.model.connector.secrets.*;
import com.czertainly.api.model.connector.secrets.content.ApiKeySecretContent;
import com.czertainly.api.model.connector.secrets.content.BasicAuthSecretContent;
import com.czertainly.api.model.connector.secrets.content.JwtTokenSecretContent;
import com.czertainly.api.model.connector.secrets.content.KeyStoreSecretContent;
import com.czertainly.api.model.connector.secrets.content.KeyStoreType;
import com.czertainly.api.model.connector.secrets.content.KeyValueSecretContent;
import com.czertainly.api.model.connector.secrets.content.PrivateKeySecretContent;
import com.czertainly.api.model.connector.secrets.content.SecretContent;
import czertainly.common.credential.provider.BuildInfoTestConfig;
import czertainly.common.credential.provider.api.SecretControllerImpl;
import czertainly.common.credential.provider.dao.repository.SecretRepository;
import czertainly.common.credential.provider.service.impl.SecretServiceImpl;
import czertainly.common.credential.provider.util.SecretEncodingVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;
import java.util.Map;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BuildInfoTestConfig.class)
class SecretServiceTest {

    private static final String TEST_SECRET_NAME = "testSecret";
    private static final String NON_EXISTENT_SECRET = "nonExistent";

    @Autowired
    private SecretService secretService;

    @Autowired
    private SecretRepository secretRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        secretRepository.deleteAll();
    }

    // ========== Create Secret Tests ==========

    @Test
    void testCreateSecret_WithBasicAuth_ShouldSucceed() throws AlreadyExistException {
        CreateSecretRequestDto request = createRequest(TEST_SECRET_NAME,
                new BasicAuthSecretContent("testUser", "testPassword"));

        SecretResponseDto response = secretService.createSecret(request);

        assertResponseIsValid(response, TEST_SECRET_NAME, SecretType.BASIC_AUTH, "1");
    }

    @Test
    void testCreateSecret_WithDuplicateNameAndType_ShouldThrowAlreadyExistException() throws AlreadyExistException {
        CreateSecretRequestDto request = createRequest(TEST_SECRET_NAME,
                new BasicAuthSecretContent("testUser", "testPassword"));
        secretService.createSecret(request);

        Assertions.assertThrows(AlreadyExistException.class,
                () -> secretService.createSecret(request));
    }

    @Test
    void testCreateSecret_WithDifferentType_ShouldSucceedForSameName() throws AlreadyExistException {
        CreateSecretRequestDto basicAuthRequest = createRequest(TEST_SECRET_NAME,
                new BasicAuthSecretContent("testUser", "testPassword"));
        secretService.createSecret(basicAuthRequest);

        CreateSecretRequestDto apiKeyRequest = createRequest(TEST_SECRET_NAME,
                new ApiKeySecretContent("content-whatever"));
        SecretResponseDto response = secretService.createSecret(apiKeyRequest);

        assertResponseIsValid(response, TEST_SECRET_NAME, SecretType.API_KEY, "1");
    }

    // ========== Get Secret Content Tests ==========

    @Test
    void testGetSecretContent_WithExistingSecret_ShouldReturnContent() throws AlreadyExistException, NotFoundException {
        CreateSecretRequestDto createRequest = createRequest(TEST_SECRET_NAME,
                new JwtTokenSecretContent("token"));
        secretService.createSecret(createRequest);

        SecretContentResponseDto response = secretService.getSecretContent(
                createRequest(TEST_SECRET_NAME, SecretType.JWT_TOKEN), null);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("1", response.getVersion());
    }

    @Test
    void testGetSecretContent_WithNonExistentSecret_ShouldThrowNotFoundException() {
        SecretRequestDto request = createRequest(NON_EXISTENT_SECRET, SecretType.BASIC_AUTH);

        Assertions.assertThrows(NotFoundException.class,
                () -> secretService.getSecretContent(request, null));
    }

    @Test
    void testGetSecretContent_WithSpecificVersion_ShouldReturnCorrectVersion() throws AlreadyExistException, NotFoundException {
        // Create an initial secret with version 1
        CreateSecretRequestDto createRequest = createRequest(TEST_SECRET_NAME,
                new JwtTokenSecretContent("token1"));
        secretService.createSecret(createRequest);

        // Update to create version 2
        UpdateSecretRequestDto updateRequest = new UpdateSecretRequestDto();
        updateRequest.setName(TEST_SECRET_NAME);
        updateRequest.setSecret(new JwtTokenSecretContent("token2"));
        secretService.updateSecret(updateRequest);

        // Update to create version 3
        updateRequest.setSecret(new JwtTokenSecretContent("token3"));
        secretService.updateSecret(updateRequest);

        // Retrieve a specific version
        SecretContentResponseDto responseV2 = secretService.getSecretContent(
                createRequest(TEST_SECRET_NAME, SecretType.JWT_TOKEN), "2");

        Assertions.assertNotNull(responseV2);
        Assertions.assertEquals("2", responseV2.getVersion());
        Assertions.assertEquals(new JwtTokenSecretContent("token2"), responseV2.getContent());
    }

    @Test
    void testGetSecretContent_WithInvalidVersion_ShouldThrowNotFoundException() throws AlreadyExistException {
        CreateSecretRequestDto createRequest = createRequest(TEST_SECRET_NAME,
                new JwtTokenSecretContent("token"));
        secretService.createSecret(createRequest);

        Assertions.assertThrows(NotFoundException.class,
                () -> secretService.getSecretContent(
                        createRequest(TEST_SECRET_NAME, SecretType.JWT_TOKEN), "99"));
    }

    // ========== Update Secret Tests ==========

    @Test
    void testUpdateSecret_WithExistingSecret_ShouldIncrementVersion() throws AlreadyExistException, NotFoundException {
        CreateSecretRequestDto createRequest = createRequest(TEST_SECRET_NAME,
                new PrivateKeySecretContent("key"));
        secretService.createSecret(createRequest);

        UpdateSecretRequestDto updateRequest = new UpdateSecretRequestDto();
        updateRequest.setName(TEST_SECRET_NAME);
        updateRequest.setSecret(new PrivateKeySecretContent("newKey"));
        SecretResponseDto response = secretService.updateSecret(updateRequest);

        assertResponseIsValid(response, TEST_SECRET_NAME, SecretType.PRIVATE_KEY, "2");
    }

    @Test
    void testUpdateSecret_WithMultipleUpdates_ShouldPersistAllVersions() throws AlreadyExistException, NotFoundException {
        CreateSecretRequestDto createRequest = createRequest(TEST_SECRET_NAME,
                new PrivateKeySecretContent("key1"));
        secretService.createSecret(createRequest);

        UpdateSecretRequestDto updateRequest = new UpdateSecretRequestDto();
        updateRequest.setName(TEST_SECRET_NAME);
        updateRequest.setSecret(new PrivateKeySecretContent("key2"));
        SecretResponseDto response2 = secretService.updateSecret(updateRequest);
        assertResponseIsValid(response2, TEST_SECRET_NAME, SecretType.PRIVATE_KEY, "2");

        updateRequest.setSecret(new PrivateKeySecretContent("key3"));
        SecretResponseDto response3 = secretService.updateSecret(updateRequest);
        assertResponseIsValid(response3, TEST_SECRET_NAME, SecretType.PRIVATE_KEY, "3");

        // Verify all versions are persisted
        SecretContentResponseDto v1 = secretService.getSecretContent(
                createRequest(TEST_SECRET_NAME, SecretType.PRIVATE_KEY), "1");
        SecretContentResponseDto v2 = secretService.getSecretContent(
                createRequest(TEST_SECRET_NAME, SecretType.PRIVATE_KEY), "2");
        SecretContentResponseDto v3 = secretService.getSecretContent(
                createRequest(TEST_SECRET_NAME, SecretType.PRIVATE_KEY), "3");

        Assertions.assertEquals("1", v1.getVersion());
        Assertions.assertEquals("2", v2.getVersion());
        Assertions.assertEquals("3", v3.getVersion());
    }

    @Test
    void testUpdateSecret_WithNonExistentSecret_ShouldThrowNotFoundException() {
        UpdateSecretRequestDto updateRequest = new UpdateSecretRequestDto();
        updateRequest.setName(NON_EXISTENT_SECRET);
        updateRequest.setSecret(new KeyStoreSecretContent(KeyStoreType.PKCS12, "content", "password"));

        Assertions.assertThrows(NotFoundException.class,
                () -> secretService.updateSecret(updateRequest));
    }

    // ========== Delete Secret Tests ==========

    @Test
    void testDeleteSecret_WithExistingSecret_ShouldRemoveSecret() throws AlreadyExistException, NotFoundException {
        CreateSecretRequestDto createRequest = createRequest(TEST_SECRET_NAME,
                new KeyValueSecretContent(Map.of("key", "value")));
        secretService.createSecret(createRequest);

        SecretRequestDto deleteRequest = createRequest(TEST_SECRET_NAME, SecretType.KEY_VALUE);
        secretService.deleteSecret(deleteRequest);

        Assertions.assertThrows(NotFoundException.class,
                () -> secretService.getSecretContent(deleteRequest, null));
    }

    @Test
    void testDeleteSecret_WithNonExistentSecret_ShouldThrowNotFoundException() {
        SecretRequestDto deleteRequest = createRequest(NON_EXISTENT_SECRET, SecretType.BASIC_AUTH);

        Assertions.assertThrows(NotFoundException.class,
                () -> secretService.deleteSecret(deleteRequest));
    }

    // ========== Rotate Secret Tests ==========

    @Test
    void testRotateSecret_ShouldThrowUnsupportedOperationException() {
        SecretRequestDto request = createRequest(TEST_SECRET_NAME, SecretType.BASIC_AUTH);

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> secretService.rotateSecret(request));
    }

    // ========== Attributes Tests ==========

    @Test
    void testGetRotateAttributes_ShouldReturnEmptyList() throws NotFoundException {
        List<BaseAttribute> attributes = secretService.getRotateAttributes();
        Assertions.assertTrue(attributes.isEmpty());
    }

    @Test
    void testGetSecretAttributes_ShouldReturnEmptyList() {
        List<BaseAttribute> attributes = secretService.getSecretAttributes(SecretType.BASIC_AUTH);
        Assertions.assertTrue(attributes.isEmpty());
    }

    // ========== Encryption Tests ==========

    @Test
    void testSecretContent_InDatabaseIsEncrypted() throws AlreadyExistException {
        // Arrange
        String secretPassword = "mySecretPassword";
        CreateSecretRequestDto createRequest = createRequest(TEST_SECRET_NAME,
                new BasicAuthSecretContent("testUser", secretPassword));

        // Act
        secretService.createSecret(createRequest);

        // Assert - Retrieve raw encrypted content from database
        String rawDatabaseContent = jdbcTemplate.queryForObject(
                "SELECT secret_content FROM secret WHERE name = ? AND secret_type = ?",
                String.class,
                TEST_SECRET_NAME,
                SecretType.BASIC_AUTH.toString()
        );

        Assertions.assertNotNull(rawDatabaseContent, "Database content should not be null");
        Assertions.assertFalse(rawDatabaseContent.contains(secretPassword),
                "Database content should not contain plaintext password");
        Assertions.assertFalse(rawDatabaseContent.contains("testUser"),
                "Database content should not contain plaintext username");
        Assertions.assertTrue(rawDatabaseContent.startsWith(SecretEncodingVersion.V1.getVersion()),
                "Encrypted content should start with version prefix 'v1'");
    }

    // ========== Helper Methods ==========

    private CreateSecretRequestDto createRequest(String name, SecretContent secretContent) {
        CreateSecretRequestDto request = new CreateSecretRequestDto();
        request.setName(name);
        request.setSecret(secretContent);
        return request;
    }

    private SecretRequestDto createRequest(String name, SecretType secretType) {
        SecretRequestDto request = new SecretRequestDto();
        request.setName(name);
        request.setType(secretType);
        return request;
    }

    private void assertResponseIsValid(SecretResponseDto response, String expectedName,
                                       SecretType expectedType, String expectedVersion) {
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertEquals(expectedName, response.getName(), "Secret name mismatch");
        Assertions.assertEquals(expectedType, response.getType(), "Secret type mismatch");
        Assertions.assertEquals(expectedVersion, response.getVersion(), "Secret version mismatch");
    }



    @Test
    void testSecretControllerBind() {
        SecretControllerImpl secretController = new SecretControllerImpl(secretService);
        Assertions.assertDoesNotThrow(() -> secretController.initBinder(new WebDataBinder(secretController)));
    }

}
