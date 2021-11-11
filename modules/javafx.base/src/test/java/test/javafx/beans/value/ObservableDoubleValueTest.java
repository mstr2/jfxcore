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

import javafx.beans.value.ObservableDoubleValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ObservableDoubleValueTest {

    @Test
    public void testObservableDoubleValue() {
        var wrapper = ObservableDoubleValue.observableDoubleValue(null);
        assertNull(wrapper.getValue());
        assertEquals(0.0, wrapper.get(), 0.001);

        wrapper = ObservableDoubleValue.observableDoubleValue(1);
        assertEquals(1.0, wrapper.getValue().doubleValue(), 0.001);
        assertEquals(1.0, wrapper.get(), 0.001);
    }

}
