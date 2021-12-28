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

import org.jfxcore.beans.property.validation.ValidatorWrapper;
import javafx.beans.Observable;
import java.util.concurrent.Executor;

/**
 * Defines a data constraint.
 * <p>
 * For a list of predefined constraints, see {@link Constraints}.
 *
 * @param <V> data type
 * @param <E> error information type
 */
public final class Constraint<V, E> {

    private static final Observable[] EMPTY = new Observable[0];

    private final AsyncValidator<V, E> validator;
    private final Executor invocationExecutor;
    private final Executor completionExecutor;
    private final Observable[] dependencies;

    /**
     * Creates a new instance of the {@code Constraint} class that synchronously
     * validates the constraint.
     *
     * @param validator the validator
     * @param dependencies the constraint dependencies or {@code null}
     */
    public Constraint(Validator<V, E> validator, Observable[] dependencies) {
        if (validator == null) {
            throw new NullPointerException("validator cannot be null");
        }

        this.validator = new ValidatorWrapper<>(validator);
        this.dependencies = dependencies != null ? dependencies : EMPTY;
        this.invocationExecutor = null;
        this.completionExecutor = null;
    }

    /**
     * Creates a new instance of the {@code Constraint} class that asynchronously
     * validates the constraint.
     *
     * @param validator the validator
     * @param invocationExecutor the executor that invokes the {@link AsyncValidator}, or {@code null}
     * @param completionExecutor the executor that yields the validation result, or {@code null}
     * @param dependencies the constraint dependencies or {@code null}
     */
    public Constraint(
            AsyncValidator<V, E> validator,
            Executor invocationExecutor,
            Executor completionExecutor,
            Observable[] dependencies) {
        if (validator == null) {
            throw new NullPointerException("validator cannot be null");
        }

        this.validator = validator;
        this.invocationExecutor = invocationExecutor;
        this.completionExecutor = completionExecutor;
        this.dependencies = dependencies != null ? dependencies : EMPTY;
    }

    /**
     * Returns the validator.
     *
     * @return the validator
     */
    public AsyncValidator<V, E> getValidator() {
        return validator;
    }

    /**
     * Returns the executor that invokes the {@link #getValidator() validator},
     * or {@code null} if no executor was specified.
     *
     * @return the executor or {@code null}
     */
    public Executor getInvocationExecutor() {
        return invocationExecutor;
    }

    /**
     * Returns the executor that completes the future returned by {@link AsyncValidator#validate(Object)},
     * or {@code null} if no executor was specified.
     *
     * @return the executor or {@code null}
     */
    public Executor getCompletionExecutor() {
        return completionExecutor;
    }

    /**
     * Returns the constraint dependencies.
     *
     * @return the constraint dependencies or an empty array
     */
    public Observable[] getDependencies() {
        return dependencies;
    }

    /**
     * Determines whether the specified observable is a dependency of this constraint.
     *
     * @param observable the observable
     * @return {@code true} if the observable is a dependency of this constraint, {@code false} otherwise
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
