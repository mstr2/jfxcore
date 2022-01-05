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

import com.sun.javafx.binding.Logging;
import com.sun.javafx.logging.PlatformLogger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.WritableFloatValue;
import org.jfxcore.beans.property.validation.PropertyHelper;

/**
 * Defines a constrained property that wraps a float value.
 *
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
public abstract class ConstrainedFloatProperty<D>
        extends ReadOnlyConstrainedFloatProperty<D>
        implements ConstrainedProperty<Number, D>, WritableFloatValue {

    /**
     * Creates a default {@code ConstrainedFloatProperty}.
     */
    protected ConstrainedFloatProperty() {
    }

    @Override
    public void setValue(Number v) {
        if (v == null) {
            var logger = Logging.getLogger();
            if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                logger.fine(
                    "Attempt to set float property to null, using default value instead.",
                    new NullPointerException());
            }

            set(0);
        } else {
            set(v.floatValue());
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

}
