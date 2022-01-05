/*
 * Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.javafx.binding.SetExpressionHelper;
import org.jfxcore.beans.property.validation.PropertyHelper;
import org.jfxcore.beans.property.validation.ValidationHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.jfxcore.beans.property.validation.WritableProperty;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides a base implementation for a constrained property that wraps an {@link ObservableSet}.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <E> element type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
public abstract class ConstrainedSetPropertyBase<E, D> extends ConstrainedSetProperty<E, D> {

    static {
        ValidationHelper.setAccessor(
            ConstrainedSetPropertyBase.class,
            property -> ((ConstrainedSetPropertyBase<?, ?>)property).validationHelper);
    }

    private final ConstrainedValuePropertyImpl constrainedValue;
    private final ValidationHelper<ObservableSet<E>, D> validationHelper;
    private final SetChangeListener<E> setChangeListener = change -> {
        invalidateProperties();
        invalidated();
        fireValueChangedEvent(change);
    };

    private ObservableSet<E> value;
    private ObservableValue<? extends ObservableSet<E>> observable;
    private InvalidationListener listener;
    private boolean valid = true;
    private SetExpressionHelper<E> expressionHelper;

    private SizeProperty size0;
    private EmptyProperty empty0;

    /**
     * The constructor of the {@code ConstrainedSetPropertyBase}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedSetPropertyBase(Constraint<? super ObservableSet<E>, D>... constraints) {
        this(null, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedSetPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedSetPropertyBase(
            ObservableSet<E> initialValue, Constraint<? super ObservableSet<E>, D>... constraints) {
        value = initialValue;
        constrainedValue = new ConstrainedValuePropertyImpl(initialValue != null ? initialValue : Collections.emptySet());
        validationHelper = new ValidationHelper<>(this, constrainedValue, constraints);

        if (initialValue != null) {
            initialValue.addListener(setChangeListener);
        }
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
    public final ReadOnlySetProperty<E> constrainedValueProperty() {
        return constrainedValue;
    }

    @Override
    public final ReadOnlyListProperty<D> errorsProperty() {
        return validationHelper.errorsProperty();
    }

    @Override
    public final ReadOnlyListProperty<D> warningsProperty() {
        return validationHelper.warningsProperty();
    }

    @Override
    public final ReadOnlyIntegerProperty sizeProperty() {
        if (size0 == null) {
            size0 = new SizeProperty(this);
        }
        return size0;
    }

    @Override
    public final ReadOnlyBooleanProperty emptyProperty() {
        if (empty0 == null) {
            empty0 = new EmptyProperty(this);
        }
        return empty0;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        expressionHelper = SetExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        expressionHelper = SetExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableSet<E>> listener) {
        expressionHelper = SetExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableSet<E>> listener) {
        expressionHelper = SetExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener) {
        expressionHelper = SetExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(SetChangeListener<? super E> listener) {
        expressionHelper = SetExpressionHelper.removeListener(expressionHelper, listener);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners},
     * {@link ChangeListener ChangeListeners}, and
     * {@link SetChangeListener}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set(ObservableSet)} or in case of a bound property, if the
     * binding becomes invalid.
     */
    protected void fireValueChangedEvent() {
        SetExpressionHelper.fireValueChangedEvent(expressionHelper);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners},
     * {@link ChangeListener ChangeListeners}, and
     * {@link SetChangeListener}.
     *
     * This method is called when the content of the list changes.
     *
     * @param change the change that needs to be propagated
     */
    protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
        SetExpressionHelper.fireValueChangedEvent(expressionHelper, change);
    }

    private void invalidateProperties() {
        if (size0 != null) {
            size0.fireValueChangedEvent();
        }
        if (empty0 != null) {
            empty0.fireValueChangedEvent();
        }
    }

    private void markInvalid(ObservableSet<E> oldValue) {
        if (valid) {
            if (oldValue != null) {
                oldValue.removeListener(setChangeListener);
            }
            valid = false;
            invalidateProperties();
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
    public ObservableSet<E> get() {
        if (!valid) {
            value = observable == null ? value : observable.getValue();
            valid = true;
            if (value != null) {
                value.addListener(setChangeListener);
            }
        }
        return value;
    }

    @Override
    public void set(ObservableSet<E> newValue) {
        if (isBound()) {
            throw PropertyHelper.cannotSetBoundProperty(this);
        }

        if (value != newValue) {
            final ObservableSet<E> oldValue = value;
            value = newValue;
            markInvalid(oldValue);
        }
    }

    @Override
    public boolean isBound() {
        return observable != null;
    }

    @Override
    public void bind(final ObservableValue<? extends ObservableSet<E>> source) {
        if (source == null) {
            throw PropertyHelper.cannotBindNull(this);
        }

        if (source != this.observable) {
            unbind();
            observable = source;
            if (listener == null) {
                listener = new Listener<>(this);
            }
            observable.addListener(listener);
            markInvalid(value);
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

    private static class SizeProperty extends ReadOnlyIntegerPropertyBase {
        private final Set<?> set;

        SizeProperty(Set<?> set) {
            this.set = set;
        }

        @Override
        public int get() {
            return set.size();
        }

        @Override
        public Object getBean() {
            return set;
        }

        @Override
        public String getName() {
            return "size";
        }

        @Override
        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private static class EmptyProperty extends ReadOnlyBooleanPropertyBase {
        private final Set<?> set;

        EmptyProperty(Set<?> set) {
            this.set = set;
        }

        @Override
        public boolean get() {
            return set.isEmpty();
        }

        @Override
        public Object getBean() {
            return set;
        }

        @Override
        public String getName() {
            return "empty";
        }

        @Override
        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private static class Listener<T, D> implements InvalidationListener, WeakListener {
        private final WeakReference<ConstrainedSetPropertyBase<T, D>> wref;

        public Listener(ConstrainedSetPropertyBase<T, D> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            ConstrainedSetPropertyBase<T, D> ref = wref.get();
            if (ref == null) {
                observable.removeListener(this);
            } else {
                ref.markInvalid(ref.value);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return wref.get() == null;
        }
    }

    private class ConstrainedValuePropertyImpl
            extends ReadOnlySetPropertyBase<E> implements WritableProperty<ObservableSet<E>> {
        private final ObservableSet<E> set;
        private final ObservableSet<E> unmodifiableSet;
        private SizeProperty size0;
        private EmptyProperty empty0;

        ConstrainedValuePropertyImpl(Set<E> initialValues) {
            set = FXCollections.observableSet(new HashSet<>(initialValues));
            set.addListener((SetChangeListener<E>) change -> {
                invalidateProperties();
                fireValueChangedEvent(change);
            });

            unmodifiableSet = FXCollections.unmodifiableObservableSet(set);
        }

        @Override
        public Object getBean() {
            return ConstrainedSetPropertyBase.this;
        }

        @Override
        public String getName() {
            return "constrainedValue";
        }

        @Override
        public ObservableSet<E> get() {
            return unmodifiableSet;
        }

        @Override
        public boolean setValue(ObservableSet<E> newList) {
            set.removeIf(item -> !newList.contains(item));
            set.addAll(newList);
            return false;
        }

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            if (size0 == null) {
                size0 = new SizeProperty(this);
            }
            return size0;
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            if (empty0 == null) {
                empty0 = new EmptyProperty(this);
            }
            return empty0;
        }

        private void invalidateProperties() {
            if (size0 != null) {
                size0.fireValueChangedEvent();
            }
            if (empty0 != null) {
                empty0.fireValueChangedEvent();
            }
        }
    }
    
}
