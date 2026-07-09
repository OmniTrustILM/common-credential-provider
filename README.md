# Common Credential Provider

> This repository is part of the commercial open-source project ILM. You can find more information about the project at the [ILM](https://github.com/OmniTrustILM/ilm) repository, including the contribution guide.

Common Credential Provider is the implementation of basic `Credential` `Kinds` and validation interfaces. This `Connector` provides options to add the `Credential` to the `Core` which can be used within other objects in the platform for authentication and authorization purposes.

> `Credentials` provided by the Common Credential Provider are not involved in
the platform authentication and authorization process.

Common Credential Provider implement the following credential `Kinds`:
- Basic (Username and Password)
- API Key
- Software KeyStore (i.e., certificate related authentication)

Beyond credentials, the connector also acts as a **Secret Provider** — a namespaced store for secrets managed by the platform (see [Secret Provider](#secret-provider) below).

## Short Process Description

The `Credential` can be created and added to the `Core` based on their `Kind`. Selecting the specific `Kind` of `Credential` and providing the necessary information is the first step in the process.  When the `Connector` or other platform object requests the specific `Kind` of the `Credential` implemented in this `Credential Provider`, it will provide its values.

To know more about the `Core`, refer to [Core](https://github.com/OmniTrustILM/core).

## Interfaces

Common Credential Provider implements the `Credential Provider` Interface from the ILM Interfaces. To learn more about the interfaces and end points, refer to the [Interfaces](https://github.com/OmniTrustILM/interfaces).

For more information regarding the `Credentials`, please refer to the [documentation](https://docs.otilm.com).

## Secret Provider

In addition to the `Credential Provider`, this connector implements a `Secret Provider`: it stores versioned secrets in its own database and serves them back to the platform. Secrets are scoped by an optional **namespace**.

The `namespace` is a vault-instance attribute set when the vault is created. It is part of a secret's identity and provides hard isolation between scopes:

- Secrets in one namespace are not visible to, and cannot be modified from, another namespace.
- The same secret name can exist independently in different namespaces.
- Leaving the namespace empty places secrets in a distinct **root** scope; a vault configured with no namespace only ever sees root-scope secrets.

This mirrors how namespaces work in HashiCorp Vault.

## Docker container

Common Credential Provider is provided as a Docker container. Use the `hub.omnitrustregistry.com/ilm/common-credential-provider:tagname` to pull the required image from the repository. It can be configured using the following environment variables:

| Variable        | Description                                              | Required                                           | Default value   |
|-----------------|----------------------------------------------------------|----------------------------------------------------|-----------------|
| `JDBC_URL`      | JDBC URL for database access                             | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`           |
| `JDBC_USERNAME` | Username to access the database                          | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`           |
| `JDBC_PASSWORD`  | Password to access the database                          | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`           |
| `DB_SCHEMA`      | Database schema to use                                   | ![](https://img.shields.io/badge/-NO-red.svg)      | `common_secret` |
| `ENCRYPTION_KEY` | Key used to encrypt stored secrets at rest. A built-in default exists so the service can start, but it **must** be overridden with a strong, unique secret in production. | ![](https://img.shields.io/badge/-NO-red.svg) | built-in (insecure) |
| `PORT`           | Port where the service is exposed                        | ![](https://img.shields.io/badge/-NO-red.svg)      | `8080`          |
| `INCLUDE_ERROR`  | Whether error responses include the exception message (`always` / `never`). | ![](https://img.shields.io/badge/-NO-red.svg) | `always`        |
| `JAVA_OPTS`      | Customize Java system properties for running application | ![](https://img.shields.io/badge/-NO-red.svg)      | `N/A`           |
