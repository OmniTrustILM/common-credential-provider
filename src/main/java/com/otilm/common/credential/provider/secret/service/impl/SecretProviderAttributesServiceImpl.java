package com.otilm.common.credential.provider.secret.service.impl;

import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackRequestDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackResponseDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeDefinitionsDto;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.common.credential.provider.secret.api.v2.AttributeCallbackNotSupportedException;
import com.otilm.common.credential.provider.secret.api.v2.AttributeDefinitionNotFoundException;
import com.otilm.common.credential.provider.secret.service.SecretProviderAttributesService;
import com.otilm.common.credential.provider.secret.service.VaultAttributeService;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SecretProviderAttributesServiceImpl implements SecretProviderAttributesService {

    private final BuildProperties buildProperties;
    private final VaultAttributeService vaultAttributeService;

    public SecretProviderAttributesServiceImpl(BuildProperties buildProperties,
                                               VaultAttributeService vaultAttributeService) {
        this.buildProperties = buildProperties;
        this.vaultAttributeService = vaultAttributeService;
    }

    /**
     * Startup self-validation: fail fast if the connector-global registry contains a malformed or
     * duplicate UUID. This connector declares no callbacks, so there is no callback-dispatchability
     * check here yet.
     */
    @PostConstruct
    void validateRegistry() {
        Set<UUID> seen = new HashSet<>();
        for (BaseAttribute definition : allDefinitions()) {
            UUID uuid = definitionUuid(definition);
            if (uuid == null) {
                throw new IllegalStateException(
                        "Attribute UUID is not a valid UUID: " + definition.getUuid());
            }
            if (!seen.add(uuid)) {
                throw new IllegalStateException("Duplicate attribute UUID in connector registry: " + uuid);
            }
        }
    }

    /**
     * Every NG attribute definition this connector exposes. Today that is only the vault-instance
     * attributes: the other NG sources (vault-profile, per-secret-type, rotate) return empty — pinned
     * by their own tests — so aggregating {@code listVaultAttributeDefinitions()} is complete. If any
     * of those sources ever becomes non-empty it must be wired in here too, or it will be absent from
     * the registry.
     */
    private List<BaseAttribute> allDefinitions() {
        return vaultAttributeService.listVaultAttributeDefinitions();
    }

    @Override
    public AttributeDefinitionsDto listDefinitions(List<UUID> uuids) {
        List<BaseAttribute> definitions = allDefinitions();
        if (uuids != null && !uuids.isEmpty()) {
            Set<UUID> wanted = new HashSet<>(uuids);
            definitions = definitions.stream()
                    .filter(definition -> wanted.contains(definitionUuid(definition)))
                    .toList();
        }
        AttributeDefinitionsDto dto = new AttributeDefinitionsDto();
        dto.setConnectorVersion(buildProperties.getVersion());
        dto.setDefinitions(definitions);
        return dto;
    }

    @Override
    public BaseAttribute getDefinition(UUID uuid) {
        return allDefinitions().stream()
                .filter(definition -> uuid.equals(definitionUuid(definition)))
                .findFirst()
                .orElseThrow(() -> new AttributeDefinitionNotFoundException(uuid));
    }

    @Override
    public AttributeCallbackResponseDto callback(AttributeCallbackRequestDto request) {
        UUID uuid = request.getAttributeUuid();
        boolean known = allDefinitions().stream()
                .anyMatch(definition -> uuid.equals(definitionUuid(definition)));
        if (!known) {
            // Unknown to this connector — 404; Core refreshing its registry is a sensible reaction.
            throw new AttributeDefinitionNotFoundException(uuid);
        }
        // The attribute is known but declares no NG callback, so nothing can be resolved. A registry
        // refresh would not change that, so this is a non-retryable rejection rather than a not-found.
        // Core does not reach here in normal operation — it dispatches only callback-enabled attributes.
        throw new AttributeCallbackNotSupportedException(uuid);
    }

    /**
     * The attribute's identifier as a {@link UUID}, or {@code null} if it is absent or not a valid
     * UUID. Matching by value keeps lookups correct regardless of the identifier's textual form;
     * {@link #validateRegistry()} rejects a malformed identifier at startup.
     */
    private static UUID definitionUuid(BaseAttribute definition) {
        String raw = definition.getUuid();
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
