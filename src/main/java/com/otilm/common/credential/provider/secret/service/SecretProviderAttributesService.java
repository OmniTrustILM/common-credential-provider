package com.otilm.common.credential.provider.secret.service;

import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackRequestDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackResponseDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeDefinitionsDto;
import com.otilm.api.model.common.attribute.common.BaseAttribute;

import java.util.List;
import java.util.UUID;

/**
 * The common Attributes v2 provider for this connector's NG Secret Provider: the UUID-keyed
 * attribute-definition registry plus the callback surface. The registry aggregates the definitions
 * the connector's existing attribute owners produce; it does not own or mutate them.
 */
public interface SecretProviderAttributesService {

    /** Full registry, or the found-only subset when {@code uuids} is non-empty (unknowns dropped). */
    AttributeDefinitionsDto listDefinitions(List<UUID> uuids);

    /** A single definition by connector-global UUID; throws when unknown. */
    BaseAttribute getDefinition(UUID uuid);

    /** Resolve dynamic content for a callback-enabled attribute. This connector declares none. */
    AttributeCallbackResponseDto callback(AttributeCallbackRequestDto request);
}
