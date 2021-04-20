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
package com.dattack.naming;

import org.junit.jupiter.api.Test;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import static com.dattack.junit.AssertionsExt.*;

/**
 * @author cvarela
 * @since 0.1
 */
public class ClasspathLoaderTest {

    @Test
    public void testLookupValidDataSourceExternalJar() {

        try {
            final InitialContext context = new InitialContext();
            final String name = "jdbc/db1";
            final DataSource dataSource = (DataSource) context.lookup(name);
            assertNotNull(dataSource, String.format("DataSource is null (name: %s)", name));
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }
}
