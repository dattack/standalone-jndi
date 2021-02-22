/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.naming.loader.factory;

import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;
import com.dattack.jtoolbox.io.FilesystemUtils;
import com.dattack.jtoolbox.jdbc.DataSourceClasspathDecorator;
import com.dattack.jtoolbox.jdbc.InitializableDataSource;
import com.dattack.jtoolbox.jdbc.SimpleDataSource;
import com.dattack.jtoolbox.security.DattackSecurityException;
import com.dattack.jtoolbox.security.RsaUtils;
import com.dattack.jtoolbox.util.PropertiesUtils;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.naming.ConfigurationException;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * A factory that allows the instantiation of DataSource objects from the configuration provided. The minimum set of
 * properties required is as follows:
 * <ul>
 * <li>driverClassName: the JDBC Driver object</li>
 * <li>url: the JDBC URL to use for accessing the DriverManager</li>
 * </ul>
 * <p>
 *  The <i>username</i> and <i>password</i> are optional. The password, if encrypted, must be configured by prefixing
 *  as <strong>encrypt:</strong><i>encrypted_password</i>, where <i>encrypted_password</i> is the value returned by
 *  {@link com.dattack.jtoolbox.security.tool.SecurityTool}. The private key used to decrypt the password will be one
 *  of the following, in order:
 *  </p>
 *  <ol>
 *      <li>the path to the file indicated in the <i>'privateKey'</i> property.</li>
 *      <li>the path to the file indicated in the <i>'globalPrivateKey'</i> property.</li>
 *      <li>the content of the <i>id_rsa</i> file</li>
 *  </ol>
 * Additional properties can be specified to configure a connection pool. See
 * org.apache.commons.dbcp.BasicDataSourceFactory for more information.
 *
 * @author cvarela
 * @since 0.1
 */
