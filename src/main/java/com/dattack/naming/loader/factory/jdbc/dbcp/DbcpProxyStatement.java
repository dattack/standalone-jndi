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

import com.dattack.jtoolbox.jdbc.JdbcObjectProxy;
import com.dattack.jtoolbox.jdbc.internal.ProxyStatement;
import org.apache.commons.dbcp2.DelegatingStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A delegating implementation of {@link Statement}. All methods call the corresponding method on the "delegate"
 * provided in the constructor.
 *
 * @author cvarela
 * @since 0.5
 */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.TooManyMethods"})
public class DbcpProxyStatement<S extends Statement> implements ProxyStatement<S> {

    private final S delegate;
    private final Connection connection;

    protected DbcpProxyStatement(final DbcpProxyConnection connection, final S delegate) {
        this.delegate = delegate;
        this.connection = connection;
    }

    protected static <S extends Statement> DbcpProxyStatement<?> build(
            final DbcpProxyConnection connection, final S delegate) {
        return new DbcpProxyStatement<>(connection, delegate);
    }

    public S getInnermostDelegate() {
        S s = getDelegate();
        while (s instanceof JdbcObjectProxy) {
            JdbcObjectProxy<S> other = (JdbcObjectProxy<S>) s;
            s = other.getDelegate();
            if (this == s) {
                return null;
            }
        }

        if (s instanceof DelegatingStatement) {
            s = (S) ((DelegatingStatement) s).getInnermostDelegate();
        }

        return s;
    }

    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        return DbcpProxyResultSet.build(this, getDelegate().executeQuery(sql));
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public S getDelegate() {
        return delegate;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return DbcpProxyResultSet.build(this, getDelegate().getGeneratedKeys());
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return DbcpProxyResultSet.build(this, getDelegate().getResultSet());
    }
}
