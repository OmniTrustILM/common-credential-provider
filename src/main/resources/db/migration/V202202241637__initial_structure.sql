CREATE TABLE secret
(
    name              VARCHAR(255) NOT NULL,
    secret_version    VARCHAR(255) NOT NULL,
    secret_type       VARCHAR(255) NOT NULL,
    secret_content    JSONB        NOT NULL,
    vault_attributes  JSONB,
    secret_attributes JSONB,
    CONSTRAINT pk_secret PRIMARY KEY (name, secret_type)
);
