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

package javafx.validation;

import javafx.util.Incubating;

/**
 * A listener that will be notified when the validation state of a {@link ConstrainedValue} changes.
 *
 * @param <T> value type
 * @param <D> diagnostic type
 */
@Incubating
@FunctionalInterface
public interface ValidationListener<T, D> {

    /**
     * Specifies the type of the change.
     */
    enum ChangeType {
        VALID,
        INVALID,
        VALIDATING
    }

    /**
     * Called when the validation state of a {@link ConstrainedValue} object changes.
     *
     * @param value the {@code ConstrainedValue}
     * @param changeType the type of the change
     * @param oldValue the old value
     * @param newValue the new value
     */
    void changed(ConstrainedValue<? extends T, D> value, ChangeType changeType, boolean oldValue, boolean newValue);

}
