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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.util.Incubating;

/**
 * Base class for bindings that listen for input events on a {@link Node}
 * and execute a command if the specified event was received.
 *
 * @param <E> the event type
 *
 * @see KeyEventBinding
 * @see MouseEventBinding
 *
 * @since JFXcore 18
 */
@Incubating
public abstract class InputEventBinding<E extends Event> extends CommandBinding<E> {

    protected InputEventBinding(EventType<E> eventType, Command command) {
        super(eventType, command);
    }

    /**
     * Indicates whether this {@code InputEventBinding} consumes its associated event.
     *
     * @defaultValue false
     */
    private final BooleanProperty consume = new SimpleBooleanProperty(this, "consume");

    public BooleanProperty consumeProperty() {
        return consume;
    }

    public boolean isConsume() {
        return consume.get();
    }

    public void setConsume(boolean consume) {
        this.consume.set(consume);
    }

    /**
     * Fires the command. If the command is not executable, this method is a no-op.
     *
     * @param event the event that caused the command to be invoked
     */
    protected void fireCommand(E event) {
        Command command = getCommand();

        if (command != null && command.isExecutable()) {
            if (command instanceof RoutedCommand) {
                ((RoutedCommand)command).execute(getNode(), getCommandParameter());
            } else {
                command.execute(getCommandParameter());
            }

            if (isConsume()) {
                event.consume();
            }
        }
    }

}
