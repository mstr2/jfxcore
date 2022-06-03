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
import javafx.scene.Node;
import javafx.scene.command.Command;
import javafx.scene.command.CommandHandler;
import javafx.scene.command.EventBinding;
import java.util.ArrayList;
import java.util.List;

public final class CommandHandlerList extends ArrayList<CommandHandler> {

    private final Node node;

    public CommandHandlerList(Node node) {
        super(2);
        this.node = node;
    }

    @Override
    public boolean add(CommandHandler handler) {
        if (handler == null) {
            throw new NullPointerException("CommandHandler cannot be null");
        }

        if (contains(handler)) {
            throw new IllegalStateException("CommandHandler is already set on " + node);
        }

        super.add(handler);

        List<EventBinding<?>> eventBindings = NodeHelper.getEventBindings(node);
        if (eventBindings != null) {
            for (EventBinding<?> binding : eventBindings) {
                Command command = binding.getCommand();
                if (command != null) {
                    try {
                        handler.onAttached(node, command);
                    } catch (Throwable ex) {
                        Thread thread = Thread.currentThread();
                        thread.getUncaughtExceptionHandler().uncaughtException(thread, ex);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (super.remove(o)) {
            onDetached((CommandHandler)o);
            return true;
        }

        return false;
    }

    private void onDetached(CommandHandler handler) {
        List<EventBinding<?>> eventBindings = NodeHelper.getEventBindings(node);
        if (eventBindings != null) {
            for (EventBinding<?> binding : eventBindings) {
                Command command = binding.getCommand();
                if (command != null) {
                    try {
                        handler.onDetached(node, command);
                    } catch (Throwable ex) {
                        Thread thread = Thread.currentThread();
                        thread.getUncaughtExceptionHandler().uncaughtException(thread, ex);
                    }
                }
            }
        }
    }

}
