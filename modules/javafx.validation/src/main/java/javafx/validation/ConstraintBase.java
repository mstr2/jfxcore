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

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.util.Incubating;
import java.util.concurrent.Executor;

/**
 * The common base interface of all constraint types.
 * This interface cannot be implemented directly; instead, applications should implement
 * {@link Constraint}, {@link ListConstraint}, {@link SetConstraint}, or {@link MapConstraint}.
 *
 * @param <T> the data type
 * @param <D> the diagnostic type
 */
@Incubating
@SuppressWarnings("unused")
public sealed interface ConstraintBase<T, D> permits Constraint, ListConstraint, SetConstraint, MapConstraint {

    /**
     * Returns the executor that is used to yield the {@link ValidationResult} to the validation system,
     * or {@code null} if no executor was specified.
     *
     * @implNote If this constraint is used with properties that live on the JavaFX application thread,
     *           a method reference to {@link Platform#runLater} should be returned from this method.
     */
    Executor getCompletionExecutor();

    /**
     * Returns the constraint dependencies.
     */
    Observable[] getDependencies();

}
