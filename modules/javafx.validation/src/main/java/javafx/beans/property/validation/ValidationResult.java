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
 * Represents the result of an invocation of a {@link Validator} or {@link AsyncValidator}.
 * <p>
 * A validator can choose to add an error information object to its {@code ValidationResult}
 * if the value was not valid. Constrained property implementations will surface error information
 * objects of their constraint validators in {@link ReadOnlyConstrainedProperty#errorsProperty()}.
 *
 * @param <E> error information type
 * @since JFXcore 18
 */
public class ValidationResult<E> {

    private static final ValidationResult<?> VALID = new ValidationResult<>(true);
    private static final ValidationResult<?> INVALID = new ValidationResult<>(false);

    /**
     * Returns a valid {@code ValidationResult}.
     *
     * @param <E> error information type
     * @return a valid {@code ValidationResult}
     */
    @SuppressWarnings("unchecked")
    public static <E> ValidationResult<E> valid() {
        return (ValidationResult<E>)VALID;
    }

    /**
     * Returns an invalid {@code ValidationResult}.
     *
     * @param <E> error information type
     * @return an invalid {@code ValidationResult}
     */
    @SuppressWarnings("unchecked")
    public static <E> ValidationResult<E> invalid() {
        return (ValidationResult<E>)INVALID;
    }

    private final boolean valid;
    private final E errorInfo;

    /**
     * Creates a new instance of the {@code ValidationResult} class.
     *
     * @param valid indicates whether the validated value is valid
     */
    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.errorInfo = null;
    }

    /**
     * Creates a new instance of the {@code ValidationResult} class.
     *
     * @param valid indicates whether the validated value is valid
     * @param errorInfo an error information object
     */
    public ValidationResult(boolean valid, E errorInfo) {
        this.valid = valid;
        this.errorInfo = errorInfo;
    }

    /**
     * Indicates whether the validated value is valid.
     *
     * @return {@code true} if the value is valid, {@code false} otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns the error information object.
     *
     * @return the error information object or {@code null}
     */
    public E getErrorInfo() {
        return errorInfo;
    }

}
