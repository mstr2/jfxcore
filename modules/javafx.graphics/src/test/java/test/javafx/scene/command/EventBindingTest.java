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

import com.sun.javafx.scene.command.EventBindingHelper;
import org.junit.jupiter.api.Test;
import javafx.scene.command.KeyEventBinding;
import javafx.scene.shape.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

public class EventBindingTest {

    @Test
    public void testBindingWithoutCommandIsDisabled() {
        var eventBinding = new TestEventBinding<>(null);
        assertTrue(EventBindingHelper.getDisabled(eventBinding).get());
    }

    @Test
    public void testBindingWithExecutableCommandIsEnabled() {
        var eventBinding = new TestEventBinding<>(null);
        var command = new TestCommand(null, null, false);
        command.executable.set(true);
        eventBinding.setCommand(command);
        assertFalse(EventBindingHelper.getDisabled(eventBinding).get());
        command.executable.set(false);
        assertTrue(EventBindingHelper.getDisabled(eventBinding).get());
    }

    @Test
    public void testBindingIsDisabledWhenCommandIsExecuting() {
        var eventBinding = new TestEventBinding<>(null);
        var command = new TestCommand(null, null, false);
        command.executable.set(true);
        command.executing.set(true);
        eventBinding.setCommand(command);
        assertTrue(EventBindingHelper.getDisabled(eventBinding).get());
        eventBinding.setDisabledWhenExecuting(false);
        assertFalse(EventBindingHelper.getDisabled(eventBinding).get());
    }

    @Test
    public void testDisabledIsUpdatedWhenCommandIsChanged() {
        var eventBinding = new TestEventBinding<>(null);
        var command1 = new TestCommand(null, null ,false);
        var command2 = new TestCommand(null, null, false);
        command1.executable.set(true);
        command2.executable.set(false);
        eventBinding.setCommand(command1);
        assertFalse(EventBindingHelper.getDisabled(eventBinding).get());
        eventBinding.setCommand(command2);
        assertTrue(EventBindingHelper.getDisabled(eventBinding).get());
    }

    @Test
    public void testBindingCannotBeReused() {
        var node = new Rectangle();
        var eventBinding = new KeyEventBinding();
        node.setOnKeyPressed(eventBinding);
        assertThrows(IllegalStateException.class, () -> node.setOnKeyReleased(eventBinding));
        node.setOnKeyPressed(null);
        assertDoesNotThrow(() -> node.setOnKeyReleased(eventBinding));
    }

}
