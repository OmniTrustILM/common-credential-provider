package com.otilm.common.credential.provider.secret.api.v2;

import java.util.UUID;

/**
 * Thrown by the callback surface when the requested attribute exists in the registry but declares no
 * callback, so there is nothing to resolve. Distinct from {@link AttributeDefinitionNotFoundException}
 * (unknown UUID): a registry refresh would never make a non-dispatchable attribute dispatchable, so
 * this is a non-retryable rejection rather than a not-found. Unchecked because the
 * {@code AttributesController} interface methods declare no checked exceptions.
 */
public class AttributeCallbackNotSupportedException extends RuntimeException {

    private final UUID uuid;

    public AttributeCallbackNotSupportedException(UUID uuid) {
        super("Attribute defines no callback: " + uuid);
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
