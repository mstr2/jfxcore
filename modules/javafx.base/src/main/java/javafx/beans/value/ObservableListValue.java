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

import com.sun.javafx.binding.ListExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An observable reference to an {@link javafx.collections.ObservableList}.
 *
 * @see javafx.collections.ObservableList
 * @see ObservableObjectValue
 * @see ObservableValue
 *
 * @param <E> the type of the {@code List} elements
 * @since JavaFX 2.1
 */
public interface ObservableListValue<E> extends ObservableObjectValue<ObservableList<E>>, ObservableList<E> {
    /**
     * Returns a new {@link ObservableListValue} that wraps a {@link List}.
     * <p>
     * The returned {@code ObservableListValue} is a thin wrapper, as it simply delegates to
     * the wrapped list. Therefore, modifications of either list will be visible in the other
     * list as well.
     * <p>
     * If the wrapped list implements {@link ObservableList}, modifications will also fire
     * change events on the returned {@code ObservableListValue}.
     *
     * @param list the wrapped list, or {@code null} (empty list)
     * @return the new {@link ObservableListValue}
     *
     * @since JFXcore 18
     */
    static <E> ObservableListValue<E> observableListValue(List<E> list) {
        return new ObservableListValue<>() {
            private ListExpressionHelper<E> helper;
            private final ObservableList<E> value;
            private final ListChangeListener<E> listChangeListener;
            private final WeakListChangeListener<E> weakListChangeListener;

            {
                ObservableList<E> value = null;

                if (list instanceof ObservableList<E>) {
                    value = (ObservableList<E>)list;
                } else if (list != null) {
                    value = FXCollections.observableList(list);
                }

                if (value != null) {
                    listChangeListener = new ListChangeListener<>() {
                        @Override
                        public void onChanged(Change<? extends E> c) {
                            ListExpressionHelper.fireValueChangedEvent(helper, c);
                        }
                    };
                    weakListChangeListener = new WeakListChangeListener<>(listChangeListener);
                    value.addListener(weakListChangeListener);
                } else {
                    value = FXCollections.emptyObservableList();
                    listChangeListener = null;
                    weakListChangeListener = null;
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
            public boolean addAll(int index, Collection<? extends E> c) {
                return value.addAll(index, c);
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
            public E get(int index) {
                return value.get(index);
            }

            @Override
            public E set(int index, E element) {
                return value.set(index, element);
            }

            @Override
            public void add(int index, E element) {
                value.add(index, element);
            }

            @Override
            public E remove(int index) {
                return value.remove(index);
            }

            @Override
            public int indexOf(Object o) {
                return value.indexOf(o);
            }

            @Override
            public int lastIndexOf(Object o) {
                return value.indexOf(o);
            }

            @Override
            public ListIterator<E> listIterator() {
                return value.listIterator();
            }

            @Override
            public ListIterator<E> listIterator(int index) {
                return value.listIterator(index);
            }

            @Override
            public List<E> subList(int fromIndex, int toIndex) {
                return value.subList(fromIndex, toIndex);
            }

            @Override
            public ObservableList<E> get() {
                return value;
            }

            @Override
            public ObservableList<E> getValue() {
                return value;
            }

            @Override
            public void addListener(InvalidationListener listener) {
                helper = ListExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                helper = ListExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(ChangeListener<? super ObservableList<E>> listener) {
                helper = ListExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ChangeListener<? super ObservableList<E>> listener) {
                helper = ListExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(ListChangeListener<? super E> listener) {
                helper = ListExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ListChangeListener<? super E> listener) {
                helper = ListExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public boolean addAll(E... elements) {
                return value.addAll(elements);
            }

            @Override
            public boolean setAll(E... elements) {
                return value.setAll(elements);
            }

            @Override
            public boolean setAll(Collection<? extends E> col) {
                return value.setAll(col);
            }

            @Override
            public boolean removeAll(E... elements) {
                return value.removeAll(elements);
            }

            @Override
            public boolean retainAll(E... elements) {
                return value.retainAll(elements);
            }

            @Override
            public void remove(int from, int to) {
                value.remove(from, to);
            }
        };
    }

    /**
     * Returns a new {@link ObservableListValue} that wraps a {@link List} contained in an {@link ObservableValue}.
     * <p>
     * The returned {@code ObservableListValue} is a thin wrapper, as it simply delegates to
     * the wrapped list. Therefore, modifications of either list will be visible in the other
     * list as well.
     * <p>
     * If the wrapped list implements {@link ObservableList}, modifications will also fire
     * change events on the returned {@code ObservableListValue}.
     *
     * @param value the {@link ObservableValue}
     * @return the new {@link ObservableListValue}
     *
     * @since JFXcore 18
     */
    static <E> ObservableListValue<E> observableListValue(ObservableValue<? extends List<E>> value) {
        return new ObservableListValue<>() {
            ListExpressionHelper<E> helper;
            ObservableList<E> list;
            boolean valid;

            final InvalidationListener invalidationListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    valid = false;
                    ListExpressionHelper.fireValueChangedEvent(helper);
                }
            };

            final ListChangeListener<E> listChangeListener = new ListChangeListener<E>() {
                @Override
                public void onChanged(Change<? extends E> c) {
                    ListExpressionHelper.fireValueChangedEvent(helper, c);
                }
            };

            final WeakListChangeListener<E> weakListChangeListener = new WeakListChangeListener<>(listChangeListener);

            {
                value.addListener(new WeakInvalidationListener(invalidationListener));
            }

            @Override
            public int size() {
                final ObservableList<E> list = get();
                return list != null ? list.size() : 0;
            }

            @Override
            public boolean isEmpty() {
                final ObservableList<E> list = get();
                return list == null || list.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                final ObservableList<E> list = get();
                return list != null && list.contains(o);
            }

            @Override
            public Iterator<E> iterator() {
                final ObservableList<E> list = get();
                return list != null ? list.iterator() : Collections.emptyIterator();
            }

            @Override
            public Object[] toArray() {
                final ObservableList<E> list = get();
                return list != null ? list.toArray() : new Object[0];
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T[] toArray(T[] a) {
                final ObservableList<E> list = get();
                return list != null ? list.<T>toArray(a) : (T[])new Object[0];
            }

            @Override
            public boolean add(E e) {
                final ObservableList<E> list = get();
                return list != null ? list.add(e) : Collections.<E>emptyList().add(e);
            }

            @Override
            public boolean remove(Object o) {
                final ObservableList<E> list = get();
                return list != null ? list.remove(o) : Collections.<E>emptyList().remove(o);
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                final ObservableList<E> list = get();
                return list != null ? list.containsAll(c) : Collections.<E>emptyList().containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends E> c) {
                final ObservableList<E> list = get();
                return list != null ? list.addAll(c) : Collections.<E>emptyList().addAll(c);
            }

            @Override
            public boolean addAll(int index, Collection<? extends E> c) {
                final ObservableList<E> list = get();
                return list != null ? list.addAll(index, c) : Collections.<E>emptyList().addAll(index, c);
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                final ObservableList<E> list = get();
                return list != null ? list.removeAll(c) : Collections.<E>emptyList().removeAll(c);
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                final ObservableList<E> list = get();
                return list != null ? list.retainAll(c) : Collections.<E>emptyList().retainAll(c);
            }

            @Override
            public void clear() {
                final ObservableList<E> list = get();
                if (list != null) {
                    list.clear();
                } else {
                    Collections.<E>emptyList().clear();
                }
            }

            @Override
            public E get(int index) {
                final ObservableList<E> list = get();
                return list != null ? list.get(index) : Collections.<E>emptyList().get(index);
            }

            @Override
            public E set(int index, E element) {
                final ObservableList<E> list = get();
                return list != null ? list.set(index, element) : Collections.<E>emptyList().set(index, element);
            }

            @Override
            public void add(int index, E element) {
                final ObservableList<E> list = get();
                if (list != null) {
                    list.add(index, element);
                } else {
                    Collections.<E>emptyList().add(index, element);
                }
            }

            @Override
            public E remove(int index) {
                final ObservableList<E> list = get();
                return list != null ? list.remove(index) : Collections.<E>emptyList().remove(index);
            }

            @Override
            public int indexOf(Object o) {
                final ObservableList<E> list = get();
                return list != null ? list.indexOf(o) : -1;
            }

            @Override
            public int lastIndexOf(Object o) {
                final ObservableList<E> list = get();
                return list != null ? list.lastIndexOf(o) : -1;
            }

            @Override
            public ListIterator<E> listIterator() {
                final ObservableList<E> list = get();
                return list != null ? list.listIterator() : Collections.emptyListIterator();
            }

            @Override
            public ListIterator<E> listIterator(int index) {
                final ObservableList<E> list = get();
                return list != null ? list.listIterator(index) : Collections.<E>emptyList().listIterator(index);
            }

            @Override
            public List<E> subList(int fromIndex, int toIndex) {
                final ObservableList<E> list = get();
                return list != null ? list.subList(fromIndex, toIndex) : Collections.<E>emptyList().subList(fromIndex, toIndex);
            }

            @Override
            public ObservableList<E> get() {
                if (valid) {
                    return this.list;
                }

                valid = true;
                List<E> list = value.getValue();

                if (this.list != null) {
                    this.list.removeListener(weakListChangeListener);
                }

                if (list instanceof ObservableList) {
                    this.list = (ObservableList<E>)list;
                    this.list.addListener(weakListChangeListener);
                } else if (list != null) {
                    this.list = FXCollections.observableList(list);
                    this.list.addListener(weakListChangeListener);
                } else {
                    this.list = null;
                }

                return this.list;
            }

            @Override
            public ObservableList<E> getValue() {
                return get();
            }

            @Override
            public void addListener(InvalidationListener listener) {
                helper = ListExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                helper = ListExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(ChangeListener<? super ObservableList<E>> listener) {
                helper = ListExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ChangeListener<? super ObservableList<E>> listener) {
                helper = ListExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(ListChangeListener<? super E> listener) {
                helper = ListExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ListChangeListener<? super E> listener) {
                helper = ListExpressionHelper.removeListener(helper, listener);
            }

            @Override
            @SafeVarargs
            public final boolean addAll(E... elements) {
                final ObservableList<E> list = get();
                return list != null ? list.addAll(elements) : FXCollections.<E>emptyObservableList().addAll(elements);
            }

            @Override
            @SafeVarargs
            public final boolean setAll(E... elements) {
                final ObservableList<E> list = get();
                return list != null ? list.setAll(elements) : FXCollections.<E>emptyObservableList().setAll(elements);
            }

            @Override
            public boolean setAll(Collection<? extends E> col) {
                final ObservableList<E> list = get();
                return list != null ? list.setAll(col) : FXCollections.<E>emptyObservableList().setAll(col);
            }

            @Override
            @SafeVarargs
            public final boolean removeAll(E... elements) {
                final ObservableList<E> list = get();
                return list != null ? list.removeAll(elements) : FXCollections.<E>emptyObservableList().removeAll(elements);
            }

            @Override
            @SafeVarargs
            public final boolean retainAll(E... elements) {
                final ObservableList<E> list = get();
                return list != null ? list.retainAll(elements) : FXCollections.<E>emptyObservableList().retainAll(elements);
            }

            @Override
            public void remove(int from, int to) {
                final ObservableList<E> list = get();
                if (list != null) {
                    list.remove(from, to);
                } else {
                    FXCollections.<E>emptyObservableList().remove(from, to);
                }
            }
        };
    }
}
