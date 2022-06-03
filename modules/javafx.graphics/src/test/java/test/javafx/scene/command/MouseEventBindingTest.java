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

package test.javafx.scene.command;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.scene.command.MouseEventBinding;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MouseEventBindingTest {

    static TestCountingCommand command;
    static MouseEventBinding eventBinding;
    static Pane pane;

    @BeforeAll
    static void beforeAll() {
        command = new TestCountingCommand();
        pane = new Pane();
    }

    @BeforeEach
    void beforeEach() {
        command.count = 0;
        eventBinding = new MouseEventBinding(command);
        pane.setOnMousePressed(eventBinding);
    }

    @Test
    public void testButton() {
        eventBinding.setButton(MouseButton.PRIMARY);
        Event.fireEvent(pane, createEvent(pane, MouseButton.SECONDARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(1, command.count);
    }

    @Test
    public void testShiftDown() {
        eventBinding.setShiftDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.SHIFT));
        assertEquals(1, command.count);
    }

    @Test
    public void testControlDown() {
        eventBinding.setControlDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.CTRL));
        assertEquals(1, command.count);
    }

    @Test
    public void testAltDown() {
        eventBinding.setAltDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.ALT));
        assertEquals(1, command.count);
    }

    @Test
    public void testMetaDown() {
        eventBinding.setMetaDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.META));
        assertEquals(1, command.count);
    }

    @Test
    public void testPrimaryButtonDown() {
        eventBinding.setPrimaryButtonDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.PRIMARY));
        assertEquals(1, command.count);
    }

    @Test
    public void testSecondaryButtonDown() {
        eventBinding.setSecondaryButtonDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.SECONDARY));
        assertEquals(1, command.count);
    }

    @Test
    public void testMiddleButtonDown() {
        eventBinding.setMiddleButtonDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.MIDDLE));
        assertEquals(1, command.count);
    }

    @Test
    public void testBackButtonDown() {
        eventBinding.setBackButtonDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.BACK));
        assertEquals(1, command.count);
    }

    @Test
    public void testForwardButtonDown() {
        eventBinding.setForwardButtonDown(true);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, MouseButton.PRIMARY, Modifier.FORWARD));
        assertEquals(1, command.count);
    }

    private static MouseEvent createEvent(EventTarget target, MouseButton button, Modifier... modifiers) {
        List<Modifier> ml = Arrays.asList(modifiers);

        return new MouseEvent(
            null,
            target,
            MouseEvent.MOUSE_PRESSED,
            0, 0, 0, 0,
            button,
            1,
            ml.contains(Modifier.SHIFT),
            ml.contains(Modifier.CTRL),
            ml.contains(Modifier.ALT),
            ml.contains(Modifier.META),
            ml.contains(Modifier.PRIMARY),
            ml.contains(Modifier.MIDDLE),
            ml.contains(Modifier.SECONDARY),
            ml.contains(Modifier.BACK),
            ml.contains(Modifier.FORWARD),
            false, false, false, null);
    }

    private enum Modifier {
        SHIFT, CTRL, ALT, META, PRIMARY, SECONDARY, MIDDLE, FORWARD, BACK
    }

}
