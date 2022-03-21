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

import com.dattack.jtoolbox.jdbc.internal.ProxyResultSet;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * A delegating implementation of {@link ResultSet}. All methods call the corresponding method on the "delegate"
 * provided in the constructor.
 *
 * @author cvarela
 * @since 0.5
 */
@SuppressWarnings({ "PMD.ExcessivePublicCount", "PMD.TooManyMethods", "checkstyle:AbbreviationAsWordInName" })
public final class DbcpProxyResultSet implements ProxyResultSet {

    private final DbcpProxyStatement<?> statement;
    private final ResultSet delegate;

    private DbcpProxyResultSet(DbcpProxyStatement<?> statement, ResultSet delegate) {
        this.statement = statement;
        this.delegate = delegate;
    }

    /**
     * Creates a new instance of DbcpProxyResultSet.
     *
     * @param statement the statement used to create the delegate ResultSet
     * @param delegate the delegate ResultSet
     * @return the new instance of DbcpProxyResultSet
     */
    /* default */ static DbcpProxyResultSet build(DbcpProxyStatement<?> statement, ResultSet delegate) {
        return new DbcpProxyResultSet(statement, delegate);
    }

    @Override
    public ResultSet getDelegate() {
        return delegate;
    }

    @Override
    public Statement getStatement() {
        return statement;
    }
}
