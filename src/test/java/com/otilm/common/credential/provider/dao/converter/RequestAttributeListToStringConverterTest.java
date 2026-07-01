package com.otilm.common.credential.provider.dao.converter;

import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.client.attribute.RequestAttributeV2;
import com.otilm.api.model.common.attribute.v2.content.StringAttributeContentV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RequestAttributeListToStringConverter to verify conversion logic
 * and edge cases when data is null.
 */
class RequestAttributeListToStringConverterTest {

    private RequestAttributeListToStringConverter converter;
    private ObjectMapper objectMapper;

    private static final UUID ATTRIBUTE1_UUID = UUID.fromString("1b6c48ad-c1c7-4c82-91ef-3e61bc9f52ac");
    private static final UUID ATTRIBUTE2_UUID = UUID.fromString("9379ca2c-aa51-42c8-8afd-2a2d16c99c56");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        converter = new RequestAttributeListToStringConverter();
    }

    // ========== convertToDatabaseColumn Tests ==========

    @ParameterizedTest
    @MethodSource("databaseColumnAndRoundTripCases")
    @DisplayName("convertToDatabaseColumn should handle null, empty, and populated lists")
    void testConvertToDatabaseColumnCases(List<RequestAttribute> input, boolean expectNull, List<String> expectedNames) throws JsonProcessingException {
        String result = converter.convertToDatabaseColumn(input);

        if (expectNull) {
            assertNull(result, "Expected null when converting null data to database column");
            return;
        }

        assertNotNull(result, "Result should not be null");
        if (input != null && input.isEmpty()) {
            List<RequestAttribute> deserializedList = objectMapper.readValue(
                    result,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RequestAttribute.class)
            );
            assertTrue(deserializedList.isEmpty(), "Deserialized list should be empty");
            return;
        }

        for (String expectedName : expectedNames) {
            assertTrue(result.contains(expectedName), "Result should contain attribute name: " + expectedName);
        }
    }

    // ========== convertToEntityAttribute Tests ==========

    @ParameterizedTest
    @MethodSource("entityAttributeCases")
    @DisplayName("convertToEntityAttribute should handle null, empty JSON, and populated JSON")
    void testConvertToEntityAttributeCases(String input, boolean expectNull, List<String> expectedNames) {
        List<RequestAttribute> result = converter.convertToEntityAttribute(input);

        if (expectNull) {
            assertNull(result, "Expected null when converting null string to entity attribute");
            return;
        }

        assertNotNull(result, "Result should not be null");
        if (expectedNames.isEmpty()) {
            assertTrue(result.isEmpty(), "Deserialized list should be empty");
            return;
        }

        assertAttributeNames(result, expectedNames.toArray(new String[0]));
    }

    // ========== Round-trip Conversion Tests ==========

    @ParameterizedTest
    @MethodSource("databaseColumnAndRoundTripCases")
    @DisplayName("Round-trip conversion should preserve null, empty, and populated lists")
    void testRoundTripConversionCases(List<RequestAttribute> input, boolean expectNull, List<String> expectedNames) {
        String dbColumn = converter.convertToDatabaseColumn(input);
        List<RequestAttribute> result = converter.convertToEntityAttribute(dbColumn);

        if (expectNull) {
            assertNull(result, "Round-trip conversion of null should result in null");
            return;
        }

        assertNotNull(result, "Result should not be null");
        if (expectedNames.isEmpty()) {
            assertTrue(result.isEmpty(), "Round-trip conversion should preserve empty list");
            return;
        }

        assertAttributeNames(result, expectedNames.toArray(new String[0]));
    }

    private static Stream<Arguments> databaseColumnAndRoundTripCases() {
        RequestAttributeV2 attribute1 = buildAttribute(ATTRIBUTE1_UUID, "testAttribute", "testValue");
        RequestAttributeV2 attribute2 = buildAttribute(ATTRIBUTE1_UUID, "attribute1", "value1");
        RequestAttributeV2 attribute3 = buildAttribute(ATTRIBUTE2_UUID, "attribute2", "value2");

        return Stream.of(
                Arguments.of(null, true, List.of()),
                Arguments.of(new ArrayList<RequestAttribute>(), false, List.of()),
                Arguments.of(List.of(attribute1), false, List.of("testAttribute")),
                Arguments.of(List.of(attribute2, attribute3), false, List.of("attribute1", "attribute2"))
        );
    }

    private static Stream<Arguments> entityAttributeCases() {
        String singleJson = "[{\"uuid\":\"1b6c48ad-c1c7-4c82-91ef-3e61bc9f52ac\",\"name\":\"testAttribute\"}]";
        String multipleJson = "[" +
                "{\"uuid\":\"1b6c48ad-c1c7-4c82-91ef-3e61bc9f52ac\",\"name\":\"attribute1\"}," +
                "{\"uuid\":\"9379ca2c-aa51-42c8-8afd-2a2d16c99c56\",\"name\":\"attribute2\"}" +
                "]";

        return Stream.of(
                Arguments.of(null, true, List.of()),
                Arguments.of("[]", false, List.of()),
                Arguments.of(singleJson, false, List.of("testAttribute")),
                Arguments.of(multipleJson, false, List.of("attribute1", "attribute2"))
        );
    }

    private static RequestAttributeV2 buildAttribute(UUID uuid, String name, String value) {
        RequestAttributeV2 attribute = new RequestAttributeV2();
        attribute.setUuid(uuid);
        attribute.setName(name);
        attribute.setContent(List.of(new StringAttributeContentV2(value)));
        return attribute;
    }

    private void assertAttributeNames(List<RequestAttribute> attributes, String... expectedNames) {
        assertEquals(expectedNames.length, attributes.size(), "Unexpected attribute count");
        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(expectedNames[i], attributes.get(i).getName(), "Attribute name should match");
        }
    }
}
