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

import javafx.beans.Observable;
import java.util.concurrent.Executor;

/**
 * Defines a data constraint.
 * <p>
 * For a list of predefined constraints, see {@link Constraints}.
 *
 * @param <T> data type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
public final class Constraint<T, D> {

    private static final Observable[] EMPTY = new Observable[0];

    private final Validator<T, D> validator;
    private final Executor completionExecutor;
    private final Observable[] dependencies;

    /**
     * Creates a new instance of the {@code Constraint} class.
     *
     * @param validator the validator
     * @param completionExecutor the executor that completes the future returned by the validator, or {@code null}
     * @param dependencies the constraint dependencies, or {@code null}
     */
    public Constraint(
            Validator<T, D> validator,
            Executor completionExecutor,
            Observable[] dependencies) {
        if (validator == null) {
            throw new NullPointerException("validator cannot be null");
        }

        this.validator = validator;
        this.completionExecutor = completionExecutor;
        this.dependencies = dependencies != null ? dependencies : EMPTY;
    }

    /**
     * Returns the validator.
     */
    public Validator<T, D> getValidator() {
        return validator;
    }

    /**
     * Returns the executor that completes the future returned by {@link Validator#validate(Object)},
     * or {@code null} if no executor was specified.
     */
    public Executor getCompletionExecutor() {
        return completionExecutor;
    }

    /**
     * Returns the constraint dependencies.
     */
    public Observable[] getDependencies() {
        return dependencies;
    }

    /**
     * Determines whether the specified {@link Observable observable} is a dependency of this constraint.
     */
    public boolean isDependency(Observable observable) {
        for (Observable dep : dependencies) {
            if (observable == dep) {
                return true;
            }
        }

        return false;
    }

}
