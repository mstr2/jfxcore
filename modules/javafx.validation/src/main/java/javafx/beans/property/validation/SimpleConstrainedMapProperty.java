/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  JFXcore designates this
 * particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package javafx.beans.property.validation;

import javafx.collections.ObservableMap;

/**
 * This class provides an implementation of {@link ConstrainedMapProperty} that wraps an {@link ObservableMap}.
 *
 * @param <K> key type
 * @param <V> value type
 * @param <E> error information type
 * @since JFXcore 18
 */
public class SimpleConstrainedMapProperty<K, V, E> extends ConstrainedMapPropertyBase<K, V, E> {

    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";

    private final Object bean;
    private final String name;

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return name;
    }

    @SafeVarargs
    public SimpleConstrainedMapProperty(Constraint<? super ObservableMap<K, V>, E>... constraints) {
        this(DEFAULT_BEAN, DEFAULT_NAME, null, constraints);
    }

    @SafeVarargs
    public SimpleConstrainedMapProperty(
            ObservableMap<K, V> initialValue,
            Constraint<? super ObservableMap<K, V>, E>... constraints) {
        this(DEFAULT_BEAN, DEFAULT_NAME, initialValue, constraints);
    }

    @SafeVarargs
    public SimpleConstrainedMapProperty(
            Object bean, String name, Constraint<? super ObservableMap<K, V>, E>... constraints) {
        this(bean, name, null, constraints);
    }

    @SafeVarargs
    public SimpleConstrainedMapProperty(
            Object bean,
            String name,
            ObservableMap<K, V> initialValue,
            Constraint<? super ObservableMap<K, V>, E>... constraints) {
        super(initialValue, constraints);
        this.bean = bean;
        this.name = (name == null) ? DEFAULT_NAME : name;
    }
    
}
