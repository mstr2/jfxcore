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

import com.sun.javafx.binding.ListExpressionHelper;
import com.sun.javafx.collections.ObservableListWrapper;
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
import javafx.beans.property.ReadOnlyListPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.jfxcore.beans.property.validation.WritableProperty;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Provides a base implementation for a constrained property that wraps an {@link ObservableList}.
 * {@link Property#getBean()} and {@link Property#getName()} must be implemented by derived classes.
 *
 * @param <E> element type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
public abstract class ConstrainedListPropertyBase<E, D> extends ConstrainedListProperty<E, D> {

    static {
        ValidationHelper.setAccessor(
            ConstrainedListPropertyBase.class,
            property -> ((ConstrainedListPropertyBase<?, ?>)property).validationHelper);
    }

    private final ConstrainedValuePropertyImpl constrainedValue;
    private final ValidationHelper<ObservableList<E>, D> validationHelper;
    private final ItemCountMap<E> listItemCount = new ItemCountMap<>();
    private final ListChangeListener<E> listChangeListener = change -> {
        while (change.next()) {
            if (change.wasRemoved()) {
                for (E value : change.getRemoved()) {
                    listItemCount.decrement(value);
                }
            }

            if (change.wasAdded()) {
                for (E value : change.getAddedSubList()) {
                    listItemCount.increment(value);
                }
            }
        }

        change.reset();
        invalidateProperties();
        invalidated();
        fireValueChangedEvent(change);
    };

    private ObservableList<E> value;
    private ObservableValue<? extends ObservableList<E>> observable;
    private InvalidationListener listener;
    private boolean valid = true;
    private ListExpressionHelper<E> expressionHelper;

    private SizeProperty size0;
    private EmptyProperty empty0;

    /**
     * The Constructor of {@code ConstrainedListPropertyBase}
     * 
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedListPropertyBase(Constraint<? super ObservableList<E>, D>... constraints) {
        this(null, constraints);
    }

    /**
     * The constructor of the {@code ConstrainedListPropertyBase}.
     *
     * @param initialValue the initial value of the wrapped value
     * @param constraints the value constraints
     */
    @SafeVarargs
    protected ConstrainedListPropertyBase(
            ObservableList<E> initialValue, Constraint<? super ObservableList<E>, D>... constraints) {
        if (initialValue != null) {
            for (E item : initialValue) {
                listItemCount.increment(item);
            }

            initialValue.addListener(listChangeListener);
        }

        value = initialValue;
        constrainedValue = new ConstrainedValuePropertyImpl(initialValue != null ? initialValue : Collections.emptyList());
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
    public final ReadOnlyListProperty<E> constrainedValueProperty() {
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

    /**
     * Maintains a map of item counts, which is used for a constant-time lookup whether an item
     * is contained in a list. The purpose of this structure is to speed up the list difference
     * algorithm that is implemented by ConstrainedValuePropertyImpl#setValue(ObservableList).
     */
    private static class ItemCountMap<T> extends HashMap<T, Integer> {
        public void increment(T value) {
            Integer count = get(value);
            put(value, count != null ? count + 1 : 1);
        }

        public void decrement(T value) {
            Integer count = get(value);
            if (count != null) {
                if (--count == 0) {
                    remove(value);
                } else {
                    put(value, count);
                }
            }
        }
    }

    private static class SizeProperty extends ReadOnlyIntegerPropertyBase {
        private final List<?> bean;

        SizeProperty(List<?> bean) {
            this.bean = bean;
        }

        @Override
        public int get() {
            return bean.size();
        }

        @Override
        public Object getBean() {
            return bean;
        }

        @Override
        public String getName() {
            return "size";
        }

        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private static class EmptyProperty extends ReadOnlyBooleanPropertyBase {
        private final List<?> bean;

        EmptyProperty(List<?> bean) {
            this.bean = bean;
        }

        @Override
        public boolean get() {
            return bean.isEmpty();
        }

        @Override
        public Object getBean() {
            return bean;
        }

        @Override
        public String getName() {
            return "empty";
        }

        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
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

    private class ConstrainedValuePropertyImpl
            extends ReadOnlyListPropertyBase<E> implements WritableProperty<ObservableList<E>> {
        private final ObservableModifiableListImpl<E> list;
        private final ObservableList<E> unmodifiableList;
        private SizeProperty size0;
        private EmptyProperty empty0;

        ConstrainedValuePropertyImpl(List<E> initialValues) {
            list = new ObservableModifiableListImpl<>(initialValues);
            list.addListener((ListChangeListener<E>) change -> {
                invalidateProperties();
                fireValueChangedEvent(change);
            });

            unmodifiableList = FXCollections.unmodifiableObservableList(list);
        }

        @Override
        public Object getBean() {
            return ConstrainedListPropertyBase.this;
        }

        @Override
        public String getName() {
            return "constrainedValue";
        }

        @Override
        public ObservableList<E> get() {
            return unmodifiableList;
        }

        @Override
        public boolean setValue(ObservableList<E> newList) {
            for (int i = 0; i < list.size();) {
                boolean removed = !listItemCount.containsKey(list.get(i));

                if (removed) {
                    list.remove(i);
                } else {
                    i += 1;
                }
            }

            for (int i = 0; i < newList.size(); ++i) {
                boolean added = !list.contains(newList.get(i));

                if (added) {
                    list.add(i, newList.get(i));
                }
            }

            list.finalizeChange();
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

    private static final class ObservableModifiableListImpl<D> extends ObservableListWrapper<D> {
        private final ItemCountMap<D> itemCount = new ItemCountMap<>();
        private boolean changing;

        ObservableModifiableListImpl(List<D> list) {
            super(new ArrayList<>(list));

            for (D item : list) {
                itemCount.increment(item);
            }
        }

        @Override
        public void add(int index, D element) {
            if (!changing) {
                changing = true;
                beginChange();
            }

            itemCount.increment(element);
            doAdd(index, element);
            nextAdd(index, index + 1);
            ++modCount;
        }

        @Override
        public D remove(int index) {
            if (!changing) {
                changing = true;
                beginChange();
            }

            D old = doRemove(index);
            nextRemove(index, old);
            ++modCount;
            itemCount.decrement(old);
            return old;
        }

        @Override
        @SuppressWarnings("SuspiciousMethodCalls")
        public boolean contains(Object o) {
            return itemCount.containsKey(o);
        }

        public void finalizeChange() {
            if (changing) {
                changing = false;
                endChange();
            }
        }
    }

}
