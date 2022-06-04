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

import com.sun.javafx.scene.NodeHelper;
import org.junit.jupiter.api.Test;
import test.javafx.scene.command.mocks.TestCommand;
import test.javafx.scene.command.mocks.TestEventBinding;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

    @Test
    public void testAttachAndDetach() {
        testAttachAndDetach(false);
    }

    @Test
    public void testAttachAndDetachExceptional() {
        testAttachAndDetach(true);
    }

    private void testAttachAndDetach(boolean failOnAttachDetach) {
        var trace = new ArrayList<String>();
        var command1 = new TestCommand(trace, "A", failOnAttachDetach);
        var command2 = new TestCommand(trace, "B", failOnAttachDetach);
        var pane = new Pane();
        pane.setOnKeyPressed(new TestEventBinding<>(command1));
        pane.setOnKeyReleased(new TestEventBinding<>(command2));
        assertEquals(2, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "+B"), trace);
        pane.setOnKeyPressed(null);
        assertEquals(1, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "+B", "-A"), trace);
        pane.setOnKeyReleased(null);
        assertEquals(0, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "+B", "-A", "-B"), trace);
    }

    @Test
    public void testAddEventBindingWithoutCommand() {
        var trace = new ArrayList<String>();
        var command = new TestCommand(trace, "A", false);
        var pane = new Pane();
        var binding = new TestEventBinding<>(null);
        pane.setOnKeyPressed(binding);
        assertEquals(1, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of(), trace);
        binding.setCommand(command);
        assertEquals(1, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A"), trace);
        binding.setCommand(null);
        assertEquals(1, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "-A"), trace);
    }

    @Test
    public void testCommandIsAttachedOnlyOnce() {
        var trace = new ArrayList<String>();
        var command = new TestCommand(trace, "A", false);
        var pane = new Pane();
        var binding1 = new TestEventBinding<>(command);
        var binding2 = new TestEventBinding<>(command);

        pane.setOnKeyPressed(binding1);
        pane.setOnKeyReleased(binding2);
        assertEquals(2, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A"), trace);
        binding1.setCommand(null);
        assertEquals(2, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A"), trace);
        binding2.setCommand(null);
        assertEquals(2, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "-A"), trace);

        binding1.setCommand(command);
        assertEquals(2, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "-A", "+A"), trace);
        binding2.setCommand(command);
        assertEquals(2, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "-A", "+A"), trace);

        pane.setOnKeyPressed(null);
        assertEquals(1, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "-A", "+A"), trace);
        pane.setOnKeyReleased(null);
        assertEquals(0, NodeHelper.getEventBindings(pane).size());
        assertEquals(List.of("+A", "-A", "+A", "-A"), trace);
    }

}
