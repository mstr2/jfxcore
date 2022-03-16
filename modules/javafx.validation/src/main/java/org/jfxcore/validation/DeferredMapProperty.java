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
import javafx.beans.property.ReadOnlyMapPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import java.util.HashMap;
import java.util.Map;

public abstract class DeferredMapProperty<K, V>
        extends ReadOnlyMapPropertyBase<K, V>
        implements DeferredProperty<ObservableMap<K, V>> {

    private final MapChangeListener<K, V> mapChangeListener = change -> {
        invalidateProperties();
        fireValueChangedEvent(change);
    };

    private ObservableMap<K, V> backingMap;
    private ObservableMap<K, V> unmodifiableSet;
    private ObservableMap<K, V> newMap;
    private MapSizeProperty size0;
    private MapEmptyProperty empty0;

    public DeferredMapProperty(Map<K, V> initialValue) {
        if (initialValue != null) {
            backingMap = FXCollections.observableMap(new HashMap<>(initialValue.size()));
            backingMap.addListener(mapChangeListener);
            unmodifiableSet = FXCollections.unmodifiableObservableMap(backingMap);
        }
    }

    protected abstract MapChange<K, V> getMapChange();

    @Override
    public ObservableMap<K, V> get() {
        return unmodifiableSet;
    }

    @Override
    public void storeValue(ObservableMap<K, V> newMap) {
        this.newMap = newMap;
    }

    @Override
    public void applyValue() {
        MapChange<K, V> change = getMapChange();

        if (backingMap != null) {
            if (newMap == null) {
                backingMap.clear();
                backingMap.removeListener(mapChangeListener);
                backingMap = null;
                unmodifiableSet = null;
            } else {
                if (change.getRemoved().size() > 0) {
                    backingMap.keySet().removeAll(change.getRemoved());
                }

                if (change.getAdded().size() > 0) {
                    backingMap.putAll(change.getAdded());
                }
            }
        } else if (newMap != null) {
            backingMap = FXCollections.observableMap(new HashMap<>(newMap.size()));
            unmodifiableSet = FXCollections.unmodifiableObservableMap(backingMap);
            backingMap.addListener(mapChangeListener);
            backingMap.putAll(newMap);
        }
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

    private void invalidateProperties() {
        if (size0 != null) {
            size0.fireValueChangedEvent();
        }
        if (empty0 != null) {
            empty0.fireValueChangedEvent();
        }
    }

}
