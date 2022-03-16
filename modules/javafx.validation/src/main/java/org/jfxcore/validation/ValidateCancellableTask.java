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

import javafx.validation.ValidationResult;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ValidateCancellableTask<T, D>
        extends CompletableFuture<ValidationResult<D>> implements Runnable {

    private final AtomicBoolean cancellationRequested = new AtomicBoolean();
    private final T value;
    private boolean hasRun;

    protected ValidateCancellableTask(T value) {
        this.value = value;
    }

    @Override
    public void run() {
        if (hasRun) {
            return;
        }

        hasRun = true;

        try {
            ValidationResult<D> result = apply(value, cancellationRequested);

            if (cancellationRequested.get()) {
                super.cancel(true);
            } else {
                complete(result);
            }
        } catch (Throwable ex) {
            completeExceptionally(ex);
        }
    }

    protected abstract ValidationResult<D> apply(T value, AtomicBoolean cancellationRequested);

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancellationRequested.set(true);
        return false;
    }

}
