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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.command.StandardCommandCapabilities.Message;
import javafx.scene.command.StandardCommandCapabilities.Title;
import javafx.util.Incubating;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An asynchronous command implementation that creates and executes a {@link javafx.concurrent.Task}
 * when the command is invoked.
 *
 * @see ServiceCommand
 * @since JFXcore 18
 */
@Incubating
public class TaskCommand<T> extends ProgressiveCommand implements Message, Title {

    private static final Executor EXECUTOR;

    private final Function<T, Task<?>> execute;
    private final Consumer<Throwable> exceptionHandler;
    private final Executor executor;
    private final ChangeListener<Worker.State> stateListener = this::handleStateChanged;
    private Task<?> task;

    static {
        try {
            Field field = Service.class.getDeclaredField("EXECUTOR");
            field.setAccessible(true);
            EXECUTOR = (Executor)field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Creates a new {@code TaskCommand} instance that delegates to a {@link Task},
     * which is executed using the default {@link Service} executor.
     *
     * @param execute a supplier that returns a new {@code Task}
     */
    public TaskCommand(Supplier<Task<?>> execute) {
        Objects.requireNonNull(execute, "execute cannot be null");
        this.execute = unused -> execute.get();
        this.exceptionHandler = null;
        this.executor = EXECUTOR;
    }

    /**
     * Creates a new {@code TaskCommand} instance that delegates to a {@link Task},
     * which is executed using the specified executor.
     *
     * @param execute a supplier that returns a new {@code Task}
     * @param executor the {@code Executor} that is used to execute the {@code Task}
     */
    public TaskCommand(Supplier<Task<?>> execute, Executor executor) {
        Objects.requireNonNull(execute, "execute cannot be null");
        this.execute = unused -> execute.get();
        this.exceptionHandler = null;
        this.executor = Objects.requireNonNull(executor, "executor cannot be null");
    }

    /**
     * Creates a new {@code TaskCommand} instance that delegates to a {@link Task},
     * which is executed using the default {@link Service} executor.
     *
     * @param execute a supplier that returns a new {@code Task}
     * @param exceptionHandler handler for exceptions thrown by the operation
     */
    public TaskCommand(Supplier<Task<?>> execute, Consumer<Throwable> exceptionHandler) {
        Objects.requireNonNull(execute, "execute cannot be null");
        this.execute = unused -> execute.get();
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        this.executor = EXECUTOR;
    }

    /**
     * Creates a new {@code TaskCommand} instance that delegates to a {@link Task},
     * which is executed using the specified executor.
     *
     * @param execute a supplier that returns a new {@code Task}
     * @param exceptionHandler handler for exceptions thrown by the operation
     * @param executor the {@code Executor} that is used to execute the {@code Task}
     */
    public TaskCommand(Supplier<Task<?>> execute, Consumer<Throwable> exceptionHandler, Executor executor) {
        Objects.requireNonNull(execute, "execute cannot be null");
        this.execute = unused -> execute.get();
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        this.executor = Objects.requireNonNull(executor, "executor cannot be null");
    }

    /**
     * Creates a new {@code TaskCommand} instance that delegates to a {@link Task},
     * which is executed using the default {@link Service} executor.
     *
     * @param execute a function that accepts the command parameter and returns a new {@code Task}
     */
    public TaskCommand(Function<T, Task<?>> execute) {
        this.execute = Objects.requireNonNull(execute, "execute cannot be null");
        this.exceptionHandler = null;
        this.executor = EXECUTOR;
    }

    /**
     * Creates a new {@code TaskCommand} instance that delegates to a {@link Task},
     * which is executed using the specified executor.
     *
     * @param execute a function that accepts the command parameter and returns a new {@code Task}
     * @param executor the {@code Executor} that is used to execute the {@code Task}
     */
    public TaskCommand(Function<T, Task<?>> execute, Executor executor) {
        this.execute = Objects.requireNonNull(execute, "execute cannot be null");
        this.executor = Objects.requireNonNull(executor, "executor cannot be null");
        this.exceptionHandler = null;
    }

    /**
     * Creates a new {@code TaskCommand} instance that delegates to a {@link Task},
     * which is executed using the default {@link Service} executor.
     *
     * @param execute a function that accepts the command parameter and returns a new {@code Task}
     * @param exceptionHandler handler for exceptions thrown by the operation
     */
    public TaskCommand(Function<T, Task<?>> execute, Consumer<Throwable> exceptionHandler) {
        this.execute = Objects.requireNonNull(execute, "execute cannot be null");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
        this.executor = EXECUTOR;
    }

    /**
     * Creates a new {@code TaskCommand} instance that delegates to a {@link Task},
     * which is executed using the specified executor.
     *
     * @param execute a function that accepts the command parameter and returns a new {@code Task}
     * @param exceptionHandler handler for exceptions thrown by the operation
     * @param executor the {@code Executor} that is used to execute the {@code Task}
     */
    public TaskCommand(Function<T, Task<?>> execute, Consumer<Throwable> exceptionHandler, Executor executor) {
        this.execute = Objects.requireNonNull(execute, "execute cannot be null");
        this.executor = Objects.requireNonNull(executor, "executor cannot be null");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null");
    }

    private final BooleanProperty executable = new SimpleBooleanProperty(this, "executable", true);

    @Override
    public final BooleanProperty executableProperty() {
        return executable;
    }

    @Override
    public final boolean isExecutable() {
        return executable.get();
    }

    public final void setExecutable(boolean executable) {
        this.executable.set(executable);
    }

    private final ReadOnlyBooleanWrapper executing = new ReadOnlyBooleanWrapper(this, "executing");

    @Override
    public final ReadOnlyBooleanProperty executingProperty() {
        return executing.getReadOnlyProperty();
    }

    @Override
    public final boolean isExecuting() {
        return executing.get();
    }

    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress", -1);

    @Override
    public final ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    @Override
    public final double getProgress() {
        return progress.get();
    }

    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper(this, "message", "");

    @Override
    public final ReadOnlyStringProperty messageProperty() {
        return message.getReadOnlyProperty();
    }

    @Override
    public final String getMessage() {
        return message.get();
    }

    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title", "");

    @Override
    public final ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    @Override
    public final String getTitle() {
        return title.get();
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Object parameter) {
        if (!isExecutable()) {
            throw new IllegalStateException("Command is not executable.");
        }

        if (task != null) {
            return;
        }

        task = execute.apply((T)parameter);
        task.stateProperty().addListener(stateListener);

        executing.bind(task.runningProperty());
        message.bind(task.messageProperty());
        title.bind(task.titleProperty());
        progress.bind(task.progressProperty());

        executor.execute(task);
    }

    private void handleStateChanged(ObservableValue<? extends Worker.State> observable,
                                    Worker.State oldState, Worker.State newState) {
        if (newState != Worker.State.CANCELLED
                && newState != Worker.State.FAILED
                && newState != Worker.State.SUCCEEDED) {
            return;
        }

        executing.unbind();
        message.unbind();
        title.unbind();
        progress.unbind();
        executing.set(false);

        if (newState == Worker.State.FAILED && exceptionHandler != null && task != null) {
            Throwable exception = task.getException();
            if (exception != null) {
                exceptionHandler.accept(exception);
            }
        }

        task = null;
    }

}
