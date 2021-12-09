/*
 * Copyright (c) 2016, The Dattack team (http://www.dattack.com)
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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import static com.dattack.junit.AssertionsExt.assertContains;
import static com.dattack.junit.AssertionsExt.assertEquals;
import static com.dattack.junit.AssertionsExt.assertNotNull;
import static com.dattack.junit.AssertionsExt.assertNull;
import static com.dattack.junit.AssertionsExt.assertThrows;
import static com.dattack.junit.AssertionsExt.fail;

/**
 * @author cvarela
 * @since 0.1
 */
/* package */ final class StandaloneJndiTest {

    private static final String INVALID_CONTEXT = "invalid-context";
    private static final String INVALID_OBJECT_NAME = "invalid-db";
    private static final String VALID_CONTEXT = "jdbc";
    private static final String VALID_OBJECT_NAME = "db1";

    private static String getCompositeName(final String context, final String objectName) {
        return String.format("%s/%s", context, objectName);
    }

    @Test
        /* package */ void testBind() {
        try {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName("jdbc", "testBind");
            final Object obj = 10;
            context.bind(name, obj);
            assertEquals(obj, context.lookup(name), String.format("Non-equals objects (%s <> %s)", obj,
                context.lookup(name)));
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }

    @Test
        /* package */ void testBindInvalidContext() {

        final Object obj = 10;
        assertThrows(NamingException.class, () -> {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName(INVALID_CONTEXT, "testBind");
            context.bind(name, obj);
        });
    }

    @Test
        /* package */ void testCreateContext() {
        try {
            final InitialContext context = new InitialContext();
            final String name = "testCreateContext";
            final Context subcontext = context.createSubcontext(name);
            assertNotNull(subcontext, String.format("Subcontext is null (name: %s)", name));
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }

    @Test
        /* package */ void testCreateMultiContext() {
        try {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName(VALID_CONTEXT, "testCreateMultiContext");
            final Context subcontext = context.createSubcontext(name);
            assertNotNull(subcontext, String.format("Subcontext is null (name: %s)", name));
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }

    @Test
        /* package */ void testLookupInvalidContext() {

        final Exception exception = assertThrows(NamingException.class, () -> {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName(INVALID_CONTEXT, VALID_OBJECT_NAME);
            context.lookup(name);
        });

        assertContains(exception.getMessage(), String.format("Invalid subcontext '%s' in context '/'",
            INVALID_CONTEXT));
    }

    @Test
        /* package */ void testLookupInvalidContextAndName() {

        final Exception exception = assertThrows(NamingException.class, () -> {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName(INVALID_CONTEXT, INVALID_OBJECT_NAME);
            context.lookup(name);
        });

        assertContains(exception.getMessage(), String.format("Invalid subcontext '%s' in context '/'",
            INVALID_CONTEXT));
    }

    @Test
        /* package */ void testLookupInvalidObjectName() throws NamingException {
        final InitialContext context = new InitialContext();
        final String name = getCompositeName(VALID_CONTEXT, INVALID_OBJECT_NAME);
        final Object obj = context.lookup(name);
        assertNull(obj, String.format("The searched object is not null (name: %s)", name));
    }

    @Test
        /* package */ void testLookupValidDataSource() {
        try {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName(VALID_CONTEXT, VALID_OBJECT_NAME);
            final DataSource dataSource = (DataSource) context.lookup(name);
            assertNotNull(dataSource);
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }
}
