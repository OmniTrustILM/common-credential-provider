package czertainly.common.credential.provider.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Component
public class SecretsUtil {
    // Using secure authenticated encryption: AES/GCM/NoPadding with PBKDF2
    private static final String AEAD_ALGORITHM = "AES/GCM/NoPadding";
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536; // Recommended minimum for PBKDF2
    private static final int AES_KEY_BIT_LENGTH = 256; // 256-bit AES key
    private static final int GCM_IV_BYTE_LENGTH = 12; // 96-bit IV for GCM
    private static final int GCM_TAG_BIT_LENGTH = 128; // 128-bit authentication tag

    private final SecureRandom random = new SecureRandom();
    private String encryptionKey;

    @Autowired
    public void setEncryptionKey(@Value("${secrets.encryption.key}") String key) {
        this.encryptionKey = key;
    }

    /**
     * Encrypts and encodes the given secret using AES/GCM/NoPadding algorithm with PBKDF2 key derivation.
     * This provides authenticated encryption with associated data (AEAD).
     *
     * @param secret        the secret to encrypt and encode
     * @param secretVersion the version of the encoding
     * @return the encrypted and encoded secret
     */
    public String encryptAndEncodeSecretString(String secret, SecretEncodingVersion secretVersion) {
        if (secret == null) {
            return null;
        }

        byte[] salt = generateRandomSalt();
        byte[] iv = generateRandomIV();

        byte[] encryptedSecret;

        try {
            // Derive key using PBKDF2
            SecretKey key = deriveKey(salt, ITERATIONS);

            // Initialize cipher with GCM mode
            Cipher cipher = Cipher.getInstance(AEAD_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            encryptedSecret = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("Padding for " + AEAD_ALGORITHM + " not found.", e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Illegal block size for " + AEAD_ALGORITHM, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm " + AEAD_ALGORITHM + " not found", e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException("Bad padding for " + AEAD_ALGORITHM, e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid key provided for " + AEAD_ALGORITHM, e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Invalid algorithm parameters for " + AEAD_ALGORITHM, e);
        }

        if (secretVersion == SecretEncodingVersion.V1) {
            return encodeSecretStringV1(encryptedSecret, salt, iv, ITERATIONS);
        } else {
            throw new IllegalArgumentException("Secret version not supported");
        }

    }

    public String decodeAndDecryptSecretString(String secret, SecretEncodingVersion secretVersion) {
        byte[] salt;
        byte[] iv;
        int iterations;
        byte[] encryptedSecret;
        if (secretVersion == SecretEncodingVersion.V1) {
            salt = decodeSaltFromSecretStringV1(secret);
            iv = decodeIVFromSecretStringV1(secret);
            iterations = getIterationsFromSecretStringV1(secret);
            encryptedSecret = decodeEncryptedSecretFromSecretStringV1(secret);
        } else {
            throw new IllegalArgumentException("Secret version not supported");
        }

        try {
            // Derive key using PBKDF2
            SecretKey key = deriveKey(salt, iterations);

            // Initialize cipher with GCM mode
            Cipher cipher = Cipher.getInstance(AEAD_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            byte[] decryptedSecret = cipher.doFinal(encryptedSecret);
            return new String(decryptedSecret, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("Padding for " + AEAD_ALGORITHM + " not found.", e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Illegal block size for " + AEAD_ALGORITHM, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm " + AEAD_ALGORITHM + " not found", e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException("Bad padding for " + AEAD_ALGORITHM + " - data may be corrupted or tampered", e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid key provided for " + AEAD_ALGORITHM, e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Invalid algorithm parameters for " + AEAD_ALGORITHM, e);
        }
    }

    /**
     * Encodes the secret value into string
     * v1|secret|salt|iv|iterations
     *
     * @param secret value to be encoded
     * @param salt   used salt for key derivation
     * @param iv     initialization vector for GCM
     * @param iterations number of iterations
     * @return encoded string
     */
    private static String encodeSecretStringV1(byte[] secret, byte[] salt, byte[] iv, int iterations) {
        return SecretEncodingVersion.V1.getVersion() +
                "|" +
                Base64.getEncoder().encodeToString(secret) +
                "|" +
                Base64.getEncoder().encodeToString(salt) +
                "|" +
                Base64.getEncoder().encodeToString(iv) +
                "|" +
                iterations;
    }

    private static byte[] decodeSaltFromSecretStringV1(String secret) {
        if (isSecretStringV1(secret)) {
            String[] parts = secret.split("\\|");
            return Base64.getDecoder().decode(parts[2]);
        } else {
            throw new IllegalArgumentException("Secret string is not in the correct format");
        }
    }

    private static byte[] decodeIVFromSecretStringV1(String secret) {
        if (isSecretStringV1(secret)) {
            String[] parts = secret.split("\\|");
            return Base64.getDecoder().decode(parts[3]);
        } else {
            throw new IllegalArgumentException("Secret string is not in the correct format");
        }
    }

    private static int getIterationsFromSecretStringV1(String secret) {
        if (isSecretStringV1(secret)) {
            String[] parts = secret.split("\\|");
            return Integer.parseInt(parts[4]);
        } else {
            throw new IllegalArgumentException("Secret string is not in the correct format");
        }
    }

    private static byte[] decodeEncryptedSecretFromSecretStringV1(String secret) {
        if (isSecretStringV1(secret)) {
            String[] parts = secret.split("\\|");
            return Base64.getDecoder().decode(parts[1]);
        } else {
            throw new IllegalArgumentException("Secret string is not in the correct format");
        }
    }

    private static boolean isSecretStringV1(String secret) {
        String[] parts = secret.split("\\|");
        if (parts.length != 5) {
            return false;
        }
        return parts[0].equals("v1");
    }

    /**
     * Derives an AES key from the password using PBKDF2
     *
     * @param salt the salt for key derivation
     * @param iterations the number of iterations for PBKDF2
     * @return the derived SecretKey
     */
    private SecretKey deriveKey(byte[] salt, int iterations) {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(encryptionKey.toCharArray(), salt, iterations, AES_KEY_BIT_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm " + KEY_DERIVATION_ALGORITHM + " not found", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid key specification", e);
        }
    }

    /**
     * Generate random salt for key derivation
     *
     * @return salt
     */
    private byte[] generateRandomSalt() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generate random IV for GCM mode
     *
     * @return initialization vector
     */
    private byte[] generateRandomIV() {
        byte[] iv = new byte[GCM_IV_BYTE_LENGTH];
        random.nextBytes(iv);
        return iv;
    }
}
