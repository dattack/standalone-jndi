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

import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * This class represents a NamingEnumeration of generic type.
 *
 * @author cvarela
 * @since 0.1
 */
abstract class AbstractNamingEnumeration<T> implements NamingEnumeration<T> {

    private Map<?, ?> bindingMap;
    private Iterator<?> iterator;

    AbstractNamingEnumeration(final Map<?, ?> bindingMap) {
        this.bindingMap = bindingMap;
        iterator = this.bindingMap.keySet().iterator();
    }

    @Override
    public final void close() {
        bindingMap = null;
        iterator = null;
    }

    /**
     * Instantiate a new object of T using the provided values.
     *
     * @param key
     *            the key from the binding map
     * @param value
     *            the binding value
     * @return the object associated with the values given
     */
    protected abstract T create(Object key, Object value);

    @Override
    public final boolean hasMore() throws NamingException {
        if (bindingMap == null) {
            throw new NamingException();
        }
        return hasMoreElements();
    }

    @Override
    public final boolean hasMoreElements() {
        if (iterator == null) {
            return false;
        }
        return iterator.hasNext();
    }

    @Override
    public final T next() throws NamingException {
        if (bindingMap == null) {
            throw new NamingException();
        }
        return nextElement();
    }

    @Override
    public final T nextElement() {
        if (bindingMap == null) {
            return null;
        }
        final Object name = iterator.next();

        return create(name.toString(), bindingMap.get(name));
    }
}
