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
 * Represents the result of an invocation of a {@link Validator}.
 * <p>
 * A validator can choose to add an application-specified diagnostic object to the returned {@code ValidationResult}.
 * Diagnostic objects of invalid results will be surfaced as errors in {@link ReadOnlyConstrainedProperty#errorsProperty()},
 * while diagnostic objects of valid results will be surfaced as warnings in {@link ReadOnlyConstrainedProperty#warningsProperty()}.
 *
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
public class ValidationResult<D> {

    private static final ValidationResult<?> VALID = new ValidationResult<>(true);
    private static final ValidationResult<?> INVALID = new ValidationResult<>(false);

    /**
     * Returns a valid {@code ValidationResult}.
     *
     * @param <D> diagnostic type
     * @return a valid {@code ValidationResult}
     */
    @SuppressWarnings("unchecked")
    public static <D> ValidationResult<D> valid() {
        return (ValidationResult<D>)VALID;
    }

    /**
     * Returns an invalid {@code ValidationResult}.
     *
     * @param <D> diagnostic type
     * @return an invalid {@code ValidationResult}
     */
    @SuppressWarnings("unchecked")
    public static <D> ValidationResult<D> invalid() {
        return (ValidationResult<D>)INVALID;
    }

    private final boolean valid;
    private final D diagnostic;

    /**
     * Creates a new instance of the {@code ValidationResult} class.
     *
     * @param valid indicates whether the validated value is valid
     */
    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.diagnostic = null;
    }

    /**
     * Creates a new instance of the {@code ValidationResult} class.
     *
     * @param valid indicates whether the validated value is valid
     * @param diagnostic a diagnostic object
     */
    public ValidationResult(boolean valid, D diagnostic) {
        this.valid = valid;
        this.diagnostic = diagnostic;
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
     * Returns the diagnostic object.
     *
     * @return the diagnostic object or {@code null}
     */
    public D getDiagnostic() {
        return diagnostic;
    }

}
