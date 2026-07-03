package com.otilm.common.credential.provider.secret.util;

import com.otilm.common.credential.provider.BuildInfoTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(BuildInfoTestConfig.class)
@DisplayName("SecretsUtil Tests")
class SecretsUtilTest {
    /**
     * Comprehensive test suite for SecretsUtil class covering:
     * - Encryption/decryption with AES-GCM
     * - Format validation and consistency
     * - Tampering detection
     * - Unsupported version handling
     * - Edge cases (special chars, Unicode, long secrets, etc.)
     */

    // ==================== Test Constants ====================
    private static final String STANDARD_SECRET = "This is my secret value I want to protect";
    private static final String SPECIAL_CHARS_SECRET = "Special!@#$%^&*()_+-={}[]|\\:\";<>?,./~`";
    private static final String UNICODE_SECRET = "Unicode: 你好世界 🌍 Привет";
    private static final SecretEncodingVersion VERSION = SecretEncodingVersion.V1;
    private static final String VERSION_PREFIX = "v1|";
    private static final int EXPECTED_PARTS_COUNT = 5;
    private static final int SALT_SIZE_BYTES = 32;
    private static final int IV_SIZE_BYTES = 12;

    @Autowired
    private SecretsUtil secretsUtil;

    @Nested
    @DisplayName("Encryption Tests")
    class EncryptionTests {

        @Test
        @DisplayName("Should encrypt and encode secret with correct version prefix")
        void shouldEncryptSecretWithCorrectVersionPrefix() {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);

            assertNotNull(encodedSecret, "Encoded secret should not be null");
            assertTrue(encodedSecret.startsWith(VERSION_PREFIX),
                    "Encoded secret should start with version prefix: " + VERSION_PREFIX);
            assertEquals(VERSION.getVersion(), encodedSecret.substring(0, 2),
                    "Version prefix should match expected version");
        }

        @Test
        @DisplayName("Should return null when encrypting null secret")
        void shouldReturnNullWhenEncryptingNullSecret() {
            String result = secretsUtil.encryptAndEncodeSecretString(null, VERSION);

            assertNull(result, "Result should be null when input secret is null");
        }

        @Test
        @DisplayName("Should successfully encrypt empty string")
        void shouldEncryptEmptyString() {
            String emptySecret = "";

            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(emptySecret, VERSION);

            assertNotNull(encodedSecret, "Encoded secret should not be null for empty string");
            assertTrue(encodedSecret.startsWith(VERSION_PREFIX),
                    "Encoded secret should have version prefix");
        }

        @ParameterizedTest
        @DisplayName("Should encrypt and decrypt various secret types")
        @ValueSource(strings = {
                "simple",
                "with spaces",
                "123456789",
                SPECIAL_CHARS_SECRET,
                UNICODE_SECRET
        })
        void shouldEncryptAndDecryptVariousSecretTypes(String secret) {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(secret, VERSION);
            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret);

