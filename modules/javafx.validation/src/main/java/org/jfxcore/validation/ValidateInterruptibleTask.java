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

public abstract class ValidateInterruptibleTask<T, D>
        extends CompletableFuture<ValidationResult<D>> implements Runnable {

    private final T value;
    private boolean hasRun;
    private volatile Thread executingThread;

    protected ValidateInterruptibleTask(T value) {
        this.value = value;
    }

    @Override
    public void run() {
        if (hasRun) {
            return;
        }

        hasRun = true;
        executingThread = Thread.currentThread();

        try {
            complete(apply(value));
        } catch (Throwable ex) {
            completeExceptionally(ex);
        }
    }

    protected abstract ValidationResult<D> apply(T value);

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (executingThread != null) {
            executingThread.interrupt();
            executingThread = null;
        }

        return false;
    }

}
