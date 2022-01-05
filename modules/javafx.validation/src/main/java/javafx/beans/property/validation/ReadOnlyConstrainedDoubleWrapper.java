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
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyListProperty;

/**
 * This class provides a convenient class to define read-only properties. It
 * creates two properties that are synchronized. One property is read-only
 * and can be passed to external users. The other property is read- and
 * writable and should be used internally only.
 *
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
public class ReadOnlyConstrainedDoubleWrapper<D> extends SimpleConstrainedDoubleProperty<D> {
    
    private ReadOnlyPropertyImpl readOnlyProperty;

    /**
     * The constructor of {@code ReadOnlyConstrainedDoubleWrapper}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedDoubleWrapper(Constraint<? super Number, D>... constraints) {
        super(0, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedDoubleWrapper}.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedDoubleWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedDoubleWrapper(double initialValue, Constraint<? super Number, D>... constraints) {
        super(initialValue, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedDoubleWrapper}.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedDoubleWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedDoubleWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedDoubleWrapper(Object bean, String name, Constraint<? super Number, D>... constraints) {
        super(bean, name, constraints);
    }

    /**
     * The constructor of {@code ReadOnlyConstrainedDoubleWrapper}.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedDoubleWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedDoubleWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedDoubleWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedDoubleWrapper(
            Object bean, String name, double initialValue, Constraint<? super Number, D>... constraints) {
        super(bean, name, initialValue, constraints);
    }

    /**
     * Returns the read-only property that is synchronized with this
     * {@code ReadOnlyConstrainedDoubleWrapper}.
     *
     * @return the read-only property
     */
    public ReadOnlyConstrainedDoubleProperty<D> getReadOnlyProperty() {
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

    private class ReadOnlyPropertyImpl extends ReadOnlyConstrainedDoublePropertyBase<D> {
        @Override
        public double get() {
            return ReadOnlyConstrainedDoubleWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyConstrainedDoubleWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyConstrainedDoubleWrapper.this.getName();
        }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return ReadOnlyConstrainedDoubleWrapper.this.validProperty();
        }

        @Override
        public ReadOnlyBooleanProperty userValidProperty() {
            return ReadOnlyConstrainedDoubleWrapper.this.userValidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty invalidProperty() {
            return ReadOnlyConstrainedDoubleWrapper.this.invalidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty userInvalidProperty() {
            return ReadOnlyConstrainedDoubleWrapper.this.userInvalidProperty();
        }

        @Override
        public ReadOnlyBooleanProperty validatingProperty() {
            return ReadOnlyConstrainedDoubleWrapper.this.validatingProperty();
        }

        @Override
        public ReadOnlyListProperty<D> errorsProperty() {
            return ReadOnlyConstrainedDoubleWrapper.this.errorsProperty();
        }

        @Override
        public ReadOnlyListProperty<D> warningsProperty() {
            return ReadOnlyConstrainedDoubleWrapper.this.warningsProperty();
        }

        @Override
        public ReadOnlyDoubleProperty constrainedValueProperty() {
            return ReadOnlyConstrainedDoubleWrapper.this.constrainedValueProperty();
        }
    }

}
