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
package com.dattack.naming.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.io.FilenameUtils;

import com.dattack.naming.loader.factory.ResourceFactory;
import com.dattack.naming.loader.factory.ResourceFactoryRegistry;

/**
 * @author cvarela
 * @since 0.1
 */
public final class NamingLoader {

    private static final Logger LOGGER = Logger.getLogger(NamingLoader.class.getName());

    private static final String TYPE_KEY = "type";
    private static final String[] EXTENSIONS = new String[] { "properties" };

    private static void createAndBind(final Properties properties, final Context context, final String name,
            final Collection<File> extraClasspath) throws NamingException {

        final String type = properties.getProperty(TYPE_KEY);
        final ResourceFactory<?> factory = ResourceFactoryRegistry.getFactory(type);
        if (factory == null) {
            LOGGER.log(Level.WARNING, "Unable to get a factory for type ''{0}''", type);
            return;
        }

        final Object value = factory.getObjectInstance(properties, extraClasspath);
        if (value != null) {
            LOGGER.log(Level.INFO, "Binding object to '{0}/{1}' (type: '{2}')",
                    new Object[] { context.getNameInNamespace(), name, type });
            execBind(context, name, value);
        }
    }

    private static void execBind(final Context context, final String key, final Object value) throws NamingException {

        Object obj = context.lookup(key);

        if (obj instanceof Context) {
            LOGGER.log(Level.FINE, "Destroying context with name '{0}'", key);
            context.destroySubcontext(key);
            obj = null;
        }

        if (obj == null) {
            LOGGER.log(Level.FINE, "Executing bind method for '{0}'", key);
            context.bind(key, value);
        } else {
            LOGGER.log(Level.FINE, "Executing rebind method for '{0}'", key);
            context.rebind(key, value);
        }
    }

    /**
     * Scans a directory hierarchy looking for <code>*.properties</code> files.
     * Creates a subcontext for each directory in the hierarchy and binds a new
     * resource for each <code>*.properties</code> file with a
     * <code>ResourceFactory</code> associated.
     *
     *
     * @param directory
     *            the directory to scan
     * @param ctxt
     *            the Context to populate
     * @param extraClasspath
     *            additional paths to include to the classpath
     * @throws NamingException
     *             if a naming exception is encountered
     * @throws IOException
     *             if an I/O error occurs
     */
    public void loadDirectory(final File directory, final Context ctxt, final Collection<File> extraClasspath)
            throws NamingException, IOException {

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(String.format("'%s' isn't a directory", directory));
        }

        final File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (final File file : files) {
            if (file.isDirectory()) {
                final Context subcontext = ctxt.createSubcontext(file.getName());
                loadDirectory(file, subcontext, extraClasspath);
            } else {

                final String fileName = file.getName();
                if (FilenameUtils.isExtension(fileName, EXTENSIONS)) {
                    final String baseName = FilenameUtils.getBaseName(fileName);
                    try (FileInputStream fin = new FileInputStream(file)) {
                        final Properties properties = new Properties();
                        properties.load(fin);
                        createAndBind(properties, ctxt, baseName, extraClasspath);
                    }
                }
            }
        }
    }
}
