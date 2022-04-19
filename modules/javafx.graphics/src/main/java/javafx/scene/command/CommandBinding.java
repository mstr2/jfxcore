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

import com.sun.javafx.scene.command.CommandBindingHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.util.Incubating;

/**
 * {@code CommandBindings} associate scene graph events with commands.
 *
 * @param <E> the event type
 *
 * @see KeyEventBinding
 * @see MouseEventBinding
 * @see RoutedCommandBinding
 *
 * @since JFXcore 18
 */
@Incubating
public abstract class CommandBinding<E extends Event> {

    static {
        CommandBindingHelper.setAccessor(new CommandBindingHelper.Accessor() {
            @Override
            public void setNode(CommandBinding<?> binding, Node node) {
                binding.setNode(node);
            }
        });
    }

    private final EventType<E> eventType;
    private final EventHandler<E> handler = this::handleEvent;
    private Node node;

    protected CommandBinding(EventType<E> eventType, Command command) {
        if (eventType == null) {
            throw new IllegalArgumentException("eventType cannot be null");
        }

        this.eventType = eventType;
        this.command.set(command);
    }

    /**
     * The command that is bound by this {@code CommandBinding}.
     */
    private final ObjectProperty<Command> command = new ObjectPropertyBase<>() {
        Command oldValue;

        @Override
        public Object getBean() {
            return CommandBinding.this;
        }

        @Override
        public String getName() {
            return "command";
        }

        @Override
        protected void invalidated() {
            Command newValue = get();
            handleCommandChanged(oldValue, newValue);
            oldValue = newValue;
        }
    };

    public ObjectProperty<Command> commandProperty() {
        return command;
    }

    public Command getCommand() {
        return command.get();
    }

    public void setCommand(Command command) {
        this.command.set(command);
    }

    /**
     * The parameter that is passed to the {@link #commandProperty command} when the command is executed.
     */
    private final ObjectProperty<Object> commandParameter = new SimpleObjectProperty<>(this, "commandParameter");

    public ObjectProperty<Object> commandParameterProperty() {
        return commandParameter;
    }

    public Object getCommandParameter() {
        return commandParameter.get();
    }

    public void setCommandParameter(Object parameter) {
        this.commandParameter.set(parameter);
    }

    /**
     * Indicates whether this binding handles its associated event in the <em>capture</em> or <em>bubble</em>
     * phase of event delivery: if {@code filter} is {@code true}, the event is handled in the <em>capture</em>
     * phase, otherwise it is handled in the <em>bubble</em> phase.
     */
    private final BooleanProperty filter = new SimpleBooleanProperty(this, "filter") {
        boolean oldValue;

        @Override
        protected void invalidated() {
            if (node != null) {
                boolean newValue = get();

                if (oldValue && !newValue) {
                    node.removeEventFilter(eventType, handler);
                    node.addEventHandler(eventType, handler);
                } else if (!oldValue && newValue) {
                    node.removeEventHandler(eventType, handler);
                    node.addEventFilter(eventType, handler);
                }

                oldValue = newValue;
            }
        }
    };

    public BooleanProperty filterProperty() {
        return filter;
    }

    public boolean isFilter() {
        return filter.get();
    }

    public void setFilter(boolean filter) {
        this.filter.set(filter);
    }

    protected abstract void handleEvent(E event);

    protected void handleCommandChanged(Command oldCommand, Command newCommand) {}

    protected Node getNode() {
        return node;
    }

    private void setNode(Node node) {
        if (this.node == node) {
            return;
        }

        if (this.node != null) {
            if (node != null) {
                throw new IllegalArgumentException("Binding is already set on another node.");
            }

            if (isFilter()) {
                this.node.removeEventFilter(eventType, handler);
            } else {
                this.node.removeEventHandler(eventType, handler);
            }
        }

        if (node != null) {
            if (isFilter()) {
                node.addEventFilter(eventType, handler);
            } else {
                node.addEventHandler(eventType, handler);
            }
        }

        this.node = node;
    }

}