            assertEquals(secret, decodedSecret,
                    "Decrypted secret should match original secret");
        }

        @Test
        @DisplayName("Should handle long secrets correctly")
        void shouldHandleLongSecrets() {
            String longSecret = "A".repeat(10000);

            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(longSecret, VERSION);
            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret);

            assertEquals(longSecret, decodedSecret,
                    "Long secret should be encrypted and decrypted correctly");
        }

        @Test
        @DisplayName("Should produce unique encrypted values for same input")
        void shouldProduceUniqueEncryptedValues() {
            String encodedSecret1 = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
            String encodedSecret2 = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);

            assertNotEquals(encodedSecret1, encodedSecret2,
                    "Two encryptions of same secret should produce different values due to random salt and IV");

            // Both should still decrypt to the same original value
            String decodedSecret1 = secretsUtil.decodeAndDecryptSecretString(encodedSecret1);
            String decodedSecret2 = secretsUtil.decodeAndDecryptSecretString(encodedSecret2);

            assertEquals(STANDARD_SECRET, decodedSecret1,
                    "First encrypted secret should decrypt to original");
            assertEquals(STANDARD_SECRET, decodedSecret2,
                    "Second encrypted secret should decrypt to original");
        }
    }

    @Nested
    @DisplayName("Decryption Tests")
    class DecryptionTests {

        @Test
        @DisplayName("Should successfully decrypt encrypted secret")
        void shouldDecryptEncryptedSecret() {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);

            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret);

            assertEquals(STANDARD_SECRET, decodedSecret,
                    "Decrypted secret should match original secret");
        }

        @Test
        @DisplayName("Should decrypt empty string correctly")
        void shouldDecryptEmptyString() {
            String emptySecret = "";
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(emptySecret, VERSION);

            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret);

            assertEquals(emptySecret, decodedSecret,
                    "Empty string should be decrypted correctly");
        }
    }

    @Nested
    @DisplayName("Format Validation Tests")
    class FormatValidationTests {

        @Test
        @DisplayName("Should have correct encrypted secret format")
        void shouldHaveCorrectEncryptedSecretFormat() {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);

            String[] parts = encodedSecret.split("\\|");
            assertEquals(EXPECTED_PARTS_COUNT, parts.length,
                    "Encoded secret should have " + EXPECTED_PARTS_COUNT + " parts");
            assertEquals(VERSION.getVersion(), parts[0],
                    "First part should be version identifier");

            // Verify encrypted data is valid base64
            assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[1]),
                    "Encrypted data part should be valid base64");

            // Verify salt is valid base64
            assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[2]),
                    "Salt part should be valid base64");

            // Verify IV is valid base64
            assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[3]),
                    "IV part should be valid base64");

            // Verify iterations is a valid integer
            assertDoesNotThrow(() -> Integer.parseInt(parts[4]),
                    "Iterations part should be a valid integer");
        }

        @ParameterizedTest(name = "Should reject invalid format case {index}")
        @MethodSource("com.otilm.common.credential.provider.secret.util.SecretsUtilTest#invalidFormatSecrets")
        void shouldThrowExceptionForInvalidFormat(String invalidSecret,
                                                  Class<? extends Throwable> expectedException,
                                                  String messageContains) {
            Throwable exception = assertThrows(
                    expectedException,
                    () -> secretsUtil.decodeAndDecryptSecretString(invalidSecret),
                    "Should throw exception for invalid secret format"
            );

            if (messageContains != null) {
                assertNotNull(exception.getMessage(), "Exception message should not be null");
                assertTrue(exception.getMessage().contains(messageContains),
                        "Exception message should indicate format error");
            }
        }
    }

    @Nested
    @DisplayName("Tampering Detection Tests")
    class TamperingDetectionTests {

        @ParameterizedTest(name = "Should detect tampering of {0}")
        @MethodSource("com.otilm.common.credential.provider.secret.util.SecretsUtilTest#tamperedSecrets")
        void shouldDetectTampering(String tamperedPartLabel, int partIndex, byte[] tamperedData) {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
            String tamperedSecret = tamperPart(encodedSecret, partIndex, tamperedData);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(tamperedSecret),
                    "Should throw exception when " + tamperedPartLabel + " is tampered"
            );

            assertTrue(exception.getMessage().contains("Bad padding") ||
                            exception.getMessage().contains("corrupted or tampered"),
                    "Exception message should indicate data corruption or tampering");
        }
    }

    @Nested
    @DisplayName("Unsupported Version Tests")
    class UnsupportedVersionTests {

        @Test
        @DisplayName("Should throw exception when encrypting with unsupported version")
        void shouldThrowExceptionForUnsupportedEncryptionVersion() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, null),
                    "Should throw exception for null version"
            );

            assertTrue(exception.getMessage().contains("Secret version not supported"),
                    "Exception message should indicate unsupported version");
        }
    }

    @Nested
    @DisplayName("Invalid Iterations Tests")
    class InvalidIterationsTests {

        @Test
        @DisplayName("Should throw exception for invalid iterations format")
        void shouldThrowExceptionForInvalidIterationsFormat() {
            String invalidSecret = "v1|" + Base64.getEncoder().encodeToString("data".getBytes()) +
                    "|" + Base64.getEncoder().encodeToString(new byte[32]) +
                    "|" + Base64.getEncoder().encodeToString(new byte[12]) +
                    "|notAnInteger";

            NumberFormatException exception = assertThrows(
                    NumberFormatException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(invalidSecret),
                    "Should throw exception when iterations is not an integer"
            );

            assertNotNull(exception, "Exception should be thrown for invalid iterations");
        }

        @ParameterizedTest
        @DisplayName("Should throw exception for non-positive iterations")
        @ValueSource(ints = {0, -1})
        void shouldThrowExceptionForNonPositiveIterations(int iterations) {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
            String modifiedSecret = replaceIterations(encodedSecret, iterations);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(modifiedSecret),
                    "Should throw exception when iterations is non-positive"
            );
        }

        @Test
        @DisplayName("Should throw exception for null secret string")
        void shouldThrowExceptionForNullSecretString() {
            assertThrows(
                    NullPointerException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(null),
                    "Should throw exception when secret string is null"
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @ParameterizedTest(name = "Should handle edge case secret {index}")
        @MethodSource("com.otilm.common.credential.provider.secret.util.SecretsUtilTest#edgeCaseSecrets")
        void shouldHandleEdgeCaseSecrets(String secret) {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(secret, VERSION);
            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret);

            assertEquals(secret, decodedSecret,
                    "Edge case secret should be encrypted and decrypted correctly");
        }

        @Test
        @DisplayName("Should handle very long base64 encoded secrets")
        void shouldHandleVeryLongSecrets() {
            String veryLongSecret = "X".repeat(100000);

            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(veryLongSecret, VERSION);
            assertNotNull(encodedSecret, "Encoded secret should not be null");

            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret);
            assertEquals(veryLongSecret, decodedSecret,
                    "Very long secret should be encrypted and decrypted correctly");
        }

        @Test
        @DisplayName("Should maintain consistency across multiple encryption/decryption cycles")
        void shouldMaintainConsistencyAcrossMultipleCycles() {
            String original = STANDARD_SECRET;

            for (int i = 0; i < 5; i++) {
                String encoded = secretsUtil.encryptAndEncodeSecretString(original, VERSION);
                String decoded = secretsUtil.decodeAndDecryptSecretString(encoded);
                assertEquals(original, decoded,
                        "Cycle " + (i + 1) + " should maintain consistency");
            }
        }
    }

    @Nested
    @DisplayName("Format Consistency Tests")
    class FormatConsistencyTests {

        @Test
        @DisplayName("Should always produce 5-part format for V1")
        void shouldAlwaysProduceFivePartFormat() {
            for (int i = 0; i < 10; i++) {
                String encoded = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
                String[] parts = encoded.split("\\|", -1);

                assertEquals(EXPECTED_PARTS_COUNT, parts.length,
                        "Iteration " + i + " should produce exactly " + EXPECTED_PARTS_COUNT + " parts");
                assertEquals("v1", parts[0],
                        "Iteration " + i + " should start with v1");
            }
        }

        @Test
        @DisplayName("Should produce valid base64 in all parts")
        void shouldProduceValidBase64InAllParts() {
            String encoded = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
            String[] parts = encoded.split("\\|");

            // Test encrypted data
            assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[1]),
                    "Encrypted data should be valid base64");

            // Test salt
            assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[2]),
                    "Salt should be valid base64");

            // Test IV
            assertDoesNotThrow(() -> Base64.getDecoder().decode(parts[3]),
                    "IV should be valid base64");
        }

        @Test
        @DisplayName("Should have correct part sizes")
        void shouldHaveCorrectPartSizes() {
            String encoded = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
            String[] parts = encoded.split("\\|");

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] iv = Base64.getDecoder().decode(parts[3]);

            assertEquals(SALT_SIZE_BYTES, salt.length,
                    "Salt should be " + SALT_SIZE_BYTES + " bytes");
            assertEquals(IV_SIZE_BYTES, iv.length,
                    "IV should be " + IV_SIZE_BYTES + " bytes");
        }
    }

    // ==================== Helper Methods ====================

    private static Stream<Arguments> invalidFormatSecrets() {
        return Stream.of(
                Arguments.of("v1|part1|part2", IllegalArgumentException.class,
                        "Secret version not supported"),
                Arguments.of("v1|part1|part2|part3|part4|part5|part6", IllegalArgumentException.class,
                        "Secret version not supported"),
                Arguments.of("v2|part1|part2|part3|part4", IllegalArgumentException.class,
                        "Secret version not supported"),
                Arguments.of("v1|notBase64!@#|notBase64!@#|notBase64!@#|65536",
                        IllegalArgumentException.class, null)
        );
    }

    private static Stream<Arguments> tamperedSecrets() {
        return Stream.of(
                Arguments.of("encrypted data", 1, "tampered data".getBytes()),
                Arguments.of("salt", 2, new byte[SALT_SIZE_BYTES]),
                Arguments.of("IV", 3, new byte[IV_SIZE_BYTES])
        );
    }

    private static Stream<Arguments> edgeCaseSecrets() {
        return Stream.of(
                Arguments.of("secret|with|pipes"),
                Arguments.of("line1\nline2\nline3\ttab"),
                Arguments.of("  leading and trailing spaces  ")
        );
    }

    /**
     * Tampers with a specific part of an encoded secret for testing purposes.
     * This is used in security tests to verify tampering detection.
     *
     * @param encodedSecret the original encoded secret string
     * @param partIndex     the index of the part to tamper (0-based, where 0 is version)
     * @param tamperedData  the data to replace the part with
     * @return the tampered secret string
     */
    private String tamperPart(String encodedSecret, int partIndex, byte[] tamperedData) {
        String[] parts = encodedSecret.split("\\|");
        parts[partIndex] = Base64.getEncoder().encodeToString(tamperedData);
        return String.join("|", parts);
    }

    private String replaceIterations(String encodedSecret, int iterations) {
        String[] parts = encodedSecret.split("\\|");
        parts[4] = String.valueOf(iterations);
        return String.join("|", parts);
    }
}
