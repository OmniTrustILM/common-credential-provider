package com.otilm.common.credential.provider.dao.repository;

import com.otilm.api.model.connector.secrets.SecretType;
import com.otilm.common.credential.provider.dao.entity.Secret;
import com.otilm.common.credential.provider.dao.entity.SecretCompositeId;
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
    @Query("SELECT s FROM Secret s WHERE s.id.name = :name AND s.id.secretType = :secretType ORDER BY s.id.version DESC")
    List<Secret> findByNameAndSecretTypeOrderByVersionDesc(@Param("name") String name, @Param("secretType") SecretType secretType);

    @Query("SELECT s FROM Secret s WHERE s.id.name = :name AND s.id.secretType = :secretType AND s.id.version = :version")
    Optional<Secret> findByNameAndSecretTypeAndVersion(@Param("name") String name, @Param("secretType") SecretType secretType, @Param("version") int version);

    /**
     * Finds the latest version of a secret with pessimistic locking to prevent concurrent updates.
     * Uses SELECT FOR UPDATE to lock the row.
     *
     * @param name the secret name
     * @param secretType the secret type
     * @return the latest version (highest version number) with an exclusive lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Secret s WHERE s.id.name = :name AND s.id.secretType = :secretType ORDER BY s.id.version DESC LIMIT 1")
    Optional<Secret> findLatestVersionForUpdate(@Param("name") String name, @Param("secretType") SecretType secretType);

    @Modifying
    @Query("DELETE FROM Secret s WHERE s.id.name = :name AND s.id.secretType = :secretType")
    long deleteByNameAndSecretType(@Param("name") String name, @Param("secretType") SecretType secretType);
}
