/*
 * Copyright (c) 2022, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  JFXcore designates this
 * particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
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
 */

package org.jfxcore.validation;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListPropertyBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import java.util.ArrayList;
import java.util.List;

public abstract class DeferredListProperty<E>
        extends ReadOnlyListPropertyBase<E>
        implements DeferredProperty<ObservableList<E>> {

    private final ListChangeListener<E> listChangeListener = change -> {
        invalidateProperties();
        fireValueChangedEvent(change);
    };

    private ConstrainedListImpl<E> backingList;
    private ObservableList<E> newList;
    private CollectionSizeProperty size0;
    private CollectionEmptyProperty empty0;

    public DeferredListProperty(ObservableList<E> initialValue) {
        if (initialValue != null) {
            backingList = new ConstrainedListImpl<>(initialValue.size());
            backingList.addListener(listChangeListener);
        }
    }

    protected abstract ListChange.ReplacedRange<E> getListChange();

    @Override
    public ObservableList<E> get() {
        return backingList;
    }

    @Override
    public void storeValue(ObservableList<E> newValue) {
        newList = newValue;
    }

    @Override
    public void applyValue() {
        ListChange.ReplacedRange<E> change = getListChange();

        if (backingList != null && newList == null) {
            backingList.removeListener(listChangeListener);
            backingList = null;
        } else if (backingList == null && newList != null) {
            backingList = new ConstrainedListImpl<>(newList.size());
            backingList.addListener(listChangeListener);
        }

        if (backingList != null) {
            backingList.applyChange(change);
        }
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

    private void invalidateProperties() {
        if (size0 != null) {
            size0.fireValueChangedEvent();
        }
        if (empty0 != null) {
            empty0.fireValueChangedEvent();
        }
    }

    private static class ConstrainedListImpl<E> extends ObservableListBase<E> {
        private final List<E> backingList;

        public ConstrainedListImpl(int capacity) {
            backingList = new ArrayList<>(capacity);
        }

        @Override
        public E get(int index) {
            return backingList.get(index);
        }

        @Override
        public int size() {
            return backingList.size();
        }

        public void applyChange(ListChange.ReplacedRange<E> change) {
            beginChange();

            if (change.getRemovedSize() > 0) {
                List<E> removed = backingList.subList(change.getFrom(), change.getFrom() + change.getRemovedSize());
                nextRemove(change.getFrom(), removed);
                removed.clear();
            }

            if (change.getElements().size() > 0) {
                nextAdd(change.getFrom(), change.getTo());
                backingList.addAll(change.getFrom(), change.getElements());
            }

            endChange();
        }
    }

}
