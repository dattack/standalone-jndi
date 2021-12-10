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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * A factory to create Atomikos pool instances. See com.atomikos.jdbc.AtomikosNonXADataSourceBean for more information.
 *
 * @author cvarela
 * @since 0.4
 */
public final class AtomikosPoolFactory extends AbstractPoolFactory {

    private static final String PREFIX = "atomikos.";
    private static final String DRIVER_KEY = "driverClassName";
    private static final String URL_KEY = "url";
    private static final String USER_KEY = "user";
    private static final String PASSWORD_KEY = "password";
    private static final String UNIQUE_RESOURCE_NAME_KEY = "uniqueResourceName";
    private static final String TYPE_NAME = "Atomikos-Pool";
    private static final AtomikosPoolFactory SINGLETON = new AtomikosPoolFactory();

    public static AtomikosPoolFactory getInstance() {
        return SINGLETON;
    }

    @Override
    public DataSource createDataSource(final DataSourceConfig dataSourceConfig) {

        final String clazzName = "com.atomikos.jdbc.AtomikosNonXADataSourceBean";

        log(dataSourceConfig, "Configuring datasource (driver: {}) ...", dataSourceConfig.getDriver());
        DataSource dataSource = null;
        try {

            final Properties poolProperties = computeProperties(dataSourceConfig);
            if (!poolProperties.containsKey(UNIQUE_RESOURCE_NAME_KEY)) {
                poolProperties.put(UNIQUE_RESOURCE_NAME_KEY, dataSourceConfig.getJndiName());
            }
            logPoolConfiguration(dataSourceConfig, poolProperties);

            final Class<?> factory = Class.forName(clazzName);
            dataSource = (DataSource) factory.newInstance();

            final Class<?> propertyUtilsClass = Class.forName("com.atomikos.beans.PropertyUtils");
            final Method setPropertiesMethod = propertyUtilsClass.getMethod("setProperty", Object.class,
                    String.class, Object.class);
            DataSource finalDataSource = dataSource;
            poolProperties.forEach((k, v) -> {
                try {
                    setPropertiesMethod.invoke(null, finalDataSource, k.toString(), v);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log(dataSourceConfig, e, "Unable to set property '{}'", k);
                }
            });

        } catch (ClassNotFoundException e) {
            log(dataSourceConfig, e, "Class not found");
            disable();
        } catch (Throwable t) { //NOPMD
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
