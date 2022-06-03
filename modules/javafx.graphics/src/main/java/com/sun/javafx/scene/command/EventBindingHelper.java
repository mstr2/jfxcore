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

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.command.EventBinding;

public final class EventBindingHelper {

    private EventBindingHelper() {}

    private static Accessor accessor;

    public static void setAccessor(Accessor accessor) {
        EventBindingHelper.accessor = accessor;
    }

    public static ObservableBooleanValue getDisabled(EventBinding<?> binding) {
        return accessor.getDisabled(binding);
    }

    public static Node getNode(EventBinding<?> binding) {
        return accessor.getNode(binding);
    }

    public static void setNode(EventBinding<?> binding, Node node) {
        accessor.setNode(binding, node);
    }

    public interface Accessor {
        ObservableBooleanValue getDisabled(EventBinding<?> binding);
        Node getNode(EventBinding<?> binding);
        void setNode(EventBinding<?> binding, Node node);
    }

}
