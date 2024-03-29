/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.validation;

import javafx.beans.NamedArg;
import javafx.beans.WeakListener;
import javafx.util.Incubating;
import java.lang.ref.WeakReference;

/**
 * A {@code WeakValidationListener} can be used if a {@link ConstrainedValue} object
 * should only maintain a weak reference to the listener. This helps to avoid
 * memory leaks that can occur if observers are not unregistered from observed
 * objects after use.
 * <p>
 * A {@code WeakValidationListener} is created by passing in the original
 * {@link ValidationListener}. The {@code WeakValidationListener} should
 * then be registered to listen for changes of the observed object.
 * <p>
 * Note: You have to keep a reference to the {@code ValidationListener} that
 * was passed in as long as it is in use, otherwise it can be garbage collected
 * too soon.
 *
 * @see ValidationListener
 * @see ConstrainedValue
 *
 * @since JFXcore 18
 */
@Incubating
public final class WeakValidationListener<T, D> implements ValidationListener<T, D>, WeakListener {

    private final WeakReference<ValidationListener<T, D>> ref;

    /**
     * The constructor of {@code WeakValidationListener}.
     *
     * @param listener the original listener that should be notified
     */
    public WeakValidationListener(@NamedArg("listener") ValidationListener<T, D> listener) {
        if (listener == null) {
            throw new NullPointerException("listener must be specified.");
        }

        this.ref = new WeakReference<>(listener);
    }

    @Override
    public boolean wasGarbageCollected() {
        return ref.get() == null;
    }

    @Override
    public void changed(
            ConstrainedValue<? extends T, D> value, ChangeType changeType, boolean oldValue, boolean newValue) {
        ValidationListener<T, D> listener = ref.get();
        if (listener != null) {
            listener.changed(value, changeType, oldValue, newValue);
        } else {
            // The weakly reference listener has been garbage collected,
            // so this WeakListener will now unhook itself from the
            // source bean
            value.removeListener(this);
        }
    }
}
