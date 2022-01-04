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
import com.sun.javafx.logging.PlatformLogger;
import org.jfxcore.beans.property.validation.PropertyHelper;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.WritableDoubleValue;
import java.util.Objects;

/**
 * Defines a constrained property that wraps a double value.
 *
 * @param <E> error information type
 * @since JFXcore 18
 */
public abstract class ConstrainedDoubleProperty<E>
        extends ReadOnlyConstrainedDoubleProperty<E>
        implements ConstrainedProperty<Number, E>, WritableDoubleValue {

    /**
     * Creates a default {@code ConstrainedDoubleProperty}.
     */
    protected ConstrainedDoubleProperty() {
    }

    @Override
    public void setValue(Number v) {
        if (v == null) {
            var logger = Logging.getLogger();
            if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                logger.fine(
                    "Attempt to set double property to null, using default value instead.",
                    new NullPointerException());
            }

            set(0);
        } else {
            set(v.doubleValue());
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
        return PropertyHelper.toString(this);
    }
    
    /**
     * Returns a {@code ConstrainedDoubleProperty} that wraps a {@link Property}.
     * If the {@code Property} is a {@code ConstrainedDoubleProperty}, it will be returned directly.
     * Otherwise a new {@code ConstrainedDoubleProperty} is created that is bound to the {@code Property}.
     * <p>
     * Note: null values in the source property will be interpreted as zero.
     *
     * @param <E> error information type
     * @param property the source {@code Property}
     * @param constraints the value constraints
     * @return a {@code ConstrainedDoubleProperty} that wraps the {@code Property}
     * @throws NullPointerException if {@code property} is {@code null}
     * @since JFXcore 18
     */
    @SafeVarargs
    public static <E> ConstrainedDoubleProperty<E> doubleProperty(
            Property<Number> property, Constraint<Number, E>... constraints) {
        Objects.requireNonNull(property, "Property cannot be null");

        return property instanceof ConstrainedDoubleProperty ? (ConstrainedDoubleProperty<E>)property :
            new ConstrainedDoublePropertyBase<>(constraints) {
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
