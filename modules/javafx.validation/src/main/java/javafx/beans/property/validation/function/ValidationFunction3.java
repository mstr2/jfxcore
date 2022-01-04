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

/**
 * Represents a validation function with three dependencies.
 *
 * @param <T> the type of the value to be validated
 * @param <D1> the type of the first dependency
 * @param <D2> the type of the second dependency
 * @param <D3> the type of the third dependency
 * @param <E> the error information type
 * @since JFXcore 18
 */
@FunctionalInterface
public interface ValidationFunction3<T, D1, D2, D3, E> {

    /**
     * Applies this function to the given arguments.
     */
    ValidationResult<E> apply(T value, D1 dependency1, D2 dependency2, D3 dependency3);

}
