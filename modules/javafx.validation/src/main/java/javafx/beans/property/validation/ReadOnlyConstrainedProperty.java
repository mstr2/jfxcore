/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  JFXcore designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

/**
 * Defines properties common to all read-only constrained properties.
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

    ReadOnlyBooleanProperty userValidProperty();

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
     * {@inheritDoc}
     *
     * @return {@code true} if at least one constraint has been violated, independently of whether other
     * constraint validators have already completed validation; {@code false} otherwise.
     */
    default boolean isInvalid() {
        return invalidProperty().get();
    }

    ReadOnlyBooleanProperty userInvalidProperty();

    default boolean isUserInvalid() {
        return userInvalidProperty().get();
    }

    /**
     * Indicates whether the property value is currently being validated.
     */
    ReadOnlyBooleanProperty validatingProperty();

    /**
     * Returns whether the property value is currently being validated.
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
     * Contains a list of error information objects.
     *
     * <p>Error information is optional for constraints; the absence of error information does not
     * imply that the property is valid.
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
     * Gets the last value that was successfully validated.
     * The constrained value is updated whenever validation completes without errors.
     */
    default T getConstrainedValue() {
        return constrainedValueProperty().getValue();
    }

}
