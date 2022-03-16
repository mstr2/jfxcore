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
import javafx.collections.ObservableSet;
import javafx.util.Incubating;
import java.util.concurrent.Executor;

/**
 * Defines a data constraint for an {@link ObservableSet}.
 * This type of constraint applies to the set as a whole, and not to each of the elements individually.
 * Use {@link Constraint} to create a constraint that applies to each set element individually.
 * <p>
 * For a list of predefined constraints, see {@link Constraints}.
 *
 * @param <E> element type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public final class SetConstraint<E, D> extends Constraint<E, D> {

    public SetConstraint(
            Validator<? super ObservableSet<? super E>, D> validator,
            Executor completionExecutor,
            Observable[] dependencies) {
        super(validator, completionExecutor, dependencies, 0);
    }

}