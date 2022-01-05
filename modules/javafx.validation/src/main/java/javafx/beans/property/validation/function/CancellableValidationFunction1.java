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

package javafx.beans.property.validation.function;

import javafx.beans.property.validation.ValidationResult;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a cancellable validation function with a single dependency.
 *
 * @param <T> the type of the value to be validated
 * @param <P1> the type of the dependency
 * @param <D> the diagnostic type
 * @since JFXcore 18
 */
@FunctionalInterface
public interface CancellableValidationFunction1<T, P1, D> {

    /**
     * Applies this function to the given arguments.
     */
    ValidationResult<D> apply(T value, P1 dependency, AtomicBoolean cancellationRequested);

}
