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

/**
 * Defines a constraint validator.
 *
 * @param <T> value type
 * @param <E> error information type
 * @since JFXcore 18
 */
public interface Validator<T, E> {

    /**
     * Determines whether the specified value is valid.
     * <p>
     * If the value is not valid, the returned {@link ValidationResult} may contain an error
     * information object of type {@code E}, which contains application-specified information about
     * the constraint violation. If the error information object is not {@code null}, it will be
     * added to the {@link ReadOnlyConstrainedProperty#errorsProperty()} of the property that
     * invoked this validator.
     *
     * @param value the value to be tested
     * @return the {@link ValidationResult}
     */
    ValidationResult<E> validate(T value);

}
