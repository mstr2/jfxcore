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

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.scene.command.StandardCommandCapabilities.Message;
import javafx.scene.command.StandardCommandCapabilities.Title;
import javafx.util.Incubating;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An asynchronous command implementation that delegates to a {@link Service}.
 *
 * @see TaskCommand
 * @since JFXcore 18
 */
@Incubating
public class ServiceCommand extends ProgressiveCommand implements Message, Title {

    private final Service<?> service;
    private final InvalidationListener exceptionChangedListener;

    /**
     * Creates a new {@code ServiceCommand} instance that delegates to a {@link Service}.
     */
    public ServiceCommand(Service<?> service) {
        this(service, null, 0);
    }

    /**
     * Creates a new {@code ServiceCommand} instance that delegates to a {@link Service}.
     */
    public ServiceCommand(Service<?> service, Consumer<Throwable> exceptionHandler) {
        this(service, Objects.requireNonNull(exceptionHandler, "exceptionHandler cannot be null"), 0);
    }

    @SuppressWarnings("unchecked")
    private ServiceCommand(Service<?> service, Consumer<Throwable> exceptionHandler, int ignored) {
        this.service = Objects.requireNonNull(service, "service cannot be null");
        this.executing.bind(service.runningProperty());
        this.message.bind(service.messageProperty());
        this.title.bind(service.titleProperty());
        this.progress.bind(service.progressProperty());

        if (exceptionHandler != null) {
            exceptionChangedListener = observable -> {
                Throwable t = ((ObservableValue<Throwable>)observable).getValue();
                if (t != null) {
                    exceptionHandler.accept(t);
                }
            };

            service.exceptionProperty().addListener(new WeakInvalidationListener(exceptionChangedListener));
        } else {
            exceptionChangedListener = null;
        }
    }

    /**
     * Indicates whether the command is currently executable.
     *
     * @defaultValue true
     */
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

    /**
     * Indicates whether the command is currently executing.
     *
     * @defaultValue false
     */
    private final ReadOnlyBooleanWrapper executing = new ReadOnlyBooleanWrapper(this, "executing");

    @Override
    public final ReadOnlyBooleanProperty executingProperty() {
        return executing.getReadOnlyProperty();
    }

    @Override
    public final boolean isExecuting() {
        return executing.get();
    }

    /**
     * Indicates the progress of the executing command, ranging from 0 (inclusive) to 1 (inclusive).
     *
     * @defaultValue 0
     */
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress");

    @Override
    public final ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    @Override
    public final double getProgress() {
        return progress.get();
    }

    /**
     * Represents the current message of the executing command.
     *
     * @defaultValue null
     */
    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper(this, "message");

    @Override
    public final ReadOnlyStringProperty messageProperty() {
        return message.getReadOnlyProperty();
    }

    @Override
    public final String getMessage() {
        return message.get();
    }

    /**
     * Represents the title of the executing command.
     *
     * @defaultValue null
     */
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");

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
        service.cancel();
    }

    @Override
    public void execute(Object parameter) {
        if (!isExecutable()) {
            throw new IllegalStateException("Command is not executable.");
        }

        if (isExecuting()) {
            return;
        }

        service.restart();
    }

}
