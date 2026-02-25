package czertainly.common.credential.provider.dao.repository;

import com.czertainly.api.model.connector.secrets.SecretType;
import czertainly.common.credential.provider.dao.entity.Secret;
import czertainly.common.credential.provider.dao.entity.SecretCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecretRepository extends JpaRepository<Secret, SecretCompositeId> {
    @Query("SELECT s FROM Secret s WHERE s.id.name = :name AND s.id.secretType = :secretType ORDER BY s.id.version DESC")
    List<Secret> findByNameAndSecretTypeOrderByVersionDesc(@Param("name") String name, @Param("secretType") SecretType secretType);

    @Query("SELECT s FROM Secret s WHERE s.id.name = :name AND s.id.secretType = :secretType AND s.id.version = :version")
    Optional<Secret> findByNameAndSecretTypeAndVersion(@Param("name") String name, @Param("secretType") SecretType secretType, @Param("version") String version);

    @Modifying
    @Query("DELETE FROM Secret s WHERE s.id.name = :name AND s.id.secretType = :secretType")
    long deleteByNameAndSecretType(@Param("name") String name, @Param("secretType") SecretType secretType);
}
