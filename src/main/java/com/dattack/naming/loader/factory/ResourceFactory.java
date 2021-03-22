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
package com.dattack.naming.loader.factory;

import java.util.Properties;
import javax.naming.NamingException;

/**
 * Interface that must be implemented by all the factories in charge of instantiating objects that can be accessed by
 * JNDI.
 *
 * @author cvarela
 * @since 0.1
 * @param <T>
 *            the type of objects instantiated by this factory
 */
public interface ResourceFactory<T> {

    /**
     * Creates a new instance using the specified properties.
     *
     * @param jndiName name of the JNDI resource
     * @param properties
     *            a Properties data structure
     * @return an instance of T
     * @throws NamingException
     *             when a configuration error occurs
     */
    T getObjectInstance(String jndiName, Properties properties) throws NamingException;
}
