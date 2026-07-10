package com.otilm.common.credential.provider.secret.service;

import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.common.attribute.common.content.AttributeContentType;
import com.otilm.api.model.common.attribute.v3.DataAttributeV3;
import com.otilm.api.model.common.attribute.v3.content.StringAttributeContentV3;
import com.otilm.common.credential.provider.secret.dao.repository.SecretRepository;
import com.otilm.common.credential.provider.secret.service.impl.VaultAttributeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class VaultAttributeServiceTest {

    private final SecretRepository secretRepository = mock(SecretRepository.class);
    private final VaultAttributeService vaultAttributeService = new VaultAttributeServiceImpl(secretRepository);

    @Test
    void listVaultAttributes_returnsExtensibleNamespaceListOfExistingNamespaces() {
        when(secretRepository.findDistinctNamespaces()).thenReturn(List.of("team-a", "team-b"));

        List<BaseAttribute> attributes = vaultAttributeService.listVaultAttributes();

        Assertions.assertEquals(1, attributes.size());
        DataAttributeV3 namespace = (DataAttributeV3) attributes.get(0);
        Assertions.assertEquals(VaultAttributeServiceImpl.ATTRIBUTE_NAMESPACE, namespace.getName());
        Assertions.assertEquals(AttributeContentType.STRING, namespace.getContentType());
        Assertions.assertFalse(namespace.getProperties().isRequired(), "namespace attribute must be optional");
        Assertions.assertTrue(namespace.getProperties().isList(), "namespace must be a list");
        Assertions.assertTrue(namespace.getProperties().isExtensibleList(), "operator must be able to add a new namespace");

        List<String> offered = namespace.getContent().stream()
                .map(c -> ((StringAttributeContentV3) c).getData())
                .toList();
        Assertions.assertEquals(List.of("team-a", "team-b"), offered);
    }

    @Test
    void listVaultProfileAttributes_isEmpty() {
        Assertions.assertEquals(List.of(), vaultAttributeService.listVaultProfileAttributes());
    }

    @Test
    void namespaceAttributeDeclaresNoCallback() {
        DataAttributeV3 namespace =
                (DataAttributeV3) vaultAttributeService.listVaultAttributes().get(0);
        Assertions.assertNull(namespace.getAttributeCallback(),
                "data_namespace must declare no callback — options are baked extensible-list content");
    }

    @Test
    void listVaultAttributeDefinitions_hasNoResolvedContentAndDoesNotQueryNamespaces() {
        DataAttributeV3 namespace =
                (DataAttributeV3) vaultAttributeService.listVaultAttributeDefinitions().get(0);

        Assertions.assertEquals(VaultAttributeServiceImpl.ATTRIBUTE_NAMESPACE, namespace.getName());
        Assertions.assertTrue(namespace.getProperties().isExtensibleList());
        Assertions.assertNull(namespace.getContent(), "registry definition carries no resolved option content");
        verifyNoInteractions(secretRepository);
    }
}
