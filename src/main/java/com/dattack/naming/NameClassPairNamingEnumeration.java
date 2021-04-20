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

import java.util.Map;

import javax.naming.NameClassPair;

/**
 * This class represents a NamingEnumeration of the NameClassPair of a Context.
 *
 * @author cvarela
 * @since 0.1
 */
public class NameClassPairNamingEnumeration extends AbstractNamingEnumeration<NameClassPair> {

    /* default */ NameClassPairNamingEnumeration(final Map<?, ?> table) {
        super(table);
    }

    @Override
    public NameClassPair create(final Object key, final Object value) {
        return new NameClassPair(key.toString(), value.getClass().getName());
    }
}
