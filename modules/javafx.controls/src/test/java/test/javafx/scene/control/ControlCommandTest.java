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

package test.javafx.scene.control;

import com.sun.javafx.scene.NodeHelper;
import org.junit.jupiter.api.Test;
import test.javafx.scene.command.TestCommand;
import test.javafx.scene.command.TestCommandHandler;
import test.javafx.scene.command.TestEventBinding;
import javafx.scene.control.Button;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ControlCommandTest {

    @Test
    public void testCommandHandlerPropertyIsInvoked() {
        var trace = new ArrayList<String>();
        var handler = new TestCommandHandler(trace, "H");
        var button = new Button();
        button.setCommandHandler(handler);
        assertEquals(List.of(), trace);
        button.setOnKeyPressed(new TestEventBinding<>(new TestCommand(trace, "A", false)));
        assertEquals(List.of("+A", "+H"), trace);
        button.setOnKeyPressed(null);
        assertEquals(List.of("+A", "+H", "-A", "-H"), trace);
    }

    @Test
    public void testRemovedCommandHandlerPropertyIsInvoked() {
        var trace = new ArrayList<String>();
        var handler = new TestCommandHandler(trace, "H");
        var button = new Button();
        button.setCommandHandler(handler);
        assertEquals(List.of(), trace);
        button.setOnKeyPressed(new TestEventBinding<>(new TestCommand(trace, "A", false)));
        assertEquals(List.of("+A", "+H"), trace);
        button.setCommandHandler(null);
        assertEquals(List.of("+A", "+H", "-H"), trace);
    }

    @Test
    public void testCommandHandlerCannotBeAddedWithInterfaceApiWhenAlreadySetViaProperty() {
        var handler = new TestCommandHandler(null, null);
        var button = new Button();
        button.setCommandHandler(handler);
        assertThrows(IllegalStateException.class, () -> button.addCommandHandler(handler));
    }

    @Test
    public void testCommandHandlerPropertyCannotBeRemovedWithInterfaceApi() {
        var handler = new TestCommandHandler(null, null);
        var button = new Button();
        button.setCommandHandler(handler);
        assertEquals(1, NodeHelper.getCommandHandlers(button).size());
        button.removeCommandHandler(handler);
        assertEquals(1, NodeHelper.getCommandHandlers(button).size());
    }

    @Test
    public void testExistingCommandHandlerIsRemovedWhenSetViaProperty() {
        var handler = new TestCommandHandler(null, null);
        var button = new Button();
        button.addCommandHandler(handler);
        assertEquals(1, NodeHelper.getCommandHandlers(button).size());
        button.setCommandHandler(handler);
        assertEquals(1, NodeHelper.getCommandHandlers(button).size());
        button.removeCommandHandler(handler);
        assertEquals(1, NodeHelper.getCommandHandlers(button).size());
    }

}
