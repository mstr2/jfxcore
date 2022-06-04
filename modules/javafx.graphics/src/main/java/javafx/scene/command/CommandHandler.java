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

import javafx.scene.Node;
import javafx.util.Incubating;

/**
 * {@code CommandHandler} provides an extension mechanism that defines a piece of behavior or configuration
 * logic that is executed when a {@link Command} is attached to, or detached from, a {@link Node}.
 * <p>
 * In this example, a {@code CommandHandler} is used to automatically set a {@code Button}'s text to
 * the name of its attached command:
 *
 * <pre>{@code
 *     // Define a behavior to apply the command title to Button controls.
 *     public class MyCommandHandler implements CommandHandler {
 *         @Override
 *         public void onAttached(Node node, Command command) {
 *             if (node instanceof Button b && command instanceof TaskCommand<?> c) {
 *                 b.textProperty().bind(c.titleProperty());
 *             }
 *         }
 *
 *         @Override
 *         public void onDetached(Node node, Command command) {
 *             if (node instanceof Button b) {
 *                 b.textProperty().unbind();
 *             }
 *         }
 *     }
 *
 *     // Wire it up.
 *     var command = new TaskCommand("Custom Command");
 *     var handler = new MyCommandHandler();
 *     var button = new Button();
 *     button.setOnAction(new ActionEventBinding(command));
 *     button.setCommandHandler(handler);
 * }</pre>
 *
 * Note that the {@link Command} class defines overridable {@link Command#onAttached onAttached} and
 * {@link Command#onDetached onDetached} methods, which can also be used to implement behavior logic.
 * The difference between those methods and {@code CommandHandler} is in scope:
 * logic in a {@code Command} subclass generally applies to all nodes that use the command,
 * while {@code CommandHandler} only applies to specific nodes on which the handler is set.
 *
 * @since JFXcore 18
 */
@Incubating
public interface CommandHandler {

    /**
     * Occurs when the command is attached to a {@link Node} by binding it to one of the {@code Node}'s events.
     * <p>
     * When the command is bound to multiple events of a single {@code Node}, this method is only invoked once.
     * Note that this method will be invoked once for each {@code Node} to which this command is attached.
     *
     * @param node the node to which the {@code command} is attached
     * @param command the command
     */
    void onAttached(Node node, Command command);

    /**
     * Occurs when the command is detached from a {@link Node}.
     * <p>
     * This happens when the command is removed from its associated {@link EventBinding}, or if the
     * {@code EventBinding} is removed from the {@code Node}.
     * When the command is bound to multiple events of a single {@code Node}, the command is only detached
     * after the last binding is removed. Note that this method will be invoked once for each {@code Node}
     * from which this command is detached.
     *
     * @param node the node to which the {@code command} was attached
     * @param command the command
     */
    void onDetached(Node node, Command command);

}
