/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

package javafx.beans.value;

import com.sun.javafx.binding.SetExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * An observable reference to an {@link javafx.collections.ObservableSet}.
 *
 * @see javafx.collections.ObservableSet
 * @see ObservableObjectValue
 * @see ObservableValue
 *
 * @param <E> the type of the {@code Set} elements
 * @since JavaFX 2.1
 */
public interface ObservableSetValue<E> extends ObservableObjectValue<ObservableSet<E>>, ObservableSet<E> {
    /**
     * Returns a new {@link ObservableSetValue} that wraps a {@link Set}.
     * <p>
     * The returned {@code ObservableSetValue} is a thin wrapper, as it simply delegates to
     * the wrapped set. Therefore, modifications of either set will be visible in the other
     * set as well.
     * <p>
     * If the wrapped set implements {@link ObservableSet}, modifications will also fire
     * change events on the returned {@code ObservableSetValue}.
     *
     * @param set the wrapped set, or {@code null} (empty set)
     * @return the new {@link ObservableSetValue}
     *
     * @since JFXcore 18
     */
    static <E> ObservableSetValue<E> observableSetValue(Set<E> set) {
        return new ObservableSetValue<>() {
            private SetExpressionHelper<E> helper;
            private final ObservableSet<E> value;
            private final SetChangeListener<E> setChangeListener;
            private final WeakSetChangeListener<E> weakSetChangeListener;

            {
                ObservableSet<E> value = null;

                if (set instanceof ObservableSet<E>) {
                    value = (ObservableSet<E>)set;
                } else if (set != null) {
                    value = FXCollections.observableSet(set);
                }

                if (value != null) {
                    setChangeListener = new SetChangeListener<>() {
                        @Override
                        public void onChanged(Change<? extends E> c) {
                            SetExpressionHelper.fireValueChangedEvent(helper, c);
                        }
                    };
                    weakSetChangeListener = new WeakSetChangeListener<>(setChangeListener);
                    value.addListener(weakSetChangeListener);
                } else {
                    value = FXCollections.emptyObservableSet();
                    setChangeListener = null;
                    weakSetChangeListener = null;
                }

                this.value = value;
            }

            @Override
            public int size() {
                return value.size();
            }

            @Override
            public boolean isEmpty() {
                return value.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return value.contains(o);
            }

            @Override
            public Iterator<E> iterator() {
                return value.iterator();
            }

            @Override
            public Object[] toArray() {
                return value.toArray();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return value.toArray(a);
            }

            @Override
            public boolean add(E e) {
                return value.add(e);
            }

            @Override
            public boolean remove(Object o) {
                return value.remove(o);
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return value.containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                return value.addAll(c);
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return value.removeAll(c);
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return value.retainAll(c);
            }

            @Override
            public void clear() {
                value.clear();
            }

            @Override
            public ObservableSet<E> get() {
                return value;
            }

            @Override
            public ObservableSet<E> getValue() {
                return value;
            }

            @Override
            public void addListener(InvalidationListener listener) {
                helper = SetExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                helper = SetExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(ChangeListener<? super ObservableSet<E>> listener) {
                helper = SetExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ChangeListener<? super ObservableSet<E>> listener) {
                helper = SetExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(SetChangeListener<? super E> listener) {
                helper = SetExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(SetChangeListener<? super E> listener) {
                helper = SetExpressionHelper.removeListener(helper, listener);
            }
        };
    }

    /**
     * Returns a new {@link ObservableSetValue} that wraps a {@link Set} contained in an {@link ObservableValue}.
     * <p>
     * The returned {@code ObservableSetValue} is a thin wrapper, as it simply delegates to
     * the wrapped set. Therefore, modifications of either set will be visible in the other
     * set as well.
     * <p>
     * If the wrapped set implements {@link ObservableSet}, modifications will also fire
     * change events on the returned {@code ObservableSetValue}.
     *
     * @param value the {@link ObservableValue}
     * @return the new {@link ObservableSetValue}
     *
     * @since JFXcore 18
     */
    static <E> ObservableSetValue<E> observableSetValue(ObservableValue<? extends Set<E>> value) {
        return new ObservableSetValue<>() {
            SetExpressionHelper<E> helper;
            ObservableSet<E> set;
            boolean valid;

            final InvalidationListener invalidationListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    valid = false;
                    SetExpressionHelper.fireValueChangedEvent(helper);
                }
            };

            final SetChangeListener<E> setChangeListener = new SetChangeListener<E>() {
                @Override
                public void onChanged(Change<? extends E> change) {
                    SetExpressionHelper.fireValueChangedEvent(helper, change);
                }
            };

            final WeakSetChangeListener<E> weakSetChangeListener = new WeakSetChangeListener<>(setChangeListener);

            {
                value.addListener(new WeakInvalidationListener(invalidationListener));
            }

            @Override
            public int size() {
                final ObservableSet<E> set = get();
                return set != null ? set.size() : 0;
            }

            @Override
            public boolean isEmpty() {
                final ObservableSet<E> set = get();
                return set == null || set.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                final ObservableSet<E> set = get();
                return set != null && set.contains(o);
            }

            @Override
            public Iterator<E> iterator() {
                final ObservableSet<E> set = get();
                return set != null ? set.iterator() : Collections.emptyIterator();
            }

            @Override
            public Object[] toArray() {
                final ObservableSet<E> set = get();
                return set != null ? set.toArray() : new Object[0];
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T[] toArray(T[] a) {
                final ObservableSet<E> set = get();
                return set != null ? set.<T>toArray(a) : (T[])new Object[0];
            }

            @Override
            public boolean add(E e) {
                final ObservableSet<E> set = get();
                return set != null ? set.add(e) : Collections.<E>emptyList().add(e);
            }

            @Override
            public boolean remove(Object o) {
                final ObservableSet<E> set = get();
                return set != null ? set.remove(o) : Collections.<E>emptyList().remove(o);
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                final ObservableSet<E> set = get();
                return set != null ? set.containsAll(c) : Collections.<E>emptyList().containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                final ObservableSet<E> set = get();
                return set != null ? set.addAll(c) : Collections.<E>emptyList().addAll(c);
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                final ObservableSet<E> set = get();
                return set != null ? set.removeAll(c) : Collections.<E>emptyList().removeAll(c);
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                final ObservableSet<E> set = get();
                return set != null ? set.retainAll(c) : Collections.<E>emptyList().retainAll(c);
            }

            @Override
            public void clear() {
                final ObservableSet<E> set = get();
                if (set != null) {
                    set.clear();
                } else {
                    Collections.<E>emptyList().clear();
                }
            }

            @Override
            public ObservableSet<E> get() {
                if (valid) {
                    return this.set;
                }

                valid = true;
                Set<E> set = value.getValue();

                if (this.set != null) {
                    this.set.removeListener(weakSetChangeListener);
                }

                if (set instanceof ObservableSet) {
                    this.set = (ObservableSet<E>)set;
                    this.set.addListener(weakSetChangeListener);
                } else if (set != null) {
                    this.set = FXCollections.observableSet(set);
                    this.set.addListener(weakSetChangeListener);
                } else {
                    this.set = null;
                }

                return this.set;
            }

            @Override
            public ObservableSet<E> getValue() {
                return get();
            }

            @Override
            public void addListener(InvalidationListener listener) {
                helper = SetExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                helper = SetExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(ChangeListener<? super ObservableSet<E>> listener) {
                helper = SetExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ChangeListener<? super ObservableSet<E>> listener) {
                helper = SetExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(SetChangeListener<? super E> listener) {
                helper = SetExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(SetChangeListener<? super E> listener) {
                helper = SetExpressionHelper.removeListener(helper, listener);
            }
        };
    }
}
