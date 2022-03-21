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
package com.dattack.naming;  // NOPMD by cvarela

import org.apache.commons.lang.StringUtils;

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Binding;
import javax.naming.CannotProceedException;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;

/**
 * Provides the base implementation of naming context.
 *
 * @author cvarela
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public abstract class AbstractContext implements Cloneable, Context {  // NOPMD by cvarela

    private final NameParser nameParser;
    private transient volatile boolean closed;
    // the environment properties
    private Hashtable<Object, Object> env; // NOPMD by cvarela on 8/02/16 22:32
    private Name nameInNamespace;
    // the direct subcontext
    private Hashtable<Name, Context> subContexts = new Hashtable<>();   // NOPMD by cvarela

    // the binded table
    private transient Map<Name, Object> objectTable = new ConcurrentHashMap<>();

    protected AbstractContext(final AbstractContext that) throws NamingException {
        this(that.env);
    }

    protected AbstractContext(final Map<?, ?> env) throws NamingException {

        this.env = new Hashtable<>();
        if (env != null) {
            this.env.putAll(env);
        }
        this.closed = false;
        nameParser = new DefaultNameParser(this);
        nameInNamespace = nameParser.parse("");
    }

    /**
     * Creates a new subcontext.
     *
     * @param name the name of the context to create
     * @return the subcontext
     * @throws NamingException if an error occurs
     */
    public abstract Context doCreateSubcontext(Name name) throws NamingException;

    @Override
    public Object addToEnvironment(final String name, final Object object) {
        return this.env.put(name, object);
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void bind(final Name name, final Object object) throws NamingException {

        ensureContextNotClosed();

        if (Objects.nonNull(object)) {

            if (name.isEmpty()) {
                throw new InvalidNameException("Cannot bind to an empty name");
            }

            final Name prefix = name.getPrefix(1);
            if (subContexts.containsKey(prefix)) {
                subContexts.get(prefix).bind(name.getSuffix(1), object);
                return;
            }

            if (name.size() > 1) { //NOPMD
                throw new NameNotFoundException(String.format("Missing context '%s'", prefix.toString()));
            }

            if (isNameAlreadyBound(name)) {
                throw new NameAlreadyBoundException(
                    String.format("Name %s already bound. Use rebind() to override", name));
            }

            if (object instanceof Context) {
                subContexts.put(name, (Context) object);
            } else {
                objectTable.put(name, object);
            }
        }
    }

    @Override
    public void bind(final String name, final Object object) throws NamingException {
        bind(nameParser.parse(name), object);
    }

    @Override
    public void close() throws NamingException {

        if (!closed) {

            synchronized (this) {
                if (closed) {
                    return;
                }
                this.closed = true;

                // close all subcontext
                destroySubcontexts();

                // release binded objects
                this.objectTable.clear();
                this.objectTable = null; //NOPMD
                this.subContexts = null; //NOPMD
                this.env = null; //NOPMD
            }
        }
    }

    @Override
    public Name composeName(final Name name, final Name prefix) throws NamingException {

        if (Objects.isNull(name) || Objects.isNull(prefix)) {
            throw new InvalidNameException(
                String.format("Unable to compose name with null values (prefix: %s, name: %s)", prefix, name));
        }

        final Name composeName = (CompoundName) prefix.clone();
        composeName.add(name.toString());
        return composeName;
    }

    @Override
    public String composeName(final String name, final String prefix) throws NamingException {
        return composeName(nameParser.parse(name), nameParser.parse(prefix)).toString();
    }

    @Override
    public Context createSubcontext(final Name name) throws NamingException {

        ensureContextNotClosed();
        if (closed) {
            throw new CannotProceedException("Context is closed");
        }
        return doCreateSubcontext(name);
    }

    @Override
    public Context createSubcontext(final String name) throws NamingException {
        return createSubcontext(nameParser.parse(name));
    }

    @Override
    public void destroySubcontext(final Name name) throws NamingException {

        if (name.size() > 1) { //NOPMD
            if (subContexts.containsKey(name.getPrefix(1))) {
                final Context subContext = subContexts.get(name.getPrefix(1));
                subContext.destroySubcontext(name.getSuffix(1));
                return;
            }
            throw new NameNotFoundException();
        }

        if (objectTable.containsKey(name) || !subContexts.containsKey(name)) {
            throw new NameNotFoundException(String.format("Context not found: %s", name));
        }

        final Context subContext = subContexts.get(name);
        final NamingEnumeration<NameClassPair> names = subContext.list("");
        if (names.hasMore()) {
            throw new ContextNotEmptyException();
        }

        subContexts.get(name).close();
        subContexts.remove(name);
    }

    @Override
    public void destroySubcontext(final String name) throws NamingException {
        destroySubcontext(nameParser.parse(name));
    }

    @Override
    @SuppressWarnings("PMD.ReplaceHashtableWithMap")
    public Hashtable<?, ?> getEnvironment() { //NOPMD

        Hashtable<?, ?> result;
        if (this.env == null) {
            result = new Hashtable<String, Object>();
        } else {
            result = (Hashtable<?, ?>) this.env.clone(); //NOPMD
        }
        return result;
    }

    @Override
    public String getNameInNamespace() {
        return nameInNamespace.toString();
    }

    /**
     * Sets the full name of this context within its own namespace.
     *
     * @param name the context's name
     * @throws NamingException if a name already set
     */
    protected void setNameInNamespace(final Name name) throws NamingException {
        if (nameInNamespace != null && !nameInNamespace.isEmpty()) {
            throw new NamingException("Name already set.");
        }
        nameInNamespace = name;
    }

    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public NameParser getNameParser(final Name name) throws NamingException {

        if (Objects.isNull(name) || name.isEmpty() //
            || (name.size() == 1 && name.toString().equals(getNameInNamespace())))
        {
            return nameParser;
        }

        final Name subName = name.getPrefix(1);
        if (subContexts.containsKey(subName)) {
            return subContexts.get(subName).getNameParser(name.getSuffix(1));
        }

        throw new NotContextException();
    }

    @Override
    public NameParser getNameParser(final String name) throws NamingException {
        return getNameParser(nameParser.parse(name));
    }

    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {

        ensureContextNotClosed();

        if (name == null || name.isEmpty()) {
            // list all elements
            final Map<Name, Object> enumStore = new ConcurrentHashMap<>();
            enumStore.putAll(objectTable);
            enumStore.putAll(subContexts);
            return new NameClassPairNamingEnumeration(enumStore);
        }

        final Name prefixName = name.getPrefix(1);
        if (objectTable.containsKey(prefixName)) {
            throw new NotContextException(String.format("%s cannot be listed", name));
        }

        if (subContexts.containsKey(prefixName)) {
            return subContexts.get(prefixName).list(name.getSuffix(1));
        }

        throw new NamingException(String.format("The context '%s' can't be found", name));
    }

    @Override
    public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        return list(nameParser.parse(name));
    }

    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {

        ensureContextNotClosed();

        if (name == null || name.isEmpty()) {
            final Map<Name, Object> enumStore = new ConcurrentHashMap<>();
            enumStore.putAll(objectTable);
            enumStore.putAll(subContexts);
            return new BindingNamingEnumeration(enumStore);
        }

        final Name subName = name.getPrefix(1);

        if (objectTable.containsKey(subName)) {
            throw new NotContextException(String.format("%s cannot be listed", name));
        }

        if (subContexts.containsKey(subName)) {
            return subContexts.get(subName).listBindings(name.getSuffix(1));
        }

        throw new NamingException(String.format("The named context '%s' can't be found", name));
    }

    @Override
    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        return listBindings(nameParser.parse(name));
    }

    @Override
    @SuppressWarnings({ "PMD.OnlyOneReturn", "PMD.CyclomaticComplexity" })
    public Object lookup(final Name name) throws NamingException {

        ensureContextNotClosed();

        /*
         * Extract from Context Javadoc: If name is empty, returns a new instance of this context (which represents the
         * same naming context as this context, but its environment may be modified independently and it may be accessed
         * concurrently).
         */
        if (name.isEmpty()) {
            try {
                return this.clone();
            } catch (final CloneNotSupportedException e) {
                // this shouldn't happen, since we are Cloneable
                throw (NamingException) new OperationNotSupportedException(e.getMessage()).initCause(e);
            }
        }

        if (name.size() > 1) { //NOPMD
            if (subContexts.containsKey(name.getPrefix(1))) {
                return subContexts.get(name.getPrefix(1)).lookup(name.getSuffix(1));
            }
            throw new NamingException(
                String.format("Invalid subcontext '%s' in context '%s'", name.getPrefix(1).toString(),
                              StringUtils.isBlank(getNameInNamespace()) ? "/" : getNameInNamespace()));
        }

        Object result = null; // not found
        if (objectTable.containsKey(name)) {
            result = objectTable.get(name);
        } else if (subContexts.containsKey(name)) {
            result = subContexts.get(name);
        } else if (env.containsKey(name.toString())) {
            result = env.get(name.toString());
        }

        if (result instanceof LazyResourceProxy) {
            final LazyResourceProxy proxy = (LazyResourceProxy) result;
            result = proxy.getObject();
        }

        return result;
    }

    @Override
    public Object lookup(final String name) throws NamingException {
        return lookup(nameParser.parse(name));
    }

    @Override
    public Object lookupLink(final Name name) throws NamingException {
        return lookup(name);
    }

    @Override
    public Object lookupLink(final String name) throws NamingException {
        return lookup(nameParser.parse(name));
    }

    @Override
    public void rebind(final Name name, final Object object) throws NamingException {

        ensureContextNotClosed();

        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot rebind to empty name");
        }

        // the parent context must exists
        getParentContext(name);
        unbind(name);
        bind(name, object);
    }

    @Override
    public void rebind(final String name, final Object object) throws NamingException {
        rebind(nameParser.parse(name), object);
    }

    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public Object removeFromEnvironment(final String name) {
        if (this.env == null) {
            return null;
        }
        return this.env.remove(name);
    }

    @Override
    public void rename(final Name oldName, final Name newName) throws NamingException {

        ensureContextNotClosed();

        if (newName.isEmpty()) {
            throw new InvalidNameException("Cannot bind to empty name");
        }

        final Object oldValue = lookup(oldName);
        if (oldValue == null) {
            throw new NamingException(String.format("Cannot rename object: name not found (%s)", oldName));
        }

        if (lookup(newName) != null) {
            throw new NameAlreadyBoundException(
                String.format("Cannot rename object: name already bound (%s)", newName));
        }

        unbind(oldName);
        unbind(newName);
        bind(newName, oldValue);
    }

    @Override
    public void rename(final String oldName, final String newName) throws NamingException {
        rename(nameParser.parse(oldName), nameParser.parse(newName));
    }

    @Override
    public void unbind(final Name name) throws NamingException {

        ensureContextNotClosed();

        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot unbind to empty name");
        }

        if (name.size() == 1) { //NOPMD
            objectTable.remove(name);
            return;
        }

        final Context parentContext = getParentContext(name);

        parentContext.unbind(name.getSuffix(name.size() - 1));
    }

    @Override
    public void unbind(final String name) throws NamingException {
        unbind(nameParser.parse(name));
    }

    /**
     * Returns the subcontexts of this context.
     *
     * @return the subcontexts of this context.
     */
    @SuppressWarnings("unchecked")
    protected Map<Name, Object> getSubContexts() {
        return (Map<Name, Object>) subContexts.clone();
    }

    private boolean isNameAlreadyBound(final Name name) {
        return objectTable.containsKey(name) || subContexts.containsKey(name) || env.containsKey(name.toString());
    }

    private void destroySubcontexts() throws NamingException {
        for (final Name name : subContexts.keySet()) {
            destroySubcontext(name);
        }
    }

    private void ensureContextNotClosed() throws NamingException {
        if (closed) {
            throw new CannotProceedException("Context is closed");
        }
    }

    private Context getParentContext(final Name name) throws NamingException {

        final Object context = lookup(name.getPrefix(name.size() - 1));
        if (context instanceof Context) {
            return (Context) context;
        }
        throw new NamingException(
            String.format("Cannot unbind object. Target context does not exist (%s)", name.getPrefix(name.size() - 1)));
    }
}
