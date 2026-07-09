package com.otilm.common.credential.provider.secret.api.v2;

import java.util.UUID;

/**
 * Thrown when a requested attribute UUID is unknown to this connector's registry (no matching
 * definition). A known attribute that merely declares no callback is a different case — see
 * {@link AttributeCallbackNotSupportedException}. Unchecked because the {@code AttributesController}
 * interface methods declare no checked exceptions.
 */
public class AttributeDefinitionNotFoundException extends RuntimeException {

    private final transient UUID uuid;

    public AttributeDefinitionNotFoundException(UUID uuid) {
        super("Attribute definition not found for UUID: " + uuid);
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
