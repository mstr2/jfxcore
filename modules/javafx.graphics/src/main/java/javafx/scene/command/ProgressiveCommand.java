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

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.util.Incubating;

/**
 * Represents an asynchronous operation that reports its progress towards completion.
 * <p>
 * See the {@link javafx.scene.command Commanding} documentation for additional information.
 *
 * @see TaskCommand
 * @see ServiceCommand
 * @since JFXcore 18
 */
@Incubating
public abstract class ProgressiveCommand extends AsyncCommand {

    /**
     * Gets a property that indicates the execution progress of the operation,
     * ranging from 0 (inclusive) to 1 (inclusive). If the progress cannot be
     * determined, the value is -1.
     *
     * @return the {@code progress} property
     * @defaultValue -1
     */
    public abstract ReadOnlyDoubleProperty progressProperty();

    /**
     * Gets the execution progress of the command.
     *
     * @return the execution progress, ranging from 0 (inclusive) to 1 (inclusive),
     *         or -1 if the progress cannot be determined
     */
    public double getProgress() {
        return progressProperty().get();
    }

}
