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

package test.com.sun.javafx.css;

import com.sun.javafx.css.StylesheetList;
import org.junit.jupiter.api.Test;
import test.javafx.collections.MockListObserver;
import javafx.beans.value.WritableValue;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StylesheetListTest {

    @Test
    public void testEmptyList() {
        var list = new StylesheetList() {
            final WritableValue<String> p1 = addStylesheet(null);
            final WritableValue<String> p2 = addStylesheet(null);
            final WritableValue<String> p3 = addStylesheet(null);
        };

        assertEquals(0, list.size());
    }

    @Test
    public void testToggleItem() {
        var list = new StylesheetList() {
            final WritableValue<String> p1 = addStylesheet(null);
            final WritableValue<String> p2 = addStylesheet(null);
            final WritableValue<String> p3 = addStylesheet(null);
        };

        list.p1.setValue("foo");
        assertEquals(List.of("foo"), list);

        list.p3.setValue("bar");
        assertEquals(List.of("foo", "bar"), list);

        list.p1.setValue(null);
        assertEquals(List.of("bar"), list);
    }

    @Test
    public void testChangeItem() {
        var list = new StylesheetList() {
            final WritableValue<String> p1 = addStylesheet(null);
            final WritableValue<String> p2 = addStylesheet(null);
            final WritableValue<String> p3 = addStylesheet(null);
        };

        list.p1.setValue("foo");
        assertEquals(List.of("foo"), list);

        list.p3.setValue("bar");
        assertEquals(List.of("foo", "bar"), list);

        list.p3.setValue("baz");
        assertEquals(List.of("foo", "baz"), list);
    }

    @Test
    public void testChangeEvent() {
        var list = new StylesheetList() {
            final WritableValue<String> p1 = addStylesheet(null);
            final WritableValue<String> p2 = addStylesheet(null);
            final WritableValue<String> p3 = addStylesheet(null);
        };

        var observer = new MockListObserver<String>();
        list.addListener(observer);

        list.p1.setValue("foo");
        observer.check1AddRemove(list, List.of(), 0, 1);
        observer.clear();

        list.p3.setValue("bar");
        observer.check1AddRemove(list, List.of(), 1, 2);
        observer.clear();

        list.p2.setValue("baz");
        observer.check1AddRemove(list, List.of(), 1, 2);
        observer.clear();

        list.p2.setValue("qux");
        observer.check1AddRemove(list, List.of("baz"), 1, 2);
        observer.clear();

        list.p2.setValue(null);
        observer.check1AddRemove(list, List.of("qux"), 1, 1);
        observer.clear();

        list.p3.setValue(null);
        observer.check1AddRemove(list, List.of("bar"), 1, 1);
        observer.clear();
    }

    @Test
    public void testBatchChange() {
        var list = new StylesheetList() {
            final WritableValue<String> p1 = addStylesheet(null);
            final WritableValue<String> p2 = addStylesheet(null);
            final WritableValue<String> p3 = addStylesheet("baz");
        };

        var observer = new MockListObserver<String>();
        list.addListener(observer);

        list.lock();
        list.p1.setValue("foo");
        observer.check0();

        list.p2.setValue("bar");
        observer.check0();

        list.p3.setValue(null);
        observer.check0();

        list.unlock();
        observer.check1();
        observer.check1AddRemove(list, List.of("baz"), 0, 2);

    }

}
