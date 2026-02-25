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
import czertainly.common.credential.provider.dao.repository.SecretRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({BuildProperties.class})
class SecretServiceTest {

    @Autowired
    private SecretService secretService;

    @Autowired
    private SecretRepository secretRepository;

    @Test
    void testCreateSecret() throws AlreadyExistException {
        CreateSecretRequestDto request = new CreateSecretRequestDto();
        request.setName("testSecret");
        request.setSecret(new BasicAuthSecretContent("testUser", "testPassword"));

        SecretResponseDto response = secretService.createSecret(request);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("testSecret", response.getName());
        Assertions.assertEquals(SecretType.BASIC_AUTH, response.getType());
        Assertions.assertNotNull(response.getMetadata());
        Assertions.assertTrue(response.getMetadata().isEmpty());
    }

    @Test
    void testCreateSecret_AlreadyExistsForTheSameType() throws AlreadyExistException {
        CreateSecretRequestDto request = new CreateSecretRequestDto();
        request.setName("testSecret");
        request.setSecret(new BasicAuthSecretContent("testUser", "testPassword"));

        secretService.createSecret(request);

        Assertions.assertThrows(AlreadyExistException.class, () -> secretService.createSecret(request));
    }

    @Test
    void testCreateSecret_ForDifferentType() throws AlreadyExistException {
        CreateSecretRequestDto request = new CreateSecretRequestDto();
        request.setName("testSecret");
        request.setSecret(new ApiKeySecretContent("content-whatever"));

        SecretResponseDto response = secretService.createSecret(request);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("testSecret", response.getName());
        Assertions.assertEquals(SecretType.API_KEY, response.getType());
    }

    @Test
    void testGetSecretContent() throws AlreadyExistException, NotFoundException {
        CreateSecretRequestDto createRequest = new CreateSecretRequestDto();
        createRequest.setName("testSecret");
        createRequest.setSecret(new JwtTokenSecretContent("token"));
        secretService.createSecret(createRequest);

        SecretRequestDto request = new SecretRequestDto();
        request.setName("testSecret");
        request.setType(SecretType.JWT_TOKEN);

        SecretContentResponseDto response = secretService.getSecretContent(request, null);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("1", response.getVersion());
    }

    @Test
    void testGetSecretContent_NotFound() {
        SecretRequestDto request = new SecretRequestDto();
        request.setName("nonExistent");
        request.setType(SecretType.BASIC_AUTH);

        Assertions.assertThrows(NotFoundException.class, () -> secretService.getSecretContent(request, null));
    }

    @Test
    void testUpdateSecret() throws AlreadyExistException, NotFoundException {
        CreateSecretRequestDto createRequest = new CreateSecretRequestDto();
        createRequest.setName("testSecret");
        createRequest.setSecret(new PrivateKeySecretContent("key"));
        secretService.createSecret(createRequest);

        UpdateSecretRequestDto updateRequest = new UpdateSecretRequestDto();
        updateRequest.setName("testSecret");
        updateRequest.setSecret(new PrivateKeySecretContent("newKey"));

        SecretResponseDto response = secretService.updateSecret(updateRequest);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("testSecret", response.getName());
        Assertions.assertEquals("2", response.getVersion());
    }

    @Test
    void testUpdateSecret_NotFound() {
        UpdateSecretRequestDto updateRequest = new UpdateSecretRequestDto();
        updateRequest.setName("nonExistent");
        updateRequest.setSecret(new KeyStoreSecretContent(KeyStoreType.PKCS12, "content", "password"));

        Assertions.assertThrows(NotFoundException.class, () -> secretService.updateSecret(updateRequest));
    }

    @Test
    void testDeleteSecret() throws AlreadyExistException, NotFoundException {
        CreateSecretRequestDto createRequest = new CreateSecretRequestDto();
        createRequest.setName("testSecret");
        createRequest.setSecret(new KeyValueSecretContent(Map.of("key", "value")));
        secretService.createSecret(createRequest);

        SecretRequestDto deleteRequest = new SecretRequestDto();
        deleteRequest.setName("testSecret");
        deleteRequest.setType(SecretType.KEY_VALUE);

        secretService.deleteSecret(deleteRequest);

        Assertions.assertThrows(NotFoundException.class, () -> secretService.getSecretContent(deleteRequest, null));
    }

    @Test
    void testDeleteSecret_NotFound() {
        SecretRequestDto deleteRequest = new SecretRequestDto();
        deleteRequest.setName("nonExistent");
        deleteRequest.setType(SecretType.BASIC_AUTH);

        Assertions.assertThrows(NotFoundException.class, () -> secretService.deleteSecret(deleteRequest));
    }

    @Test
    void testRotateSecret() {
        SecretRequestDto request = new SecretRequestDto();
        request.setName("testSecret");
        request.setType(SecretType.BASIC_AUTH);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> secretService.rotateSecret(request));
    }

    @Test
    void testGetRotateAttributes() throws NotFoundException {
        List<BaseAttribute> attributes = secretService.getRotateAttributes();
        Assertions.assertTrue(attributes.isEmpty());
    }

    @Test
    void testGetSecretAttributes() {
        List<BaseAttribute> attributes = secretService.getSecretAttributes(SecretType.BASIC_AUTH);
        Assertions.assertTrue(attributes.isEmpty());
    }
}
