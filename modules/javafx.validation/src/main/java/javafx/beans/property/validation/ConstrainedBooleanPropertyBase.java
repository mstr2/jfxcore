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

import com.sun.javafx.binding.ExpressionHelper;
import org.jfxcore.beans.property.validation.BooleanPropertyImpl;
import org.jfxcore.beans.property.validation.PropertyHelper;
import org.jfxcore.beans.property.validation.ValidationHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import java.lang.ref.WeakReference;

/**
 * Provides a base implementation for a constrained property that wraps a boolean value.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <E> error information type
 * @since JFXcore 18
 */
public abstract class ConstrainedBooleanPropertyBase<E> extends ConstrainedBooleanProperty<E> {

    static {
        ValidationHelper.setAccessor(
            ConstrainedBooleanPropertyBase.class,
            property -> ((ConstrainedBooleanPropertyBase<?>)property).validationHelper);
    }

    private final BooleanPropertyImpl constrainedValue;
    private final ValidationHelper<Boolean, E> validationHelper;
    private boolean value;
    private ObservableBooleanValue observable;
    private InvalidationListener listener;
    private boolean valid = true;
    private ExpressionHelper<Boolean> expressionHelper;

    /**
     * The constructor of the {@code ConstrainedIntegerPropertyBase}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedBooleanPropertyBase(Constraint<? super Boolean, E>... constraints) {
        this(false, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedIntegerPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedBooleanPropertyBase(boolean initialValue, Constraint<? super Boolean, E>... constraints) {
        value = initialValue;

        constrainedValue = new BooleanPropertyImpl(initialValue) {
            @Override public String getName() { return "constrainedValue"; }
            @Override public Object getBean() { return ConstrainedBooleanPropertyBase.this; }
        };

        validationHelper = new ValidationHelper<>(this, constrainedValue, constraints);
    }

    @Override
    public final ReadOnlyBooleanProperty validProperty() {
        return validationHelper.validProperty();
    }

    @Override
    public final ReadOnlyBooleanProperty userValidProperty() {
        return validationHelper.userValidProperty();
    }

    @Override
    public final ReadOnlyBooleanProperty invalidProperty() {
        return validationHelper.invalidProperty();
    }

    @Override
    public final ReadOnlyBooleanProperty userInvalidProperty() {
        return validationHelper.userInvalidProperty();
    }

    @Override
    public final ReadOnlyBooleanProperty validatingProperty() {
        return validationHelper.validatingProperty();
    }

    @Override
    public final ReadOnlyBooleanProperty constrainedValueProperty() {
        return constrainedValue;
    }

    @Override
    public final ReadOnlyListProperty<E> errorsProperty() {
        return validationHelper.errorsProperty();
    }

    @Override
    public final void addListener(InvalidationListener listener) {
        expressionHelper = ExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public final void removeListener(InvalidationListener listener) {
        expressionHelper = ExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public final void addListener(ChangeListener<? super Boolean> listener) {
        expressionHelper = ExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public final void removeListener(ChangeListener<? super Boolean> listener) {
        expressionHelper = ExpressionHelper.removeListener(expressionHelper, listener);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners} and
     * {@link ChangeListener ChangeListeners}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set(boolean)} or in case of a bound property, if the
     * binding becomes invalid.
     */
    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(expressionHelper);
    }

    private void markInvalid() {
        if (valid) {
            valid = false;
            invalidated();
            fireValueChangedEvent();
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

    @Override
    public boolean get() {
        valid = true;
        return observable == null ? value : observable.get();
    }

    @Override
    public void set(boolean newValue) {
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
    public void bind(final ObservableValue<? extends Boolean> source) {
        if (source == null) {
            throw PropertyHelper.cannotBindNull(this);
        }

        final ObservableBooleanValue newObservable = (source instanceof ObservableBooleanValue) ?
                (ObservableBooleanValue) source : new ValueWrapper(source);

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

    private static class Listener<E> implements InvalidationListener, WeakListener {
        private final WeakReference<ConstrainedBooleanPropertyBase<E>> wref;

        public Listener(ConstrainedBooleanPropertyBase<E> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            ConstrainedBooleanPropertyBase<E> ref = wref.get();
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

    private static class ValueWrapper extends BooleanBinding {
        private final ObservableValue<? extends Boolean> observable;

        public ValueWrapper(ObservableValue<? extends Boolean> observable) {
            this.observable = observable;
            bind(observable);
        }

        @Override
        protected boolean computeValue() {
            final Boolean value = observable.getValue();
            return value != null && value;
        }

        @Override
        public void dispose() {
            unbind(observable);
        }
    }

}
