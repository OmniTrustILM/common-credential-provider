CREATE TABLE secret
(
    name              VARCHAR(255) NOT NULL,
    secret_type       VARCHAR(255) NOT NULL,
    secret_version    INTEGER      NOT NULL,
    secret_content    VARCHAR      NOT NULL,
    vault_attributes  TEXT,
    secret_attributes TEXT,
    CONSTRAINT pk_secret PRIMARY KEY (name, secret_type, secret_version)
);
