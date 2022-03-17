/*
 * Copyright (c) 2022, JFXcore. All rights reserved.
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

package javafx.validation.property;

import javafx.util.Incubating;
import javafx.validation.Constraint;
import javafx.validation.ValidationState;

/**
 * This class provides an implementation of {@link ConstrainedLongProperty} that wraps a long value.
 *
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public class SimpleConstrainedLongProperty<D> extends ConstrainedLongPropertyBase<D> {

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

    /**
     * Initializes a new instance of {@code SimpleConstrainedLongProperty}.
     * The specified constraints are immediately evaluated.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    public SimpleConstrainedLongProperty(Constraint<? super Number, D>... constraints) {
        this(DEFAULT_BEAN, DEFAULT_NAME, 0, ValidationState.UNKNOWN, constraints);
    }

    /**
     * Initializes a new instance of {@code SimpleConstrainedLongProperty}.
     * The specified constraints are immediately evaluated.
     *
     * @param initialValue the initial value of this {@code SimpleConstrainedLongProperty}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public SimpleConstrainedLongProperty(long initialValue, Constraint<? super Number, D>... constraints) {
        this(DEFAULT_BEAN, DEFAULT_NAME, initialValue, ValidationState.UNKNOWN, constraints);
    }

    /**
     * Initializes a new instance of {@code SimpleConstrainedLongProperty}.
     * If the initial state is {@link ValidationState#UNKNOWN}, the constraints are immediately evaluated.
     * Otherwise, the constraints will be evaluated when the property value is changed.
     *
     * @param initialValue the initial value of this {@code SimpleConstrainedLongProperty}
     * @param initialValidationState the initial validation state of this {@code SimpleConstrainedLongProperty}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public SimpleConstrainedLongProperty(
            long initialValue,
            ValidationState initialValidationState,
            Constraint<? super Number, D>... constraints) {
        this(DEFAULT_BEAN, DEFAULT_NAME, initialValue, initialValidationState, constraints);
    }

    /**
     * Initializes a new instance of {@code SimpleConstrainedLongProperty}.
     * The specified constraints are immediately evaluated.
     *
     * @param bean the bean of this {@code SimpleConstrainedLongProperty}
     * @param name the name of this {@code SimpleConstrainedLongProperty}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public SimpleConstrainedLongProperty(Object bean, String name, Constraint<? super Number, D>... constraints) {
        this(bean, name, 0, ValidationState.UNKNOWN, constraints);
    }

    /**
     * Initializes a new instance of {@code SimpleConstrainedLongProperty}.
     * The specified constraints are immediately evaluated.
     *
     * @param bean the bean of this {@code SimpleConstrainedLongProperty}
     * @param name the name of this {@code SimpleConstrainedLongProperty}
     * @param initialValue the initial value of this {@code SimpleConstrainedLongProperty}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public SimpleConstrainedLongProperty(
            Object bean, String name, long initialValue, Constraint<? super Number, D>... constraints) {
        this(bean, name, initialValue, ValidationState.UNKNOWN, constraints);
    }

    /**
     * Initializes a new instance of {@code SimpleConstrainedLongProperty}.
     * If the initial state is {@link ValidationState#UNKNOWN}, the constraints are immediately evaluated.
     * Otherwise, the constraints will be evaluated when the property value is changed.
     *
     * @param bean the bean of this {@code SimpleConstrainedLongProperty}
     * @param name the name of this {@code SimpleConstrainedLongProperty}
     * @param initialValue the initial value of this {@code SimpleConstrainedLongProperty}
     * @param initialValidationState the initial validation state of this {@code SimpleConstrainedLongProperty}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public SimpleConstrainedLongProperty(
            Object bean, String name, long initialValue, ValidationState initialValidationState,
            Constraint<? super Number, D>... constraints) {
        super(initialValue, initialValidationState, constraints);
        this.bean = bean;
        this.name = (name == null) ? DEFAULT_NAME : name;
    }

}
