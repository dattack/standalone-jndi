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

import com.dattack.naming.LazyResourceProxy;
import com.dattack.naming.loader.factory.ResourceFactory;
import com.dattack.naming.loader.factory.ResourceFactoryRegistry;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * This class is responsible for instantiating and registering the configured JNDI resources. To do this, it scans the
 * configuration directory and creates the necessary sub-contexts associated with the different subdirectories.  For
 * each one of them, it processes the .properties files that it finds and instance, using the factory corresponding to
 * the type of resource indicated in the configuration file, the object that later will bind into the JNDI register.
 *
 * @author cvarela
 * @since 0.1
 */
public final class NamingLoader {

    private static final String[] EXTENSIONS = new String[]{ "properties" };
    private static final Logger LOGGER = LoggerFactory.getLogger(NamingLoader.class);

    private static void createAndBind(final Properties properties, final Context context,
        final String name) throws NamingException
    {
        final String type = properties.getProperty(CommonConstants.TYPE_KEY);
        final ResourceFactory<?> factory = ResourceFactoryRegistry.getFactory(type);
        if (factory == null) {
            LOGGER.warn("Unable to get a factory for type '{}'", type);
            return;
        }

        final String jndiName = String.format("%s/%s", context.getNameInNamespace(), name);
        final Object proxy = new LazyResourceProxy(factory, jndiName, properties);
        LOGGER.debug("Binding object to '{}/{}' (type: '{}')", context.getNameInNamespace(), name, type);
        execBind(context, name, proxy);
    }

    private static void execBind(final Context context, final String key, final Object value) throws NamingException {

        Object obj = context.lookup(key);

        if (obj instanceof Context) {
            context.destroySubcontext(key);
            obj = null; // NOPMD
        }

        if (obj == null) {
            context.bind(key, value);
        } else {
            context.rebind(key, value);
        }
    }

    /**
     * Scans a directory hierarchy looking for <code>*.properties</code> files. Creates a subcontext for each directory
     * in the hierarchy and binds a new resource for each <code>*.properties</code> file with a
     * <code>ResourceFactory</code> associated.
     *
     * @param directory the directory to scan
     * @param context   the Context to populate
     * @throws NamingException          if a naming exception is encountered
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException when parameter 'directory' does not reference to a directory
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void loadDirectory(final File directory,
        final Context context) throws NamingException, IOException, IllegalArgumentException
    {

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(String.format("'%s' isn't a directory", directory));
        }

        final File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (final File file : files) {
            if (file.isDirectory()) {
                final Context subcontext = context.createSubcontext(file.getName());
                loadDirectory(file, subcontext);
            } else {

                final String fileName = file.getName();
                if (FilenameUtils.isExtension(fileName, EXTENSIONS)) {
                    final String baseName = FilenameUtils.getBaseName(fileName);
                    try (InputStream fin = Files.newInputStream(file.toPath())) {
                        final Properties properties = new Properties();
                        properties.load(fin);
                        createAndBind(properties, context, baseName);
                    } catch (final NamingException | IOException e) {
                        LOGGER.warn("Unable to bind object from file '{}': {}", file, e.getMessage());
                    }
                }
            }
        }
    }
}
