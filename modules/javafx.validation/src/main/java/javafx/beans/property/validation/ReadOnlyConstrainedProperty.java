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

package javafx.beans.property.validation;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.validation.ValidationState;

/**
 * Defines methods and properties common to all read-only constrained properties.
 *
 * @param <T> data type
 * @param <E> error information type
 * @since JFXcore 18
 */
public interface ReadOnlyConstrainedProperty<T, E> extends ReadOnlyProperty<T> {

    /**
     * Indicates whether the property value is currently known to be valid.
     * <p>
     * The property value is valid if all constraint validators have successfully completed.
     */
    ReadOnlyBooleanProperty validProperty();

    /**
     * Gets the value of the {@link #validProperty() valid} property.
     */
    default boolean isValid() {
        return validProperty().get();
    }

    /**
     * Indicates whether the property value is currently known to be valid after the user has
     * significantly interacted with it. This information is only available when the constrained
     * property is bound to a scene graph node that provides user interaction information using
     * {@link ValidationState#setSource(Node, ReadOnlyConstrainedProperty)}.
     */
    ReadOnlyBooleanProperty userValidProperty();

    /**
     * Gets the value of the {@link #userValidProperty() userValid} property.
     */
    default boolean isUserValid() {
        return userValidProperty().get();
    }

    /**
     * Indicates whether the property value is currently known to be invalid.
     * <p>
     * The property value is invalid if at least one constraint has been violated, independently of
     * whether other constraint validators have already completed validation.
     */
    ReadOnlyBooleanProperty invalidProperty();

    /**
     * Gets the value of the {@link #invalidProperty() invalid} property.
     */
    default boolean isInvalid() {
        return invalidProperty().get();
    }

    ReadOnlyBooleanProperty userInvalidProperty();

    /**
     * Gets the value of the {@link #userInvalidProperty() userInvalid} property.
     */
    default boolean isUserInvalid() {
        return userInvalidProperty().get();
    }

    /**
     * Indicates whether the property value is currently being validated.
     */
    ReadOnlyBooleanProperty validatingProperty();

    /**
     * Gets the value of the {@link #validatingProperty() validating} property.
     */
    default boolean isValidating() {
        return validatingProperty().get();
    }

    /**
     * Contains a list of error information objects.
     *
     * <p>Error information is optional for constraints; the absence of error information does not
     * imply that the property is valid.
     */
    ReadOnlyListProperty<E> errorsProperty();

    /**
     * Gets the value of the {@link #errorsProperty() errors} property.
     */
    default ObservableList<E> getErrors() {
        return errorsProperty().get();
    }

    /**
     * Contains the last value that was successfully validated.
     * The constrained value is updated whenever validation completes without errors.
     */
    ReadOnlyProperty<T> constrainedValueProperty();

    /**
     * Gets the value of the {@link #constrainedValueProperty() constrainedValue} property.
     */
    default T getConstrainedValue() {
        return constrainedValueProperty().getValue();
    }

}
