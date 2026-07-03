package com.otilm.common.credential.provider.secret.service;

import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.common.attribute.common.BaseAttribute;

import java.util.List;

/**
 * Attribute definitions for the secret/vault provider. These are the
 * vault-instance (authority) attributes the platform shows when a vault is
 * created and sends back on every secret operation as the request's vault
 * attributes. Kept separate from the credential-provider {@code AttributeService}.
 */
public interface VaultAttributeService {

    List<BaseAttribute> listVaultAttributes();

    List<BaseAttribute> listVaultProfileAttributes(List<RequestAttribute> vaultAttributes);
}
