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

package org.jfxcore.validation;

import com.sun.javafx.logging.PlatformLogger;
import javafx.validation.ValidationResult;
import javafx.validation.Validator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * Wrapper for a {@link Validator} that enforces serialized execution of validation requests.
 * If the validator is still running when a new validation run is requested, the new validation run
 * will be deferred until after the validator has completed. If several validation runs are requested
 * while the validator is still running, only the last request will be executed and all intermediate
 * requests will be dropped.
 *
 * @param <T> type of the value to be validated
 * @param <D> diagnostic type
 */
public abstract class SerializedValidator<T, D> {

    private static final CancellationException CANCELLED = new CancellationException();

    private final Validator<? super T, D> validator;
    private final Executor completionExecutor;
    private CompletableFuture<ValidationResult<D>> validatingFuture;
    private T currentValue;
    private T nextValue;
    private boolean hasNextValue;

    public SerializedValidator(Validator<? super T, D> validator, Executor completionExecutor) {
        this.validator = validator;
        this.completionExecutor = completionExecutor;
    }

    /**
     * Gets the last {@code ValidationResult} that was produced by this validator.
     */
    public abstract ValidationResult<D> getValidationResult();

    /**
     * Occurs before the validator is invoked.
     */
    protected abstract void onValidationStarted();

    /**
     * Occurs when the future returned by the validator has completed, independent of whether it started
     * in the completed state or was completed later.
     *
     * @param value the value that was validated
     * @param result the validation result, or {@code null} if the validator was cancelled
     * @param intermediateCompletion indicates whether a follow-up validation is already scheduled
     */
    protected abstract void onValidationCompleted(T value, ValidationResult<D> result, boolean intermediateCompletion);

    /**
     * Requests a validation run for the specified value.
     * {@link #onValidationStarted()} and {@link #onValidationCompleted} are invoked before and after the validator
     * is invoked; either immediately from this method, or at a later time using {@link #completionExecutor}.
     */
    public void validate(T value) {
        if (validatingFuture != null) {
            nextValue = value;
            hasNextValue = true;
            validatingFuture.cancel(false);
        } else {
            try {
                onValidationStarted();
                CompletableFuture<ValidationResult<D>> future = validator.validate(value);

                if (future.isDone()) {
                    try {
                        ValidationResult<D> result = future.get();
                        if (result == ValidationResult.none()) {
                            result = null;
                        }

                        onValidationCompleted(value, result, false);
                    } catch (Throwable ex) {
                        if (!(ex instanceof CancellationException)) {
                            getLogger().severe(
                                "Exception in constraint validator " + validator.getClass().getName(),
                                ex instanceof CompletionException ? ex.getCause() : ex);
                        }

                        onValidationCompleted(value, null, false);
                    }
                } else {
                    currentValue = value;
                    validatingFuture = future;
                    future.whenCompleteAsync(this::handleValidationCompleted, completionExecutor);
                }
            } catch (Throwable ex) {
                getLogger().severe(
                    "Exception in constraint validator " + validator.getClass().getName(), ex);

                onValidationCompleted(value, null, false);
            }
        }
    }

    public void dispose() {
        nextValue = null;
        hasNextValue = false;

        if (validatingFuture != null) {
            validatingFuture.cancel(false);
        }
    }

    private void handleValidationCompleted(ValidationResult<D> result, Throwable exception) {
        if (exception != null && !(exception instanceof CancellationException)) {
            getLogger().severe(
                "Exception in constraint validator " + validator.getClass().getName(),
                exception instanceof CompletionException ? exception.getCause() : exception);
        }

        if (hasNextValue) {
            exception = CANCELLED;
        }

        onValidationCompleted(currentValue, exception != null ? null : result, hasNextValue);

        currentValue = null;
        validatingFuture = null;

        if (hasNextValue) {
            T value = nextValue;
            hasNextValue = false;
            nextValue = null;

            validate(value);
        }
    }

    private static PlatformLogger getLogger() {
        return PlatformLogger.getLogger("javafx.validation");
    }

}
