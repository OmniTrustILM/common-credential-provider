package com.otilm.common.credential.provider.secret.service.impl;

import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.common.attribute.common.AttributeType;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.common.attribute.common.content.AttributeContentType;
import com.otilm.api.model.common.attribute.common.properties.DataAttributeProperties;
import com.otilm.api.model.common.attribute.v3.DataAttributeV3;
import com.otilm.api.model.common.attribute.v3.content.StringAttributeContentV3;
import com.otilm.common.credential.provider.secret.dao.repository.SecretRepository;
import com.otilm.common.credential.provider.secret.service.VaultAttributeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VaultAttributeServiceImpl implements VaultAttributeService {

    public static final String ATTRIBUTE_NAMESPACE = "data_namespace";
    public static final String ATTRIBUTE_NAMESPACE_LABEL = "Namespace";
    private static final String ATTRIBUTE_NAMESPACE_UUID = "d18a014f-cfb9-4e72-8acc-4616e14fe8ad";

    private final SecretRepository secretRepository;

    public VaultAttributeServiceImpl(SecretRepository secretRepository) {
        this.secretRepository = secretRepository;
    }

    @Override
    public List<BaseAttribute> listVaultAttributes() {
        return List.of(namespaceAttribute());
    }

    @Override
    public List<BaseAttribute> listVaultProfileAttributes(List<RequestAttribute> vaultAttributes) {
        return List.of();
    }

    private BaseAttribute namespaceAttribute() {
        DataAttributeV3 namespace = new DataAttributeV3();
        namespace.setUuid(ATTRIBUTE_NAMESPACE_UUID);
        namespace.setName(ATTRIBUTE_NAMESPACE);
        namespace.setDescription("Optional namespace used to group and scope secrets within this vault");
        namespace.setType(AttributeType.DATA);
        namespace.setContentType(AttributeContentType.STRING);

        // Offer the namespaces already in use as options; extensibleList lets the operator type a new one.
        List<StringAttributeContentV3> existing = secretRepository.findDistinctNamespaces().stream()
                .map(StringAttributeContentV3::new)
                .toList();
        namespace.setContent(existing);

        DataAttributeProperties properties = new DataAttributeProperties();
        properties.setLabel(ATTRIBUTE_NAMESPACE_LABEL);
        properties.setRequired(false);
        properties.setReadOnly(false);
        properties.setVisible(true);
        properties.setList(true);
        properties.setExtensibleList(true);
        properties.setMultiSelect(false);
        properties.setGroup("Vault");
        namespace.setProperties(properties);

        return namespace;
    }
}
