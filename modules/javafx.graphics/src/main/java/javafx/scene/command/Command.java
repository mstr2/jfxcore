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

import com.sun.javafx.scene.command.CommandHelper;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;
import javafx.util.Incubating;

/**
 * Represents an operation that can be invoked in various ways, such as by clicking a button,
 * pressing a key, or typing a shortcut.
 * <p>
 * See the {@link javafx.scene.command Commanding} documentation for additional information.
 *
 * @see RelayCommand
 * @see TaskCommand
 * @see ServiceCommand
 * @since JFXcore 18
 */
@Incubating
public abstract class Command {

    static {
        CommandHelper.setAccessor(new CommandHelper.Accessor() {
            @Override
            public void attach(Command command, Node node) {
                command.onAttached(node);
            }

            @Override
            public void detach(Command command, Node node) {
                command.onDetached(node);
            }
        });
    }

    /**
     * Gets a property that indicates whether the command is currently executable.
     *
     * @return the {@code executable} property
     */
    public abstract ReadOnlyBooleanProperty executableProperty();

    /**
     * Indicates whether the command is currently executable.
     *
     * @return {@code true} if the command is executable, {@code false} otherwise
     */
    public boolean isExecutable() {
        return executableProperty().get();
    }

    /**
     * Executes the command.
     *
     * @param parameter the parameter that is passed to the command, or {@code null}
     * @throws IllegalStateException if the command is not executable
     */
    public abstract void execute(Object parameter);

    /**
     * Occurs when the command is bound to an event of a {@link Node}.
     * <p>
     * When the command is bound to multiple events of a single {@code Node}, this method is only invoked once.
     * <p>
     * Derived classes can override this method to implement custom logic.
     * For example, a command implementation could set a control's {@link javafx.scene.control.Labeled#textProperty()}
     * to a user-defined value. Implementing the {@code onAttached} and {@code onDetached} methods is an alternative
     * to using a {@link CommandHandler}. The major difference is that {@code CommandHandler} only applies to specific
     * nodes on which the {@code CommandHandler} is set, while overriding {@code onAttached} and {@code onDetached}
     * applies to all nodes to which this command is bound.
     *
     * @param node the node to which this command is bound
     */
    protected void onAttached(Node node) {}

    /**
     * Occurs when the command is unbound from an event of a {@link Node}.
     * <p>
     * This happens when the command is removed from its associated {@link EventBinding}, or if the
     * {@code EventBinding} is removed from the {@code Node}.
     * When the command is bound to multiple events of a single {@code Node}, this method is only invoked
     * after the last binding is removed.
     * <p>
     * Derived classes can override this method to roll back changes that were established
     * by the {@link #onAttached onAttached} method.
     *
     * @param node the node to which this command is bound
     */
    protected void onDetached(Node node) {}

}
