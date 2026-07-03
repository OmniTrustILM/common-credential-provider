package com.otilm.common.credential.provider.dao.converter;

import com.otilm.api.model.client.attribute.RequestAttribute;
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

    private final TypeReference<List<RequestAttribute>> typeRef = new TypeReference<>() {
    };

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<RequestAttribute> data) {
        if (data == null) {
            return null;
        }

        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error converting List<RequestAttribute> to String", e);
        }
    }

    @Override
    public List<RequestAttribute> convertToEntityAttribute(String data) {
        if (data == null) {
            return null;
        }

        try {
            return mapper.readValue(data, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error converting String to List<RequestAttribute>", e);
        }
    }
}
