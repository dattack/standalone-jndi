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

import com.dattack.naming.AbstractContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * A generic {@link javax.naming.Context} designed to be used by standalone applications without a web-container.
 *
 * @author cvarela
 * @since 0.1
 */
public class StandaloneContext extends AbstractContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneContext.class);

    private StandaloneContext(final AbstractContext that) throws NamingException {
        super(that);
    }

    public StandaloneContext(final Map<?, ?> env) throws NamingException {
        super(env);
    }

    @Override
    public Context doCreateSubcontext(final Name name) throws NamingException {

        LOGGER.debug("Creating subcontext '{}/{}'", getNameInNamespace(), name.toString());
        final Map<Name, Object> subContexts = getSubContexts();

        if (name.size() > 1) {
            if (subContexts.containsKey(name.getPrefix(1))) {
                final Context subContext = (Context) subContexts.get(name.getPrefix(1));
                return subContext.createSubcontext(name.getSuffix(1));
            }
            throw new NameNotFoundException(String.format("The subcontext '%s' was not found.", name.getPrefix(1)));
        }

        if (lookup(name) != null) {
            throw new NameAlreadyBoundException(name.toString());
        }

        final Name contextName = getNameParser((Name) null).parse(getNameInNamespace());
        contextName.addAll(name);
        final StandaloneContext subcontext = new StandaloneContext(this);
        subcontext.setNameInNamespace(contextName);
        bind(name, subcontext);
        return subcontext;
    }
}
