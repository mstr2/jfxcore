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
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ObservableListValueTest {

    @Test
    public void testNullList() {
        var wrapper = ObservableListValue.observableListValue((List<String>) null);
        assertEquals(0, wrapper.size());
    }

    @Test
    public void testSimpleList() {
        var wrapper = ObservableListValue.observableListValue(List.of("foo", "bar"));
        assertEquals(2, wrapper.size());
        assertEquals("foo", wrapper.get(0));
        assertEquals("bar", wrapper.get(1));
    }

    @Test(expected = RuntimeException.class)
    public void testUnmodifiableListThrows() {
        var wrapper = ObservableListValue.observableListValue(List.of("foo", "bar"));
        wrapper.add("baz");
    }

    @Test
    public void testWrappedListModifications() {
        var list = new ArrayList<>();
        var wrapper = ObservableListValue.observableListValue(list);
        list.add("foo");
        list.add("bar");
        assertEquals(2, wrapper.size());
        assertEquals("foo", wrapper.get(0));
        assertEquals("bar", wrapper.get(1));
    }

    @Test
    public void testWrappedObservableListFiresChangeEvents() {
        var list = FXCollections.observableArrayList();
        var wrapper = ObservableListValue.observableListValue(list);
        var notifications = new int[1];
        wrapper.addListener((ListChangeListener<? super Object>)change -> notifications[0]++);
        wrapper.addListener((InvalidationListener)obs -> notifications[0]++);

        list.add("foo");
        assertEquals(2, notifications[0]);
        list.remove("foo");
        assertEquals(4, notifications[0]);
    }

    @Test
    public void testObservableValueOfList() {
        var list = new SimpleObjectProperty<>(FXCollections.observableArrayList());
        var wrapper = ObservableListValue.observableListValue(list);
        var notifications = new int[1];
        wrapper.addListener((ListChangeListener<? super Object>)change -> notifications[0]++);
        wrapper.addListener((InvalidationListener)obs -> notifications[0]++);

        list.get().add("foo");
        list.get().add("bar");
        assertEquals(4, notifications[0]);
        assertEquals("foo", wrapper.get(0));
        assertEquals("bar", wrapper.get(1));

        list.set(FXCollections.observableArrayList("baz", "qux"));
        assertEquals(6, notifications[0]);
        assertEquals("baz", wrapper.get(0));
        assertEquals("qux", wrapper.get(1));
    }

}
