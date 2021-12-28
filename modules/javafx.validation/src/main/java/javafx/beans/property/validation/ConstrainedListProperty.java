/*
 * Copyright (c) 2011, 2020, Oracle and/or its affiliates. All rights reserved.
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

package javafx.beans.property.validation;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.WritableListValue;
import javafx.collections.ObservableList;

/**
 * Defines a constrained property that wraps an {@link ObservableList}.
 *
 * @param <T> element type
 * @param <E> error information type
 * @since JFXcore 18
 */
public abstract class ConstrainedListProperty<T, E>
        extends ReadOnlyConstrainedListProperty<T, E>
        implements Property<ObservableList<T>>, WritableListValue<T> {

    /**
     * Creates a default {@code ConstrainedListProperty}.
     */
    protected ConstrainedListProperty() {
    }

    @Override
    public void setValue(ObservableList<T> v) {
        set(v);
    }

    @Override
    public void bindBidirectional(Property<ObservableList<T>> other) {
        Bindings.bindBidirectional(this, other);
    }

    @Override
    public void unbindBidirectional(Property<ObservableList<T>> other) {
        Bindings.unbindBidirectional(this, other);
    }

    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("ConstrainedListProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && (!name.equals(""))) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get()).append("]");
        return result.toString();
    }

}
