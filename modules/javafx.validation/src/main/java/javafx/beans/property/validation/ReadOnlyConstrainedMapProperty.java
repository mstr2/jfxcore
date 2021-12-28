/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
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

package javafx.beans.property.validation;

import javafx.beans.binding.MapExpression;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.collections.ObservableMap;

/**
 * Represents a {@link ReadOnlyConstrainedProperty} that wraps an {@link ObservableMap}.
 *
 * @param <K> key type
 * @param <V> value type
 * @param <E> error information type
 * @since JFXcore 18
 */
public abstract class ReadOnlyConstrainedMapProperty<K, V, E>
        extends MapExpression<K, V> implements ReadOnlyConstrainedProperty<ObservableMap<K, V>, E> {

    @Override
    public abstract ReadOnlyMapProperty<K, V> constrainedValueProperty();

}
