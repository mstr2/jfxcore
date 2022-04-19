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

import com.sun.javafx.scene.command.CommandPropertyImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.util.Incubating;

/**
 * Defines an object that can invoke a command.
 * <p>
 * When a command is installed on a command source, the command will "take over" the {@code disable}
 * property of the command source and bind it to its {@link Command#executableProperty()}.
 * If the command is executable, the command source is enabled; if the command is not executable, the
 * command source is disabled.
 * <p>
 * The command is invoked when the command source handles an {@link ActionEvent}.
 * For button-like controls, this usually happens when the control is clicked.
 *
 * @since JFXcore 18
 */
@Incubating
public interface CommandSource extends EventTarget {

    @SuppressWarnings("unchecked")
    default ObjectProperty<Command> commandProperty() {
        final String key = "javafx.scene.command.CommandSource.command";

        ObjectProperty<Command> property = (ObjectProperty<Command>)getProperties().get(key);
        if (property == null) {
            getProperties().put(key, property = new CommandPropertyImpl(this));
        }

        return property;
    }

    default Command getCommand() {
        return commandProperty().get();
    }

    default void setCommand(Command command) {
        commandProperty().set(command);
    }

    @SuppressWarnings("unchecked")
    default ObjectProperty<Object> commandParameterProperty() {
        final String key = "javafx.scene.command.CommandSource.commandParameter";

        ObjectProperty<Object> property = (ObjectProperty<Object>)getProperties().get(key);
        if (property == null) {
            getProperties().put(key, property = new ObjectPropertyBase<>() {
                @Override
                public Object getBean() {
                    return CommandSource.this;
                }

                @Override
                public String getName() {
                    return "commandParameter";
                }
            });
        }

        return property;
    }

    default Object getCommandParameter() {
        return commandParameterProperty().get();
    }

    default void setCommandParameter(Object parameter) {
        commandParameterProperty().set(parameter);
    }

    @SuppressWarnings("unchecked")
    default ObjectProperty<EventTarget> commandTargetProperty() {
        final String key = "javafx.scene.command.CommandSource.commandTarget";

        ObjectProperty<EventTarget> property = (ObjectProperty<EventTarget>)getProperties().get(key);
        if (property == null) {
            getProperties().put(key, property = new ObjectPropertyBase<>() {
                @Override
                public Object getBean() {
                    return CommandSource.this;
                }

                @Override
                public String getName() {
                    return "commandTarget";
                }
            });
        }

        return property;
    }

    default EventTarget getCommandTarget() {
        return commandTargetProperty().get();
    }

    default void setCommandTarget(EventTarget target) {
        commandTargetProperty().set(target);
    }

    ReadOnlyObjectProperty<Scene> sceneProperty();

    BooleanProperty disableProperty();

    ObservableMap<Object, Object> getProperties();

    <T extends Event> void addEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler);

    <T extends Event> void removeEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler);

}
