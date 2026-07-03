package com.otilm.common.credential.provider.secret.dao.repository;

import com.otilm.api.model.connector.secrets.SecretType;
import com.otilm.common.credential.provider.secret.dao.entity.Secret;
import com.otilm.common.credential.provider.secret.dao.entity.SecretCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecretRepository extends JpaRepository<Secret, SecretCompositeId> {

    // Every lookup is scoped by namespace (part of the id; "" is the root scope) so scopes stay isolated.

    @Query("SELECT s FROM Secret s WHERE s.id.namespace = :namespace AND s.id.name = :name AND s.id.secretType = :secretType ORDER BY s.id.version DESC")
    List<Secret> findByNamespaceAndNameAndSecretTypeOrderByVersionDesc(@Param("namespace") String namespace, @Param("name") String name, @Param("secretType") SecretType secretType);

    @Query("SELECT s FROM Secret s WHERE s.id.namespace = :namespace AND s.id.name = :name AND s.id.secretType = :secretType AND s.id.version = :version")
    Optional<Secret> findByNamespaceAndNameAndSecretTypeAndVersion(@Param("namespace") String namespace, @Param("name") String name, @Param("secretType") SecretType secretType, @Param("version") int version);

    /**
     * Finds the latest version of a secret within a namespace with pessimistic locking
     * (SELECT FOR UPDATE) to prevent concurrent updates from creating a duplicate version.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Secret s WHERE s.id.namespace = :namespace AND s.id.name = :name AND s.id.secretType = :secretType ORDER BY s.id.version DESC LIMIT 1")
    Optional<Secret> findLatestVersionForUpdate(@Param("namespace") String namespace, @Param("name") String name, @Param("secretType") SecretType secretType);

    @Modifying
    @Query("DELETE FROM Secret s WHERE s.id.namespace = :namespace AND s.id.name = :name AND s.id.secretType = :secretType")
    long deleteByNamespaceAndNameAndSecretType(@Param("namespace") String namespace, @Param("name") String name, @Param("secretType") SecretType secretType);
}
