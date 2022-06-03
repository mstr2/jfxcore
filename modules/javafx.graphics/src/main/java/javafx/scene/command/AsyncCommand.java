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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.util.Incubating;

/**
 * Represents an asynchronous operation.
 * <p>
 * See the {@link javafx.scene.command Commanding} documentation for additional information.
 *
 * @see TaskCommand
 * @see ServiceCommand
 * @since JFXcore 18
 */
@Incubating
public abstract class AsyncCommand extends Command {

    /**
     * Gets a property that indicates whether the command is currently executing.
     *
     * @return the {@code executing} property
     */
    public abstract ReadOnlyBooleanProperty executingProperty();

    /**
     * Indicates whether the command is currently executing.
     *
     * @return {@code true} if the command is currently executing, {@code false} otherwise
     */
    public boolean isExecuting() {
        return executingProperty().get();
    }

    /**
     * Requests the cancellation of the currently executing command.
     * If the command is not currently executing, calling this method has no effect.
     * <p>
     * This is an optional operation: command implementations are not required to support
     * cancellation, and if they choose to support cancellation, no guarantee is made as to
     * when or how the running command will terminate.
     */
    public void cancel() {}

}
