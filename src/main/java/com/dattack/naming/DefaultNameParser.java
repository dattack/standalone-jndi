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

import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link NameParser}.
 *
 * @author cvarela
 * @since 0.1
 */
class DefaultNameParser implements NameParser {

    private final transient Properties properties;

    /* default */ DefaultNameParser(final Context parent) throws NamingException {
        this.properties = new Properties();
        properties.putAll(parent.getEnvironment());
    }

    @Override
    public Name parse(final String name) throws NamingException {
        return new CompoundName(StringUtils.trimToEmpty(name), properties);
    }
}
