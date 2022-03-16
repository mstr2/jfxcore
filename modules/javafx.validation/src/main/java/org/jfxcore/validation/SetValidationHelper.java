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

package org.jfxcore.validation;

import javafx.beans.Observable;
import javafx.beans.property.SetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.validation.ConstrainedElement;
import javafx.validation.Constraint;
import javafx.validation.ValidationState;
import javafx.validation.property.ReadOnlyConstrainedSetProperty;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class SetValidationHelper<T, D>
        extends ValidationHelper<ObservableSet<T>, D>
        implements ElementValidationHelper<T, D> {

    private static final Constraint<?, ?>[] NO_CONSTRAINTS = new Constraint[0];

    private final Constraint<? super T, D>[] elementConstraints;
    private final SetChangeAggregator<T> setChangeAggregator;
    private ObservableMap<T, ConstrainedElement<T, D>> elements;

    @SuppressWarnings("unchecked")
    public SetValidationHelper(
            ReadOnlyConstrainedSetProperty<T, D> observable,
            DeferredProperty<ObservableSet<T>> constrainedValue,
            ValidationState initialValidationState,
            Constraint<? super T, D>[] constraints) {
        super(observable, constrainedValue, initialValidationState, constraints, ConstraintType.SET);

        if (constraints != null && constraints.length > 0) {
            int elementConstraints = 0;
            for (var constraint : constraints) {
                if (constraint.getClass() == Constraint.class) {
                    elementConstraints++;
                }
            }

            this.elementConstraints = elementConstraints > 0 ?
                (Constraint<? super T, D>[])Array.newInstance(Constraint.class, elementConstraints) :
                (Constraint<? super T, D>[])NO_CONSTRAINTS;

            for (int i = 0, j = 0; i < constraints.length; ++i) {
                if (constraints[i].getClass() == Constraint.class) {
                    this.elementConstraints[j++] = constraints[i];
                }
            }
        } else {
            this.elementConstraints = (Constraint<? super T, D>[])NO_CONSTRAINTS;
        }

        setChangeAggregator = new SetChangeAggregator<>();
    }

    @Override
    protected ReadOnlyConstrainedSetProperty<T, D> getObservable() {
        return (ReadOnlyConstrainedSetProperty<T, D>)super.getObservable();
    }

    @Override
    protected void onStartValidation(Observable dependency, ObservableSet<T> newValue) {
        super.onStartValidation(dependency, newValue);

        if (elementConstraints.length > 0) {
            for (ConstrainedElement<T, D> element : getElements().values()) {
                ConstrainedElementHelper.validate(element);
            }
        }
    }

    @Override
    protected ValidationState getValidationState() {
        return switch (super.getValidationState()) {
            case UNKNOWN -> ValidationState.UNKNOWN;
            case INVALID -> ValidationState.INVALID;
            case VALID -> {
                if (elements != null) {
                    for (ConstrainedElement<T, D> element : elements.values()) {
                        if (element.isInvalid()) {
                            yield ValidationState.INVALID;
                        }

                        if (!element.isValid()) {
                            yield ValidationState.UNKNOWN;
                        }
                    }
                }

                yield ValidationState.VALID;
            }
        };
    }

    @Override
    public Constraint<? super T, D>[] getElementConstraints() {
        return elementConstraints;
    }

    public ObservableMap<T, ConstrainedElement<T, D>> getElements() {
        if (elements == null) {
            // We need to be careful to not validate the property here, so we can't call Set methods
            // on getObservable() directly (doing so would validate the property)
            ObservableSet<T> observableSet = PropertyHelper.readValue(getObservable());
            if (observableSet != null) {
                Map<T, ConstrainedElement<T, D>> map = new HashMap<>(observableSet.size());

                for (T value : observableSet) {
                    map.put(value, ConstrainedElementHelper.newInstance(value, this));
                }

                elements = FXCollections.observableMap(map);
            } else {
                elements = FXCollections.observableHashMap();
            }
        }

        return elements;
    }

    public SetChange<T> completeSetChange() {
        return setChangeAggregator.completeAggregatedChange();
    }

    /**
     * Called when a dependency or the observable itself has changed.
     *
     * Note that this method is also called when {@link SetProperty#setValue(ObservableSet)} was called.
     * In this case, we need to discard all {@link ConstrainedElement} values and recreate them again
     * with the contents of the new map.
     */
    @Override
    public void invalidated(Observable dependency) {
        // We need to be careful to not validate the property here, so we can't call Set methods
        // on getObservable() directly (doing so would validate the property)
        ReadOnlyConstrainedSetProperty<T, D> observable = getObservable();
        if (dependency == observable) {
            ObservableSet<T> observableSet = PropertyHelper.readValue(observable);
            if (elements != null && elements.size() > 0) {
                for (Map.Entry<T, ConstrainedElement<T, D>> entry : elements.entrySet()) {
                    setChangeAggregator.addRemoved(entry.getKey());
                    ConstrainedElementHelper.dispose(entry.getValue());
                }

                elements.clear();
            }

            if (observableSet != null && observableSet.size() > 0) {
                if (elements != null) {
                    for (T value : observableSet) {
                        elements.put(value, ConstrainedElementHelper.newInstance(value, this));
                        setChangeAggregator.addAdded(value);
                    }
                } else {
                    for (T value : observableSet) {
                        setChangeAggregator.addAdded(value);
                    }

                    // Initializes the elements map and validates its values.
                    getElements();
                }
            }
        }

        super.invalidated(dependency);
    }

    /**
     * Called when the source set has changed.
     *
     * This method invokes set validators (which are handled by the {@link ValidationHelper} superclass)
     * for the set as a whole, as well as element validators for all set elements.
     */
    public void invalidated(SetChangeListener.Change<? extends T> change) {
        beginQuiescence();

        ReadOnlyConstrainedSetProperty<T, D> observable = getObservable();
        super.onStartValidation(observable, PropertyHelper.readValue(observable));

        if (elementConstraints.length == 0) {
            aggregateChanges(change);
        } else {
            validateElementsAndAggregateChanges(change);
        }

        endQuiescence();
    }

    private void aggregateChanges(SetChangeListener.Change<? extends T> change) {
        if (change.wasRemoved()) {
            setChangeAggregator.addRemoved(change.getElementRemoved());
        }

        if (change.wasAdded()) {
            setChangeAggregator.addAdded(change.getElementAdded());
        }
    }

    private void validateElementsAndAggregateChanges(SetChangeListener.Change<? extends T> change) {
        ObservableMap<T, ConstrainedElement<T, D>> elements = this.elements;
        if (elements == null) {
            elements = this.elements = FXCollections.observableHashMap();
        }

        if (change.wasRemoved()) {
            setChangeAggregator.addRemoved(change.getElementRemoved());
            ConstrainedElement<T, D> element = elements.remove(change.getElementRemoved());
            if (element != null) {
                ConstrainedElementHelper.dispose(element);
            }
        }

        if (change.wasAdded()) {
            setChangeAggregator.addAdded(change.getElementAdded());
            ConstrainedElement<T, D> element = ConstrainedElementHelper.newInstance(change.getElementAdded(), this);
            elements.put(change.getElementAdded(), element);
            ConstrainedElementHelper.validate(element);
        }
    }

}
