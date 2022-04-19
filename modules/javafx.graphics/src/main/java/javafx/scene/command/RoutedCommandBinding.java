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

package javafx.scene.command;

import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Incubating;

/**
 * A {@link CommandBinding} that invokes a user-specified method or command
 * if it receives a {@link RoutedCommand}.
 *
 * @since JFXcore 18
 */
@Incubating
public final class RoutedCommandBinding extends CommandBinding<RoutedCommandEvent> {

    @SuppressWarnings("FieldCanBeLocal")
    private final InvalidationListener invalidationListener;
    private final Runnable executeMethod;
    private final Command executeCommand;
    private ObservableValue<Boolean> executable;

    /**
     * Creates a new {@code RoutedCommandBinding} instance that executes a {@link Runnable}
     * when a {@link Command} is received.
     *
     * @param command the {@link Command} that this binding should handle
     * @param execute the code that should be executed when the {@link RoutedCommand} is received
     */
    public RoutedCommandBinding(
            @NamedArg("command") Command command,
            @NamedArg("execute") Runnable execute) {
        this(command, execute, null);
    }

    /**
     * Creates a new {@code RoutedCommandBinding} instance that executes a {@link Runnable}
     * when a {@link Command} is received.
     *
     * @param command the {@link Command} that this binding should handle
     * @param execute the code that should be executed when the {@link Command} is received
     * @param executable whether the {@code execute} code is currently executable
     */
    public RoutedCommandBinding(
            @NamedArg("command") Command command,
            @NamedArg("execute") Runnable execute,
            @NamedArg("executable") ObservableValue<Boolean> executable) {
        super(RoutedCommandEvent.ANY, command);

        this.executeMethod = execute;
        this.executeCommand = null;
        this.executable = executable;

        if (executable != null) {
            this.invalidationListener = observable -> {
                if (getCommand() instanceof RoutedCommand routedCommand
                        && routedCommand.isExecutable() != this.executable.getValue()) {
                    routedCommand.updateExecutable();
                }
            };

            executable.addListener(new WeakInvalidationListener(invalidationListener));
        } else {
            this.invalidationListener = null;
        }
    }

    /**
     * Creates a new {@code RoutedCommandBinding} instance that executes a {@link Command}
     * when a {@link RoutedCommand} is received.
     *
     * @param command the {@link RoutedCommand} that this binding should handle
     * @param relayTo the command that should be executed when the {@link RoutedCommand} is received
     */
    public RoutedCommandBinding(
            @NamedArg("command") Command command,
            @NamedArg("relayTo") Command relayTo) {
        super(RoutedCommandEvent.ANY, command);

        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }

        if (relayTo == null) {
            throw new IllegalArgumentException("relayTo cannot be null");
        }

        this.executeMethod = null;
        this.executeCommand = command;
        this.invalidationListener = observable -> {
            if (getCommand() instanceof RoutedCommand routedCommand
                    && routedCommand.isExecutable() != this.executable.getValue()) {
                routedCommand.updateExecutable();
            }
        };

        this.executable.addListener(new WeakInvalidationListener(invalidationListener));
    }

    @Override
    protected void handleCommandChanged(Command oldCommand, Command newCommand) {
        if (newCommand != null) {
            executable = newCommand.executableProperty();
        } else {
            executable = null;
        }

        if (newCommand instanceof RoutedCommand routedCommand) {
            routedCommand.updateExecutable();
        }
    }

    @Override
    protected void handleEvent(RoutedCommandEvent event) {
        if (event.getCommand() == getCommand()) {
            if (event.getEventType() == RoutedCommandEvent.EXECUTE) {
                if (executeMethod != null) {
                    executeMethod.run();
                } else if (executeCommand instanceof RoutedCommand routedCommand) {
                    routedCommand.execute(getNode(), getCommandParameter());
                } else if (executeCommand != null) {
                    executeCommand.execute(getCommandParameter());
                }
            } else if (event.getEventType() == RoutedCommandEvent.EXECUTABLE) {
                event.setExecutable(executable == null || executable.getValue() == Boolean.TRUE);
            }

            event.consume();
        }
    }

}
