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
import org.jfxcore.validation.DeferredFloatProperty;
import org.jfxcore.validation.PropertyHelper;
import org.jfxcore.validation.ValidationHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.FloatBinding;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableFloatValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.util.Incubating;
import javafx.validation.Constraint;
import javafx.validation.DiagnosticList;
import javafx.validation.ValidationListener;
import javafx.validation.ValidationState;
import java.lang.ref.WeakReference;

/**
 * Provides a base implementation for a constrained property that wraps a float value.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public abstract class ConstrainedFloatPropertyBase<D> extends ConstrainedFloatProperty<D> {

    static {
        PropertyHelper.setFloatAccessor(
            new PropertyHelper.Accessor<Number>() {
                @Override
                public <D1> ValidationHelper<Number, D1> getValidationHelper(
                        ReadOnlyConstrainedProperty<Number, D1> property) {
                    return ((ConstrainedFloatPropertyBase<D1>)property).validationHelper;
                }

                @Override
                public <D1> Number readValue(
                        ReadOnlyConstrainedProperty<Number, D1> property) {
                    return ((ConstrainedFloatPropertyBase<D1>)property).readValue();
                }
            }
        );
    }

    private final ValidationHelper<Number, D> validationHelper;
    private final DeferredFloatProperty constrainedValue;
    private ObservableFloatValue observable;
    private InvalidationListener listener;
    private ExpressionHelper<Number> expressionHelper;
    private boolean valid = true;
    private float value;

    /**
     * The constructor of the {@code ConstrainedFloatPropertyBase}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedFloatPropertyBase(Constraint<? super Number, D>... constraints) {
        this(0, ValidationState.UNKNOWN, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedFloatPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedFloatPropertyBase(float initialValue, Constraint<? super Number, D>... constraints) {
        this(initialValue, ValidationState.UNKNOWN, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedFloatPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param initialValidationState the initial validations tate
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedFloatPropertyBase(
            float initialValue,
            ValidationState initialValidationState,
            Constraint<? super Number, D>... constraints) {
        value = initialValue;

        constrainedValue = new DeferredFloatProperty(initialValue) {
            @Override public String getName() { return "constrainedValue"; }
            @Override public Object getBean() { return ConstrainedFloatPropertyBase.this; }
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
    public final boolean isValid() {
        return validationHelper.isValid();
    }

    @Override
    public final ReadOnlyBooleanProperty invalidProperty() {
        return validationHelper.invalidProperty();
    }

    @Override
    public final boolean isInvalid() {
        return validationHelper.isInvalid();
    }

    @Override
    public final ReadOnlyBooleanProperty validatingProperty() {
        return validationHelper.validatingProperty();
    }

    @Override
    public final boolean isValidating() {
        return validationHelper.isValidating();
    }

    @Override
    public final ReadOnlyFloatProperty constrainedValueProperty() {
        return constrainedValue;
    }

    @Override
    public final ReadOnlyDiagnosticListProperty<D> diagnosticsProperty() {
        return validationHelper.diagnosticsProperty();
    }

    @Override
    public final DiagnosticList<D> getDiagnostics() {
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
    public void addListener(ChangeListener<? super Number> listener) {
        expressionHelper = ExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener) {
        expressionHelper = ExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(ValidationListener<? super Number, D> listener) {
        validationHelper.addListener(listener);
    }

    @Override
    public void removeListener(ValidationListener<? super Number, D> listener) {
        validationHelper.removeListener(listener);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners} and
     * {@link ChangeListener ChangeListeners}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set(float)} or in case of a bound property, if the
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
     * {@code Objects} defining the property, because it requires less memory.
     *
     * The default implementation is empty.
     */
    protected void invalidated() {
    }

    private float readValue() {
        return observable == null ? value : observable.get();
    }

    @Override
    public float get() {
        valid = true;
        return observable == null ? value : observable.get();
    }

    @Override
    public void set(float newValue) {
        if (isBound()) {
            throw PropertyHelper.cannotSetBoundProperty(this);
        }

        if (value != newValue) {
            value = newValue;
            markInvalid();
        }
    }

    @Override
    public boolean isBound() {
        return observable != null;
    }

    @Override
    public void bind(final ObservableValue<? extends Number> source) {
        if (source == null) {
            throw PropertyHelper.cannotBindNull(this);
        }

        ObservableFloatValue newObservable;
        if (source instanceof ObservableFloatValue) {
            newObservable = (ObservableFloatValue)source;
        } else if (source instanceof ObservableNumberValue) {
            final ObservableNumberValue numberValue = (ObservableNumberValue)source;
            newObservable = new ValueWrapper(source) {
                @Override
                protected float computeValue() {
                    return numberValue.floatValue();
                }
            };
        } else {
            newObservable = new ValueWrapper(source) {
                @Override
                protected float computeValue() {
                    final Number value = source.getValue();
                    return (value == null) ? 0 : value.floatValue();
                }
            };
        }

        if (!newObservable.equals(observable)) {
            unbind();
            observable = newObservable;
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
            value = observable.get();
            observable.removeListener(listener);
            if (observable instanceof ValueWrapper) {
                ((ValueWrapper)observable).dispose();
            }
            observable = null;
        }
    }

    @Override
    public String toString() {
        return PropertyHelper.toString(this, valid);
    }

    private static class Listener<D> implements InvalidationListener, WeakListener {
        private final WeakReference<ConstrainedFloatPropertyBase<D>> wref;

        public Listener(ConstrainedFloatPropertyBase<D> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            ConstrainedFloatPropertyBase<D> ref = wref.get();
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

    private static abstract class ValueWrapper extends FloatBinding {
        private final ObservableValue<? extends Number> observable;

        public ValueWrapper(ObservableValue<? extends Number> observable) {
            this.observable = observable;
            bind(observable);
        }

        @Override
        public void dispose() {
            unbind(observable);
        }
    }

}
