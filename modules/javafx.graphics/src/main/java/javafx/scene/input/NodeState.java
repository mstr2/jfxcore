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

package javafx.scene.input;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.util.Incubating;

/**
 * Allows nodes to indicate whether their value was changed as a result of user input.
 *
 * @since JFXcore 18
 */
@Incubating
public final class NodeState {

    private NodeState() {}

    private static final String USER_MODIFIED_KEY = NodeState.class.getName() + ".userModifiedProperty";

    /**
     * Returns the {@code userModified} attached property for the specified node, which indicates whether
     * the value of the node was changed as a result of user input.
     */
    public static BooleanProperty userModifiedProperty(Node node) {
        BooleanProperty property = (BooleanProperty)node.getProperties().get(USER_MODIFIED_KEY);
        if (property == null) {
            property = new SimpleBooleanProperty(NodeState.class, "userModified", false);
            node.getProperties().put(USER_MODIFIED_KEY, property);
        }

        return property;
    }

    /**
     * Returns the value of the {@link #userModifiedProperty(Node) userModified} attached property.
     */
    public static boolean isUserModified(Node node) {
        return node.hasProperties()
            && node.getProperties().containsKey(USER_MODIFIED_KEY)
            && userModifiedProperty(node).get();
    }

    /**
     * Sets the value of the {@link #userModifiedProperty(Node) userModified} attached property.
     */
    public static void setUserModified(Node node, boolean value) {
        userModifiedProperty(node).set(value);
    }

}
