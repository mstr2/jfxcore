/*
 * Copyright (c) 2011, 2016, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javafx.binding.ListExpressionHelper;
import org.jfxcore.validation.ValidationListenerWrapper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
public class ReadOnlyConstrainedListWrapper<E, D> extends SimpleConstrainedListProperty<E, D> {

    private ReadOnlyPropertyImpl readOnlyProperty;

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedListWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(Constraint<? super E, D>... constraints) {
        super(null, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedListWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedListWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(
            ObservableList<E> initialValue, Constraint<? super E, D>... constraints) {
        super(initialValue, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedListWrapper}.
     * If the initial state is {@link ValidationState#UNKNOWN}, the constraints are immediately evaluated.
     * Otherwise, the constraints will be evaluated when the property value is changed.
     *
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedListWrapper}
     * @param initialValidationState the initial validations tate of this {@code ReadOnlyConstrainedListWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(
            ObservableList<E> initialValue,
            ValidationState initialValidationState,
            Constraint<? super E, D>... constraints) {
        super(initialValue, initialValidationState, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedListWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedListWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedListWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(
            Object bean, String name, Constraint<? super E, D>... constraints) {
        super(bean, name, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedListWrapper}.
     * The specified constraints are immediately evaluated.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedListWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedListWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedListWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(
            Object bean,
            String name,
            ObservableList<E> initialValue,
            Constraint<? super E, D>... constraints) {
        super(bean, name, initialValue, constraints);
    }

    /**
     * Initializes a new instance of {@code ReadOnlyConstrainedListWrapper}.
     * If the initial state is {@link ValidationState#UNKNOWN}, the constraints are immediately evaluated.
     * Otherwise, the constraints will be evaluated when the property value is changed.
     *
     * @param bean the bean of this {@code ReadOnlyConstrainedListWrapper}
     * @param name the name of this {@code ReadOnlyConstrainedListWrapper}
     * @param initialValue the initial value of this {@code ReadOnlyConstrainedListWrapper}
     * @param initialValidationState the initial validation state of this {@code ReadOnlyConstrainedListWrapper}
     * @param constraints the value constraints
     */
    @SafeVarargs
    public ReadOnlyConstrainedListWrapper(
            Object bean,
            String name,
            ObservableList<E> initialValue,
            ValidationState initialValidationState,
            Constraint<? super E, D>... constraints) {
        super(bean, name, initialValue, initialValidationState, constraints);
    }

    /**
     * Returns the read-only property that is synchronized with this
     * {@code ReadOnlyConstrainedListWrapper}.
     *
     * @return the read-only property
     */
    public ReadOnlyConstrainedListProperty<E, D> getReadOnlyProperty() {
        if (readOnlyProperty == null) {
            readOnlyProperty = new ReadOnlyPropertyImpl();
        }

        return readOnlyProperty;
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();

        if (readOnlyProperty != null) {
            ListExpressionHelper.fireValueChangedEvent(readOnlyProperty.helper);
        }
    }

    @Override
    protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
        super.fireValueChangedEvent(change);

        if (readOnlyProperty != null) {
            change.reset();
            ListExpressionHelper.fireValueChangedEvent(readOnlyProperty.helper, change);
        }
    }

    private class ReadOnlyPropertyImpl extends ReadOnlyConstrainedListProperty<E, D> {
        ListExpressionHelper<E> helper;

        @Override
        public void addListener(InvalidationListener listener) {
            helper = ListExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            helper = ListExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ChangeListener<? super ObservableList<E>> listener) {
            helper = ListExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(ChangeListener<? super ObservableList<E>> listener) {
            helper = ListExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener) {
            helper = ListExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(ListChangeListener<? super E> listener) {
            helper = ListExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ValidationListener<? super ObservableList<E>, D> listener) {
            ReadOnlyConstrainedListWrapper.this.addListener(new ValidationListenerWrapper<>(this, listener));
        }

        @Override
        public void removeListener(ValidationListener<? super ObservableList<E>, D> listener) {
            ReadOnlyConstrainedListWrapper.this.removeListener(new ValidationListenerWrapper<>(this, listener));
        }

        @Override
        public ObservableList<E> get() {
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
        public int getSize() {
            return ReadOnlyConstrainedListWrapper.this.getSize();
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            return ReadOnlyConstrainedListWrapper.this.emptyProperty();
        }

        @Override
        public boolean isEmpty() {
            return ReadOnlyConstrainedListWrapper.this.isEmpty();
        }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return ReadOnlyConstrainedListWrapper.this.validProperty();
        }

        @Override
        public boolean isValid() {
            return ReadOnlyConstrainedListWrapper.this.isValid();
        }

        @Override
        public ReadOnlyBooleanProperty invalidProperty() {
            return ReadOnlyConstrainedListWrapper.this.invalidProperty();
        }

        @Override
        public boolean isInvalid() {
            return ReadOnlyConstrainedListWrapper.this.isInvalid();
        }

        @Override
        public ReadOnlyBooleanProperty validatingProperty() {
            return ReadOnlyConstrainedListWrapper.this.validatingProperty();
        }

        @Override
        public boolean isValidating() {
            return ReadOnlyConstrainedListWrapper.this.isValidating();
        }

        @Override
        public ReadOnlyDiagnosticListProperty<D> diagnosticsProperty() {
            return ReadOnlyConstrainedListWrapper.this.diagnosticsProperty();
        }

        @Override
        public DiagnosticList<D> getDiagnostics() {
            return ReadOnlyConstrainedListWrapper.this.getDiagnostics();
        }

        @Override
        public ReadOnlyListProperty<E> constrainedValueProperty() {
            return ReadOnlyConstrainedListWrapper.this.constrainedValueProperty();
        }

        @Override
        public ObservableList<E> getConstrainedValue() {
            return ReadOnlyConstrainedListWrapper.this.getConstrainedValue();
        }

        @Override
        public ReadOnlyListProperty<ConstrainedElement<E, D>> constrainedElementsProperty() {
            return ReadOnlyConstrainedListWrapper.this.constrainedElementsProperty();
        }

        @Override
        public ObservableList<ConstrainedElement<E, D>> getConstrainedElements() {
            return ReadOnlyConstrainedListWrapper.this.getConstrainedElements();
        }
    }

}
