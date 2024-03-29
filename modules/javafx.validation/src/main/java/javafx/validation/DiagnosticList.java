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

package javafx.validation;

import javafx.collections.ObservableList;
import javafx.util.Incubating;

/**
 * An observable list of diagnostics.
 *
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public interface DiagnosticList<D> extends ObservableList<D> {

    /**
     * Returns {@code true} if the diagnostic at the specified index was generated
     * for a successfully validated value; {@code false} otherwise.
     */
    boolean isValid(int index);

    /**
     * Returns an observable sublist view that only includes diagnostics of
     * {@link Constraint} validators that successfully validated the value.
     */
    ObservableList<D> validSubList();

    /**
     * Returns an observable sublist view that only includes diagnostics of
     * {@link Constraint} validators that failed to validate the value.
     */
    ObservableList<D> invalidSubList();

}
