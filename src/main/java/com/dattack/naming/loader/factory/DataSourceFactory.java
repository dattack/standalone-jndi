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
import com.dattack.jtoolbox.jdbc.InitializableDataSource;
import com.dattack.jtoolbox.jdbc.SimpleDataSource;
import com.dattack.jtoolbox.security.DattackSecurityException;
import com.dattack.jtoolbox.security.RsaUtils;
import com.dattack.naming.loader.CommonConstants;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.List;
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
 *
 * @author cvarela
 * @since 0.1
 */
public final class DataSourceFactory implements ResourceFactory<DataSource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);

    private static final String ENCRYPT_PREFIX = "encrypt";
    private static final String DEFAULT_PRIVATE_KEY = "id_rsa";

    private static String getMandatoryProperty(final AbstractConfiguration configuration, final String propertyName)
            throws ConfigurationException {

        final String value = configuration.getString(propertyName);
        if (value == null) {
            throw new ConfigurationException(String.format("Missing property '%s'", propertyName));
        }
        return value;
    }

    private static String decrypt(final String value, final PrivateKey privateKey, final String jndiName)
            throws DattackSecurityException, NullPointerException {

        String plainText;
        if (StringUtils.startsWithIgnoreCase(value, ENCRYPT_PREFIX)) {
            Objects.requireNonNull(privateKey, //
                    String.format("A private key is required to decrypt the datasource configuration (JNDI name: %s)",
                            jndiName));
            String encryptedValue = value.substring(ENCRYPT_PREFIX.length() + 1);
            plainText = new String(RsaUtils.decryptBase64(encryptedValue.getBytes(Charset.defaultCharset()),
                    privateKey), Charset.defaultCharset());
        } else {
            plainText = value;
        }
        return plainText;
    }

    private static PrivateKey getPrivateKey(final String jndiName, final AbstractConfiguration configuration) {

        String keyFilename = configuration.getString(CommonConstants.PRIVATE_KEY_FILENAME);

        if (keyFilename == null) {
            keyFilename = configuration.getString(CommonConstants.GLOBAL_PRIVATE_KEY_FILENAME);
        }

        if (keyFilename == null) {
            keyFilename = FilesystemUtils.locateFile(DEFAULT_PRIVATE_KEY).getAbsolutePath();
        }

        try {
            LOGGER.debug("[{}] Trying to load the private key '{}'", jndiName, keyFilename);
            return RsaUtils.loadPrivateKey(keyFilename);
        } catch (DattackSecurityException e) {
            LOGGER.debug("[{}] Unable to load the private key '{}'", jndiName, keyFilename);
        }
        return null;
    }

    @Override
    public DataSource getObjectInstance(final String jndiName, final Properties properties) throws NamingException {

        try {

            final MapConfiguration mapConfiguration = new MapConfiguration(properties);
            mapConfiguration.setDelimiterParsingDisabled(true);

            final CompositeConfiguration configuration = ConfigurationUtil.createEnvSystemConfiguration();
            configuration.addConfiguration(mapConfiguration);

            final PrivateKey privateKey = getPrivateKey(jndiName, configuration);

            DataSourceConfig dataSourceConfig = new DataSourceConfig()
                    .withJndiName(jndiName)
                    .withDriver(decrypt(getMandatoryProperty(configuration, CommonConstants.DRIVER_KEY), //
                            privateKey, jndiName)) //
                    .withUrl(decrypt(
                            getMandatoryProperty(configuration, CommonConstants.URL_KEY), //
                            privateKey, jndiName)) //
                    .withUser(decrypt(configuration.getString(CommonConstants.USERNAME_KEY), //
                            privateKey, jndiName)) //
                    .withPassword(decrypt(configuration.getString(CommonConstants.PASSWORD_KEY), //
                            privateKey, jndiName)) //
                    .withProperties(properties);

            DataSource dataSource = null;
            LOGGER.debug("[{}] Instantiating datasource '{}'@'{}'", jndiName, dataSourceConfig.getUser(),
                    dataSourceConfig.getUrl());

            if (!configuration.getBoolean(CommonConstants.DISABLE_POOL_KEY, false)) {
                if (isDbcpEnabled(configuration)) {
                    dataSource = DbcpPoolFactory.getInstance().createDataSource(dataSourceConfig);
                }

                if (Objects.isNull(dataSource) && isAtomikosEnabled(configuration)) {
                    dataSource = AtomikosPoolFactory.getInstance().createDataSource(dataSourceConfig);
                }
            }

            if (Objects.isNull(dataSource)) {
                LOGGER.debug("[{}] Connection pool disabled", jndiName);
                dataSource = new SimpleDataSource(dataSourceConfig.getDriver(),
                        dataSourceConfig.getUrl(),
                        dataSourceConfig.getUser(),
                        dataSourceConfig.getPassword());
            }

            // include on-connect script, if one exists
            dataSource = decorateWithOnConnectScript(jndiName,
                    configuration.getString(CommonConstants.ON_CONNECT_SCRIPT_KEY),
                    dataSource);

            LOGGER.info("[{}] Datasource '{}'@'{}': {}", jndiName, dataSourceConfig.getUser(),
                    dataSourceConfig.getUrl(), dataSource.getClass());
            return dataSource;
        } catch (final DattackSecurityException e) {
            throw new SecurityConfigurationException(e);
        }
    }

    private boolean isAtomikosEnabled(final CompositeConfiguration configuration) {
        return AtomikosPoolFactory.getInstance().isAvailable()
                && !configuration.getBoolean(CommonConstants.DISABLE_ATOMIKOS_POOL_KEY, false);
    }

    private boolean isDbcpEnabled(final CompositeConfiguration configuration) {
        return DbcpPoolFactory.getInstance().isAvailable()
                && !configuration.getBoolean(CommonConstants.DISABLE_DBCP_POOL_KEY, false);
    }

    private DataSource decorateWithOnConnectScript(final String jndiName, final String script,
                                                   final DataSource dataSource) {

        DataSource result = dataSource;
        if (StringUtils.isNotBlank(script)) {
            final List<String> sqlStatements = Arrays.asList(script.split(";"));
            LOGGER.debug("[{}] Commands to execute on connect: {}", jndiName, sqlStatements);
            result = new InitializableDataSource(result, sqlStatements);
        }
        return result;
    }
}
