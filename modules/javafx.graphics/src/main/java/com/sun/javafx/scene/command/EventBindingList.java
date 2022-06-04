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

package com.sun.javafx.scene.command;

import com.sun.javafx.scene.NodeHelper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.command.Command;
import javafx.scene.command.CommandHandler;
import javafx.scene.command.EventBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds all event bindings that are set for the node's event handler properties.
 */
@SuppressWarnings("rawtypes")
public final class EventBindingList extends ArrayList<EventBinding<?>> implements ChangeListener {

    private final Node node;

    public EventBindingList(Node node) {
        super(1);
        this.node = node;
    }

    public boolean isDisabled() {
        for (int i = 0, max = size(); i < max; ++i) {
            if (EventBindingHelper.getDisabled(get(i)).get()) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public void addEventBinding(EventBinding<?> binding) {
        Node otherNode = EventBindingHelper.getNode(binding);
        if (otherNode != null) {
            throw new IllegalStateException("EventBinding is already set on " + otherNode);
        }

        EventBindingHelper.setNode(binding, node);
        EventBindingHelper.getDisabled(binding).addListener(this);
        binding.commandProperty().addListener(this);
        add(binding);
        NodeHelper.updateDisabled(node);
        Command addedCommand = binding.getCommand();

        if (addedCommand != null && contains(addedCommand, 1)) {
            updateCommand(null, addedCommand);
        }
    }

    @SuppressWarnings("unchecked")
    public void removeEventBinding(EventBinding<?> binding) {
        int index = indexOf(binding);
        if (index >= 0) {
            EventBindingHelper.setNode(binding, null);
            EventBindingHelper.getDisabled(binding).removeListener(this);
            binding.commandProperty().removeListener(this);
            remove(index);

            Command removedCommand = binding.getCommand();
            if (removedCommand != null && contains(removedCommand, 0)) {
                updateCommand(removedCommand, null);
            }

            NodeHelper.updateDisabled(node);
        }
    }

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        if (observable instanceof ReadOnlyObjectProperty<?>) {
            Command removedCommand = contains((Command)oldValue, 0) ? (Command)oldValue : null;
            Command addedCommand = contains((Command)newValue, 1) ? (Command)newValue : null;
            updateCommand(removedCommand, addedCommand);
        } else {
            NodeHelper.updateDisabled(node);
        }
    }

    private void updateCommand(Command removedCommand, Command addedCommand) {
        if (removedCommand != null) {
            invokeCommand(removedCommand, false);
            invokeHandler(removedCommand, false);
        }

        if (addedCommand != null) {
            invokeCommand(addedCommand, true);
            invokeHandler(addedCommand, true);
        }
    }

    private void invokeCommand(Command command, boolean attach) {
        try {
            if (attach) {
                CommandHelper.attach(command, node);
            } else {
                CommandHelper.detach(command, node);
            }
        } catch (Throwable ex) {
            Thread thread = Thread.currentThread();
            thread.getUncaughtExceptionHandler().uncaughtException(thread, ex);
        }
    }

    private void invokeHandler(Command command, boolean attach) {
        List<CommandHandler> commandHandlers = NodeHelper.getCommandHandlers(node);
        if (commandHandlers == null || commandHandlers.isEmpty()) {
            return;
        }

        for (CommandHandler handler : commandHandlers) {
            try {
                if (attach) {
                    handler.onAttached(node, command);
                } else {
                    handler.onDetached(node, command);
                }
            } catch (Throwable ex) {
                Thread thread = Thread.currentThread();
                thread.getUncaughtExceptionHandler().uncaughtException(thread, ex);
            }
        }
    }

    private boolean contains(Command command, int n) {
        int c = 0;

        for (int i = 0, max = size(); i < max; ++i) {
            if (get(i).getCommand() == command && ++c > n) {
                return false;
            }
        }

        return c == n;
    }

}