public class DataSourceFactory implements ResourceFactory<DataSource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);

    private static final String ENCRYPT_PREFIX = "encrypt";
    private static final String PRIVATE_KEY_FILENAME = "privateKey";
    private static final String GLOBAL_PRIVATE_KEY_FILENAME = "globalPrivateKey";
    private static final String DEFAULT_PRIVATE_KEY = "id_rsa";

    private static final String DRIVER_KEY = "driverClassName";
    private static final String URL_KEY = "url";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String ON_CONNECT_SCRIPT_KEY = "onConnectScript";
    private static final String DISABLE_POOL_KEY = "disablePool";

    private static final String DISABLE_ATOMIKOS_POOL_KEY = "disablePool.atomikos";
    private static final String DISABLE_DBCP_POOL_KEY = "disablePool.dbcp";

    private static final String ATOMIKOS_DRIVER_KEY = "driverClassName";
    private static final String ATOMIKOS_URL_KEY = "url";
    private static final String ATOMIKOS_USER_KEY = "user";
    private static final String ATOMIKOS_PASSWORD_KEY = "password";

    private static final String DBCP_DRIVER_KEY = "driverClassName";
    private static final String DBCP_URL_KEY = "url";
    private static final String DBCP_USER_KEY = "username";
    private static final String DBCP_PASSWORD_KEY = "password";


    public static final String TYPE = "javax.sql.DataSource";


    private static String getMandatoryProperty(final AbstractConfiguration configuration, final String propertyName)
            throws ConfigurationException {

        final String value = configuration.getString(propertyName);
        if (value == null) {
            throw new ConfigurationException(String.format("Missing property '%s'", propertyName));
        }
        return value;
    }

    private static String getPassword(final AbstractConfiguration configuration)
            throws DattackSecurityException {

        final String password = configuration.getString(PASSWORD_KEY);

        if (password != null && password.startsWith(ENCRYPT_PREFIX)) {
            final PrivateKey privateKey = getPrivateKey(configuration);
            final String encryptedPassword = password.substring(ENCRYPT_PREFIX.length() + 1);
            return new String(RsaUtils.decryptBase64(encryptedPassword.getBytes(Charset.defaultCharset()), privateKey),
                    Charset.defaultCharset());
        }

        // plain password
        return password;
    }

    private static PrivateKey getPrivateKey(final AbstractConfiguration configuration)
            throws DattackSecurityException {

        String keyFilename = configuration.getString(PRIVATE_KEY_FILENAME);
        LOGGER.debug("Trying to locate private key ({} = {})", PRIVATE_KEY_FILENAME, keyFilename);

        if (keyFilename == null) {
            keyFilename = configuration.getString(GLOBAL_PRIVATE_KEY_FILENAME);
            LOGGER.debug("Trying to locate private key ({} = {})", GLOBAL_PRIVATE_KEY_FILENAME, keyFilename);
        }

        if (keyFilename == null) {
            keyFilename = FilesystemUtils.locateFile(DEFAULT_PRIVATE_KEY).getAbsolutePath();
            LOGGER.debug("Trying to locate private key (default = {})", keyFilename);
        }

        LOGGER.debug("Loading private key '{}'", keyFilename);
        return RsaUtils.loadPrivateKey(keyFilename);
    }

    @Override
    public DataSource getObjectInstance(final Properties properties, final Collection<File> extraClasspath)
            throws NamingException {

        try {
            final MapConfiguration mapConfiguration = new MapConfiguration(PropertiesUtils.toMap(properties));
            mapConfiguration.setDelimiterParsingDisabled(true);

            final CompositeConfiguration configuration = ConfigurationUtil.createEnvSystemConfiguration();
            configuration.addConfiguration(mapConfiguration);

            final String driver = getMandatoryProperty(configuration, DRIVER_KEY);
            final String url = getMandatoryProperty(configuration, URL_KEY);
            final String user = configuration.getString(USERNAME_KEY);
            final String plainPassword = getPassword(configuration);

            DataSource dataSource = null;
            LOGGER.info("Instantiating datasource '{}'@'{}'", user, url);

            if (!configuration.getBoolean(DISABLE_POOL_KEY, false)) {

                mapConfiguration.setProperty(PASSWORD_KEY, plainPassword);
                final Properties props = ConfigurationConverter.getProperties(configuration);

                if (!configuration.getBoolean(DISABLE_ATOMIKOS_POOL_KEY, false)) {
                    dataSource = createAtomikosDataSource(driver, url, user, plainPassword, properties);
                }

                if (Objects.isNull(dataSource) && !configuration.getBoolean(DISABLE_DBCP_POOL_KEY, false)) {
                    dataSource = createDbcpDataSource(driver, url, user, plainPassword, properties);
                }

            } else {
                LOGGER.info("Connection pool disabled");
            }

            if (dataSource == null) {
                dataSource = new SimpleDataSource(driver, url, user, plainPassword);
            }

            // include on-connect script, if one exists
            dataSource = decorateWithOnConnectScript(configuration.getString(ON_CONNECT_SCRIPT_KEY), dataSource);

            LOGGER.info("Datasource '{}'@'{}': {}", user, url, dataSource.getClass());
            return new DataSourceClasspathDecorator(dataSource, extraClasspath);
        } catch (final DattackSecurityException e) {
            throw new SecurityConfigurationException(e);
        }
    }

    private DataSource createDbcpDataSource(final String driver, final String url,
                                            final String user, final String plainPassword, Properties properties) {

        LOGGER.info("Configuring DBCP connection pool ...");
        DataSource dataSource = null;
        try {

            Properties props = new Properties(properties);
            props.put(DBCP_DRIVER_KEY, driver);
            props.put(DBCP_URL_KEY, url);
            props.put(DBCP_USER_KEY, user);
            props.put(DBCP_PASSWORD_KEY, plainPassword);
            props.putAll(filterDbcpProperties(properties));

            Class<?> factory = Class.forName("org.apache.commons.dbcp.BasicDataSourceFactory");
            Method method = factory.getDeclaredMethod("createDataSource", Properties.class);
            dataSource = (DataSource) method.invoke(null, props);

        } catch (Throwable t) {
            LOGGER.warn("Unable to configure DBCP connection pool: {}", t.getMessage());
        }
        return dataSource;
    }

    private DataSource createAtomikosDataSource(final String driver, final String url,
                                                final String user, final String plainPassword, Properties properties) {

        LOGGER.info("Configuring Atomikos connection pool ...");
        DataSource dataSource;
        try {

            Properties atomikosProps = filterAtomikosProperties(properties);
            atomikosProps.put(ATOMIKOS_DRIVER_KEY, driver);
            atomikosProps.put(ATOMIKOS_URL_KEY, url);
            atomikosProps.put(ATOMIKOS_USER_KEY, user);

            LOGGER.info("Atomikos configuration: {}", atomikosProps);

            // IMPORTANT: the password must not be written to the log
            atomikosProps.put(ATOMIKOS_PASSWORD_KEY, plainPassword);

            String clazzName = "com.atomikos.jdbc.AtomikosNonXADataSourceBean";
            LOGGER.info("Atomikos class: {}", clazzName);

            dataSource = (DataSource) Class.forName(clazzName).newInstance();

            Class<?> propertyUtilsClass = Class.forName("com.atomikos.beans.PropertyUtils");
            Method setPropertiesMethod = propertyUtilsClass.getMethod("setProperties", Object.class,
                    Map.class);
            setPropertiesMethod.invoke(null, dataSource, atomikosProps);

        } catch (Throwable t) {
            LOGGER.warn("Unable to configure Atomikos connection pool: {} {} ({})", t.getMessage(), t.getCause(),
                    t.getClass());
            dataSource = null;
        }
        return dataSource;
    }

    private Properties filterAtomikosProperties(Properties properties) {
        return filterProperties(properties, "atomikos.");
    }

    private Properties filterDbcpProperties(Properties properties) {
        return filterProperties(properties, "dbcp.");
    }

    private Properties filterProperties(Properties properties, String prefix) {

        Properties props = new Properties();
        properties.forEach((key, value) -> {
            if (key.toString().startsWith(prefix)) {
                props.put(key.toString().substring(prefix.length()), value);
            }
        });
        return props;
    }

    private DataSource decorateWithOnConnectScript(String script, DataSource dataSource) {

        DataSource result = dataSource;
        if (StringUtils.isNotBlank(script)) {
            List<String> sqlStatements = Arrays.asList(script.split(";"));
            LOGGER.debug("Commands to execute on connect: {}", sqlStatements);
            result = new InitializableDataSource(result, sqlStatements);
        }
        return result;
    }
}
