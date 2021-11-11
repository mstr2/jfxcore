/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  JFXcore designates this
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
 */

package test.javafx.beans.value;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableMapValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ObservableMapValueTest {

    @Test
    public void testNullMap() {
        var wrapper = ObservableMapValue.observableMapValue((Map<String, String>) null);
        assertEquals(0, wrapper.size());
    }

    @Test
    public void testSimpleMap() {
        var wrapper = ObservableMapValue.observableMapValue(Map.of("k1", "foo", "k2", "bar"));
        assertEquals(2, wrapper.size());
        assertEquals("foo", wrapper.get("k1"));
        assertEquals("bar", wrapper.get("k2"));
    }

    @Test(expected = RuntimeException.class)
    public void testUnmodifiableMapThrows() {
        var wrapper = ObservableMapValue.observableMapValue(Map.of("k1", "foo"));
        wrapper.put("k2", "bar");
    }

    @Test
    public void testWrappedMapModifications() {
        var map = new HashMap<>();
        var wrapper = ObservableMapValue.observableMapValue(map);
        map.put("k1", "foo");
        map.put("k2", "bar");
        assertEquals(2, wrapper.size());
        assertEquals("foo", wrapper.get("k1"));
        assertEquals("bar", wrapper.get("k2"));
    }

    @Test
    public void testWrappedObservableMapFiresChangeEvents() {
        var map = FXCollections.observableHashMap();
        var wrapper = ObservableMapValue.observableMapValue(map);
        var notifications = new int[1];
        wrapper.addListener((MapChangeListener<? super Object, ? super Object>) change -> notifications[0]++);
        wrapper.addListener((InvalidationListener)obs -> notifications[0]++);

        map.put("k1", "foo");
        assertEquals(2, notifications[0]);
        map.remove("k1");
        assertEquals(4, notifications[0]);
    }

    @Test
    public void testObservableValueOfMap() {
        var map = new SimpleObjectProperty<>(FXCollections.observableMap(new HashMap<>()));
        var wrapper = ObservableMapValue.observableMapValue(map);
        var notifications = new int[1];
        wrapper.addListener((MapChangeListener<? super Object, ? super Object>)change -> notifications[0]++);
        wrapper.addListener((InvalidationListener)obs -> notifications[0]++);

        map.get().put("k1", "foo");
        map.get().put("k2", "bar");
        assertEquals(4, notifications[0]);
        assertEquals("foo", wrapper.get("k1"));
        assertEquals("bar", wrapper.get("k2"));

        map.set(FXCollections.observableMap(Map.of("k1", "baz", "k2", "qux")));
        assertEquals(7, notifications[0]);
        assertEquals("baz", wrapper.get("k1"));
        assertEquals("qux", wrapper.get("k2"));
    }

}
