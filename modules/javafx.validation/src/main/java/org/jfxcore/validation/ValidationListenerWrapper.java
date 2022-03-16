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

import javafx.validation.ConstrainedValue;
import javafx.validation.ValidationListener;
import java.util.Objects;

public final class ValidationListenerWrapper<T, D> implements ValidationListener<T, D> {

    private final ConstrainedValue<T, D> value;
    private final ValidationListener<? super T, D> listener;

    public ValidationListenerWrapper(ConstrainedValue<T, D> value, ValidationListener<? super T, D> listener) {
        Objects.requireNonNull(listener, "listener");
        this.value = value;
        this.listener = listener;
    }

    @Override
    public void changed(ConstrainedValue<? extends T, D> value, ChangeType changeType, boolean oldValue, boolean newValue) {
        listener.changed(this.value, changeType, oldValue, newValue);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ValidationListenerWrapper<?, ?> w && Objects.equals(listener, w.listener);
    }

    @Override
    public int hashCode() {
        return listener.hashCode();
    }

}
