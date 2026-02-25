package czertainly.common.credential.provider.dao.entity;

import com.czertainly.api.model.client.attribute.RequestAttribute;
import com.czertainly.api.model.connector.secrets.SecretType;
import com.czertainly.api.model.connector.secrets.content.SecretContent;
import czertainly.common.credential.provider.util.SecretContentEncryptionConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
public class Secret {
    @Id
    @Column(name = "name", nullable = false, updatable = false)
    private String name;

    @Column(name = "secret_version", nullable = false)
    private String secretVersion;

    @Column(name = "secret_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SecretType secretType;

    @Column(name = "secret_content", nullable = false, length = 65535)
    @Convert(converter = SecretContentEncryptionConverter.class)
    private SecretContent secretContent; // encrypted

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vault_attributes", columnDefinition = "jsonb")
    private List<RequestAttribute> vaultAttributes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "secret_attributes", columnDefinition = "jsonb")
    private List<RequestAttribute> secretAttributes;

    // No metadata. We don't need them.

    public void incrementVersionIfNumeric() {
        try {
            int existingVersion = Integer.parseInt(secretVersion);
            setSecretVersion(String.valueOf(existingVersion + 1));
        } catch (NumberFormatException e) {
            // Preserve the existing version when it cannot be parsed as a number.
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Secret secret = (Secret) o;
        return Objects.equals(name, secret.name) && Objects.equals(secretType, secret.secretType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, secretType);
    }
}
