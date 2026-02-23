# CZERTAINLY Common Credential Provider

> This repository is part of the commercial open-source project CZERTAINLY. 
> You can find more information about the project at [CZERTAINLY](https://github.com/CZERTAINLY/CZERTAINLY) repository, including the contribution guide.

Common Credential Provider is the implementation of basic `Credential` `Kinds` and validation interfaces. This `Connector` provides options to add the `Credential` to the `Core` which can be used within other objects in the platform for authentication and authorization purposes.

> `Credentials` provided by the Common Credential Provider are not involved in
the platform authentication and authorization process.

Common Credential Provider implement the following credential `Kinds`:
- Basic (Username and Password)
- API Key
- Software KeyStore (i.e., certificate related authentication)

## Short Process Description

The `Credential` can be created and added to the `Core` based on their `Kind`. Selecting the specific `Kind` of `Credential` and providing the necessary information is the first step in the process.  When the `Connector` or other platform object requests the specific `Kind` of the `Credential` implemented in this `Credential Provider`, it will provide its values.

To know more about the `Core`, refer to [CZERTAINLY Core](https://github.com/CZERTAINLY/CZERTAINLY-Core).

## Interfaces

Common Credential Provider implements the `Credential Provider` Interface from the CZERTAINLY Interfaces. To learn more about the interfaces and end points, refer to the [CZERTAINLY Interfaces](https://github.com/CZERTAINLY/CZERTAINLY-Interfaces).

For more information regarding the `Credentials`, please refer to the [CZERTAINLY documentation](https://docs.czertainly.com).

## Docker container

Common Credential Provider is provided as a Docker container. Use the `czertainly/czertainly-common-credential-provider:tagname` to pull the required image from the repository. It can be configured using the following environment variables:

| Variable        | Description                                              | Required                                           | Default value   |
|-----------------|----------------------------------------------------------|----------------------------------------------------|-----------------|
| `JDBC_URL`      | JDBC URL for database access                             | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`           |
| `JDBC_USERNAME` | Username to access the database                          | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`           |
| `JDBC_PASSWORD` | Password to access the database                          | ![](https://img.shields.io/badge/-YES-success.svg) | `N/A`           |
| `DB_SCHEMA`     | Database schema to use                                   | ![](https://img.shields.io/badge/-NO-red.svg)      | `common_secret` |
| `PORT`          | Port where the service is exposed                        | ![](https://img.shields.io/badge/-NO-red.svg)      | `8080`          |
| `JAVA_OPTS`     | Customize Java system properties for running application | ![](https://img.shields.io/badge/-NO-red.svg)      | `N/A`           |
