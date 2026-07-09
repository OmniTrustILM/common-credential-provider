package com.otilm.common.credential.provider.secret.api.v2;

import java.util.UUID;

/**
 * Thrown when a requested attribute UUID is not a resolvable definition on this connector —
 * unknown to the registry, or (on the callback surface) present but not callback-dispatchable.
 * Unchecked because the {@code AttributesController} interface methods declare no checked exceptions.
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
