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

import java.util.Properties;

/**
 * Set of properties needed to configure a new DataSource.
 *
 * @author cvarela
 * @since 0.4
 */
public final class DataSourceConfig {

    private transient String jndiName;
    private transient String driver;
    private transient String url;
    private transient String user;
    private transient String password;
    private transient Properties properties;

    public String getJndiName() {
        return jndiName;
    }

    public DataSourceConfig withJndiName(String jndiName) {
        this.jndiName = jndiName;
        return this;
    }

    public String getDriver() {
        return driver;
    }

    public DataSourceConfig withDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DataSourceConfig withUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUser() {
        return user;
    }

    public DataSourceConfig withUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DataSourceConfig withPassword(String password) {
        this.password = password;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public DataSourceConfig withProperties(Properties properties) {
        this.properties = properties;
        return this;
    }
}
