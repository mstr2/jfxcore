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

import com.sun.javafx.binding.BidirectionalBinding;
import com.sun.javafx.binding.Logging;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.WritableIntegerValue;

import java.util.Objects;

/**
 * Implements a constrained property that wraps an integer value.
 *
 * @param <E> error information type
 * @since JFXcore 18
 */
public abstract class ConstrainedIntegerProperty<E>
        extends ReadOnlyConstrainedIntegerProperty<E>
        implements Property<Number>, WritableIntegerValue {

    /**
     * Creates a default {@code ConstrainedIntegerProperty}.
     */
    protected ConstrainedIntegerProperty() {
    }

    @Override
    public void setValue(Number v) {
        if (v == null) {
            Logging.getLogger().fine("Attempt to set integer property to null, using default value instead.", new NullPointerException());
            set(0);
        } else {
            set(v.intValue());
        }
    }

    @Override
    public void bindBidirectional(Property<Number> other) {
        Bindings.bindBidirectional(this, other);
    }

    @Override
    public void unbindBidirectional(Property<Number> other) {
        Bindings.unbindBidirectional(this, other);
    }

    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("ConstrainedIntegerProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && (!name.equals(""))) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get()).append("]");
        return result.toString();
    }

    /**
     * Returns a {@code ConstrainedIntegerProperty} that wraps a {@link Property}.
     * If the {@code Property} is a {@code ConstrainedIntegerProperty}, it will be returned directly.
     * Otherwise a new {@code ConstrainedIntegerProperty} is created that is bound to the {@code Property}.
     * <p>
     * Note: null values in the source property will be interpreted as zero.
     *
     * @param <E> error information type
     * @param property the source {@code Property}
     * @param constraints the value constraints
     * @return a {@code ConstrainedIntegerProperty} that wraps the {@code Property}
     * @throws NullPointerException if {@code property} is {@code null}
     * @since JFXcore 18
     */
    @SafeVarargs
    public static <E> ConstrainedIntegerProperty<E> integerProperty(
            Property<Number> property, Constraint<Number, E>... constraints) {
        Objects.requireNonNull(property, "Property cannot be null");

        return property instanceof ConstrainedIntegerProperty ? (ConstrainedIntegerProperty<E>)property :
            new ConstrainedIntegerPropertyBase<>(constraints) {
                {
                    BidirectionalBinding.bind(this, property);
                }

                @Override
                public Object getBean() {
                    return null; // Virtual property, no bean
                }

                @Override
                public String getName() {
                    return property.getName();
                }
            };
    }
    
}
