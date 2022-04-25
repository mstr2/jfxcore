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

package javafx.scene.command;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.util.Incubating;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A command implementation that uses {@link CompletableFuture} to encapsulate its operation.
 *
 * @since JFXcore 18
 */
@Incubating
public class FutureRelayCommand extends CommandBase implements Command {

    private final Supplier<CompletableFuture<?>> futureSupplier;
    private final Consumer<Throwable> exceptionHandler;

    private CancelCommand cancelCommand;
    private CompletableFuture<?> runningFuture;

    /**
     * Initializes a new instance of the {@link FutureRelayCommand} class.
     *
     * @param futureSupplier supplier that returns a {@link CompletableFuture} encapsulating a parameterless operation
     */
    public FutureRelayCommand(Supplier<CompletableFuture<?>> futureSupplier) {
        if (futureSupplier == null) {
            throw new IllegalArgumentException("futureSupplier");
        }

        this.futureSupplier = futureSupplier;
        this.exceptionHandler = null;
    }

    /**
     * Initializes a new instance of the {@link FutureRelayCommand} class.
     *
     * @param futureSupplier supplier that returns a {@link CompletableFuture} encapsulating a parameterless operation
     * @param exceptionHandler handler for exceptions thrown by the operation; if none is provided, exceptions will be thrown on the calling thread
     */
    public FutureRelayCommand(Supplier<CompletableFuture<?>> futureSupplier, Consumer<Throwable> exceptionHandler) {
        if (futureSupplier == null) {
            throw new IllegalArgumentException("futureSupplier");
        }

        this.futureSupplier = futureSupplier;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Initializes a new instance of the {@link FutureRelayCommand} class.
     *
     * @param futureSupplier supplier that returns a {@link CompletableFuture} encapsulating a parameterless operation
     * @param condition a value that controls the executability of the command
     */
    public FutureRelayCommand(Supplier<CompletableFuture<?>> futureSupplier, ObservableValue<Boolean> condition) {
        super(condition);

        if (futureSupplier == null) {
            throw new IllegalArgumentException("futureSupplier");
        }

        this.futureSupplier = futureSupplier;
        this.exceptionHandler = null;
    }

    /**
     * Initializes a new instance of the {@link FutureRelayCommand} class.
     *
     * @param futureSupplier supplier that returns a {@link CompletableFuture} encapsulating a parameterless operation
     * @param condition a value that controls the executability of the command
     * @param exceptionHandler handler for exceptions thrown by the operation; if none is provided, exceptions will be thrown on the calling thread
     */
    public FutureRelayCommand(Supplier<CompletableFuture<?>> futureSupplier, ObservableValue<Boolean> condition, Consumer<Throwable> exceptionHandler) {
        super(condition);

        if (futureSupplier == null) {
            throw new IllegalArgumentException("futureSupplier");
        }

        this.futureSupplier = futureSupplier;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Gets a command that can be used to cancel the currently running operation.
     * Invoking the command calls {@link CompletableFuture#cancel(boolean)} on the current operation.
     */
    public Command getCancelCommand() {
        if (cancelCommand == null) {
            cancelCommand = new CancelCommand();
        }

        return cancelCommand;
    }

    @Override
    public void execute(Object parameter) {
        startExecution();

        try {
            runningFuture = futureSupplier.get();
            runningFuture.whenComplete((res, ex) -> Platform.runLater(() -> {
                endExecution();
                runningFuture = null;
                handleException(ex);
            }));
        } catch (Throwable ex){
            endExecution();
            runningFuture = null;
            handleException(ex);
        }
    }

    @Override
    protected void endExecution() {
        if (cancelCommand != null) {
            cancelCommand.endExecution();
        }

        super.endExecution();
    }

    private void handleException(Throwable ex) {
        if (ex == null || ex instanceof CancellationException) {
            return;
        }

        if (exceptionHandler != null) {
            exceptionHandler.accept(ex);
        } else {
            throw new RuntimeException("Unhandled exception in command execution.", ex);
        }
    }

    private final class CancelCommand extends CommandBase {
        CancelCommand() {
            super(FutureRelayCommand.this.executingProperty());
        }

        @Override
        public void execute(Object parameter) {
            startExecution();
            runningFuture.cancel(false);
        }

        @Override
        public void endExecution() {
            super.endExecution();
        }
    }

}
