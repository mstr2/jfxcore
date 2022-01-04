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
import org.jfxcore.beans.property.validation.ObjectPropertyImpl;
import org.jfxcore.beans.property.validation.PropertyHelper;
import org.jfxcore.beans.property.validation.ValidationHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.lang.ref.WeakReference;

/**
 * Provides a base implementation for a constrained property that wraps an object value.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <E> error information type
 * @since JFXcore 18
 */
public abstract class ConstrainedObjectPropertyBase<T, E> extends ConstrainedObjectProperty<T, E> {

    static {
        ValidationHelper.setAccessor(
            ConstrainedObjectPropertyBase.class,
            property -> ((ConstrainedObjectPropertyBase<?, ?>)property).validationHelper);
    }

    private final ObjectPropertyImpl<T> constrainedValue;
    private final ValidationHelper<T, E> validationHelper;
    private T value;
    private ObservableValue<? extends T> observable;
    private InvalidationListener listener;
    private boolean valid = true;
    private ExpressionHelper<T> expressionHelper;

    /**
     * The constructor of the {@code ConstrainedObjectPropertyBase}.
     */
    @SafeVarargs
    protected ConstrainedObjectPropertyBase(Constraint<? super T, E>... constraints) {
        this(null, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedObjectPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped object
     */
    @SafeVarargs
    protected ConstrainedObjectPropertyBase(T initialValue, Constraint<? super T, E>... constraints) {
        value = initialValue;

        constrainedValue = new ObjectPropertyImpl<>(initialValue) {
            @Override public String getName() { return "constrainedValue"; }
            @Override public Object getBean() { return ConstrainedObjectPropertyBase.this; }
        };

        validationHelper = new ValidationHelper<>(this, constrainedValue, constraints);
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return validationHelper.validProperty();
    }

    @Override
    public ReadOnlyBooleanProperty userValidProperty() {
        return validationHelper.userValidProperty();
    }

    @Override
    public ReadOnlyBooleanProperty invalidProperty() {
        return validationHelper.invalidProperty();
    }

    @Override
    public final ReadOnlyBooleanProperty userInvalidProperty() {
        return validationHelper.userInvalidProperty();
    }

    @Override
    public ReadOnlyBooleanProperty validatingProperty() {
        return validationHelper.validatingProperty();
    }

    @Override
    public ReadOnlyObjectProperty<T> constrainedValueProperty() {
        return constrainedValue;
    }

    @Override
    public ReadOnlyListProperty<E> errorsProperty() {
        return validationHelper.errorsProperty();
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
    public void addListener(ChangeListener<? super T> listener) {
        expressionHelper = ExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        expressionHelper = ExpressionHelper.removeListener(expressionHelper, listener);
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
    public T get() {
        valid = true;
        return observable == null ? value : observable.getValue();
    }

    @Override
    public void set(T newValue) {
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
    public void bind(final ObservableValue<? extends T> source) {
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

    private static class Listener<E> implements InvalidationListener, WeakListener {
        private final WeakReference<ConstrainedObjectPropertyBase<?, E>> wref;

        public Listener(ConstrainedObjectPropertyBase<?, E> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            ConstrainedObjectPropertyBase<?, E> ref = wref.get();
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
