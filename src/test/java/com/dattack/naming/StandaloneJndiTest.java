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

import org.apache.commons.lang.ObjectUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import static org.junit.Assert.*;

/**
 * @author cvarela
 * @since 0.1
 */
public final class StandaloneJndiTest {

    private static final String VALID_OBJECT_NAME = "db1";
    private static final String VALID_CONTEXT = "jdbc";
    private static final String INVALID_OBJECT_NAME = "invalid-db";
    private static final String INVALID_CONTEXT = "invalid-context";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testBind() {
        try {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName("jdbc", "testBind");
            final Object obj = 10;
            context.bind(name, obj);
            assertEquals(obj, context.lookup(name));
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }

    private static String getCompositeName(final String context, final String objectName) {
        return String.format("%s/%s", context, objectName);
    }

    @Ignore("Validate the response against the JNDI specification")
    @Test
    public void testBindInvalidContext() throws NamingException {
        exception.expect(NamingException.class);
        final InitialContext context = new InitialContext();
        final String name = getCompositeName(INVALID_CONTEXT, "testBind");
        final Object obj = 10;
        context.bind(name, obj);
        fail(String.format("This test must fail because the name '%s' not exists (object: %s)", INVALID_CONTEXT,
                ObjectUtils.toString(obj)));
    }

    @Test
    public void testCreateContext() {
        try {
            final InitialContext context = new InitialContext();
            final String name = "testCreateContext";
            final Context subcontext = context.createSubcontext(name);
            assertNotNull(subcontext);
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateMultiContext() {
        try {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName(VALID_CONTEXT, "testCreateMultiContext");
            final Context subcontext = context.createSubcontext(name);
            assertNotNull(subcontext);
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testLookupInvalidContext() throws NamingException {

        exception.expect(NamingException.class);
        exception.expectMessage(String.format("Invalid subcontext '%s' in context '/'", INVALID_CONTEXT));
        final InitialContext context = new InitialContext();
        final String name = getCompositeName(INVALID_CONTEXT, VALID_OBJECT_NAME);
        final Object obj = context.lookup(name);
        fail(String.format("This test must fail because the name '%s' not exists in context '/' (object: %s)",
                INVALID_CONTEXT, ObjectUtils.toString(obj)));
    }

    @Test
    public void testLookupInvalidContextAndName() throws NamingException {

        exception.expect(NamingException.class);
        exception.expectMessage(String.format("Invalid subcontext '%s' in context '/'", INVALID_CONTEXT));
        final InitialContext context = new InitialContext();
        final String name = getCompositeName(INVALID_CONTEXT, INVALID_OBJECT_NAME);
        final Object obj = context.lookup(name);
        fail(String.format("This test must fail because the name '%s' not exists (object: %s)", INVALID_CONTEXT,
                ObjectUtils.toString(obj)));
    }

    @Test
    public void testLookupInvalidObjectName() throws NamingException {
        final InitialContext context = new InitialContext();
        final String name = getCompositeName(VALID_CONTEXT, INVALID_OBJECT_NAME);
        final Object obj = context.lookup(name);
        assertNull(obj);
    }
/*
    @Test
    public void testLookupValidDataSource() {

        try {
            final InitialContext context = new InitialContext();
            final String name = getCompositeName(VALID_CONTEXT, VALID_OBJECT_NAME);
            final DataSource dataSource = (DataSource) context.lookup(name);
            assertNotNull(dataSource);
        } catch (final NamingException e) {
            fail(e.getMessage());
        }
    }
 */
}
