/*
 * Copyright (c) 2011, 2016, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.beans.property.validation;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * This class provides a convenient class to define read-only properties. It
 * creates two properties that are synchronized. One property is read-only
 * and can be passed to external users. The other property is read- and
 * writable and should be used internally only.
 *
 * @param <T> element type
 * @param <E> error information type
 *
 * @since JFXcore 18
 */
public class ReadOnlyConstrainedListWrapper<T, E> extends SimpleConstrainedListProperty<T, E> {

    private ReadOnlyPropertyImpl readOnlyProperty;

    /**
     * The constructor of {@code ReadOnlyConstrainedListWrapper}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(Constraint<? super ObservableList<T>, E>... constraints) {
        super(null, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedListWrapper}.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedListWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(
            ObservableList<T> initialValue, Constraint<? super ObservableList<T>, E>... constraints) {
        super(initialValue, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedListWrapper}.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedListWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedListWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(
            Object bean, String name, Constraint<? super ObservableList<T>, E>... constraints) {
        super(bean, name, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedListWrapper}.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedListWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedListWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedListWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(
            Object bean, String name, ObservableList<T> initialValue,
            Constraint<? super ObservableList<T>, E>... constraints) {
        super(bean, name, initialValue, constraints);
    }

    /**
     * Returns the read-only property that is synchronized with this
     * {@code ReadOnlyConstrainedListWrapper}.
     *
     * @return the read-only property
     */
    public ReadOnlyConstrainedListProperty<T, E> getReadOnlyProperty() {
        if (readOnlyProperty == null) {
            readOnlyProperty = new ReadOnlyPropertyImpl();
        }

        return readOnlyProperty;
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();

        if (readOnlyProperty != null) {
            readOnlyProperty.fireValueChangedEvent();
        }
    }

    @Override
    protected void fireValueChangedEvent(ListChangeListener.Change<? extends T> change) {
        super.fireValueChangedEvent(change);

        if (readOnlyProperty != null) {
            change.reset();
            readOnlyProperty.fireValueChangedEvent(change);
        }
    }

    private class ReadOnlyPropertyImpl extends ReadOnlyConstrainedListPropertyBase<T, E> {
        @Override
        public ObservableList<T> get() {
            return ReadOnlyConstrainedListWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyConstrainedListWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyConstrainedListWrapper.this.getName();
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            return ReadOnlyConstrainedListWrapper.this.sizeProperty();
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            return ReadOnlyConstrainedListWrapper.this.emptyProperty();
        }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return ReadOnlyConstrainedListWrapper.this.validProperty();
        }

        @Override
        public ReadOnlyBooleanProperty userValidProperty() {
            return ReadOnlyConstrainedListWrapper.this.userValidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty invalidProperty() {
            return ReadOnlyConstrainedListWrapper.this.invalidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty userInvalidProperty() {
            return ReadOnlyConstrainedListWrapper.this.userInvalidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty validatingProperty() {
            return ReadOnlyConstrainedListWrapper.this.validatingProperty();
        }

        @Override
        public ReadOnlyListProperty<E> errorsProperty() {
            return ReadOnlyConstrainedListWrapper.this.errorsProperty();
        }

        @Override
        public ReadOnlyListProperty<T> constrainedValueProperty() {
            return ReadOnlyConstrainedListWrapper.this.constrainedValueProperty();
        }
    }

}
