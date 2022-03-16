/*
 * Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022, JFXcore. All rights reserved.
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

package javafx.validation.property;

import com.sun.javafx.binding.ExpressionHelper;
import org.jfxcore.validation.ValidationListenerWrapper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.util.Incubating;
import javafx.validation.Constraint;
import javafx.validation.DiagnosticList;
import javafx.validation.ValidationListener;
import javafx.validation.ValidationState;

/**
 * This class provides a convenient class to define read-only properties. It
 * creates two properties that are synchronized. One property is read-only
 * and can be passed to external users. The other property is read- and
 * writable and should be used internally only.
 *
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public class ReadOnlyConstrainedBooleanWrapper<D> extends SimpleConstrainedBooleanProperty<D> {

    private ReadOnlyPropertyImpl readOnlyProperty;

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedBooleanWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedBooleanWrapper(Constraint<? super Boolean, D>... constraints) {
        super(false, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedBooleanWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedBooleanWrapper(boolean initialValue, Constraint<? super Boolean, D>... constraints) {
        super(initialValue, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedBooleanWrapper}.
     * If the initial state is {@link ValidationState#UNKNOWN}, the constraints are immediately evaluated.
     * Otherwise, the constraints will be evaluated when the property value is changed.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param initialValidationState the initial validation state of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedBooleanWrapper(
            boolean initialValue,
            ValidationState initialValidationState,
            Constraint<? super Boolean, D>... constraints) {
        super(initialValue, initialValidationState, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedBooleanWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedBooleanWrapper(Object bean, String name, Constraint<? super Boolean, D>... constraints) {
        super(bean, name, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedBooleanWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedBooleanWrapper(
            Object bean, String name, boolean initialValue, Constraint<? super Boolean, D>... constraints) {
        super(bean, name, initialValue, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedBooleanWrapper}.
     * If the initial state is {@link ValidationState#UNKNOWN}, the constraints are immediately evaluated.
     * Otherwise, the constraints will be evaluated when the property value is changed.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param initialValidationState the initial validation state of this {@code ReadOnlyConstrainedBooleanWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedBooleanWrapper(
            Object bean,
            String name,
            boolean initialValue,
            ValidationState initialValidationState,
            Constraint<? super Boolean, D>... constraints) {
        super(bean, name, initialValue, initialValidationState, constraints);
    }

    /**
     * Returns the read-only property that is synchronized with this
     * {@code ReadOnlyConstrainedBooleanWrapper}.
     *
     * @return the read-only property
     */
    public ReadOnlyConstrainedBooleanProperty<D> getReadOnlyProperty() {
        if (readOnlyProperty == null) {
            readOnlyProperty = new ReadOnlyPropertyImpl();
        }
        return readOnlyProperty;
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();

        if (readOnlyProperty != null) {
            ExpressionHelper.fireValueChangedEvent(readOnlyProperty.helper);
        }
    }

    private class ReadOnlyPropertyImpl extends ReadOnlyConstrainedBooleanProperty<D> {
        ExpressionHelper<Boolean> helper;

        @Override
        public void addListener(InvalidationListener listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ChangeListener<? super Boolean> listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Boolean> listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ValidationListener<? super Boolean, D> listener) {
            ReadOnlyConstrainedBooleanWrapper.this.addListener(new ValidationListenerWrapper<>(this, listener));
        }

        @Override
        public void removeListener(ValidationListener<? super Boolean, D> listener) {
            ReadOnlyConstrainedBooleanWrapper.this.removeListener(new ValidationListenerWrapper<>(this, listener));
        }

        @Override
        public boolean get() {
            return ReadOnlyConstrainedBooleanWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyConstrainedBooleanWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyConstrainedBooleanWrapper.this.getName();
        }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return ReadOnlyConstrainedBooleanWrapper.this.validProperty();
        }

        @Override
        public boolean isValid() {
            return ReadOnlyConstrainedBooleanWrapper.this.isValid();
        }

        @Override
        public ReadOnlyBooleanProperty invalidProperty() {
            return ReadOnlyConstrainedBooleanWrapper.this.invalidProperty();
        }

        @Override
        public boolean isInvalid() {
            return ReadOnlyConstrainedBooleanWrapper.this.isInvalid();
        }

        @Override
        public ReadOnlyBooleanProperty validatingProperty() {
            return ReadOnlyConstrainedBooleanWrapper.this.validatingProperty();
        }

        @Override
        public boolean isValidating() {
            return ReadOnlyConstrainedBooleanWrapper.this.isValidating();
        }

        @Override
        public ReadOnlyDiagnosticListProperty<D> diagnosticsProperty() {
            return ReadOnlyConstrainedBooleanWrapper.this.diagnosticsProperty();
        }

        @Override
        public DiagnosticList<D> getDiagnostics() {
            return ReadOnlyConstrainedBooleanWrapper.this.getDiagnostics();
        }

        @Override
        public ReadOnlyBooleanProperty constrainedValueProperty() {
            return ReadOnlyConstrainedBooleanWrapper.this.constrainedValueProperty();
        }

        @Override
        public Boolean getConstrainedValue() {
            return ReadOnlyConstrainedBooleanWrapper.this.getConstrainedValue();
        }
    }

}
