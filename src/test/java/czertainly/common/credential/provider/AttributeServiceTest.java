package czertainly.common.credential.provider;

import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.model.client.attribute.RequestAttribute;
import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.client.attribute.RequestAttributeV2;
import com.czertainly.api.model.common.attribute.common.BaseAttribute;
import com.czertainly.api.model.common.attribute.common.content.data.SecretAttributeContentData;
import com.czertainly.api.model.common.attribute.v2.content.SecretAttributeContentV2;
import com.czertainly.api.model.common.attribute.v2.content.StringAttributeContentV2;
import czertainly.common.credential.provider.service.AttributeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class AttributeServiceTest {
    @Autowired
    private AttributeService attributeService;

    private List<RequestAttribute> attributesBasic;
    private List<RequestAttribute> attributesApiKey;

    @BeforeEach
    public void setup(){
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
    }

    @Test
    public void testSoftkeyAttributeResponse() {
        List<BaseAttribute> attributes = attributeService.getAttributes("SoftKeyStore");
        Assertions.assertEquals(6, attributes.size());
    }

    @Test
    public void testBasicAttributeResponse() {
        List<BaseAttribute> attributes = attributeService.getAttributes("Basic");
        Assertions.assertEquals(2, attributes.size());
    }

    @Test
    public void testApiKeyAttributeResponse() {
        List<BaseAttribute> attributes = attributeService.getAttributes("ApiKey");
        Assertions.assertEquals(1, attributes.size());
    }

    @Test
    public void testValidateAttributesBasic() {
        Assertions.assertTrue(attributeService.validateAttributes("Basic", attributesBasic));
    }

    @Test
    public void testValidateAttributesApiKey() {
        Assertions.assertTrue(attributeService.validateAttributes("ApiKey", attributesApiKey));
    }

    @Test
    public void testValidateAttributes_Fail() {
        Assertions.assertThrows(ValidationException.class, () -> attributeService.validateAttributes("default",null));
    }
}
