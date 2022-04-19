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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.command.Command;
import javafx.scene.command.CommandSource;
import javafx.scene.command.RoutedCommand;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

final class ActionEventHandler implements EventHandler<ActionEvent> {

    private final CommandSource source;

    ActionEventHandler(CommandSource source) {
        this.source = source;
    }

    @Override
    public void handle(ActionEvent event) {
        Command command = source.getCommand();

        if (command instanceof RoutedCommand) {
            routedCall(this::executeRoutedCommand);
        } else if (command != null) {
            command.execute(source.getCommandParameter());
        }
    }

    void updateExecutable() {
        if (source.getCommand() instanceof RoutedCommand) {
            routedCall(this::queryRoutedCommandExecutable);
        }
    }

    private void routedCall(EventTargetCall call) {
        EventTarget target = source.getCommandTarget();
        if (target != null) {
            call.apply(target);
        } else {
            Scene scene;
            Node focusOwner = source.getCommand() instanceof RoutedCommand routedCommand ?
                RoutedCommandHelper.getLastFocusOwner(routedCommand) : null;

            if (focusOwner != null) {
                if (call.apply(focusOwner)) {
                    return;
                }

                Scene thisScene = source.sceneProperty().get();
                Window owner = thisScene != null ? getOwnerWindow(thisScene.getWindow()) : null;
                scene = owner != null ? owner.getScene() : null;
            } else {
                scene = source.sceneProperty().get();
            }

            while (scene != null) {
                focusOwner = scene.getFocusOwner();
                if (focusOwner != null && call.apply(focusOwner)) {
                    break;
                }

                Window owner = getOwnerWindow(scene.getWindow());
                scene = owner != null ? owner.getScene() : null;
            }
        }
    }

    private boolean executeRoutedCommand(EventTarget target) {
        if (source.getCommand() instanceof RoutedCommand command) {
            return command.execute(target, source.getCommandParameter());
        }

        return false;
    }

    private boolean queryRoutedCommandExecutable(EventTarget target) {
        if (source.getCommand() instanceof RoutedCommand command) {
            return RoutedCommandHelper.updateExecutable(command, target);
        }

        return false;
    }

    private Window getOwnerWindow(Window window) {
        return window instanceof PopupWindow popupWindow ? popupWindow.getOwnerWindow() : null;
    }

    private interface EventTargetCall {
        boolean apply(EventTarget target);
    }

}
