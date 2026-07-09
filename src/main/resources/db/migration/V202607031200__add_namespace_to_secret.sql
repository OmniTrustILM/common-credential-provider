-- Namespace scopes a secret; it is part of the identity so the same name can exist
-- independently per namespace and scopes stay isolated. "" is the root scope; existing
-- rows backfill to it via the default, so a vault instance with no namespace still sees them.
ALTER TABLE secret ADD COLUMN namespace VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE secret DROP CONSTRAINT pk_secret;
ALTER TABLE secret ADD CONSTRAINT pk_secret PRIMARY KEY (namespace, name, secret_type, secret_version);
