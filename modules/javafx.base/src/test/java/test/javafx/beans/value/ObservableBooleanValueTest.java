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

import javafx.beans.value.ObservableBooleanValue;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ObservableBooleanValueTest {

    @Test
    public void testObservableBooleanValue() {
        var wrapper = ObservableBooleanValue.observableBooleanValue(null);
        assertNull(wrapper.getValue());
        assertFalse(wrapper.get());

        wrapper =  ObservableBooleanValue.observableBooleanValue(true);
        assertTrue(wrapper.getValue());
        assertTrue(wrapper.get());

        wrapper =  ObservableBooleanValue.observableBooleanValue(false);
        assertFalse(wrapper.getValue());
        assertFalse(wrapper.get());
    }

}
