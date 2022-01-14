/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

/**
 * Allows nodes to indicate whether their value was changed as a result of user input.
 *
 * @since JFXcore 18
 */
public final class NodeState {

    private NodeState() {}

    private static final String USER_MODIFIED_KEY = NodeState.class.getName() + ".userModifiedProperty";

    @SuppressWarnings("unchecked")
    public static <T> Property<T> defaultValueProperty(Property<T> property) {
        if (!(property.getBean() instanceof Node bean)) {
            throw new IllegalArgumentException("Property must be defined on a scene graph node.");
        }

        String name = property.getName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Property name cannot be null or empty.");
        }

        String key = name + "-defaultValue";
        Property<T> defaultValueProperty = (Property<T>)bean.getProperties().get(key);
        if (defaultValueProperty == null) {
            defaultValueProperty = new SimpleObjectProperty<>(bean, name, property.getValue());
        }

        return defaultValueProperty;
    }

    public static <T> T getDefaultValue(Property<T> property) {
        return defaultValueProperty(property).getValue();
    }

    public static <T> void setDefaultValue(Property<T> property, T value) {
        defaultValueProperty(property).setValue(value);
    }

    /**
     * Returns the {@code userModified} attached property for the specified node, which indicates whether
     * the value of the node was changed as a result of user input.
     */
    public static BooleanProperty userModifiedProperty(Node node) {
        BooleanProperty property = (BooleanProperty)node.getProperties().get(USER_MODIFIED_KEY);
        if (property == null) {
            property = new SimpleBooleanProperty(node, "userModified", false);
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
