/*
 * Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
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
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

/**
 * This class provides a convenient class to define read-only properties. It
 * creates two properties that are synchronized. One property is read-only
 * and can be passed to external users. The other property is read- and
 * writable and should be used internally only.
 *
 * @param <K> key type
 * @param <V> value type
 * @param <D> diagnostic type
 *
 * @since JFXcore 18
 */
public class ReadOnlyConstrainedMapWrapper<K, V, D> extends SimpleConstrainedMapProperty<K, V, D> {

    private ReadOnlyPropertyImpl readOnlyProperty;

    /**
     * The constructor of {@code ReadOnlyConstrainedMapWrapper}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedMapWrapper(Constraint<? super ObservableMap<K, V>, D>... constraints) {
        super(null, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedMapWrapper}.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedMapWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedMapWrapper(
            ObservableMap<K, V> initialValue,
            Constraint<? super ObservableMap<K, V>, D>... constraints) {
        super(initialValue, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedMapWrapper}.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedMapWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedMapWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedMapWrapper(
            Object bean, String name, Constraint<? super ObservableMap<K, V>, D>... constraints) {
        super(bean, name, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedMapWrapper}.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedMapWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedMapWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedMapWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedMapWrapper(
            Object bean, String name, ObservableMap<K, V> initialValue,
            Constraint<? super ObservableMap<K, V>, D>... constraints) {
        super(bean, name, initialValue, constraints);
    }

    /**
     * Returns the read-only property that is synchronized with this
     * {@code ReadOnlyConstrainedMapWrapper}.
     *
     * @return the read-only property
     */
    public ReadOnlyConstrainedMapProperty<K, V, D> getReadOnlyProperty() {
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
    protected void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> change) {
        super.fireValueChangedEvent(change);
        
        if (readOnlyProperty != null) {
            readOnlyProperty.fireValueChangedEvent(change);
        }
    }

    private class ReadOnlyPropertyImpl extends ReadOnlyConstrainedMapPropertyBase<K, V, D> {
        @Override
        public ObservableMap<K, V> get() {
            return ReadOnlyConstrainedMapWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyConstrainedMapWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyConstrainedMapWrapper.this.getName();
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            return ReadOnlyConstrainedMapWrapper.this.sizeProperty();
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            return ReadOnlyConstrainedMapWrapper.this.emptyProperty();
        }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return ReadOnlyConstrainedMapWrapper.this.validProperty();
        }

        @Override
        public ReadOnlyBooleanProperty userValidProperty() {
            return ReadOnlyConstrainedMapWrapper.this.userValidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty invalidProperty() {
            return ReadOnlyConstrainedMapWrapper.this.invalidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty userInvalidProperty() {
            return ReadOnlyConstrainedMapWrapper.this.userInvalidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty validatingProperty() {
            return ReadOnlyConstrainedMapWrapper.this.validatingProperty();
        }

        @Override
        public ReadOnlyListProperty<D> errorsProperty() {
            return ReadOnlyConstrainedMapWrapper.this.errorsProperty();
        }

        @Override
        public ReadOnlyListProperty<D> warningsProperty() {
            return ReadOnlyConstrainedMapWrapper.this.warningsProperty();
        }

        @Override
        public ReadOnlyMapProperty<K, V> constrainedValueProperty() {
            return ReadOnlyConstrainedMapWrapper.this.constrainedValueProperty();
        }
    }
    
}
