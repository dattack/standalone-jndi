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

import java.io.File;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.Properties;

import javax.naming.ConfigurationException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.jtoolbox.io.FilesystemUtils;
import com.dattack.jtoolbox.jdbc.DataSourceClasspathDecorator;
import com.dattack.jtoolbox.jdbc.SimpleDataSource;
import com.dattack.jtoolbox.security.DattackSecurityException;
import com.dattack.jtoolbox.security.RsaUtils;

/**
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
    public static final String TYPE = "javax.sql.DataSource";

    private static AbstractConfiguration getConfiguration(final Properties properties) {
        final CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(new SystemConfiguration());
        configuration.addConfiguration(new EnvironmentConfiguration());
        configuration.addConfiguration(new MapConfiguration(properties));
        return configuration;
    }

    private static String getMandatoryProperty(final AbstractConfiguration configuration, final String propertyName)
            throws ConfigurationException {

        final String value = configuration.getString(propertyName);
        if (value == null) {
            throw new ConfigurationException(String.format("Missing property '%s'", propertyName));
        }
        return value;
    }

    private static String getPassword(final AbstractConfiguration configuration)
            throws ConfigurationException, DattackSecurityException {

        final String password = configuration.getString(PASSWORD_KEY);

        if (password != null && password.startsWith(ENCRYPT_PREFIX)) {
            final PrivateKey privateKey = getPrivateKey(configuration);
            final String encryptedPassword = password.substring(PASSWORD_KEY.length());
            return new String(RsaUtils.decryptBase64(encryptedPassword.getBytes(Charset.defaultCharset()), privateKey),
                    Charset.defaultCharset());
        }

        // not encrypted password
        return password;
    }

    private static PrivateKey getPrivateKey(final AbstractConfiguration configuration)
            throws DattackSecurityException, ConfigurationException {

        String keyFilename = configuration.getString(PRIVATE_KEY_FILENAME);

        if (keyFilename == null) {
            keyFilename = configuration.getString(GLOBAL_PRIVATE_KEY_FILENAME);
        }

        if (keyFilename == null) {
            keyFilename = FilesystemUtils.locateFile(DEFAULT_PRIVATE_KEY).getAbsolutePath();
        }

        // Guard: This should never happen because we have a default private-key
        // to use
        if (keyFilename == null) {
            throw new ConfigurationException("Unable to find the private key. Check your configuration");
        }

        return RsaUtils.loadPrivateKey(keyFilename);
    }

    @Override
    public DataSource getObjectInstance(final Properties properties, final Collection<File> extraClasspath)
            throws NamingException {

        try {
            final AbstractConfiguration configuration = getConfiguration(properties);

            final String driver = getMandatoryProperty(configuration, DRIVER_KEY);
            final String url = getMandatoryProperty(configuration, URL_KEY);
            final String plainPassword = getPassword(configuration);

            DataSource dataSource = null;
            try {
                configuration.setProperty(PASSWORD_KEY, plainPassword);
                final Properties props = ConfigurationConverter.getProperties(configuration);
                dataSource = BasicDataSourceFactory.createDataSource(props);
            } catch (final Exception e) { // NOPMD by cvarela on 8/02/16 22:28
                // we will use a DataSource without a connection pool
                LOGGER.info(e.getMessage());
                final String user = configuration.getString(USERNAME_KEY);
                dataSource = new SimpleDataSource(driver, url, user, plainPassword);
            }

            return new DataSourceClasspathDecorator(dataSource, extraClasspath);
        } catch (final DattackSecurityException e) {
            throw new SecurityConfigurationException(e);
        }
    }
}
