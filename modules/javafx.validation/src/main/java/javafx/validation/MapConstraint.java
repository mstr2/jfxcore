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

package javafx.validation;

import javafx.beans.Observable;
import javafx.collections.ObservableMap;
import javafx.util.Incubating;
import java.util.concurrent.Executor;

/**
 * Defines a data constraint for an {@link ObservableMap}.
 * This type of constraint applies to the map as a whole, and not to each of the elements individually.
 * Use {@link Constraint} to create a constraint that applies to each map element individually.
 * <p>
 * For a list of predefined constraints, see {@link Constraints}.
 *
 * @param <V> element type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public final class MapConstraint<K, V, D> extends Constraint<V, D> {

    public MapConstraint(
            Validator<? super ObservableMap<? super K, ? super V>, D> validator,
            Executor completionExecutor,
            Observable[] dependencies) {
        super(validator, completionExecutor, dependencies, 0);
    }

}