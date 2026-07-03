package com.otilm.common.credential.provider.secret.dao.entity;

import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.connector.secrets.content.SecretContent;
import com.otilm.common.credential.provider.secret.dao.converter.RequestAttributeListToStringConverter;
import com.otilm.common.credential.provider.secret.dao.converter.SecretContentEncryptionConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "secret")
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class Secret {
    @EmbeddedId
    private SecretCompositeId id;

    @Column(name = "secret_content", nullable = false)
    @Convert(converter = SecretContentEncryptionConverter.class)
    private SecretContent secretContent; // encrypted

    @Column(name = "vault_attributes")
    @Convert(converter = RequestAttributeListToStringConverter.class)
    private List<RequestAttribute> vaultAttributes;

    @Column(name = "secret_attributes")
    @Convert(converter = RequestAttributeListToStringConverter.class)
    private List<RequestAttribute> secretAttributes;

    // No metadata. We don't need them.

    public void incrementVersion() {
        id.setVersion(id.getVersion() + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Secret secret = (Secret) o;
        return Objects.equals(id, secret.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
