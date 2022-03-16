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

package org.jfxcore.validation;

import javafx.beans.property.Property;
import javafx.validation.property.ConstrainedBooleanProperty;
import javafx.validation.property.ConstrainedBooleanPropertyBase;
import javafx.validation.property.ConstrainedDoubleProperty;
import javafx.validation.property.ConstrainedDoublePropertyBase;
import javafx.validation.property.ConstrainedFloatProperty;
import javafx.validation.property.ConstrainedFloatPropertyBase;
import javafx.validation.property.ConstrainedIntegerProperty;
import javafx.validation.property.ConstrainedIntegerPropertyBase;
import javafx.validation.property.ConstrainedListProperty;
import javafx.validation.property.ConstrainedListPropertyBase;
import javafx.validation.property.ConstrainedLongProperty;
import javafx.validation.property.ConstrainedLongPropertyBase;
import javafx.validation.property.ConstrainedMapProperty;
import javafx.validation.property.ConstrainedMapPropertyBase;
import javafx.validation.property.ConstrainedObjectProperty;
import javafx.validation.property.ConstrainedObjectPropertyBase;
import javafx.validation.property.ConstrainedProperty;
import javafx.validation.property.ConstrainedSetProperty;
import javafx.validation.property.ConstrainedSetPropertyBase;
import javafx.validation.property.ConstrainedStringProperty;
import javafx.validation.property.ConstrainedStringPropertyBase;
import javafx.validation.property.ReadOnlyConstrainedBooleanProperty;
import javafx.validation.property.ReadOnlyConstrainedDoubleProperty;
import javafx.validation.property.ReadOnlyConstrainedFloatProperty;
import javafx.validation.property.ReadOnlyConstrainedIntegerProperty;
import javafx.validation.property.ReadOnlyConstrainedListProperty;
import javafx.validation.property.ReadOnlyConstrainedLongProperty;
import javafx.validation.property.ReadOnlyConstrainedMapProperty;
import javafx.validation.property.ReadOnlyConstrainedObjectProperty;
import javafx.validation.property.ReadOnlyConstrainedProperty;
import javafx.validation.property.ReadOnlyConstrainedSetProperty;
import javafx.validation.property.ReadOnlyConstrainedStringProperty;
import java.util.List;

public class PropertyHelper {

    private PropertyHelper() {}

    private static Accessor<?> booleanAccessor;
    private static Accessor<?> integerAccessor;
    private static Accessor<?> longAccessor;
    private static Accessor<?> floatAccessor;
    private static Accessor<?> doubleAccessor;
    private static Accessor<?> stringAccessor;
    private static Accessor<?> objectAccessor;
    private static Accessor<?> listAccessor;
    private static Accessor<?> setAccessor;
    private static Accessor<?> mapAccessor;

    public static void setBooleanAccessor(Accessor<?> accessor) {
        booleanAccessor = accessor;
    }

    public static void setIntegerAccessor(Accessor<?> accessor) {
        integerAccessor = accessor;
    }

    public static void setLongAccessor(Accessor<?> accessor) {
        longAccessor = accessor;
    }

    public static void setFloatAccessor(Accessor<?> accessor) {
        floatAccessor = accessor;
    }

    public static void setDoubleAccessor(Accessor<?> accessor) {
        doubleAccessor = accessor;
    }

    public static void setStringAccessor(Accessor<?> accessor) {
        stringAccessor = accessor;
    }

    public static void setObjectAccessor(Accessor<?> accessor) {
        objectAccessor = accessor;
    }

    public static void setListAccessor(Accessor<?> accessor) {
        listAccessor = accessor;
    }

    public static void setSetAccessor(Accessor<?> accessor) {
        setAccessor = accessor;
    }

    public static void setMapAccessor(Accessor<?> accessor) {
        mapAccessor = accessor;
    }

    @SuppressWarnings("unchecked")
    private static <T, D> Accessor<T> getAccessor(ReadOnlyConstrainedProperty<T, D> property) {
        if (property instanceof ConstrainedBooleanPropertyBase<?>) {
            return (Accessor<T>)booleanAccessor;
        }

        if (property instanceof ConstrainedDoublePropertyBase<?>) {
            return (Accessor<T>)doubleAccessor;
        }

        if (property instanceof ConstrainedObjectPropertyBase<?, ?>) {
            return (Accessor<T>)objectAccessor;
        }

        if (property instanceof ConstrainedStringPropertyBase<?>) {
            return (Accessor<T>)stringAccessor;
        }

        if (property instanceof ConstrainedIntegerPropertyBase<?>) {
            return (Accessor<T>)integerAccessor;
        }

        if (property instanceof ConstrainedListPropertyBase<?, ?>) {
            return (Accessor<T>)listAccessor;
        }

        if (property instanceof ConstrainedSetPropertyBase<?, ?>) {
            return (Accessor<T>)setAccessor;
        }

        if (property instanceof ConstrainedMapPropertyBase<?, ?, ?>) {
            return (Accessor<T>)mapAccessor;
        }

        if (property instanceof ConstrainedLongPropertyBase<?>) {
            return (Accessor<T>)longAccessor;
        }

        if (property instanceof ConstrainedFloatPropertyBase<?>) {
            return (Accessor<T>)floatAccessor;
        }

        return null;
    }

    public static <T, D> ValidationHelper<T, D> getValidationHelper(ReadOnlyConstrainedProperty<T, D> property) {
        Accessor<T> accessor = getAccessor(property);
        return accessor != null ? accessor.getValidationHelper(property) : null;
    }

    public static <T, D> T readValue(ReadOnlyConstrainedProperty<T, D> property) {
        return getAccessor(property).readValue(property);
    }

    public interface Accessor<T> {
        <D> ValidationHelper<T, D> getValidationHelper(ReadOnlyConstrainedProperty<T, D> property);
        <D> T readValue(ReadOnlyConstrainedProperty<T, D> property);
    }

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
        List<? extends Class<? extends ReadOnlyConstrainedProperty>> propertyClasses =
            property instanceof ConstrainedProperty<?, ?> ? PROPERTY_CLASSES : READONLY_PROPERTY_CLASSES;

        for (Class<? extends ReadOnlyConstrainedProperty> clazz : propertyClasses) {
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
        return new NullPointerException(getBeanInfo(property) + "Cannot bind to null.");
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
