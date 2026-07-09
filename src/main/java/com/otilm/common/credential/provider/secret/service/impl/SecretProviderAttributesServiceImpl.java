package com.otilm.common.credential.provider.secret.service.impl;

import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackRequestDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackResponseDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeDefinitionsDto;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
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
     * Startup self-validation: fail fast if the connector-global registry contains a
     * duplicate UUID. The "every {@code dependsOn} attribute is dispatchable" half is vacuously
     * satisfied — this connector declares no callbacks — but the check guards future additions.
     */
    @PostConstruct
    void validateRegistry() {
        Set<String> seen = new HashSet<>();
        for (BaseAttribute definition : allDefinitions()) {
            if (!seen.add(definition.getUuid())) {
                throw new IllegalStateException(
                        "Duplicate attribute UUID in connector registry: " + definition.getUuid());
            }
        }
    }

    /**
     * Every NG attribute definition this connector exposes. Only vault-instance attributes are
     * non-empty today; the other NG sources (vault-profile, per-secret-type, rotate) return empty,
     * so aggregating {@code listVaultAttributes()} is complete. New attributes appear here once their
     * owner returns them.
     */
    private List<BaseAttribute> allDefinitions() {
        return vaultAttributeService.listVaultAttributes();
    }

    @Override
    public AttributeDefinitionsDto listDefinitions(List<UUID> uuids) {
        List<BaseAttribute> definitions = allDefinitions();
        if (uuids != null && !uuids.isEmpty()) {
            Set<String> wanted = new HashSet<>();
            uuids.forEach(uuid -> wanted.add(uuid.toString()));
            definitions = definitions.stream()
                    .filter(definition -> wanted.contains(definition.getUuid()))
                    .toList();
        }
        AttributeDefinitionsDto dto = new AttributeDefinitionsDto();
        dto.setConnectorVersion(buildProperties.getVersion());
        dto.setDefinitions(definitions);
        return dto;
    }

    @Override
    public BaseAttribute getDefinition(UUID uuid) {
        String key = uuid.toString();
        return allDefinitions().stream()
                .filter(definition -> key.equals(definition.getUuid()))
                .findFirst()
                .orElseThrow(() -> new AttributeDefinitionNotFoundException(uuid));
    }

    @Override
    public AttributeCallbackResponseDto callback(AttributeCallbackRequestDto request) {
        // No attribute declares an NG callback (dependsOn), so nothing is dispatchable. In normal
        // operation Core never calls this — it only dispatches for attributes advertising dependsOn.
        throw new AttributeDefinitionNotFoundException(request.getAttributeUuid());
    }
}
