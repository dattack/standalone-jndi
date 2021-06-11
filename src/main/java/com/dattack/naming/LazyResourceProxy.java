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

import com.dattack.naming.loader.factory.ResourceFactory;
import org.apache.commons.lang.builder.ToStringBuilder;
import java.util.Objects;
import java.util.Properties;
import javax.naming.NamingException;

/**
 * Proxy of a resource to be initialized the first time it is used.
 *
 * @author cvarela
 * @since 0.4
 */
public class LazyResourceProxy {

    private final transient ResourceFactory<?> factory;
    private final transient String jndiName;
    private final transient Properties properties;
    private transient volatile Object obj;

    /**
     * Constructor.
     *
     * @param factory the factory that will provide the resource
     * @param jndiName JNDI name of the resource
     * @param properties resource configuration
     */
    public LazyResourceProxy(final ResourceFactory<?> factory, final String jndiName, final Properties properties) {
        this.factory = factory;
        this.jndiName = jndiName;
        this.properties = properties;
    }

    /**
     * Returns the initialized underlying object.
     *
     * @return the initialized underlying object
     *
     * @throws NamingException when a configuration error occurs
     */
    public Object getObject() throws NamingException {

        if (Objects.isNull(obj)) {
            synchronized (this) {
                if (Objects.isNull(obj)) {
                    obj = factory.getObjectInstance(jndiName, properties);
                }
            }
        }
        return obj;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("factory", factory.getClass())
            .append("jndiName", jndiName)
            .toString();
    }
}
