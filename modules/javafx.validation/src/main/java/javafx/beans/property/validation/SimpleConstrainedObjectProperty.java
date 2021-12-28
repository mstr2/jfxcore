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

/**
 * This class provides an implementation of {@link ConstrainedObjectPropertyBase} that wraps an object value.
 *
 * @param <T> data type
 * @param <E> error information type
 * @since JFXcore 18
 */
public class SimpleConstrainedObjectProperty<T, E> extends ConstrainedObjectPropertyBase<T, E> {

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
    public SimpleConstrainedObjectProperty(Constraint<? super T, E>... constraints) {
        this(DEFAULT_BEAN, DEFAULT_NAME, null, constraints);
    }

    @SafeVarargs
    public SimpleConstrainedObjectProperty(T initialValue, Constraint<? super T, E>... constraints) {
        this(DEFAULT_BEAN, DEFAULT_NAME, initialValue, constraints);
    }

    @SafeVarargs
    public SimpleConstrainedObjectProperty(Object bean, String name, Constraint<? super T, E>... constraints) {
        this(bean, name, null, constraints);
    }

    @SafeVarargs
    public SimpleConstrainedObjectProperty(
            Object bean, String name, T initialValue, Constraint<? super T, E>... constraints) {
        super(initialValue, constraints);
        this.bean = bean;
        this.name = (name == null) ? DEFAULT_NAME : name;
    }
    
}
