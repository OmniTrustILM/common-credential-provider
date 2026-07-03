package com.otilm.common.credential.provider.secret.service.impl;

import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.common.attribute.common.AttributeType;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.common.attribute.common.content.AttributeContentType;
import com.otilm.api.model.common.attribute.common.properties.DataAttributeProperties;
import com.otilm.api.model.common.attribute.v2.DataAttributeV2;
import com.otilm.common.credential.provider.secret.service.VaultAttributeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VaultAttributeServiceImpl implements VaultAttributeService {

    public static final String ATTRIBUTE_NAMESPACE = "namespace";
    public static final String ATTRIBUTE_NAMESPACE_LABEL = "Namespace";
    private static final String ATTRIBUTE_NAMESPACE_UUID = "d18a014f-cfb9-4e72-8acc-4616e14fe8ad";

    @Override
    public List<BaseAttribute> listVaultAttributes() {
        return List.of(namespaceAttribute());
    }

    @Override
    public List<BaseAttribute> listVaultProfileAttributes(List<RequestAttribute> vaultAttributes) {
        return List.of();
    }

    private BaseAttribute namespaceAttribute() {
        DataAttributeV2 namespace = new DataAttributeV2();
        namespace.setUuid(ATTRIBUTE_NAMESPACE_UUID);
        namespace.setName(ATTRIBUTE_NAMESPACE);
        namespace.setDescription("Optional namespace used to group and scope secrets within this vault");
        namespace.setType(AttributeType.DATA);
        namespace.setContentType(AttributeContentType.STRING);

        DataAttributeProperties properties = new DataAttributeProperties();
        properties.setLabel(ATTRIBUTE_NAMESPACE_LABEL);
        properties.setRequired(false);
        properties.setReadOnly(false);
        properties.setVisible(true);
        properties.setList(false);
        properties.setMultiSelect(false);
        properties.setGroup("Vault");
        namespace.setProperties(properties);

        return namespace;
    }
}
