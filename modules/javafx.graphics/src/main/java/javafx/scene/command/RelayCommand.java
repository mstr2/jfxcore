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

import javafx.beans.value.ObservableValue;
import javafx.util.Incubating;
import java.util.function.Consumer;

/**
 * A command implementation that invokes a user-specified {@link Runnable}.
 *
 * @since JFXcore 18
 */
@Incubating
public class RelayCommand extends CommandBase {

    private final Consumer<?> execute;
    private final Consumer<Throwable> exceptionHandler;

    /**
     * Creates a new {@link RelayCommand} instance that executes a parameterless operation.
     *
     * @param execute a parameterless operation
     */
    public RelayCommand(Runnable execute) {
        if (execute == null) {
            throw new IllegalArgumentException("execute cannot be null");
        }

        this.execute = parameter -> execute.run();
        this.exceptionHandler = null;
    }

    /**
     * Creates a new {@link RelayCommand} instance that executes a parameterless operation.
     *
     * @param execute a parameterless operation
     * @param exceptionHandler handler for exceptions thrown by the operation; if none is provided, exceptions will be thrown on the calling thread
     */
    public RelayCommand(Runnable execute, Consumer<Throwable> exceptionHandler) {
        if (execute == null) {
            throw new IllegalArgumentException("execute cannot be null");
        }

        this.execute = parameter -> execute.run();
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Creates a new {@link RelayCommand} instance that executes a parameterless operation that
     * is dependent on the specified condition.
     *
     * @param execute a parameterless operation
     * @param condition a value that determines whether the command is executable
     */
    public RelayCommand(Runnable execute, ObservableValue<Boolean> condition) {
        super(condition);

        if (execute == null) {
            throw new IllegalArgumentException("execute cannot be null");
        }

        this.execute = parameter -> execute.run();
        this.exceptionHandler = null;
    }

    /**
     * Creates a new {@link RelayCommand} instance that executes a parameterless operation that
     * is dependent on the specified condition.
     *
     * @param execute a parameterless operation
     * @param condition a value that determines whether the command is executable
     * @param exceptionHandler handler for exceptions thrown by the operation; if none is provided, exceptions will be thrown on the calling thread
     */
    public RelayCommand(Runnable execute, ObservableValue<Boolean> condition, Consumer<Throwable> exceptionHandler) {
        super(condition);

        if (execute == null) {
            throw new IllegalArgumentException("execute cannot be null");
        }

        this.execute = parameter -> execute.run();
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Creates a new {@link RelayCommand} instance that executes a parameterless operation.
     *
     * @param execute a parameterless operation
     */
    public <T> RelayCommand(Consumer<T> execute) {
        if (execute == null) {
            throw new IllegalArgumentException("execute cannot be null");
        }

        this.execute = execute;
        this.exceptionHandler = null;
    }

    /**
     * Creates a new {@link RelayCommand} instance that executes a parameterless operation.
     *
     * @param execute a parameterless operation
     * @param exceptionHandler handler for exceptions thrown by the operation; if none is provided, exceptions will be thrown on the calling thread
     */
    public <T> RelayCommand(Consumer<T> execute, Consumer<Throwable> exceptionHandler) {
        if (execute == null) {
            throw new IllegalArgumentException("execute cannot be null");
        }

        this.execute = execute;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Creates a new {@link RelayCommand} instance that executes a parameterless operation that
     * is dependent on the specified condition.
     *
     * @param execute a parameterless operation
     * @param condition a value that determines whether the command is executable
     */
    public <T> RelayCommand(Consumer<T> execute, ObservableValue<Boolean> condition) {
        super(condition);

        if (execute == null) {
            throw new IllegalArgumentException("execute cannot be null");
        }

        this.execute = execute;
        this.exceptionHandler = null;
    }

    /**
     * Creates a new {@link RelayCommand} instance that executes a parameterless operation that
     * is dependent on the specified condition.
     *
     * @param execute a parameterless operation
     * @param condition a value that determines whether the command is executable
     * @param exceptionHandler handler for exceptions thrown by the operation; if none is provided, exceptions will be thrown on the calling thread
     */
    public <T> RelayCommand(Consumer<T> execute, ObservableValue<Boolean> condition, Consumer<Throwable> exceptionHandler) {
        super(condition);

        if (execute == null) {
            throw new IllegalArgumentException("execute cannot be null");
        }

        this.execute = execute;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void execute(Object parameter) {
        try {
            startExecution();
            ((Consumer)execute).accept(parameter);
            endExecution();
        } catch (Throwable ex) {
            endExecution();

            if (exceptionHandler != null) {
                exceptionHandler.accept(ex);
            } else {
                throw ex;
            }
        }
    }

}
