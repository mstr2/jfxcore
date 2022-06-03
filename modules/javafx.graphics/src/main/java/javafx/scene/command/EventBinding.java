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

import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.scene.command.EventBindingHelper;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Incubating;
import java.util.Objects;

/**
 * {@code EventBinding} is a special type of {@link EventHandler} that binds a {@link Command} to an event.
 * When the event binding receives the specified event, the command is invoked.
 * <p>
 * Event bindings should not be used with the lower-level {@link Node#addEventHandler addEventHandler} or
 * {@link Node#addEventFilter addEventFilter} APIs, but instead with event handler properties like
 * {@link Node#setOnKeyPressed setOnKeyPressed} or
 * {@link javafx.scene.control.ButtonBase#setOnAction setOnAction}.
 * <p>
 * In this example, {@link ActionEventBinding} is used to bind a command to {@code ActionEvent}.
 * <pre>{@code
 *    var command = new RelayCommand<>(() -> System.out.println("Command was executed"));
 *    var button = new Button();
 *    button.setOnAction(new ActionEventBinding(command));
 * }</pre>
 *
 * @see ActionEventBinding
 * @see KeyEventBinding
 * @see MouseEventBinding
 * @see TouchEventBinding
 *
 * @param <E> the event type
 * @since JFXcore 18
 */
@Incubating
@DefaultProperty("command")
public abstract class EventBinding<E extends Event> implements EventHandler<E> {

    static {
        EventBindingHelper.setAccessor(new EventBindingHelper.Accessor() {
            @Override
            public ObservableBooleanValue getDisabled(EventBinding<?> binding) {
                return binding.disabled;
            }

            @Override
            public Node getNode(EventBinding<?> binding) {
                return binding.node;
            }

            @Override
            public void setNode(EventBinding<?> binding, Node node) {
                binding.node = node;
            }
        });
    }

    private Node node;

    private class DisabledValue implements ObservableBooleanValue, InvalidationListener {
        ExpressionHelper<Boolean> helper;
        boolean disabledWhenExecuting = true;
        boolean executing;
        boolean executable;

        void set(boolean executable, boolean executing) {
            boolean changed = this.executable != executable || this.executing != executing;
            this.executable = executable;
            this.executing = executing;

            if (changed) {
                ExpressionHelper.fireValueChangedEvent(helper);
            }
        }

        @Override
        public boolean get() {
            return !executable || (disabledWhenExecuting && executing);
        }

        @Override
        public Boolean getValue() {
            return get();
        }

        @Override
        public void invalidated(Observable observable) {
            boolean value = ((ObservableBooleanValue)observable).get();

            if (observable == EventBinding.this.disabledWhenExecuting) {
                if (disabledWhenExecuting != value) {
                    disabledWhenExecuting = value;
                    ExpressionHelper.fireValueChangedEvent(helper);
                }
            } else {
                Command command = getCommand();
                if (command != null) {
                    if (observable == command.executableProperty()) {
                        if (executable != value) {
                            executable = value;
                            ExpressionHelper.fireValueChangedEvent(helper);
                        }
                    } else if (command instanceof AsyncCommand c && observable == c.executingProperty()) {
                        if (executing != value) {
                            executing = value;
                            ExpressionHelper.fireValueChangedEvent(helper);
                        }
                    }
                }
            }
        }

        @Override
        public void addListener(InvalidationListener listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ChangeListener<? super Boolean> listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(ChangeListener<? super Boolean> listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }
    }

    private final DisabledValue disabled = new DisabledValue();

    private class CommandProperty extends ObjectPropertyBase<Command> {
        Command currentValue;

        @Override
        public Object getBean() {
            return EventBinding.this;
        }

        @Override
        public String getName() {
            return "command";
        }

        @Override
        protected void invalidated() {
            boolean newExecutable = false;
            boolean newExecuting = false;

            if (currentValue != null) {
                currentValue.executableProperty().removeListener(disabled);
            }

            if (currentValue instanceof AsyncCommand command) {
                command.executingProperty().removeListener(disabled);
            }

            currentValue = get();

            if (currentValue != null) {
                currentValue.executableProperty().addListener(disabled);
                newExecutable = currentValue.isExecutable();
            }

            if (currentValue instanceof AsyncCommand command) {
                command.executingProperty().addListener(disabled);
                newExecuting = command.isExecuting();
            }

            disabled.set(newExecutable, newExecuting);
        }
    }

    /**
     * Initializes a new {@code EventBinding} instance.
     */
    protected EventBinding() {}

    /**
     * Initializes a new {@code EventBinding} instance.
     *
     * @param command the command that is bound to the event
     */
    protected EventBinding(Command command) {
        setCommand(command);
    }

    /**
     * Initializes a new {@code EventBinding} instance.
     *
     * @param command the command that is bound to the event
     * @param parameter the parameter that is passed to the command
     */
    protected EventBinding(Command command, Object parameter) {
        setCommand(command);
        setParameter(parameter);
    }

    /**
     * Indicates whether the {@link Node} to which this {@code EventBinding} applies will be
     * {@link Node#disabledProperty() disabled} for the duration of the command execution.
     *
     * @defaultValue true
     */
    private final BooleanProperty disabledWhenExecuting = new SimpleBooleanProperty(this, "disabledWhenExecuting", true) {
        @Override
        protected void invalidated() {
            disabled.invalidated(this);
        }
    };

    public final BooleanProperty disabledWhenExecutingProperty() {
        return disabledWhenExecuting;
    }

    public final boolean isDisabledWhenExecuting() {
        return disabledWhenExecuting.get();
    }

    public final void setDisabledWhenExecuting(boolean value) {
        disabledWhenExecuting.set(value);
    }

    /**
     * The command that is invoked by this {@code EventBinding}.
     *
     * @defaultValue {@code null}
     */
    private final CommandProperty command = new CommandProperty();

    public final ObjectProperty<Command> commandProperty() {
        return command;
    }

    public final Command getCommand() {
        return command.get();
    }

    public final void setCommand(Command command) {
        this.command.set(command);
    }

    /**
     * The parameter that is passed to the {@link Command#execute(Object)} method when
     * the command is invoked.
     *
     * @defaultValue {@code null}
     */
    private final ObjectProperty<Object> parameter = new SimpleObjectProperty<>(this, "parameter");

    public final ObjectProperty<Object> parameterProperty() {
        return parameter;
    }

    public final Object getParameter() {
        return parameter.get();
    }

    public final void setParameter(Object parameter) {
        this.parameter.set(parameter);
    }

    @Override
    public final void handle(E event) {
        if (handleEvent(event)) {
            Command command = getCommand();
            if (command != null && command.isExecutable()) {
                event.consume();
                if (command instanceof AsyncCommand ac && ac.isExecuting()) {
                    ac.cancel();
                } else {
                    command.execute(getParameter());
                }
            }
        }
    }

    /**
     * Determines whether this {@code EventBinding} will handle the specified event.
     * <p>
     * Derived classes should override this method to inspect the event and return {@code true}
     * if the event should be handled by this {@code EventBinding}.
     *
     * @return {@code true} if the event should be handled, {@code false} otherwise
     */
    protected boolean handleEvent(E event) {
        return true;
    }

    static <T> boolean matches(ObjectProperty<T> property, T value) {
        return property == null || Objects.equals(property.get(), value);
    }

}
