package czertainly.common.credential.provider.util;

import lombok.Getter;

@Getter
public enum SecretEncodingVersion {
    V1("v1");

    private final String version;

    SecretEncodingVersion(String version) {
        this.version = version;
    }
}
