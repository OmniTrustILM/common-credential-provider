package com.otilm.common.credential.provider.dao.entity;

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
    @Column(name = "name", nullable = false, updatable = false)
    private String name;

    @Column(name = "secret_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private SecretType secretType;

    @Column(name = "secret_version", nullable = false, updatable = false)
    private int version;
}
