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
import org.jfxcore.validation.DeferredStringProperty;
import org.jfxcore.validation.PropertyHelper;
import org.jfxcore.validation.ValidationHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Incubating;
import javafx.validation.Constraint;
import javafx.validation.DiagnosticList;
import javafx.validation.ValidationListener;
import javafx.validation.ValidationState;
import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * Provides a base implementation for a constrained property that wraps a string value.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public abstract class ConstrainedStringPropertyBase<D> extends ConstrainedStringProperty<D> {

    static {
        PropertyHelper.setStringAccessor(
            new PropertyHelper.Accessor<String>() {
                @Override
                public <D1> ValidationHelper<String, D1> getValidationHelper(
                        ReadOnlyConstrainedProperty<String, D1> property) {
                    return ((ConstrainedStringPropertyBase<D1>)property).validationHelper;
                }

                @Override
                public <D1> String readValue(
                        ReadOnlyConstrainedProperty<String, D1> property) {
                    return ((ConstrainedStringPropertyBase<D1>)property).readValue();
                }
            }
        );
    }

    private final ValidationHelper<String, D> validationHelper;
    private final DeferredStringProperty constrainedValue;
    private ObservableValue<? extends String> observable;
    private InvalidationListener listener;
    private ExpressionHelper<String> expressionHelper;
    private boolean valid = true;
    private String value;

    /**
     * The constructor of the {@code ConstrainedStringPropertyBase}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedStringPropertyBase(Constraint<? super String, D>... constraints) {
        this(null, ValidationState.UNKNOWN, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedStringPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped object
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedStringPropertyBase(String initialValue, Constraint<? super String, D>... constraints) {
        this(initialValue, ValidationState.UNKNOWN, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedStringPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped object
     * @param initialValidationState the initial validation state
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedStringPropertyBase(
            String initialValue,
            ValidationState initialValidationState,
            Constraint<? super String, D>... constraints) {
        value = initialValue;

        constrainedValue = new DeferredStringProperty(initialValue) {
            @Override public String getName() { return "constrainedValue"; }
            @Override public Object getBean() { return ConstrainedStringPropertyBase.this; }
        };

        validationHelper = new ValidationHelper<>(this, constrainedValue, initialValidationState, constraints);

        if (initialValidationState == ValidationState.UNKNOWN) {
            validationHelper.invalidated(this);
        }
    }

    @Override
    public final ReadOnlyBooleanProperty validProperty() {
        return validationHelper.validProperty();
    }

    @Override
    public boolean isValid() {
        return validationHelper.isValid();
    }

    @Override
    public final ReadOnlyBooleanProperty invalidProperty() {
        return validationHelper.invalidProperty();
    }

    @Override
    public boolean isInvalid() {
        return validationHelper.isInvalid();
    }

    @Override
    public final ReadOnlyBooleanProperty validatingProperty() {
        return validationHelper.validatingProperty();
    }

    @Override
    public boolean isValidating() {
        return validationHelper.isValidating();
    }

    @Override
    public final ReadOnlyStringProperty constrainedValueProperty() {
        return constrainedValue;
    }

    @Override
    public final ReadOnlyDiagnosticListProperty<D> diagnosticsProperty() {
        return validationHelper.diagnosticsProperty();
    }

    @Override
    public DiagnosticList<D> getDiagnostics() {
        return validationHelper.getDiagnostics();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        expressionHelper = ExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        expressionHelper = ExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(ChangeListener<? super String> listener) {
        expressionHelper = ExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super String> listener) {
        expressionHelper = ExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(ValidationListener<? super String, D> listener) {
        validationHelper.addListener(listener);
    }

    @Override
    public void removeListener(ValidationListener<? super String, D> listener) {
        validationHelper.removeListener(listener);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners} and
     * {@link ChangeListener ChangeListeners}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set} or in case of a bound property, if the
     * binding becomes invalid.
     */
    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(expressionHelper);
    }

    private void markInvalid() {
        if (valid) {
            valid = false;
            validationHelper.invalidated(this);
            invalidated();
            fireValueChangedEvent();
        } else {
            validationHelper.invalidated(this);
        }
    }

    /**
     * The method {@code invalidated()} can be overridden to receive
     * invalidation notifications. This is the preferred option in
     * {@code Strings} defining the property, because it requires less memory.
     *
     * The default implementation is empty.
     */
    protected void invalidated() {
    }

    private String readValue() {
        return observable == null ? value : observable.getValue();
    }

    @Override
    public String get() {
        valid = true;
        return observable == null ? value : observable.getValue();
    }

    @Override
    public void set(String newValue) {
        if (isBound()) {
            throw PropertyHelper.cannotSetBoundProperty(this);
        }

        if (!Objects.equals(value, newValue)) {
            value = newValue;
            markInvalid();
        }
    }

    @Override
    public boolean isBound() {
        return observable != null;
    }

    @Override
    public void bind(final ObservableValue<? extends String> source) {
        if (source == null) {
            throw PropertyHelper.cannotBindNull(this);
        }

        if (!source.equals(this.observable)) {
            unbind();
            observable = source;
            if (listener == null) {
                listener = new Listener<>(this);
            }
            observable.addListener(listener);
            markInvalid();
        }
    }

    @Override
    public void unbind() {
        if (observable != null) {
            value = observable.getValue();
            observable.removeListener(listener);
            observable = null;
        }
    }

    @Override
    public String toString() {
        return PropertyHelper.toString(this, valid);
    }

    private static class Listener<D> implements InvalidationListener, WeakListener {
        private final WeakReference<ConstrainedStringPropertyBase<D>> wref;

        public Listener(ConstrainedStringPropertyBase<D> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            ConstrainedStringPropertyBase<D> ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }
    
}
