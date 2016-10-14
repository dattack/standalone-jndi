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
import java.util.Collection;
import java.util.Properties;

import javax.naming.ConfigurationException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dattack.jtoolbox.jdbc.DataSourceClasspathDecorator;
import com.dattack.jtoolbox.jdbc.SimpleDataSource;

/**
 * @author cvarela
 * @since 0.1
 */
public class DataSourceFactory implements ResourceFactory<DataSource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);

    private static final String DRIVER_KEY = "driverClassName";
    private static final String URL_KEY = "url";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    public static final String TYPE = "javax.sql.DataSource";

    @Override
    public DataSource getObjectInstance(final Properties properties, final Collection<File> extraClasspath)
            throws NamingException {

        final CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(new SystemConfiguration());
        configuration.addConfiguration(new EnvironmentConfiguration());
        configuration.addConfiguration(new MapConfiguration(properties));

        final String driver = configuration.getString(DRIVER_KEY);
        if (driver == null) {
            throw new ConfigurationException(String.format("Missing property '%s'", DRIVER_KEY));
        }

        final String url = configuration.getString(URL_KEY);
        if (url == null) {
            throw new ConfigurationException(String.format("Missing property '%s'", URL_KEY));
        }

        final String user = configuration.getString(USERNAME_KEY);
        final String password = configuration.getString(PASSWORD_KEY);

        DataSource dataSource = null;
        try {
            final Properties props = ConfigurationConverter.getProperties(configuration);
            dataSource = BasicDataSourceFactory.createDataSource(props);
        } catch (final Exception e) { // NOPMD by cvarela on 8/02/16 22:28
            // we will use a DataSource without a connection pool
            LOGGER.info(e.getMessage());
            dataSource = new SimpleDataSource(driver, url, user, password);
        }

        return new DataSourceClasspathDecorator(dataSource, extraClasspath);
    }
}
