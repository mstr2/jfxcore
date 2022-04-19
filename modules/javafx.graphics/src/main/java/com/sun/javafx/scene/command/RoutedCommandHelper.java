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

import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.command.RoutedCommand;

public final class RoutedCommandHelper {

    private static Accessor accessor;

    private RoutedCommandHelper() {}

    public static void setAccessor(Accessor accessor) {
        RoutedCommandHelper.accessor = accessor;
    }

    public static void registerScene(RoutedCommand routedCommand, Scene scene) {
        accessor.registerScene(routedCommand, scene);
    }

    public static void unregisterScene(RoutedCommand routedCommand, Scene scene) {
        accessor.unregisterScene(routedCommand, scene);
    }

    public static boolean updateExecutable(RoutedCommand routedCommand, EventTarget target) {
        return accessor.updateExecutable(routedCommand, target);
    }

    public static Node getLastFocusOwner(RoutedCommand routedCommand) {
        return accessor.getLastFocusOwner(routedCommand);
    }

    public interface Accessor {
        void registerScene(RoutedCommand routedCommand, Scene scene);
        void unregisterScene(RoutedCommand routedCommand, Scene scene);
        boolean updateExecutable(RoutedCommand routedCommand, EventTarget target);
        Node getLastFocusOwner(RoutedCommand routedCommand);
    }

}
