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

import com.dattack.naming.loader.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * An abstract factory to be extended by connection pool providers.
 *
 * @author cvarela
 * @since 0.4
 */
public abstract class AbstractPoolFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPoolFactory.class);

    private transient volatile boolean available;

    protected AbstractPoolFactory() {
        this.available = true;
    }

    protected void logPoolConfiguration(final DataSourceConfig dataSourceConfig, final Properties properties) {
        if (LOGGER.isDebugEnabled()) {
            Properties hiddenPassProperties = new Properties();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key.toString().contains("pass")) {
                    hiddenPassProperties.put(key, "*****");
                } else {
                    hiddenPassProperties.put(key, value);
                }
            }
            log(dataSourceConfig, "Using configuration: {}", hiddenPassProperties);
        }
    }

    protected Properties computeProperties(final DataSourceConfig dataSourceConfig) {
        final Properties poolProperties = new Properties();
        poolProperties.putAll(dataSourceConfig.getProperties());
        CommonConstants.RESERVED_NAMES.forEach(poolProperties::remove);
        dataSourceConfig.getProperties().forEach((k, v) -> {
            if (k.toString().contains(".")) {
                poolProperties.remove(k);
            }
        });
        poolProperties.putAll(filterProperties(dataSourceConfig.getProperties(), getPrefixKey()));
        poolProperties.put(getDriverKey(), dataSourceConfig.getDriver());
        poolProperties.put(getUrlKey(), dataSourceConfig.getUrl());
        poolProperties.put(getUsernameKey(), dataSourceConfig.getUser());
        poolProperties.put(getPasswordKey(), dataSourceConfig.getPassword());
        return poolProperties;
    }

    protected static Properties filterProperties(final Properties properties, final String prefix) {

        final Properties props = new Properties();
        properties.forEach((key, value) -> {
            if (key.toString().startsWith(prefix)) {
                props.put(key.toString().substring(prefix.length()), value);
            }
        });
        return props;
    }

    protected void log(final DataSourceConfig dataSourceConfig, final Throwable t, final String message,
                       final Object... objects) {

        if (LOGGER.isDebugEnabled()) {
            Object[] params = new Object[4 + objects.length];
            params[0] = dataSourceConfig.getJndiName();
            System.arraycopy(objects, 0, params, 1, objects.length);
            params[params.length - 3] = Objects.toString(t.getMessage(), "");
            params[params.length - 2] = Objects.toString(t.getCause(), "");
            params[params.length - 1] = Objects.toString(t.getClass().getName(), "");
            log(message + ": {} (cause: {}, class: {})", params);
        }
    }

    protected void log(final DataSourceConfig dataSourceConfig, final String message, final Object... objects) {

        if (LOGGER.isDebugEnabled()) {
            Object[] params = new Object[1 + objects.length];
            params[0] = dataSourceConfig.getJndiName();
            System.arraycopy(objects, 0, params, 1, objects.length);
            log(message, params);
        }
    }

    private void log(final String message, final Object... objects) {
        LOGGER.debug("[{}] <" + getTypeName() + "> " + message, objects);
    }

    public abstract DataSource createDataSource(final DataSourceConfig dataSourceConfig);

    public final boolean isAvailable() {
        return available;
    }

    public final void disable() {
        this.available = false;
    }

    public abstract String getTypeName();

    public abstract String getPrefixKey();

    public abstract String getDriverKey();

    public abstract String getUrlKey();

    public abstract String getUsernameKey();

    public abstract String getPasswordKey();
}
