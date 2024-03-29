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
package com.dattack.naming.standalone;

import com.dattack.jtoolbox.commons.configuration.ConfigurationUtil;
import com.dattack.jtoolbox.io.FilesystemUtils;
import com.dattack.jtoolbox.util.FilesystemClassLoaderUtils;
import com.dattack.naming.loader.NamingLoader;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Initial Context Factory for {@link StandaloneContext}.
 *
 * @author cvarela
 * @since 0.1
 */
public final class StandaloneContextFactory implements InitialContextFactory {

    private static final String CLASSPATH_DIRECTORY_PROPERTY =
        StandaloneContextFactory.class.getName() + ".classpath.directory";
    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneContextFactory.class);
    private static final String RESOURCES_DIRECTORY_PROPERTY =
        StandaloneContextFactory.class.getName() + ".resources.directory";

    private static volatile Context context;

    private static Context createInitialContext(final File dir, final Map<?, ?> environment,
        final CompositeConfiguration configuration) throws NamingException
    {
        LOGGER.debug("Scanning directory '{}' for JNDI resources.", dir);
        try {
            final StandaloneContext ctx = new StandaloneContext(environment);
            final NamingLoader loader = new NamingLoader();
            final Collection<File> extraClasspath =
                FilesystemUtils.locateFiles(configuration.getList(CLASSPATH_DIRECTORY_PROPERTY));

            FilesystemClassLoaderUtils.ensureClassLoaded(new HashSet<>(extraClasspath));
            loader.loadDirectory(dir, ctx);

            LOGGER.debug("JNDI context is ready");

            return ctx;
        } catch (final IOException e) {
            throw (NamingException) new NamingException(e.getMessage()).initCause(e);
        }
    }

    private static CompositeConfiguration getConfiguration(final Map<?, ?> environment) {

        final BaseConfiguration baseConf = new BaseConfiguration();
        for (final Entry<?, ?> entry : environment.entrySet()) {
            baseConf.setProperty(ObjectUtils.toString(entry.getKey()), entry.getValue());
        }

        final CompositeConfiguration configuration = ConfigurationUtil.createEnvSystemConfiguration();
        configuration.addConfiguration(baseConf);
        return configuration;
    }

    private static File getResourcesDirectory(
        final CompositeConfiguration configuration) throws ConfigurationException
    {

        final Object configDir =
            PropertyConverter.interpolate(configuration.getProperty(RESOURCES_DIRECTORY_PROPERTY), configuration);

        if (configDir == null) {
            throw new ConfigurationException(
                String.format("JNDI configuration error: missing property '%s'", RESOURCES_DIRECTORY_PROPERTY));
        }

        return FilesystemUtils.locateFile(configDir.toString());
    }

    private static Context loadInitialContext(final Map<?, ?> environment) // NOPMD by cvarela
        throws NamingException
    {

        LOGGER.debug("loadInitialContext: '{}'", environment);
        final CompositeConfiguration configuration = getConfiguration(environment);

        final File dir = getResourcesDirectory(configuration);
        if (dir.exists()) {
            return createInitialContext(dir, environment, configuration);
        }

        throw new ConfigurationException(
            String.format("JNDI configuration error: the directory does not exists '%s'", dir));
    }

    @Override
    @SuppressWarnings("PMD.ReplaceHashtableWithMap")
    public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {

        if (context == null) {
            synchronized (StandaloneContextFactory.class) {
                if (context == null) {
                    context = loadInitialContext(getDefaultProperties(environment));
                }
            }
        }
        return context;
    }

    private Map<?, ?> getDefaultProperties(final Map<?, ?> environment) {

        final Map<String, String> table = new ConcurrentHashMap<>();
        environment.forEach((key, value) -> table.put(Objects.toString(key), Objects.toString(value)));

        setDefaultValue(table, "jndi.syntax.direction", "left_to_right");
        setDefaultValue(table, "jndi.syntax.separator", "/");
        setDefaultValue(table, "jndi.syntax.ignorecase", "true");
        return table;
    }

    private void setDefaultValue(final Map<String, String> environment, final String key, final String value) {
        if (!environment.containsKey(key)) {
            environment.put(key, value);
        }
    }
}
