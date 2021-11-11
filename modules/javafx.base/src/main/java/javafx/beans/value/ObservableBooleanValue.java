/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021, JFXcore. All rights reserved.
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

package javafx.beans.value;

import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;

/**
 * An observable boolean value.
 *
 * @see ObservableValue
 *
 *
 * @since JavaFX 2.0
 */
public interface ObservableBooleanValue extends ObservableValue<Boolean> {

    /**
     * Returns the current value of this {@code ObservableBooleanValue}.
     *
     * @return The current value
     */
    boolean get();

    /**
     * Returns a new {@link ObservableBooleanValue} that wraps a constant boolean value.
     *
     * @param value the constant boolean value
     * @return the new {@link ObservableBooleanValue}
     *
     * @since JFXcore 18
     */
    static ObservableBooleanValue observableBooleanValue(Boolean value) {
        return new ObservableBooleanValue() {
            private ExpressionHelper<Boolean> helper;

            @Override
            public boolean get() {
                return value != null ? value : false;
            }

            @Override
            public Boolean getValue() {
                return value;
            }

            @Override
            public void addListener(ChangeListener<? super Boolean> listener) {
                helper = ExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ChangeListener<? super Boolean> listener) {
                helper = ExpressionHelper.removeListener(helper, listener);
            }

            @Override
            public void addListener(InvalidationListener listener) {
                helper = ExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                helper = ExpressionHelper.removeListener(helper, listener);
            }
        };
    }
}
