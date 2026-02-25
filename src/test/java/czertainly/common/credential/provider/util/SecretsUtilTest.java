package czertainly.common.credential.provider.util;

import czertainly.common.credential.provider.BuildInfoTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Base64;

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
     * - Edge cases (special chars, unicode, long secrets, etc.)
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
            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, VERSION);

            assertEquals(secret, decodedSecret,
                    "Decrypted secret should match original secret");
        }

        @Test
        @DisplayName("Should handle long secrets correctly")
        void shouldHandleLongSecrets() {
            String longSecret = "A".repeat(10000);

            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(longSecret, VERSION);
            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, VERSION);

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
            String decodedSecret1 = secretsUtil.decodeAndDecryptSecretString(encodedSecret1, VERSION);
            String decodedSecret2 = secretsUtil.decodeAndDecryptSecretString(encodedSecret2, VERSION);

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

            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, VERSION);

            assertEquals(STANDARD_SECRET, decodedSecret,
                    "Decrypted secret should match original secret");
        }

        @Test
        @DisplayName("Should decrypt empty string correctly")
        void shouldDecryptEmptyString() {
            String emptySecret = "";
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(emptySecret, VERSION);

            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, VERSION);

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

        @Test
        @DisplayName("Should throw exception for invalid format")
        void shouldThrowExceptionForInvalidFormat() {
            String invalidSecret = "not|a|valid|secret";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(invalidSecret, VERSION),
                    "Should throw IllegalArgumentException for invalid format"
            );

            assertTrue(exception.getMessage().contains("Secret string is not in the correct format"),
                    "Exception message should indicate invalid format");
        }

        @Test
        @DisplayName("Should throw exception for missing parts")
        void shouldThrowExceptionForMissingParts() {
            String invalidSecret = "v1|part1|part2";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(invalidSecret, VERSION),
                    "Should throw exception when secret has missing parts"
            );

            assertTrue(exception.getMessage().contains("Secret string is not in the correct format"),
                    "Exception message should indicate format error");
        }

        @Test
        @DisplayName("Should throw exception for too many parts")
        void shouldThrowExceptionForTooManyParts() {
            String invalidSecret = "v1|part1|part2|part3|part4|part5|part6";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(invalidSecret, VERSION),
                    "Should throw exception when secret has too many parts"
            );

            assertTrue(exception.getMessage().contains("Secret string is not in the correct format"),
                    "Exception message should indicate format error");
        }

        @Test
        @DisplayName("Should throw exception for wrong version")
        void shouldThrowExceptionForWrongVersion() {
            String invalidSecret = "v2|part1|part2|part3|part4";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(invalidSecret, VERSION),
                    "Should throw exception for unsupported version"
            );

            assertTrue(exception.getMessage().contains("Secret string is not in the correct format"),
                    "Exception message should indicate format error");
        }

        @Test
        @DisplayName("Should throw exception for invalid base64 encoding")
        void shouldThrowExceptionForInvalidBase64() {
            String invalidSecret = "v1|notBase64!@#|notBase64!@#|notBase64!@#|65536";

            assertThrows(
                    IllegalArgumentException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(invalidSecret, VERSION),
                    "Should throw exception for invalid base64 encoding"
            );
        }
    }

    @Nested
    @DisplayName("Tampering Detection Tests")
    class TamperingDetectionTests {

        @Test
        @DisplayName("Should detect tampered encrypted data")
        void shouldDetectTamperedEncryptedData() {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
            String tamperedSecret = tamperPart(encodedSecret, 1, "tampered data".getBytes());

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(tamperedSecret, VERSION),
                    "Should throw exception when encrypted data is tampered"
            );

            assertTrue(exception.getMessage().contains("Bad padding") ||
                            exception.getMessage().contains("corrupted or tampered"),
                    "Exception message should indicate data corruption or tampering");
        }

        @Test
        @DisplayName("Should detect tampered salt")
        void shouldDetectTamperedSalt() {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
            String tamperedSecret = tamperPart(encodedSecret, 2, new byte[SALT_SIZE_BYTES]);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(tamperedSecret, VERSION),
                    "Should throw exception when salt is tampered"
            );

            assertTrue(exception.getMessage().contains("Bad padding") ||
                            exception.getMessage().contains("corrupted or tampered"),
                    "Exception message should indicate data corruption or tampering");
        }

        @Test
        @DisplayName("Should detect tampered IV")
        void shouldDetectTamperedIV() {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);
            String tamperedSecret = tamperPart(encodedSecret, 3, new byte[IV_SIZE_BYTES]);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(tamperedSecret, VERSION),
                    "Should throw exception when IV is tampered"
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

        @Test
        @DisplayName("Should throw exception when decrypting with unsupported version")
        void shouldThrowExceptionForUnsupportedDecryptionVersion() {
            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(STANDARD_SECRET, VERSION);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(encodedSecret, null),
                    "Should throw exception for null version during decryption"
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
                    () -> secretsUtil.decodeAndDecryptSecretString(invalidSecret, VERSION),
                    "Should throw exception when iterations is not an integer"
            );

            assertNotNull(exception, "Exception should be thrown for invalid iterations");
        }

        @Test
        @DisplayName("Should throw exception for null secret string")
        void shouldThrowExceptionForNullSecretString() {
            assertThrows(
                    NullPointerException.class,
                    () -> secretsUtil.decodeAndDecryptSecretString(null, VERSION),
                    "Should throw exception when secret string is null"
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle secret with pipe character")
        void shouldHandleSecretWithPipeCharacter() {
            String secretWithPipe = "secret|with|pipes";

            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(secretWithPipe, VERSION);
            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, VERSION);

            assertEquals(secretWithPipe, decodedSecret,
                    "Secret containing pipe characters should be encrypted and decrypted correctly");
        }

        @Test
        @DisplayName("Should handle very long base64 encoded secrets")
        void shouldHandleVeryLongSecrets() {
            String veryLongSecret = "X".repeat(100000);

            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(veryLongSecret, VERSION);
            assertNotNull(encodedSecret, "Encoded secret should not be null");

            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, VERSION);
            assertEquals(veryLongSecret, decodedSecret,
                    "Very long secret should be encrypted and decrypted correctly");
        }

        @Test
        @DisplayName("Should handle secret with newlines and special formatting")
        void shouldHandleSecretWithNewlines() {
            String secretWithNewlines = "line1\nline2\nline3\ttab";

            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(secretWithNewlines, VERSION);
            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, VERSION);

            assertEquals(secretWithNewlines, decodedSecret,
                    "Secret with newlines should be encrypted and decrypted correctly");
        }

        @Test
        @DisplayName("Should maintain consistency across multiple encryption/decryption cycles")
        void shouldMaintainConsistencyAcrossMultipleCycles() {
            String original = STANDARD_SECRET;

            for (int i = 0; i < 5; i++) {
                String encoded = secretsUtil.encryptAndEncodeSecretString(original, VERSION);
                String decoded = secretsUtil.decodeAndDecryptSecretString(encoded, VERSION);
                assertEquals(original, decoded,
                        "Cycle " + (i + 1) + " should maintain consistency");
            }
        }

        @Test
        @DisplayName("Should preserve exact content of secret including leading/trailing spaces")
        void shouldPreserveExactContent() {
            String secretWithSpaces = "  leading and trailing spaces  ";

            String encodedSecret = secretsUtil.encryptAndEncodeSecretString(secretWithSpaces, VERSION);
            String decodedSecret = secretsUtil.decodeAndDecryptSecretString(encodedSecret, VERSION);

            assertEquals(secretWithSpaces, decodedSecret,
                    "Exact content with spaces should be preserved");
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
}
