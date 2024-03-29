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

package javafx.validation.property;

import org.jfxcore.validation.PropertyHelper;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.util.Incubating;

/**
 * Represents a {@link ReadOnlyConstrainedProperty} that wraps an object value.
 *
 * @param <T> data type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public abstract class ReadOnlyConstrainedObjectProperty<T, D>
        extends ObjectExpression<T> implements ReadOnlyConstrainedProperty<T, D> {

    @Override
    public abstract ReadOnlyObjectProperty<T> constrainedValueProperty();

    @Override
    public String toString() {
        return PropertyHelper.toString(this);
    }

}
