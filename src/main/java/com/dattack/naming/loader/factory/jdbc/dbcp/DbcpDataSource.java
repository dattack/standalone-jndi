/*
 * Copyright (c) 2022, The Dattack team (http://www.dattack.com)
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
package com.dattack.naming.loader.factory.jdbc.dbcp;

import com.dattack.jtoolbox.jdbc.AbstractDataSourceDecorator;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * A factory to create DBCP pool instances. See org.apache.commons.dbcp.BasicDataSourceFactory for more information.
 *
 * @author cvarela
 * @since 0.5
 */
public class DbcpDataSource extends AbstractDataSourceDecorator {

    DbcpDataSource(final DataSource delegate) {
        super(delegate);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DbcpProxyConnection.build(getDelegate().getConnection());
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return DbcpProxyConnection.build(getDelegate().getConnection(username, password));
    }
}
