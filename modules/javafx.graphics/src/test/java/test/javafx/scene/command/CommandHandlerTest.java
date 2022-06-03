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

import org.junit.jupiter.api.Test;
import javafx.scene.command.CommandHandler;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandHandlerTest {

    private static class TestPane extends Pane {
        @Override
        public void addCommandHandler(CommandHandler handler) {
            super.addCommandHandler(handler);
        }

        @Override
        public void removeCommandHandler(CommandHandler handler) {
            super.removeCommandHandler(handler);
        }
    }

    @Test
    public void testCommandHandlerIsInvoked() {
        var trace = new ArrayList<String>();
        var handler = new TestCommandHandler(trace, "H");
        var pane = new TestPane();
        pane.addCommandHandler(handler);
        assertEquals(List.of(), trace);
        pane.setOnKeyPressed(new TestEventBinding<>(new TestCommand(trace, "A", false)));
        assertEquals(List.of("+A", "+H"), trace);
        pane.setOnKeyPressed(null);
        assertEquals(List.of("+A", "+H", "-A", "-H"), trace);
    }

    @Test
    public void testRemovedCommandHandlerIsInvoked() {
        var trace = new ArrayList<String>();
        var handler = new TestCommandHandler(trace, "H");
        var pane = new TestPane();
        pane.addCommandHandler(handler);
        assertEquals(List.of(), trace);
        pane.setOnKeyPressed(new TestEventBinding<>(new TestCommand(trace, "A", false)));
        assertEquals(List.of("+A", "+H"), trace);
        pane.removeCommandHandler(handler);
        assertEquals(List.of("+A", "+H", "-H"), trace);
    }

    @Test
    public void testCommandHandlerCannotBeAddedTwice() {
        var handler = new TestCommandHandler(null, null);
        var pane = new TestPane();
        pane.addCommandHandler(handler);
        assertThrows(IllegalStateException.class, () -> pane.addCommandHandler(handler));
    }

}
