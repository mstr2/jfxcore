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

import javafx.beans.property.ObjectPropertyBase;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.command.Command;
import javafx.scene.command.CommandSource;
import javafx.scene.command.RoutedCommand;

public class CommandPropertyImpl extends ObjectPropertyBase<Command> {

    private final CommandSource bean;
    private Command oldCommand;
    private ActionEventHandler handler;

    public CommandPropertyImpl(CommandSource bean) {
        this.bean = bean;
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return "command";
    }

    @Override
    protected void invalidated() {
        Node node = (Node)bean;
        Command newCommand = get();

        if (oldCommand instanceof RoutedCommand routedCommand) {
            routedCommand.removeOwner(bean);
        }

        if (handler != null) {
            if (newCommand == null) {
                node.disableProperty().unbind();
                node.removeEventHandler(ActionEvent.ACTION, handler);
            }

            handler.updateExecutable();
        }

        if (newCommand != null) {
            handler = new ActionEventHandler(bean);
            node.disableProperty().bind(newCommand.executableProperty().not());
            node.addEventHandler(ActionEvent.ACTION, handler);
        }

        if (newCommand instanceof RoutedCommand routedCommand) {
            routedCommand.addOwner(bean);
        }

        oldCommand = newCommand;
    }

}
