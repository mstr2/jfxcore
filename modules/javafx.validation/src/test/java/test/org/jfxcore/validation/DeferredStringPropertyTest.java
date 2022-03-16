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

package test.org.jfxcore.validation;

import org.jfxcore.validation.DeferredStringProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeferredStringPropertyTest {

    @Test
    public void testInitialValue() {
        var property = new DeferredStringProperty("foo") {
            @Override public Object getBean() { return null; }
            @Override public String getName() { return null; }
        };

        assertEquals("foo", property.get());
    }

    @Test
    public void testStoreValue() {
        var property = new DeferredStringProperty("foo") {
            @Override public Object getBean() { return null; }
            @Override public String getName() { return null; }
        };

        assertEquals("foo", property.get());
        property.storeValue("bar");
        assertEquals("foo", property.get());
    }

    @Test
    public void testApplyValue() {
        var property = new DeferredStringProperty("foo") {
            @Override public Object getBean() { return null; }
            @Override public String getName() { return null; }
        };

        assertEquals("foo", property.get());
        property.storeValue("bar");
        property.applyValue();
        assertEquals("bar", property.get());
    }

}
