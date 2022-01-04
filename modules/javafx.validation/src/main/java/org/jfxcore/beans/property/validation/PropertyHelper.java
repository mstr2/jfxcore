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

package org.jfxcore.beans.property.validation;

import javafx.beans.property.Property;
import javafx.beans.property.validation.ConstrainedBooleanProperty;
import javafx.beans.property.validation.ConstrainedDoubleProperty;
import javafx.beans.property.validation.ConstrainedFloatProperty;
import javafx.beans.property.validation.ConstrainedIntegerProperty;
import javafx.beans.property.validation.ConstrainedListProperty;
import javafx.beans.property.validation.ConstrainedLongProperty;
import javafx.beans.property.validation.ConstrainedMapProperty;
import javafx.beans.property.validation.ConstrainedObjectProperty;
import javafx.beans.property.validation.ConstrainedProperty;
import javafx.beans.property.validation.ConstrainedSetProperty;
import javafx.beans.property.validation.ConstrainedStringProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedBooleanProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedDoubleProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedFloatProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedIntegerProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedListProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedLongProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedMapProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedObjectProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedSetProperty;
import javafx.beans.property.validation.ReadOnlyConstrainedStringProperty;
import java.util.List;

public class PropertyHelper {

    private PropertyHelper() {}

    @SuppressWarnings("rawtypes")
    private static final List<Class<? extends ConstrainedProperty>> PROPERTY_CLASSES = List.of(
        ConstrainedBooleanProperty.class, ConstrainedDoubleProperty.class, ConstrainedFloatProperty.class,
        ConstrainedIntegerProperty.class, ConstrainedLongProperty.class, ConstrainedStringProperty.class,
        ConstrainedObjectProperty.class, ConstrainedListProperty.class, ConstrainedSetProperty.class,
        ConstrainedMapProperty.class);

    @SuppressWarnings("rawtypes")
    private static final List<Class<? extends ReadOnlyConstrainedProperty>> READONLY_PROPERTY_CLASSES = List.of(
        ReadOnlyConstrainedBooleanProperty.class, ReadOnlyConstrainedDoubleProperty.class,
        ReadOnlyConstrainedFloatProperty.class, ReadOnlyConstrainedIntegerProperty.class,
        ReadOnlyConstrainedLongProperty.class, ReadOnlyConstrainedStringProperty.class,
        ReadOnlyConstrainedObjectProperty.class, ReadOnlyConstrainedListProperty.class,
        ReadOnlyConstrainedSetProperty.class, ReadOnlyConstrainedMapProperty.class);

    @SuppressWarnings("rawtypes")
    private static Class<? extends ReadOnlyConstrainedProperty> getPropertyClass(
            ReadOnlyConstrainedProperty<?, ?> property) {
        for (Class<? extends ReadOnlyConstrainedProperty> clazz :
                property instanceof ConstrainedProperty<?, ?> ? PROPERTY_CLASSES : READONLY_PROPERTY_CLASSES) {
            if (clazz.isInstance(property)) {
                return clazz;
            }
        }

        throw new IllegalArgumentException();
    }

    public static String toString(ReadOnlyConstrainedProperty<?, ?> property) {
        return toString(property, false);
    }

    public static String toString(ReadOnlyConstrainedProperty<?, ?> property, boolean valid) {
        Object bean = property.getBean();
        String name = property.getName();
        StringBuilder result = new StringBuilder(getPropertyClass(property).getSimpleName()).append(" [");

        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }

        if (name != null && !name.isBlank()) {
            result.append("name: ").append(name).append(", ");
        }

        if (property instanceof ConstrainedProperty<?, ?> constrainedProperty && constrainedProperty.isBound()) {
            result.append("bound, ");

            if (valid) {
                result.append("value: ").append(property.getValue());
            } else {
                result.append("invalid");
            }
        } else {
            result.append("value: ").append(property.getValue());
        }

        result.append("]");
        return result.toString();
    }

    public static RuntimeException cannotSetBoundProperty(Property<?> property) {
        return new RuntimeException(getBeanInfo(property) + "A bound value cannot be set.");
    }

    public static RuntimeException cannotBindNull(Property<?> property) {
        return new RuntimeException(getBeanInfo(property) + "Cannot bind to null.");
    }

    private static String getBeanInfo(Property<?> property) {
        Object bean = property.getBean();
        String name = property.getName();

        if (bean != null && name != null) {
            return bean.getClass().getSimpleName() + "." + name + ": ";
        }

        return "";
    }

}
