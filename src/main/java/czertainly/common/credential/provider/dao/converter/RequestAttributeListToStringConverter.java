package czertainly.common.credential.provider.dao.converter;

import com.czertainly.api.model.client.attribute.RequestAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Converter
@Slf4j
public class RequestAttributeListToStringConverter implements AttributeConverter<List<RequestAttribute>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<RequestAttribute> data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error converting List<RequestAttribute> to String", e);
        }
    }

    @Override
    public List<RequestAttribute> convertToEntityAttribute(String data) {
        TypeReference<List<RequestAttribute>> typeRef = new TypeReference<>() {
        };

        try {
            return mapper.readValue(data, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error converting String to List<RequestAttribute>", e);
        }
    }
}
