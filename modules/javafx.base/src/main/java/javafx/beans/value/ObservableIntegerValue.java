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
 * An observable integer value.
 *
 * @see ObservableValue
 * @see ObservableNumberValue
 *
 *
 * @since JavaFX 2.0
 */
public interface ObservableIntegerValue extends ObservableNumberValue {

    /**
     * Returns the current value of this {@code ObservableIntegerValue}.
     *
     * @return The current value
     */
    int get();

    /**
     * Returns a new {@link ObservableIntegerValue} that wraps a constant integer value.
     *
     * @param value the constant integer value
     * @return the new {@link ObservableIntegerValue}
     *
     * @since JFXcore 18
     */
    static ObservableIntegerValue observableIntegerValue(Number value) {
        return new ObservableIntegerValue() {
            private ExpressionHelper<Number> helper;

            @Override
            public int get() {
                return value != null ? value.intValue() : 0;
            }

            @Override
            public Number getValue() {
                return value;
            }

            @Override
            public int intValue() {
                return value != null ? value.intValue() : 0;
            }

            @Override
            public long longValue() {
                return value != null ? value.longValue() : 0;
            }

            @Override
            public float floatValue() {
                return value != null ? value.floatValue() : 0;
            }

            @Override
            public double doubleValue() {
                return value != null ? value.doubleValue() : 0;
            }

            @Override
            public void addListener(ChangeListener<? super Number> listener) {
                helper = ExpressionHelper.addListener(helper, this, listener);
            }

            @Override
            public void removeListener(ChangeListener<? super Number> listener) {
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
