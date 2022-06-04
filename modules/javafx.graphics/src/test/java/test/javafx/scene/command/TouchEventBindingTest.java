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
import test.javafx.scene.command.mocks.TestCountingCommand;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.scene.command.TouchEventBinding;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.Pane;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TouchEventBindingTest {

    static TestCountingCommand command;
    static TouchEventBinding eventBinding;
    static Pane pane;

    @BeforeAll
    static void beforeAll() {
        command = new TestCountingCommand();
        pane = new Pane();
    }

    @BeforeEach
    void beforeEach() {
        command.count = 0;
        eventBinding = new TouchEventBinding(command);
        pane.setOnTouchPressed(eventBinding);
    }

    @Test
    public void testTouchCount() {
        eventBinding.setTouchCount(2);
        Event.fireEvent(pane, createEvent(pane));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, Modifier.DUAL_TOUCH));
        assertEquals(1, command.count);
    }

    @Test
    public void testShiftDown() {
        eventBinding.setShiftDown(true);
        Event.fireEvent(pane, createEvent(pane));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, Modifier.SHIFT));
        assertEquals(1, command.count);
    }

    @Test
    public void testControlDown() {
        eventBinding.setControlDown(true);
        Event.fireEvent(pane, createEvent(pane));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, Modifier.CTRL));
        assertEquals(1, command.count);
    }

    @Test
    public void testAltDown() {
        eventBinding.setAltDown(true);
        Event.fireEvent(pane, createEvent(pane));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, Modifier.ALT));
        assertEquals(1, command.count);
    }

    @Test
    public void testMetaDown() {
        eventBinding.setMetaDown(true);
        Event.fireEvent(pane, createEvent(pane));
        assertEquals(0, command.count);
        Event.fireEvent(pane, createEvent(pane, Modifier.META));
        assertEquals(1, command.count);
    }

    private static TouchEvent createEvent(EventTarget target, Modifier... modifiers) {
        List<Modifier> ml = Arrays.asList(modifiers);

        return new TouchEvent(
            null,
            target,
            TouchEvent.TOUCH_PRESSED,
            new TouchPoint(0, TouchPoint.State.PRESSED, 0, 0, 0, 0, target, null),
            ml.contains(Modifier.DUAL_TOUCH) ? List.of(
                new TouchPoint(0, TouchPoint.State.PRESSED, 0, 0, 0, 0, target, null),
                new TouchPoint(1, TouchPoint.State.PRESSED, 0, 0, 0, 0, target, null)
            ) : List.of(),
            0,
            ml.contains(Modifier.SHIFT),
            ml.contains(Modifier.CTRL),
            ml.contains(Modifier.ALT),
            ml.contains(Modifier.META));
    }

    private enum Modifier {
        SHIFT, CTRL, ALT, META, DUAL_TOUCH
    }

}
