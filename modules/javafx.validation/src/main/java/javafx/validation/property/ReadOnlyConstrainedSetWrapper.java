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

import com.sun.javafx.binding.SetExpressionHelper;
import org.jfxcore.validation.ValidationListenerWrapper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.util.Incubating;
import javafx.validation.ConstrainedElement;
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
 * @param <E> element type
 * @param <D> diagnostic type
 *
 * @since JFXcore 18
 */
@Incubating
public class ReadOnlyConstrainedSetWrapper<E, D> extends SimpleConstrainedSetProperty<E, D> {

    private ReadOnlyPropertyImpl readOnlyProperty;

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedSetWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedSetWrapper(Constraint<? super E, D>... constraints) {
        super(null, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedSetWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedSetWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedSetWrapper(
            ObservableSet<E> initialValue, Constraint<? super E, D>... constraints) {
        super(initialValue, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedSetWrapper}.
     * If the initial state is {@link ValidationState#UNKNOWN}, the constraints are immediately evaluated.
     * Otherwise, the constraints will be evaluated when the property value is changed.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedSetWrapper}
     * @param initialValidationState the initial validation state of this {@code ReadOnlyConstrainedSetWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedSetWrapper(
            ObservableSet<E> initialValue,
            ValidationState initialValidationState,
            Constraint<? super E, D>... constraints) {
        super(initialValue, initialValidationState, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedSetWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedSetWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedSetWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedSetWrapper(
            Object bean, String name, Constraint<? super E, D>... constraints) {
        super(bean, name, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedSetWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedSetWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedSetWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedSetWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedSetWrapper(
            Object bean,
            String name,
            ObservableSet<E> initialValue,
            Constraint<? super E, D>... constraints) {
        super(bean, name, initialValue, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedSetWrapper}.
     * If the initial state is {@link ValidationState#UNKNOWN}, the constraints are immediately evaluated.
     * Otherwise, the constraints will be evaluated when the property value is changed.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedSetWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedSetWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedSetWrapper}
     * @param initialValidationState the initial validation state of this {@code ReadOnlyConstrainedSetWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedSetWrapper(
            Object bean,
            String name,
            ObservableSet<E> initialValue,
            ValidationState initialValidationState,
            Constraint<? super E, D>... constraints) {
        super(bean, name, initialValue, initialValidationState, constraints);
    }

    /**
     * Returns the read-only property that is synchronized with this
     * {@code ReadOnlyConstrainedSetWrapper}.
     *
     * @return the read-only property
     */
    public ReadOnlyConstrainedSetProperty<E, D> getReadOnlyProperty() {
        if (readOnlyProperty == null) {
            readOnlyProperty = new ReadOnlyPropertyImpl();
        }
        
        return readOnlyProperty;
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();
        
        if (readOnlyProperty != null) {
            SetExpressionHelper.fireValueChangedEvent(readOnlyProperty.helper);
        }
    }

    @Override
    protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
        super.fireValueChangedEvent(change);
        
        if (readOnlyProperty != null) {
            SetExpressionHelper.fireValueChangedEvent(readOnlyProperty.helper, change);
        }
    }

    private class ReadOnlyPropertyImpl extends ReadOnlyConstrainedSetProperty<E, D> {
        SetExpressionHelper<E> helper;

        @Override
        public void addListener(InvalidationListener listener) {
            helper = SetExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            helper = SetExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ChangeListener<? super ObservableSet<E>> listener) {
            helper = SetExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(ChangeListener<? super ObservableSet<E>> listener) {
            helper = SetExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(SetChangeListener<? super E> listener) {
            helper = SetExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(SetChangeListener<? super E> listener) {
            helper = SetExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ValidationListener<? super ObservableSet<E>, D> listener) {
            ReadOnlyConstrainedSetWrapper.this.addListener(new ValidationListenerWrapper<>(this, listener));
        }

        @Override
        public void removeListener(ValidationListener<? super ObservableSet<E>, D> listener) {
            ReadOnlyConstrainedSetWrapper.this.removeListener(new ValidationListenerWrapper<>(this, listener));
        }

        @Override
        public ObservableSet<E> get() {
            return ReadOnlyConstrainedSetWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyConstrainedSetWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyConstrainedSetWrapper.this.getName();
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            return ReadOnlyConstrainedSetWrapper.this.sizeProperty();
        }

        @Override
        public int getSize() {
            return ReadOnlyConstrainedSetWrapper.this.getSize();
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            return ReadOnlyConstrainedSetWrapper.this.emptyProperty();
        }

        @Override
        public boolean isEmpty() {
            return ReadOnlyConstrainedSetWrapper.this.isEmpty();
        }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return ReadOnlyConstrainedSetWrapper.this.validProperty();
        }

        @Override
        public boolean isValid() {
            return ReadOnlyConstrainedSetWrapper.this.isValid();
        }

        @Override
        public ReadOnlyBooleanProperty invalidProperty() {
            return ReadOnlyConstrainedSetWrapper.this.invalidProperty();
        }

        @Override
        public boolean isInvalid() {
            return ReadOnlyConstrainedSetWrapper.this.isInvalid();
        }

        @Override
        public ReadOnlyBooleanProperty validatingProperty() {
            return ReadOnlyConstrainedSetWrapper.this.validatingProperty();
        }

        @Override
        public boolean isValidating() {
            return ReadOnlyConstrainedSetWrapper.this.isValidating();
        }

        @Override
        public ReadOnlyDiagnosticListProperty<D> diagnosticsProperty() {
            return ReadOnlyConstrainedSetWrapper.this.diagnosticsProperty();
        }

        @Override
        public DiagnosticList<D> getDiagnostics() {
            return ReadOnlyConstrainedSetWrapper.this.getDiagnostics();
        }

        @Override
        public ReadOnlySetProperty<E> constrainedValueProperty() {
            return ReadOnlyConstrainedSetWrapper.this.constrainedValueProperty();
        }

        @Override
        public ObservableSet<E> getConstrainedValue() {
            return ReadOnlyConstrainedSetWrapper.this.getConstrainedValue();
        }

        @Override
        public ReadOnlyMapProperty<E, ConstrainedElement<E, D>> constrainedElementsProperty() {
            return ReadOnlyConstrainedSetWrapper.this.constrainedElementsProperty();
        }

        @Override
        public ObservableMap<E, ConstrainedElement<E, D>> getConstrainedElements() {
            return ReadOnlyConstrainedSetWrapper.this.getConstrainedElements();
        }
    }
    
}
