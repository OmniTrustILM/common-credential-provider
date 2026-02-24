package czertainly.common.credential.provider.dao.repository;

import com.czertainly.api.model.connector.secrets.SecretType;
import czertainly.common.credential.provider.dao.entity.Secret;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecretRepository extends JpaRepository<Secret, UUID> {
    Optional<Secret> findByNameAndSecretType(String name, SecretType secretType);
    long deleteByNameAndSecretType(String name, SecretType secretType);
}
