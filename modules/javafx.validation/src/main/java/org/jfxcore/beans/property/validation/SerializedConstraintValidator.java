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

package org.jfxcore.beans.property.validation;

import com.sun.javafx.binding.Logging;
import javafx.beans.Observable;
import javafx.beans.property.validation.Constraint;
import javafx.beans.property.validation.ValidationResult;
import java.util.concurrent.CompletableFuture;

/**
 * Wrapper for a {@link Constraint} that enforces serialized execution of its constraint validator.
 * If the validator is still running when a new validation run is requested, the new validation run
 * will be deferred until after the validator has completed. If several validation runs are requested
 * while the validator is still running, only the last request will be executed and all intermediate
 * requests will be dropped.
 *
 * @param <T> type of the value to be validated
 * @param <D> diagnostic type
 */
public abstract class SerializedConstraintValidator<T, D> {

    private final Constraint<? super T, D> constraint;
    private CompletableFuture<ValidationResult<D>> validatingFuture;
    private T currentValue;
    private T nextValue;
    private boolean hasNextValue;

    public SerializedConstraintValidator(Constraint<? super T, D> constraint) {
        this.constraint = constraint;
    }

    /**
     * Occurs when the validator returns a non-completed {@link CompletableFuture}.
     */
    protected abstract void onAsyncValidationStarted();

    /**
     * Occurs when the non-completed {@link CompletableFuture} returned by the validator has completed.
     * Note that this method is only called if {@link #onAsyncValidationStarted()} was called before.
     */
    protected abstract void onAsyncValidationEnded();

    /**
     * Occurs when the future returned by the validator has completed, independent of whether it started
     * in the completed state or was completed later.
     */
    protected abstract void onValidationCompleted(T value, ValidationResult<D> result, Throwable exception);

    /**
     * Returns whether the specified {@link Observable} is a dependency of the constraint.
     */
    public boolean isDependency(Observable observable) {
        return constraint.isDependency(observable);
    }

    public void validate(T value) {
        if (validatingFuture != null) {
            nextValue = value;
            hasNextValue = true;
            validatingFuture.cancel(true);
        } else {
            try {
                CompletableFuture<ValidationResult<D>> result = constraint.getValidator().validate(value);

                if (result.isDone()) {
                    try {
                        onValidationCompleted(value, result.get(), null);
                    } catch (Throwable ex) {
                        onValidationCompleted(value, null, ex);
                    }
                } else {
                    currentValue = value;
                    validatingFuture = result;
                    onAsyncValidationStarted();
                    result.whenCompleteAsync(this::handleValidationCompleted, constraint.getCompletionExecutor());
                }
            } catch (Throwable ex) {
                Logging.getLogger().severe("Exception in constraint validator", ex);
            }
        }
    }

    private void handleValidationCompleted(ValidationResult<D> result, Throwable exception) {
        onAsyncValidationEnded();
        onValidationCompleted(currentValue, exception == null ? result : null, exception);

        currentValue = null;
        validatingFuture = null;

        if (hasNextValue) {
            T value = nextValue;
            hasNextValue = false;
            nextValue = null;

            validate(value);
        }
    }

}
