[![Build](https://github.com/dattack/standalone-jndi/actions/workflows/maven-deploy.yml/badge.svg?branch=dev)](https://github.com/dattack/standalone-jndi/actions)
[![license](https://img.shields.io/:license-Apache-blue.svg?style=plastic-square)](LICENSE.md)
[![Maven Central](https://img.shields.io/maven-central/v/com.dattack/standalone-jndi.svg?label=Maven%20Central)](https://search.maven.org/artifact/com.dattack/standalone-jndi)
[![javadoc](https://javadoc.io/badge2/com.dattack/standalone-jndi/javadoc.svg)](https://javadoc.io/doc/com.dattack/standalone-jndi)

# Standalone JNDI

A JNDI implementation to use with your standalone applications.

Standalone-JNDI uses a filesystem directory structure to create the hierarchical structure of JNDI contexts. The name of
each context will match the name of the equivalent directory. Each directory can contain one or more `.properties` files
that will be mapped with JNDI resources created in the context corresponding to the directory structure.

## Where can I get the latest release?

You can pull it from the Central Maven repositories:

```xml
<dependency>
    <groupId>com.dattack</groupId>
    <artifactId>standalone-jndi</artifactId>
    <version>0.6-SNAPSHOT</version>
</dependency>
```

The source code on the master branch is the current state of development; it is not
recommended for general use. If you prefer to build from source, please use an appropriate
release tag.

## Usage

1) Create an application resource file called `jndi.properties` and place it somewhere in the application's
   CLASSPATH.

2) Edit that file and set the following properties:

    - `java.naming.factory.initial`: set this property to the classname (including the package) of the Initial Context
      Factory for the JNDI Service Provider. In this case, use `com.dattack.naming.standalone.StandaloneContextFactory`.

    - `com.dattack.naming.standalone.StandaloneContextFactory.resources.directory`: the root directory from which the
      _properties_ files of the JNDI resources are located.

    - `com.dattack.naming.standalone.StandaloneContextFactory.classpath.directory`: list of directories (separated by
      commas) containing additional libraries needed to instantiate JNDI resources (e.g. JAR files containing JDBC
      drivers)

3) Create the necessary directory structure according to the required JNDI contexts within the directory referenced by
   the parameter `com.dattack.naming.standalone.StandaloneContextFactory.resources.directory`.

4) Create a `.properties` file and configure the necessary properties for each required JNDI resource. Currently, the
   only type of JNDI resources that this factory creates are `javax.sql.DataSource`.

### javax.sql.DataSource

Currently, Standalone-JNDI can be used with one of the following connection pool: Apache Commons DBCP and
TransactionsEssentials (Atomikos).

The minimum set of properties required to configure this kind of JNDI resource is as follows:

- `type` indicates the type of resource involved. In this case, the value is always javax.sql.DataSource

- `driverClassName` the fully qualified Java class name of the JDBC driver to be used.

- `url` the connection URL to be passed to our JDBC driver to establish a connection.

- `username` the connection username to be passed to our JDBC driver to establish a connection.

- `password` the connection password to be passed to our JDBC driver to establish a connection.

Example:

```properties
   type=javax.sql.DataSource
   driverClassName=org.sqlite.JDBC
   url=jdbc:sqlite:db1.sqlite
   username=login
   password=changeme
```

Additionally, it is also possible to configure the following properties:

- onConnectScript: an ordered sequence of SQL statements to be executed whenever a new connection is created. The
  semicolon character ( _;_ ) is the separator to be used between statements.

```properties
  onConnectScript=\
      ALTER SESSION SET CURRENT_SCHEMA = myschema ;\
      ALTER SESSION ENABLE PARALLEL DML ;          \
      ALTER SESSION FORCE PARALLEL DML PARALLEL 16
```

- disablePool: a flag that allows you to quickly disable the use of the connection pool if it is set. The default value
  is false.

   - disablePool.dbcp: when true disables DBCP only, allowing other connection pools to be used.

   - disablePool.atomikos: when true disables Atomikos only, allowing other connection pools to be used.

#### Apache Commons DBCP
See [BasicDataSource Configuration Parameters](https://commons.apache.org/proper/commons-dbcp/configuration.html)
for a detailed list of parameters that can be configured.

You can configure DBCP-specific properties by prefixing them with `dbcp.`. The following are some of the most common
properties:

- `dbcp.initialSize`: The initial number of connections that are created when the pool is started.

- `dbcp.maxTotal`: The maximum number of active connections that can be allocated from this pool at the same time, or
negative for no limit.

- `dbcp.maxIdle`: The maximum number of connections that can remain idle in the pool, without extra ones being released,
or negative for no limit.

- `dbcp.minIdle`: The minimum number of connections that can remain idle in the pool, without extra ones being created,
or zero to create none.

- `dbcp.maxWaitMillis`: The maximum number of milliseconds that the pool will wait (when there are no available
connections) for a connection to be returned before throwing an exception, or -1 to wait indefinitely.

#### TransactionsEssentials (Atomikos)
See [Configuring TransactionsEssentials](https://www.atomikos.com/Documentation/ConfiguringTransactionsEssentials)
for a detailed list of parameters that can be configured.

You can configure DBCP-specific properties by prefixing them with `atomikos.`The following are some of the most common
properties:

- `atomikos.maxPoolSize`: the maximum pool size.

- `atomikos.minPoolSize`: the minimum size of the pool.

- `atomikos.maxIdleTime`: the maximum amount of seconds that unused excess connections should stay in the pool.

- `atomikos.reapTimeout`: the amount of time (in seconds) that the connection pool will allow a connection to be in use,
before claiming it back.

**NOTE:** when a datasource has both types of connection pool (DBCP and Atomikos) configured and activated,
Standalone-JNDI will use DBCP.

#### Secure password

Standalone-JNDI allows encrypting the passwords, so they are not stored in clear within the configuration files of the
JNDI resources. To do this, you must concatenate the prefix "encrypt:" to the encrypted value of the password and
provide the path to the private key that must be used to decrypt the password.

There are three ways to indicate the private key to be used to decrypt the password:

1) Use the parameter "_privateKey_" within the configuration file of the JNDI resource. The value of this parameter
must be the path to the file containing the private key to be used. This option allows you to use a different private
key for each JNDI resource:

    ```properties
        type=javax.sql.DataSource
        driverClassName=org.sqlite.JDBC
        url=jdbc:sqlite:db1.sqlite
        username=login
        password=encrypt:YH+W6wiS0vuTsL7uMpFmbvlzCm4d0L6hKtnJCpjExgcYrumFvwRurOg8X6BsDrJvQ7knka5M6KmVJv6CHxDhldTVLO77f3xXhZOuvw/VYL4Bl2YyAy/eVFoK3/TtKIQWnL5a9CfGTX0FFnHrCyybGFNOnXINYKJYxw1G7NVAAxQ=
        privateKey=<path_to_private_key_file>
        disablePool=false
    ```

2) Set the "_globalPrivateKey_" environment variable to reference the path to the file containing the private key.

3) If you do not set any of the above options, Standalone-JNDI will try to locate the `id_rsa` file in the
application's classpath and will try to use it as a private key to decrypt the encrypted passwords.

## Contributing

Pull requests and stars are always welcome. For bugs and feature
requests, [please create an issue](https://github.com/dattack/standalone-jndi/issues).

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

If you have other questions, please contact by [email](mailto:dev@dattack.com) or
[@dattackteam](https://twitter.com/dattackteam)

## License

Code is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0.txt).
