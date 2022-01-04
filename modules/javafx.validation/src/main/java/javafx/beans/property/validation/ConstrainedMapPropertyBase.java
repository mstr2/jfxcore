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

import com.sun.javafx.binding.MapExpressionHelper;
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
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.jfxcore.beans.property.validation.WritableProperty;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a base implementation for a constrained property that wraps an {@link ObservableMap}.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <K> key type
 * @param <V> value type
 * @param <E> error information type
 * @since JFXcore 18
 */
public abstract class ConstrainedMapPropertyBase<K, V, E> extends ConstrainedMapProperty<K, V, E> {

    static {
        ValidationHelper.setAccessor(
            ConstrainedMapPropertyBase.class,
            property -> ((ConstrainedMapPropertyBase<?, ?, ?>)property).validationHelper);
    }

    private final ConstrainedValuePropertyImpl constrainedValue;
    private final ValidationHelper<ObservableMap<K, V>, E> validationHelper;
    private final MapChangeListener<K, V> mapChangeListener = change -> {
        invalidateProperties();
        invalidated();
        fireValueChangedEvent(change);
    };

    private ObservableMap<K, V> value;
    private ObservableValue<? extends ObservableMap<K, V>> observable;
    private InvalidationListener listener;
    private boolean valid = true;
    private MapExpressionHelper<K, V> expressionHelper;

    private SizeProperty size0;
    private EmptyProperty empty0;

    /**
     * The constructor of the {@code ConstrainedMapPropertyBase}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedMapPropertyBase(Constraint<? super ObservableMap<K, V>, E>... constraints) {
        this(null, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedMapPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedMapPropertyBase(
            ObservableMap<K, V> initialValue, Constraint<? super ObservableMap<K, V>, E>... constraints) {
        value = initialValue;
        constrainedValue = new ConstrainedValuePropertyImpl(initialValue != null ? initialValue : Collections.emptyMap());
        validationHelper = new ValidationHelper<>(this, constrainedValue, constraints);

        if (initialValue != null) {
            initialValue.addListener(mapChangeListener);
        }
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
    public ReadOnlyMapProperty<K, V> constrainedValueProperty() {
        return constrainedValue;
    }

    @Override
    public ReadOnlyListProperty<E> errorsProperty() {
        return validationHelper.errorsProperty();
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

    @Override
    public void addListener(InvalidationListener listener) {
        expressionHelper = MapExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        expressionHelper = MapExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableMap<K, V>> listener) {
        expressionHelper = MapExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableMap<K, V>> listener) {
        expressionHelper = MapExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(MapChangeListener<? super K, ? super V> listener) {
        expressionHelper = MapExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(MapChangeListener<? super K, ? super V> listener) {
        expressionHelper = MapExpressionHelper.removeListener(expressionHelper, listener);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners},
     * {@link ChangeListener ChangeListeners}, and
     * {@link MapChangeListener}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set(ObservableMap)} or in case of a bound property, if the
     * binding becomes invalid.
     */
    protected void fireValueChangedEvent() {
        MapExpressionHelper.fireValueChangedEvent(expressionHelper);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners},
     * {@link ChangeListener ChangeListeners}, and
     * {@link MapChangeListener}.
     *
     * This method is called when the content of the list changes.
     *
     * @param change the change that needs to be propagated
     */
    protected void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> change) {
        MapExpressionHelper.fireValueChangedEvent(expressionHelper, change);
    }

    private void invalidateProperties() {
        if (size0 != null) {
            size0.fireValueChangedEvent();
        }
        if (empty0 != null) {
            empty0.fireValueChangedEvent();
        }
    }

    private void markInvalid(ObservableMap<K, V> oldValue) {
        if (valid) {
            if (oldValue != null) {
                oldValue.removeListener(mapChangeListener);
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
    public ObservableMap<K, V> get() {
        if (!valid) {
            value = observable == null ? value : observable.getValue();
            valid = true;
            if (value != null) {
                value.addListener(mapChangeListener);
            }
        }
        return value;
    }

    @Override
    public void set(ObservableMap<K, V> newValue) {
        if (isBound()) {
            throw PropertyHelper.cannotSetBoundProperty(this);
        }

        if (value != newValue) {
            final ObservableMap<K, V> oldValue = value;
            value = newValue;
            markInvalid(oldValue);
        }
    }

    @Override
    public boolean isBound() {
        return observable != null;
    }

    @Override
    public void bind(final ObservableValue<? extends ObservableMap<K, V>> source) {
        if (source == null) {
            throw PropertyHelper.cannotBindNull(this);
        }

        if (source != observable) {
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
        private final Map<?, ?> map;

        SizeProperty(Map<?, ?> map) {
            this.map = map;
        }

        @Override
        public int get() {
            return map.size();
        }

        @Override
        public Object getBean() {
            return map;
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
        private final Map<?, ?> map;

        EmptyProperty(Map<?, ?> map) {
            this.map = map;
        }

        @Override
        public boolean get() {
            return map.isEmpty();
        }

        @Override
        public Object getBean() {
            return map;
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

    private static class Listener<K, V, E> implements InvalidationListener, WeakListener {
        private final WeakReference<ConstrainedMapPropertyBase<K, V, E>> wref;

        public Listener(ConstrainedMapPropertyBase<K, V, E> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            ConstrainedMapPropertyBase<K, V, E> ref = wref.get();
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
            extends ReadOnlyMapPropertyBase<K, V> implements WritableProperty<ObservableMap<K, V>> {
        private final ObservableMap<K, V> set;
        private final ObservableMap<K, V> unmodifiableSet;
        private SizeProperty size0;
        private EmptyProperty empty0;

        ConstrainedValuePropertyImpl(Map<K, V> initialValues) {
            set = FXCollections.observableMap(new HashMap<>(initialValues));
            set.addListener((MapChangeListener<K, V>)change -> {
                invalidateProperties();
                fireValueChangedEvent(change);
            });

            unmodifiableSet = FXCollections.unmodifiableObservableMap(set);
        }

        @Override
        public Object getBean() {
            return ConstrainedMapPropertyBase.this;
        }

        @Override
        public String getName() {
            return "constrainedValue";
        }

        @Override
        public ObservableMap<K, V> get() {
            return unmodifiableSet;
        }

        @Override
        public boolean setValue(ObservableMap<K, V> newList) {
            set.keySet().removeIf(item -> !newList.containsKey(item));
            set.putAll(newList);
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
