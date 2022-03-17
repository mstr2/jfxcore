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
import javafx.beans.binding.SetExpression;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.util.Incubating;
import javafx.validation.ConstrainedElement;
import javafx.validation.Constraint;
import javafx.validation.SetConstraint;

/**
 * Represents a {@link ReadOnlyConstrainedProperty} that wraps an {@link ObservableSet}.
 *
 * @param <E> element type
 * @param <D> diagnostic type
 * @since JFXcore 18
 */
@Incubating
public abstract class ReadOnlyConstrainedSetProperty<E, D>
        extends SetExpression<E> implements ReadOnlyConstrainedProperty<ObservableSet<E>, D>{

    /**
     * Represents a mapping of this set into a set of {@link ConstrainedElement} values.
     * Each {@code ConstrainedElement} holds the validation state of the corresponding set element.
     */
    public abstract ReadOnlyMapProperty<E, ConstrainedElement<E, D>> constrainedElementsProperty();

    /**
     * Gets the value of the {@link #constrainedElementsProperty() constrainedElements} property.
     */
    public ObservableMap<E, ConstrainedElement<E, D>> getConstrainedElements() {
        return constrainedElementsProperty().get();
    }

    /**
     * Contains a snapshot of the last set state that successfully completed validation, or {@code null}
     * if the unconstrained source set is {@code null}.
     * The snapshot is updated when all {@link SetConstraint} and {@link Constraint} validators
     * have successfully completed.
     * <p>
     * Note that the {@link ObservableSet} instance contained in this property (the constrained set
     * snapshot) is not the same instance as the unconstrained source set, therefore applications
     * should not rely on identity semantics when comparing the unconstrained source set and the
     * constrained set snapshot.
     */
    @Override
    public abstract ReadOnlySetProperty<E> constrainedValueProperty();

    @Override
    public String toString() {
        return PropertyHelper.toString(this);
    }

}
