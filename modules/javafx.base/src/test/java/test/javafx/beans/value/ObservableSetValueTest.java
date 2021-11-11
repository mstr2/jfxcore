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
import javafx.beans.value.ObservableSetValue;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObservableSetValueTest {

    @Test
    public void testNullSet() {
        var wrapper = ObservableSetValue.observableSetValue((Set<String>) null);
        assertEquals(0, wrapper.size());
    }

    @Test
    public void testSimpleSet() {
        var wrapper = ObservableSetValue.observableSetValue(Set.of("foo", "bar"));
        assertEquals(2, wrapper.size());
        assertTrue(wrapper.contains("foo"));
        assertTrue(wrapper.contains("bar"));
    }

    @Test(expected = RuntimeException.class)
    public void testUnmodifiableSetThrows() {
        var wrapper = ObservableSetValue.observableSetValue(Set.of("foo", "bar"));
        wrapper.add("baz");
    }

    @Test
    public void testWrappedSetModifications() {
        var set = new HashSet<>();
        var wrapper = ObservableSetValue.observableSetValue(set);
        set.add("foo");
        set.add("bar");
        assertEquals(2, wrapper.size());
        assertTrue(wrapper.contains("foo"));
        assertTrue(wrapper.contains("bar"));
    }

    @Test
    public void testWrappedObservableSetFiresChangeEvents() {
        var set = FXCollections.observableSet(new HashSet<>());
        var wrapper = ObservableSetValue.observableSetValue(set);
        var notifications = new int[1];
        wrapper.addListener((SetChangeListener<? super Object>)change -> notifications[0]++);
        wrapper.addListener((InvalidationListener)obs -> notifications[0]++);

        set.add("foo");
        assertEquals(2, notifications[0]);
        set.remove("foo");
        assertEquals(4, notifications[0]);
    }

    @Test
    public void testObservableValueOfSet() {
        var set = new SimpleObjectProperty<>(FXCollections.observableSet(new HashSet<>()));
        var wrapper = ObservableSetValue.observableSetValue(set);
        var notifications = new int[1];
        wrapper.addListener((SetChangeListener<? super Object>)change -> notifications[0]++);
        wrapper.addListener((InvalidationListener)obs -> notifications[0]++);

        set.get().add("foo");
        set.get().add("bar");
        assertEquals(4, notifications[0]);
        assertTrue(wrapper.contains("foo"));
        assertTrue(wrapper.contains("bar"));

        set.set(FXCollections.observableSet("baz", "qux"));
        assertEquals(9, notifications[0]);
        assertFalse(wrapper.contains("foo"));
        assertFalse(wrapper.contains("bar"));
        assertTrue(wrapper.contains("baz"));
        assertTrue(wrapper.contains("qux"));
    }

}
