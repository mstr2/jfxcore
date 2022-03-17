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
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.validation.ConstrainedElement;
import javafx.validation.Constraint;
import javafx.validation.ConstraintBase;
import javafx.validation.ValidationState;
import javafx.validation.property.ReadOnlyConstrainedMapProperty;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds map element validation capabilities to {@link ValidationHelper} to support
 * data validation for {@link ReadOnlyConstrainedMapProperty}.
 *
 * @param <K> key type
 * @param <V> value type
 * @param <D> diagnostic type
 */
public class MapValidationHelper<K, V, D>
        extends ValidationHelper<ObservableMap<K, V>, D>
        implements ElementValidationHelper<V, D> {

    private static final Constraint<?, ?>[] NO_CONSTRAINTS = new Constraint[0];

    private final Constraint<? super V, D>[] elementConstraints;
    private final MapChangeAggregator<K, V> mapChangeAggregator;
    private ObservableMap<K, ConstrainedElement<V, D>> elements;

    @SuppressWarnings("unchecked")
    public MapValidationHelper(
            ReadOnlyConstrainedMapProperty<K, V, D> observable,
            DeferredProperty<ObservableMap<K, V>> constrainedValue,
            ValidationState initialValidationState,
            ConstraintBase<? super V, D>[] constraints) {
        super(observable, constrainedValue, initialValidationState, constraints, ConstraintType.MAP);

        if (constraints != null && constraints.length > 0) {
            int elementConstraints = 0;
            for (var constraint : constraints) {
                if (constraint instanceof Constraint) {
                    elementConstraints++;
                }
            }

            this.elementConstraints = elementConstraints > 0 ?
                (Constraint<? super V, D>[])Array.newInstance(Constraint.class, elementConstraints) :
                (Constraint<? super V, D>[])NO_CONSTRAINTS;

            for (int i = 0, j = 0; i < constraints.length; ++i) {
                if (constraints[i] instanceof Constraint) {
                    this.elementConstraints[j++] = (Constraint<? super V, D>)constraints[i];
                }
            }
        } else {
            this.elementConstraints = (Constraint<? super V, D>[])NO_CONSTRAINTS;
        }

        mapChangeAggregator = new MapChangeAggregator<>();
    }

    @Override
    protected ReadOnlyConstrainedMapProperty<K, V, D> getObservable() {
        return (ReadOnlyConstrainedMapProperty<K, V, D>)super.getObservable();
    }

    @Override
    protected void onStartValidation(Observable dependency, ObservableMap<K, V> newValue) {
        super.onStartValidation(dependency, newValue);

        if (elementConstraints.length > 0) {
            for (ConstrainedElement<V, D> element : getElements().values()) {
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
                    for (ConstrainedElement<V, D> element : elements.values()) {
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
    public Constraint<? super V, D>[] getElementConstraints() {
        return elementConstraints;
    }

    public ObservableMap<K, ConstrainedElement<V, D>> getElements() {
        if (elements == null) {
            // We need to be careful to not validate the property here, so we can't call Map methods
            // on getObservable() directly (doing so would validate the property)
            ObservableMap<K, V> observableMap = PropertyHelper.readValue(getObservable());
            if (observableMap != null) {
                Map<K, ConstrainedElement<V, D>> map = new HashMap<>(observableMap.size());

                for (Map.Entry<K, V> entry : observableMap.entrySet()) {
                    map.put(entry.getKey(), ConstrainedElementHelper.newInstance(entry.getValue(), this));
                }

                elements = FXCollections.observableMap(map);
            } else {
                elements = FXCollections.observableHashMap();
            }
        }

        return elements;
    }

    public MapChange<K, V> completeMapChange() {
        return mapChangeAggregator.completeAggregatedChange();
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
        // We need to be careful to not validate the property here, so we can't call Map methods
        // on getObservable() directly (doing so would validate the property)
        ReadOnlyConstrainedMapProperty<K, V, D> observable = getObservable();
        if (dependency == observable) {
            ObservableMap<K, V> observableMap = PropertyHelper.readValue(observable);
            if (elements != null && elements.size() > 0) {
                for (Map.Entry<K, ConstrainedElement<V, D>> entry : elements.entrySet()) {
                    mapChangeAggregator.addRemoved(entry.getKey());
                    ConstrainedElementHelper.dispose(entry.getValue());
                }

                elements.clear();
            }

            if (observableMap != null && observableMap.size() > 0) {
                if (elements != null) {
                    for (Map.Entry<K, V> entry : observableMap.entrySet()) {
                        elements.put(entry.getKey(), ConstrainedElementHelper.newInstance(entry.getValue(), this));
                        mapChangeAggregator.addAdded(entry.getKey(), entry.getValue());
                    }
                } else {
                    for (Map.Entry<K, V> entry : observableMap.entrySet()) {
                        mapChangeAggregator.addAdded(entry.getKey(), entry.getValue());
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
    public void invalidated(MapChangeListener.Change<? extends K, ? extends V> change) {
        beginQuiescence();

        ReadOnlyConstrainedMapProperty<K, V, D> observable = getObservable();
        super.onStartValidation(observable, PropertyHelper.readValue(observable));

        if (elementConstraints.length == 0) {
            aggregateChanges(change);
        } else {
            validateElementsAndAggregateChanges(change);
        }

        endQuiescence();
    }

    private void aggregateChanges(MapChangeListener.Change<? extends K, ? extends V> change) {
        if (change.wasRemoved()) {
            mapChangeAggregator.addRemoved(change.getKey());
        }

        if (change.wasAdded()) {
            mapChangeAggregator.addAdded(change.getKey(), change.getValueAdded());
        }
    }

    private void validateElementsAndAggregateChanges(MapChangeListener.Change<? extends K, ? extends V> change) {
        ObservableMap<K, ConstrainedElement<V, D>> elements = this.elements;
        if (elements == null) {
            elements = this.elements = FXCollections.observableHashMap();
        }

        if (change.wasRemoved()) {
            mapChangeAggregator.addRemoved(change.getKey());
            ConstrainedElement<V, D> element = elements.remove(change.getKey());
            if (element != null) {
                ConstrainedElementHelper.dispose(element);
            }
        }

        if (change.wasAdded()) {
            mapChangeAggregator.addAdded(change.getKey(), change.getValueAdded());
            ConstrainedElement<V, D> element = ConstrainedElementHelper.newInstance(change.getValueAdded(), this);
            elements.put(change.getKey(), element);
            ConstrainedElementHelper.validate(element);
        }
    }

}
