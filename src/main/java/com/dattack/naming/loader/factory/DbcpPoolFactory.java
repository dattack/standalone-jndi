/*
 * Copyright (c) 2021, The Dattack team (http://www.dattack.com)
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

import java.lang.reflect.Method;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * A factory to create DBCP pool instances. See org.apache.commons.dbcp.BasicDataSourceFactory for more information.
 *
 * @author cvarela
 * @since 0.4
 */
public final class DbcpPoolFactory extends AbstractPoolFactory {

    private static final String PREFIX = "dbcp.";
    private static final String DRIVER_KEY = "driverClassName";
    private static final String URL_KEY = "url";
    private static final String USER_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String TYPE_NAME = "DBCP-Pool";
    private static final DbcpPoolFactory SINGLETON = new DbcpPoolFactory();

    public static DbcpPoolFactory getInstance() {
        return SINGLETON;
    }

    @Override
    public DataSource createDataSource(final DataSourceConfig dataSourceConfig) {

        final String clazzName = "org.apache.commons.dbcp.BasicDataSourceFactory";

        log(dataSourceConfig, "Configuring datasource (driver: {}) ...", dataSourceConfig.getDriver());
        DataSource dataSource = null;
        try {
            final Properties poolProperties = computeProperties(dataSourceConfig);
            logPoolConfiguration(dataSourceConfig, poolProperties);

            final Class<?> factory = Class.forName(clazzName);
            final Method method = factory.getDeclaredMethod("createDataSource", Properties.class);
            dataSource = (DataSource) method.invoke(null, poolProperties);

        } catch (ClassNotFoundException e) {
            log(dataSourceConfig, e, "Class not found");
            disable();
        } catch (final Throwable t) { //NOPMD
            log(dataSourceConfig, t, "Unable to configure datasource");
        }
        return dataSource;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getPrefixKey() {
        return PREFIX;
    }

    @Override
    public String getDriverKey() {
        return DRIVER_KEY;
    }

    @Override
    public String getUrlKey() {
        return URL_KEY;
    }

    @Override
    public String getUsernameKey() {
        return USER_KEY;
    }

    @Override
    public String getPasswordKey() {
        return PASSWORD_KEY;
    }
}
