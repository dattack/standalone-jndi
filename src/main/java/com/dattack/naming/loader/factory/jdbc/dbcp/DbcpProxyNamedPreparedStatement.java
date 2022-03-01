/*
 * Copyright (c) 2017, The Dattack team (http://www.dattack.com)
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

import com.dattack.jtoolbox.jdbc.internal.NamedPreparedStatementConfig;
import com.dattack.jtoolbox.jdbc.internal.ProxyNamedPreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A delegating implementation of {@link java.sql.PreparedStatement} with support for <i>parameter-name</i>. The
 * parameter-name is identified by the format <i>:param_name</i>. All methods call the corresponding method on the
 * "delegate" provided in the constructor.
 *
 * @author cvarela
 * @since 0.5
 */
@SuppressWarnings({"PMD", "checkstyle:AbbreviationAsWordInName"})
public class DbcpProxyNamedPreparedStatement extends DbcpProxyPreparedStatement<PreparedStatement>
        implements ProxyNamedPreparedStatement {

    private final transient NamedPreparedStatementConfig namedPreparedStatementConfig;

    protected DbcpProxyNamedPreparedStatement(final DbcpProxyConnection connection,
            final PreparedStatement delegate,
            final NamedPreparedStatementConfig namedPreparedStatementConfig) {
        super(connection, delegate);
        this.namedPreparedStatementConfig = namedPreparedStatementConfig;
    }

    /**
     * Creates a <code>NamedParameterPreparedStatement</code> object for sending parameterized SQL statements to the
     * database.
     *
     * @param connection a connection (session) with a specific database.
     * @param sql        a SQL statement that may contain one or more ':parameterName' IN parameter placeholders
     * @return a <code>NamedParameterPreparedStatement</code> object containing the pre-compiled SQL statement
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    public static DbcpProxyNamedPreparedStatement build(final DbcpProxyConnection connection,
            final String sql) throws SQLException {
        final NamedPreparedStatementConfig preparedStatementConfig = NamedPreparedStatementConfig.parse(sql);
        return new DbcpProxyNamedPreparedStatement(connection, connection.prepareStatement(
                preparedStatementConfig.getCompiledSql()), preparedStatementConfig);
    }

    @Override
    public NamedPreparedStatementConfig getNamedPreparedStatementConfig() {
        return namedPreparedStatementConfig;
    }
}
