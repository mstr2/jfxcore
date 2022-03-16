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

import com.sun.javafx.binding.ListExpressionHelper;
import org.jfxcore.validation.CollectionEmptyProperty;
import org.jfxcore.validation.CollectionSizeProperty;
import org.jfxcore.validation.DeferredListProperty;
import org.jfxcore.validation.ListChange;
import org.jfxcore.validation.ListValidationHelper;
import org.jfxcore.validation.PropertyHelper;
import org.jfxcore.validation.ValidationHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Incubating;
import javafx.validation.ConstrainedElement;
import javafx.validation.Constraint;
import javafx.validation.DiagnosticList;
import javafx.validation.ValidationListener;
import javafx.validation.ValidationState;
import java.lang.ref.WeakReference;

/**
 * Provides a base implementation for a constrained property that wraps an {@link ObservableList}.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <E> element type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public abstract class ConstrainedListPropertyBase<E, D> extends ConstrainedListProperty<E, D> {

    static {
        PropertyHelper.setListAccessor(
            new PropertyHelper.Accessor<ObservableList<Object>>() {
                @Override
                public <D1> ValidationHelper<ObservableList<Object>, D1> getValidationHelper(
                        ReadOnlyConstrainedProperty<ObservableList<Object>, D1> property) {
                    return ((ConstrainedListPropertyBase<Object, D1>)property).validationHelper;
                }

                @Override
                public <D1> ObservableList<Object> readValue(
                        ReadOnlyConstrainedProperty<ObservableList<Object>, D1> property) {
                    return ((ConstrainedListPropertyBase<Object, D1>)property).readValue();
                }
            }
        );
    }

    private boolean valid = true;
    private ObservableList<E> value;
    private ObservableValue<? extends ObservableList<E>> observable;
    private InvalidationListener listener;
    private ListValidationHelper<E, D> validationHelper;
    private ListExpressionHelper<E> expressionHelper;
    private CollectionSizeProperty size0;
    private CollectionEmptyProperty empty0;
    private ReadOnlyListProperty<ConstrainedElement<E, D>> constrainedElements;

    private final DeferredListProperty<E> constrainedValue;
    private final ListChangeListener<E> listChangeListener = change -> {
        validationHelper.invalidated(change);
        change.reset();
        invalidateProperties();
        invalidated();
        fireValueChangedEvent(change);
    };

    /**
     * The Constructor of {@code ConstrainedListPropertyBase}
     * 
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedListPropertyBase(Constraint<? super E, D>... constraints) {
        this(null, ValidationState.UNKNOWN, constraints);
    }

    /**
     * The Constructor of {@code ConstrainedListPropertyBase}
     *
     * @param initialValue the initial value of the wrapped value
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedListPropertyBase(ObservableList<E> initialValue, Constraint<? super E, D>... constraints) {
        this(initialValue, ValidationState.UNKNOWN, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedListPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param initialValidationState the initial validation state
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedListPropertyBase(
            ObservableList<E> initialValue,
            ValidationState initialValidationState,
            Constraint<? super E, D>... constraints) {
        if (initialValue != null) {
            initialValue.addListener(listChangeListener);
        }

        value = initialValue;

        constrainedValue = new DeferredListProperty<>(initialValue) {
            @Override
            public Object getBean() {
                return ConstrainedListPropertyBase.this;
            }

            @Override
            public String getName() {
                return "constrainedValue";
            }

            @Override
            protected ListChange.ReplacedRange<E> getListChange() {
                return validationHelper.completeListChange();
            }
        };

        validationHelper = new ListValidationHelper<>(this, constrainedValue, initialValidationState, constraints);

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
    public final ReadOnlyListProperty<E> constrainedValueProperty() {
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
    public ReadOnlyListProperty<ConstrainedElement<E, D>> constrainedElementsProperty() {
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
        expressionHelper = ListExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        expressionHelper = ListExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableList<E>> listener) {
        expressionHelper = ListExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableList<E>> listener) {
        expressionHelper = ListExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(ListChangeListener<? super E> listener) {
        expressionHelper = ListExpressionHelper.addListener(expressionHelper, this, listener);
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listener) {
        expressionHelper = ListExpressionHelper.removeListener(expressionHelper, listener);
    }

    @Override
    public void addListener(ValidationListener<? super ObservableList<E>, D> listener) {
        validationHelper.addListener(listener);
    }

    @Override
    public void removeListener(ValidationListener<? super ObservableList<E>, D> listener) {
        validationHelper.removeListener(listener);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners},
     * {@link ChangeListener ChangeListeners}, and
     * {@link ListChangeListener}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set(ObservableList)} or in case of a bound property, if the
     * binding becomes invalid.
     */
    protected void fireValueChangedEvent() {
        ListExpressionHelper.fireValueChangedEvent(expressionHelper);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners},
     * {@link ChangeListener ChangeListeners}, and
     * {@link ListChangeListener}.
     *
     * This method is called when the content of the list changes.
     *
     * @param change the change that needs to be propagated
     */
    protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
        ListExpressionHelper.fireValueChangedEvent(expressionHelper, change);
    }

    private void invalidateProperties() {
        if (size0 != null) {
            size0.fireValueChangedEvent();
        }
        if (empty0 != null) {
            empty0.fireValueChangedEvent();
        }
    }

    private void markInvalid(ObservableList<E> oldValue) {
        if (valid) {
            if (oldValue != null) {
                oldValue.removeListener(listChangeListener);
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

    private ObservableList<E> readValue() {
        return observable == null ? value : observable.getValue();
    }

    @Override
    public ObservableList<E> get() {
        if (!valid) {
            value = observable == null ? value : observable.getValue();
            valid = true;
            if (value != null) {
                value.addListener(listChangeListener);
            }
        }
        return value;
    }

    @Override
    public void set(ObservableList<E> newValue) {
        if (isBound()) {
            throw PropertyHelper.cannotSetBoundProperty(this);
        }

        if (value != newValue) {
            final ObservableList<E> oldValue = value;
            value = newValue;
            markInvalid(oldValue);
        }
    }

    @Override
    public boolean isBound() {
        return observable != null;
    }

    @Override
    public void bind(final ObservableValue<? extends ObservableList<E>> source) {
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

    @SuppressWarnings("FieldCanBeLocal")
    private class ConstrainedElementsProperty extends ReadOnlyListPropertyBase<ConstrainedElement<E, D>> {
        private final ObservableList<ConstrainedElement<E, D>> elements;
        private CollectionSizeProperty size0;
        private CollectionEmptyProperty empty0;

        ConstrainedElementsProperty() {
            ObservableList<ConstrainedElement<E, D>> elements = validationHelper.getElements();
            elements.addListener((ListChangeListener<? super ConstrainedElement<E,D>>) change -> {
                if (size0 != null) {
                    size0.fireValueChangedEvent();
                }

                if (empty0 != null) {
                    empty0.fireValueChangedEvent();
                }

                fireValueChangedEvent(change);
            });

            this.elements = FXCollections.unmodifiableObservableList(elements);
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            if (size0 == null) {
                size0 = new CollectionSizeProperty(this);
            }

            return size0;
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            if (empty0 == null) {
                empty0 = new CollectionEmptyProperty(this);
            }

            return empty0;
        }

        @Override
        public Object getBean() {
            return ConstrainedListPropertyBase.this;
        }

        @Override
        public String getName() {
            return "constrainedElements";
        }

        @Override
        public ObservableList<ConstrainedElement<E, D>> get() {
            return elements;
        }
    }

    private static class Listener<T, D> implements InvalidationListener, WeakListener {
        private final WeakReference<ConstrainedListPropertyBase<T, D>> wref;

        public Listener(ConstrainedListPropertyBase<T, D> ref) {
            this.wref = new WeakReference<>(ref);
        }

        @Override
        public void invalidated(Observable observable) {
            ConstrainedListPropertyBase<T, D> ref = wref.get();
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
