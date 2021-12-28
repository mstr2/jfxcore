/*
 * Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
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

package test.javafx.scene.control.skin;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import static org.junit.Assert.*;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.NodeState;
import org.junit.Before;
import org.junit.Test;
import test.com.sun.javafx.scene.control.infrastructure.KeyEventFirer;
import test.com.sun.javafx.scene.control.infrastructure.MouseEventFirer;

/**
 */
public class SliderSkinTest {
    private Slider slider;
    private SliderSkinMock skin;

    @Before public void setup() {
        slider = new Slider();
        skin = new SliderSkinMock(slider);
        slider.setSkin(skin);
    }

    @Test public void maxWidthTracksPreferred() {
        slider.setOrientation(Orientation.VERTICAL);
        slider.setPrefWidth(500);
        assertEquals(500, slider.maxWidth(-1), 0);
    }

    @Test public void maxHeightTracksPreferred() {
        slider.setOrientation(Orientation.HORIZONTAL);
        slider.setPrefHeight(500);
        assertEquals(500, slider.maxHeight(-1), 0);
    }

    @Test public void testUserModifiedWhenTrackIsClicked() {
        slider.setValue(1);
        assertFalse(NodeState.isUserModified(slider));

        var track = slider.getSkin().getNode().lookup(".track");
        MouseEventFirer firer = new MouseEventFirer(track);
        firer.fireMousePressAndRelease();
        assertTrue(NodeState.isUserModified(slider));
    }

    @Test public void testNotUserModifiedWhenThumbIsDraggedBackAndForth() {
        slider.setSnapToTicks(true);
        assertFalse(NodeState.isUserModified(slider));

        var thumb = slider.getSkin().getNode().lookup(".thumb");
        MouseEventFirer firer = new MouseEventFirer(thumb);
        firer.fireMousePressed();
        firer.fireMouseEvent(MouseEvent.MOUSE_DRAGGED, 100, 0);
        firer.fireMouseEvent(MouseEvent.MOUSE_DRAGGED, -100, 0);
        firer.fireMouseReleased();
        assertFalse(NodeState.isUserModified(slider));
    }

    @Test public void testUserModifiedWhenHomeIsPressed() {
        slider.setValue(0);
        assertFalse(NodeState.isUserModified(slider));

        KeyEventFirer firer = new KeyEventFirer(slider.getSkin().getNode());
        firer.doKeyPress(KeyCode.HOME);
        assertFalse(NodeState.isUserModified(slider)); // slider thumb didn't move

        slider.setValue(1);
        firer.doKeyPress(KeyCode.HOME);
        assertTrue(NodeState.isUserModified(slider));
    }

    @Test public void testUserModifiedWhenEndIsPressed() {
        slider.setValue(0);
        assertFalse(NodeState.isUserModified(slider));

        KeyEventFirer firer = new KeyEventFirer(slider.getSkin().getNode());
        firer.doKeyPress(KeyCode.END);
        assertTrue(NodeState.isUserModified(slider));

        slider.setValue(100);
        NodeState.setUserModified(slider, false);
        firer.doKeyPress(KeyCode.END);
        assertFalse(NodeState.isUserModified(slider));
    }

    public static final class SliderSkinMock extends SliderSkin {
        boolean propertyChanged = false;
        int propertyChangeCount = 0;
        public SliderSkinMock(Slider slider) {
            super(slider);
        }

        public void addWatchedProperty(ObservableValue<?> p) {
            p.addListener(o -> {
                propertyChanged = true;
                propertyChangeCount++;
            });
        }
    }
}
