package com.otilm.common.credential.provider.secret.dao.entity;

import com.otilm.api.model.connector.secrets.SecretType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Composite primary key for Secret entity. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SecretCompositeId implements Serializable {
    // Scope the secret belongs to; "" is the root scope. Part of the identity so the
    // same name can exist independently per namespace and scopes stay isolated.
    @Column(name = "namespace", nullable = false, updatable = false)
    private String namespace;

    @Column(name = "name", nullable = false, updatable = false)
    private String name;

    @Column(name = "secret_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private SecretType secretType;

    @Column(name = "secret_version", nullable = false, updatable = false)
    private int version;
}
