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

import com.dattack.jtoolbox.jdbc.internal.NamedPreparedStatement;
import com.dattack.jtoolbox.jdbc.internal.ProxyConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A delegating implementation of {@link Connection}. All methods call the corresponding method on the "delegate"
 * provided in the constructor.
 *
 * @author cvarela
 * @since 0.5
 */
@SuppressWarnings({"unused", "PMD.TooManyMethods", "PMD.ExcessivePublicCount"})
public class DbcpProxyConnection implements ProxyConnection {

    private final Connection delegate;

    protected DbcpProxyConnection(final Connection delegate) {
        this.delegate = delegate;
    }

    public static DbcpProxyConnection build(Connection delegate) {
        return new DbcpProxyConnection(delegate);
    }

    @Override
    @SuppressWarnings("PMD.CloseResource")
    public Statement createStatement() throws SQLException {
        Statement statement = getDelegate().createStatement();
        return DbcpProxyStatement.build(this, statement);
    }

    @Override
    @SuppressWarnings("PMD.CloseResource")
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        Statement statement = getDelegate().createStatement(resultSetType, resultSetConcurrency);
        return DbcpProxyStatement.build(this, statement);
    }

    @Override
    @SuppressWarnings("PMD.CloseResource")
    public Statement createStatement(int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        Statement statement = getDelegate().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        return DbcpProxyStatement.build(this, statement);
    }

    @Override
    public Connection getDelegate() {
        return delegate;
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return doPrepareCall(getDelegate().prepareCall(sql));
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return doPrepareCall(getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency));
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return doPrepareCall(getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public NamedPreparedStatement prepareNamedStatement(String sql) throws SQLException {
        return DbcpProxyNamedPreparedStatement.build(this, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return doPrepareStatement(getDelegate().prepareStatement(sql));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        return doPrepareStatement(getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return doPrepareStatement(
                getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return doPrepareStatement(getDelegate().prepareStatement(sql, autoGeneratedKeys));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return doPrepareStatement(getDelegate().prepareStatement(sql, columnIndexes));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return doPrepareStatement(getDelegate().prepareStatement(sql, columnNames));
    }

    private CallableStatement doPrepareCall(final CallableStatement callableStatement) {
        return DbcpProxyCallableStatement.build(this, callableStatement);
    }

    private PreparedStatement doPrepareStatement(final PreparedStatement preparedStatement) {
        return DbcpProxyPreparedStatement.build(this, preparedStatement);
    }
}
