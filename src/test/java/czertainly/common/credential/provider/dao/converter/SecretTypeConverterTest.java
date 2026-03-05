package czertainly.common.credential.provider.dao.converter;

import com.czertainly.api.model.connector.secrets.SecretType;
import czertainly.common.credential.provider.api.SecretTypeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class SecretTypeConverterTest {

    private final SecretTypeConverter converter = new SecretTypeConverter();

    @Test
    void testConvert() {
        List<String> secretCodes = Arrays.stream(SecretType.values()).map(SecretType::getCode).toList();
        for (String code : secretCodes) {
            Assertions.assertDoesNotThrow(() -> converter.setAsText(code));
        }
    }
}
