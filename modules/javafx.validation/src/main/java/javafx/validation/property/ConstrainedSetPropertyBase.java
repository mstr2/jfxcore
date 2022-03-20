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
import org.jfxcore.validation.CollectionEmptyProperty;
import org.jfxcore.validation.CollectionSizeProperty;
import org.jfxcore.validation.DeferredSetProperty;
import org.jfxcore.validation.MapEmptyProperty;
import org.jfxcore.validation.MapSizeProperty;
import org.jfxcore.validation.PropertyHelper;
import org.jfxcore.validation.SetChange;
import org.jfxcore.validation.SetValidationHelper;
import org.jfxcore.validation.ValidationHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapPropertyBase;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.util.Incubating;
import javafx.validation.ConstrainedElement;
import javafx.validation.ConstraintBase;
import javafx.validation.DiagnosticList;
import javafx.validation.ValidationListener;
import javafx.validation.ValidationState;
import java.lang.ref.WeakReference;

/**
 * Provides a base implementation for a constrained property that wraps an {@link ObservableSet}.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <E> element type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public abstract class ConstrainedSetPropertyBase<E, D> extends ConstrainedSetProperty<E, D> {

    static {
        PropertyHelper.setSetAccessor(
            new PropertyHelper.Accessor<ObservableSet<Object>>() {
                @Override
                public <D1> ValidationHelper<ObservableSet<Object>, D1> getValidationHelper(
                        ReadOnlyConstrainedProperty<ObservableSet<Object>, D1> property) {
                    return ((ConstrainedSetPropertyBase<Object, D1>)property).validationHelper;
                }

                @Override
                public <D1> ObservableSet<Object> readValue(
                        ReadOnlyConstrainedProperty<ObservableSet<Object>, D1> property) {
                    return ((ConstrainedSetPropertyBase<Object, Object>)property).readValue();
                }
            }
        );
    }

    private boolean valid = true;
    private ObservableSet<E> value;
    private ObservableValue<? extends ObservableSet<E>> observable;
    private InvalidationListener listener;
    private SetValidationHelper<E, D> validationHelper;
    private SetExpressionHelper<E> expressionHelper;
    private CollectionSizeProperty size0;
    private CollectionEmptyProperty empty0;
    private ReadOnlyMapProperty<E, ConstrainedElement<E, D>> constrainedElements;

    private final DeferredSetProperty<E> constrainedValue;
    private final SetChangeListener<E> setChangeListener = change -> {
        validationHelper.invalidated(change);
        invalidateProperties();
        invalidated();
        fireValueChangedEvent(change);
    };

    /**
     * The constructor of the {@code ConstrainedSetPropertyBase}.
     *
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedSetPropertyBase(ConstraintBase<? super E, D>... constraints) {
        this(null, ValidationState.UNKNOWN, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedSetPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedSetPropertyBase(
            ObservableSet<E> initialValue, ConstraintBase<? super E, D>... constraints) {
        this(initialValue, ValidationState.UNKNOWN, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedSetPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param initialValidationState the initial validation state
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedSetPropertyBase(
            ObservableSet<E> initialValue,
            ValidationState initialValidationState,
            ConstraintBase<? super E, D>... constraints) {
        if (initialValue != null) {
            initialValue.addListener(setChangeListener);
        }

        value = initialValue;

        constrainedValue = new DeferredSetProperty<>(initialValue) {
            @Override
            public Object getBean() {
                return ConstrainedSetPropertyBase.this;
            }

            @Override
            public String getName() {
                return "constrainedValue";
            }

            @Override
            protected SetChange<E> getSetChange() {
                return validationHelper.completeSetChange();
            }
        };

        validationHelper = new SetValidationHelper<>(this, constrainedValue, initialValidationState, constraints);

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
    public final ReadOnlySetProperty<E> constrainedValueProperty() {
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
    public ReadOnlyMapProperty<E, ConstrainedElement<E, D>> constrainedElementsProperty() {
        if (constrainedElements == null) {
            constrainedElements = new ConstrainedElementsProperty();
        }
        return constrainedElements;
    }

    @Override
    public final ReadOnlyIntegerProperty sizeProperty() {
        if (size0 == null) {
            size0 = new CollectionSizeProperty(this);
        }
        return size0;
    }

    @Override
    public final ReadOnlyBooleanProperty emptyProperty() {
        if (empty0 == null) {
            empty0 = new CollectionEmptyProperty(this);
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

    @Override
    public void addListener(ValidationListener<? super ObservableSet<E>, D> listener) {
        validationHelper.addListener(listener);
    }

    @Override
    public void removeListener(ValidationListener<? super ObservableSet<E>, D> listener) {
        validationHelper.removeListener(listener);
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
            validationHelper.invalidated(this);
            invalidateProperties();
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

    private ObservableSet<E> readValue() {
        return observable == null ? value : observable.getValue();
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

    private class ConstrainedElementsProperty extends ReadOnlyMapPropertyBase<E, ConstrainedElement<E, D>> {
        private final ObservableMap<E, ConstrainedElement<E, D>> elements;
        private MapSizeProperty size0;
        private MapEmptyProperty empty0;

        ConstrainedElementsProperty() {
            ObservableMap<E, ConstrainedElement<E, D>> elements = validationHelper.getElements();
            elements.addListener((MapChangeListener<? super E, ? super ConstrainedElement<E, D>>) change -> {
                if (size0 != null) {
                    size0.fireValueChangedEvent();
                }

                if (empty0 != null) {
                    empty0.fireValueChangedEvent();
                }

                fireValueChangedEvent(change);
            });

            this.elements = FXCollections.unmodifiableObservableMap(elements);
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            if (size0 == null) {
                size0 = new MapSizeProperty(this);
            }

            return size0;
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            if (empty0 == null) {
                empty0 = new MapEmptyProperty(this);
            }

            return empty0;
        }

        @Override
        public Object getBean() {
            return ConstrainedSetPropertyBase.this;
        }

        @Override
        public String getName() {
            return "constrainedElements";
        }

        @Override
        public ObservableMap<E, ConstrainedElement<E, D>> get() {
            return elements;
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

}
