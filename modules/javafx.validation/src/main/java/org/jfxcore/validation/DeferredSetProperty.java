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
import javafx.beans.property.ReadOnlySetPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import java.util.HashSet;
import java.util.Set;

public abstract class DeferredSetProperty<E>
        extends ReadOnlySetPropertyBase<E>
        implements DeferredProperty<ObservableSet<E>> {

    private final SetChangeListener<E> setChangeListener = change -> {
        invalidateProperties();
        fireValueChangedEvent(change);
    };

    private ObservableSet<E> backingSet;
    private ObservableSet<E> unmodifiableSet;
    private ObservableSet<E> newSet;
    private CollectionSizeProperty size0;
    private CollectionEmptyProperty empty0;

    public DeferredSetProperty(Set<E> initialValue) {
        if (initialValue != null) {
            backingSet = FXCollections.observableSet(new HashSet<>(initialValue.size()));
            backingSet.addListener(setChangeListener);
            unmodifiableSet = FXCollections.unmodifiableObservableSet(backingSet);
        }
    }

    protected abstract SetChange<E> getSetChange();

    @Override
    public ObservableSet<E> get() {
        return unmodifiableSet;
    }

    @Override
    public void storeValue(ObservableSet<E> newSet) {
        this.newSet = newSet;
    }

    @Override
    public void applyValue() {
        SetChange<E> change = getSetChange();

        if (backingSet != null) {
            if (newSet == null) {
                backingSet.clear();
                backingSet.removeListener(setChangeListener);
                backingSet = null;
                unmodifiableSet = null;
            } else {
                if (change.getRemoved().size() > 0) {
                    backingSet.removeAll(change.getRemoved());
                }

                if (change.getAdded().size() > 0) {
                    backingSet.addAll(change.getAdded());
                }
            }
        } else if (newSet != null) {
            backingSet = FXCollections.observableSet(new HashSet<>(newSet.size()));
            unmodifiableSet = FXCollections.unmodifiableObservableSet(backingSet);
            backingSet.addListener(setChangeListener);
            backingSet.addAll(newSet);
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

}
