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

import com.sun.javafx.binding.MapExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.WeakMapChangeListener;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An observable reference to an {@link javafx.collections.ObservableMap}.
 *
 * @see javafx.collections.ObservableMap
 * @see ObservableObjectValue
 * @see ObservableValue
 *
 * @param <K> the type of the key elements of the {@code Map}
 * @param <V> the type of the value elements of the {@code Map}
 * @since JavaFX 2.1
 */
public interface ObservableMapValue<K, V> extends ObservableObjectValue<ObservableMap<K,V>>, ObservableMap<K, V> {
    /**
     * Returns a new {@link ObservableMapValue} that wraps a {@link Map}.
     * <p>
     * The returned {@code ObservableMapValue} is a thin wrapper, as it simply delegates to
     * the wrapped map. Therefore, modifications of either map will be visible in the other
     * map as well.
     * <p>
     * If the wrapped map implements {@link ObservableMap}, modifications will also fire
     * change events on the returned {@code ObservableMapValue}.
     *
     * @param map the wrapped map, or {@code null} (empty map)
     * @return the new {@link ObservableMapValue}
     *
     * @since JFXcore 18
     */
    static <K, V> ObservableMapValue<K, V> observableMapValue(Map<K, V> map) {
        return new ObservableMapValue<>() {
            private MapExpressionHelper<K, V> helper;
            private final ObservableMap<K, V> value;
            private final MapChangeListener<K, V> mapChangeListener;
            private final WeakMapChangeListener<K, V> weakMapChangeListener;

            {
                ObservableMap<K, V> value = null;

                if (map instanceof ObservableMap<K, V>) {
                    value = (ObservableMap<K, V>)map;
                } else if (map != null) {
                    value = FXCollections.observableMap(map);
                }

                if (value != null) {
                    mapChangeListener = new MapChangeListener<>() {
                        @Override
                        public void onChanged(Change<? extends K, ? extends V> c) {
                            MapExpressionHelper.fireValueChangedEvent(helper, c);
                        }
                    };
                    weakMapChangeListener = new WeakMapChangeListener<>(mapChangeListener);
                    value.addListener(weakMapChangeListener);
                } else {
                    value = FXCollections.emptyObservableMap();
                    mapChangeListener = null;
                    weakMapChangeListener = null;
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
            public boolean containsKey(Object key) {
                return value.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return this.value.containsValue(value);
            }

            @Override
            public V get(Object key) {
                return value.get(key);
            }

            @Override
            public V put(K key, V value) {
                return this.value.put(key, value);
            }

            @Override
            public V remove(Object key) {
                return value.remove(key);
            }

            @Override
            public void putAll(Map<? extends K, ? extends V> m) {
                value.putAll(m);
            }

            @Override
            public void clear() {
                value.clear();
            }

            @Override
            public Set<K> keySet() {
                return value.keySet();
            }

            @Override
            public Collection<V> values() {
                return value.values();
            }

            @Override
            public Set<Map.Entry<K, V>> entrySet() {
                return value.entrySet();
            }

            @Override
            public ObservableMap<K, V> get() {
                return value;
            }

            @Override
            public ObservableMap<K, V> getValue() {
                return value;
            }

            @Override
            public void addListener(ChangeListener<? super ObservableMap<K, V>> listener) {
                helper = MapExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ChangeListener<? super ObservableMap<K, V>> listener) {
                helper = MapExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(MapChangeListener<? super K, ? super V> listener) {
                helper = MapExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(MapChangeListener<? super K, ? super V> listener) {
                helper = MapExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(InvalidationListener listener) {
                helper = MapExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                helper = MapExpressionHelper.removeListener(helper, listener);
            }
        };
    }

    /**
     * Returns a new {@link ObservableMapValue} that wraps a {@link Map} contained in an {@link ObservableValue}.
     * <p>
     * The returned {@code ObservableMapValue} is a thin wrapper, as it simply delegates to
     * the wrapped map. Therefore, modifications of either map will be visible in the other
     * map as well.
     * <p>
     * If the wrapped map implements {@link ObservableMap}, modifications will also fire
     * change events on the returned {@code ObservableMapValue}.
     *
     * @param value the {@link ObservableValue}
     * @return the new {@link ObservableMapValue}
     *
     * @since JFXcore 18
     */
    static <K, V> ObservableMapValue<K, V> observableMapValue(ObservableValue<? extends Map<K, V>> value) {
        return new ObservableMapValue<>() {
            MapExpressionHelper<K, V> helper;
            ObservableMap<K, V> map;
            boolean valid;

            final InvalidationListener invalidationListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    valid = false;
                    MapExpressionHelper.fireValueChangedEvent(helper);
                }
            };

            final MapChangeListener<K, V> mapChangeListener = new MapChangeListener<K, V>() {
                @Override
                public void onChanged(Change<? extends K, ? extends V> change) {
                    MapExpressionHelper.fireValueChangedEvent(helper, change);
                }
            };

            final WeakMapChangeListener<K, V> weakMapChangeListener = new WeakMapChangeListener<>(mapChangeListener);

            {
                value.addListener(new WeakInvalidationListener(invalidationListener));
            }

            @Override
            public int size() {
                final ObservableMap<K, V> map = get();
                return map != null ? map.size() : 0;
            }

            @Override
            public boolean isEmpty() {
                final ObservableMap<K, V> map = get();
                return map == null || map.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                final ObservableMap<K, V> map = get();
                return map != null && map.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                final ObservableMap<K, V> map = get();
                return map != null && map.containsValue(value);
            }

            @Override
            public V get(Object key) {
                final ObservableMap<K, V> map = get();
                return map != null ? map.get(key) : null;
            }

            @Override
            public V put(K key, V value) {
                final ObservableMap<K, V> map = get();
                return map != null ? map.put(key, value) : Collections.<K, V>emptyMap().put(key, value);
            }

            @Override
            public V remove(Object key) {
                final ObservableMap<K, V> map = get();
                return map != null ? map.remove(key) : Collections.<K, V>emptyMap().remove(key);
            }

            @Override
            public void putAll(Map<? extends K, ? extends V> m) {
                final ObservableMap<K, V> map = get();
                if (map != null) {
                    map.putAll(m);
                } else {
                    Collections.<K, V>emptyMap().putAll(m);
                }
            }

            @Override
            public void clear() {
                final ObservableMap<K, V> map = get();
                if (map != null) {
                    map.clear();
                } else {
                    Collections.<K, V>emptyMap().clear();
                }
            }

            @Override
            public Set<K> keySet() {
                final ObservableMap<K, V> map = get();
                return map != null ? map.keySet() : Collections.<K, V>emptyMap().keySet();
            }

            @Override
            public Collection<V> values() {
                final ObservableMap<K, V> map = get();
                return map != null ? map.values() : Collections.<K, V>emptyMap().values();
            }

            @Override
            public Set<Map.Entry<K, V>> entrySet() {
                final ObservableMap<K, V> map = get();
                return map != null ? map.entrySet() : Collections.<K, V>emptyMap().entrySet();
            }

            @Override
            public ObservableMap<K, V> get() {
                if (valid) {
                    return this.map;
                }

                valid = true;
                Map<K, V> map = value.getValue();

                if (this.map != null) {
                    this.map.removeListener(weakMapChangeListener);
                }

                if (map instanceof ObservableMap) {
                    this.map = (ObservableMap<K, V>)map;
                    this.map.addListener(weakMapChangeListener);
                } else if (map != null) {
                    this.map = FXCollections.observableMap(map);
                    this.map.addListener(weakMapChangeListener);
                } else {
                    this.map = null;
                }

                return this.map;
            }

            @Override
            public ObservableMap<K, V> getValue() {
                return get();
            }

            @Override
            public void addListener(ChangeListener<? super ObservableMap<K, V>> listener) {
                helper = MapExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ChangeListener<? super ObservableMap<K, V>> listener) {
                helper = MapExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(MapChangeListener<? super K, ? super V> listener) {
                helper = MapExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(MapChangeListener<? super K, ? super V> listener) {
                helper = MapExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(InvalidationListener listener) {
                helper = MapExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                helper = MapExpressionHelper.removeListener(helper, listener);
            }
        };
    }
}
