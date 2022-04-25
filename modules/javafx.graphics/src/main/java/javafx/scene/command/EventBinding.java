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

import com.sun.javafx.scene.EventBindingHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;

public abstract class EventBinding<E extends Event> implements EventHandler<E> {

    static {
        EventBindingHelper.setAccessor(new EventBindingHelper.Accessor() {
            @Override
            public void initialize(EventBinding<?> binding, EventTarget target) {
                binding.initialize(target);
            }
        });
    }

    private static class NotExecutableProperty extends ReadOnlyBooleanPropertyBase implements InvalidationListener {
        boolean value;

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean get() {
            return value;
        }

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }

        @Override
        public void invalidated(Observable observable) {
            boolean newValue = !((ObservableBooleanValue)observable).get();
            if (value != newValue) {
                value = newValue;
                fireValueChangedEvent();
            }
        }
    }

    private class CommandProperty extends ObjectPropertyBase<Command> {
        final NotExecutableProperty notExecutable = new NotExecutableProperty();
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
            boolean newNotExecutable = false;

            if (currentValue != null) {
                currentValue.executableProperty().removeListener(notExecutable);
            }

            currentValue = get();

            if (currentValue != null) {
                currentValue.executableProperty().addListener(notExecutable);
                newNotExecutable = !currentValue.isExecutable();
            }

            if (notExecutable.value != newNotExecutable) {
                notExecutable.value = newNotExecutable;
                notExecutable.fireValueChangedEvent();
            }
        }
    }

    private final CommandProperty command = new CommandProperty();
    public final ObjectProperty<Command> commandProperty() { return command; }
    public final Command getCommand() { return command.get(); }
    public final void setCommand(Command command) { this.command.set(command); }

    public final ObjectProperty<Object> parameter = new SimpleObjectProperty<>(this, "parameter");
    public final ObjectProperty<Object> parameterProperty() { return parameter; }
    public final Object getParameter() { return parameter.get(); }
    public final void setParameter(Object parameter) { this.parameter.set(parameter); }

    @Override
    public final void handle(E event) {
        if (handleEvent(event)) {
            Command command = getCommand();
            if (command != null && command.isExecutable()) {
                event.consume();
                command.execute(getParameter());
            }
        }
    }

    protected void initialize(EventTarget eventTarget) {
        if (eventTarget instanceof Node node && !node.disableProperty().isBound()) {
            node.disableProperty().bind(command.notExecutable);
        }
    }

    protected boolean handleEvent(E event) {
        return true;
    }

}
