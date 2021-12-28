/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
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

package test.javafx.scene.control.skin;

import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.skin.SpinnerSkin;
import javafx.scene.input.NodeState;
import org.junit.Before;
import org.junit.Test;
import test.com.sun.javafx.scene.control.infrastructure.MouseEventFirer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpinnerSkinTest {
    private Spinner<Double> spinner;

    @Before
    public void setup() {
        spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 0));
        spinner.setSkin(new SpinnerSkin<>(spinner));
    }

    @Test
    public void testNotUserModifiedWhenDecrementButtonIsClickedWithMinValue() {
        Node button = spinner.getSkin().getNode().lookup(".decrement-arrow-button");
        new MouseEventFirer(button).fireMousePressAndRelease();
        assertEquals(0, spinner.getValue(), 0.001);
        assertFalse(NodeState.isUserModified(spinner));
    }

    @Test
    public void testUserModifiedWhenIncrementButtonIsClicked() {
        Node button = spinner.getSkin().getNode().lookup(".increment-arrow-button");
        new MouseEventFirer(button).fireMousePressAndRelease();
        assertEquals(1, spinner.getValue(), 0.001);
        assertTrue(NodeState.isUserModified(spinner));
    }

    @Test
    public void testNotUserModifiedWhenIncrementedProgrammatically() {
        spinner.increment();
        assertEquals(1, spinner.getValue(), 0.001);
        assertFalse(NodeState.isUserModified(spinner));
    }
}
