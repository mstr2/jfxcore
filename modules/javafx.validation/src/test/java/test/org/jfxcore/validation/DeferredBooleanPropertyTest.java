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

import org.jfxcore.validation.DeferredBooleanProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeferredBooleanPropertyTest {

    @Test
    public void testInitialValue() {
        var property = new DeferredBooleanProperty(true) {
            @Override public Object getBean() { return null; }
            @Override public String getName() { return null; }
        };

        assertTrue(property.get());
    }

    @Test
    public void testStoreValue() {
        var property = new DeferredBooleanProperty(false) {
            @Override public Object getBean() { return null; }
            @Override public String getName() { return null; }
        };

        assertFalse(property.get());
        property.storeValue(true);
        assertFalse(property.get());
    }

    @Test
    public void testApplyValue() {
        var property = new DeferredBooleanProperty(false) {
            @Override public Object getBean() { return null; }
            @Override public String getName() { return null; }
        };

        assertFalse(property.get());
        property.storeValue(true);
        property.applyValue();
        assertTrue(property.get());
    }

}
