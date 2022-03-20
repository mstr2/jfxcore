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
import javafx.beans.property.ListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.validation.ConstrainedElement;
import javafx.validation.Constraint;
import javafx.validation.ConstraintBase;
import javafx.validation.ValidationState;
import javafx.validation.property.ReadOnlyConstrainedListProperty;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adds list element validation capabilities to {@link ValidationHelper} to support
 * data validation for {@link ReadOnlyConstrainedListProperty}.
 *
 * @param <T> data type
 * @param <D> diagnostic type
 */
public class ListValidationHelper<T, D>
        extends ValidationHelper<ObservableList<T>, D>
        implements ElementValidationHelper<T, D> {

    private static final Constraint<?, ?>[] NO_CONSTRAINTS = new Constraint[0];

    private final Constraint<? super T, D>[] elementConstraints;
    private final ListChangeAggregator<T> listChangeAggregator;
    private ObservableList<ConstrainedElement<T, D>> elements;

    @SuppressWarnings("unchecked")
    public ListValidationHelper(
            ReadOnlyConstrainedListProperty<T, D> observable,
            DeferredListProperty<T> constrainedValue,
            ValidationState initialValidationState,
            ConstraintBase<? super T, D>[] constraints) {
        super(observable, constrainedValue, initialValidationState, constraints, ConstraintType.LIST);

        if (constraints != null && constraints.length > 0) {
            int elementConstraints = 0;
            for (var constraint : constraints) {
                if (constraint instanceof Constraint) {
                    elementConstraints++;
                }
            }

            this.elementConstraints = elementConstraints > 0 ?
                (Constraint<? super T, D>[])Array.newInstance(Constraint.class, elementConstraints) :
                (Constraint<? super T, D>[])NO_CONSTRAINTS;

            for (int i = 0, j = 0; i < constraints.length; ++i) {
                if (constraints[i] instanceof Constraint) {
                    this.elementConstraints[j++] = (Constraint<? super T, D>)constraints[i];
                }
            }
        } else {
            this.elementConstraints = (Constraint<? super T, D>[])NO_CONSTRAINTS;
        }

        listChangeAggregator = new ListChangeAggregator<>(constrainedValue);
    }

    @Override
    protected ReadOnlyConstrainedListProperty<T, D> getObservable() {
        return (ReadOnlyConstrainedListProperty<T, D>)super.getObservable();
    }

    /**
     * Called when a new validation run is requested.
     *
     * This method starts all list validators (which are handled by the {@link ValidationHelper} superclass),
     * as well as all element validators (which are handled by {@link ConstrainedElement}).
     */
    @Override
    protected void onStartValidation(Observable dependency, ObservableList<T> newValue) {
        super.onStartValidation(dependency, newValue);

        if (elementConstraints.length > 0) {
            for (ConstrainedElement<T, D> element : getElements()) {
                ConstrainedElementHelper.validate(element);
            }
        }
    }

    /**
     * Computes the combined validation state for the list as a whole, as well as all of its elements.
     */
    @Override
    protected ValidationState getValidationState() {
        return switch (super.getValidationState()) {
            case UNKNOWN -> ValidationState.UNKNOWN;
            case INVALID -> ValidationState.INVALID;
            case VALID -> {
                if (elements != null) {
                    for (ConstrainedElement<T, D> element : elements) {
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

    public ObservableList<ConstrainedElement<T, D>> getElements() {
        // We need to be careful to not validate the property here, so we can't call List methods
        // on getObservable() directly (doing so would validate the property)
        if (elements == null) {
            ObservableList<T> observableList = PropertyHelper.readValue(getObservable());
            if (observableList != null) {
                List<ConstrainedElement<T, D>> list = new ArrayList<>(observableList.size());

                for (T value : observableList) {
                    list.add(ConstrainedElementHelper.newInstance(value, this));
                }

                elements = FXCollections.observableList(list);
            } else {
                elements = FXCollections.observableArrayList();
            }
        }

        return elements;
    }

    public ListChange.ReplacedRange<T> completeListChange() {
        return listChangeAggregator.completeAggregatedChange();
    }

    /**
     * Called when a dependency or the observable itself has changed.
     *
     * Note that this method is also called when {@link ListProperty#setValue(ObservableList)} was called.
     * In this case, we need to discard all {@link ConstrainedElement} values and recreate them again
     * with the contents of the new list.
     */
    @Override
    public void invalidated(Observable dependency) {
        // We need to be careful to not validate the property here, so we can't call List methods
        // on getObservable() directly (doing so would validate the property)
        ReadOnlyConstrainedListProperty<T, D> observable = getObservable();
        if (dependency == observable) {
            ObservableList<T> observableList = PropertyHelper.readValue(observable);
            if (elements != null && elements.size() > 0) {
                listChangeAggregator.add(new ListChange.RemovedRange<>(0, elements.size()));
                elements.forEach(ConstrainedElementHelper::dispose);
                elements.clear();
            }

            if (observableList != null && observableList.size() > 0) {
                listChangeAggregator.add(new ListChange.AddedRange<>(0, observableList));

                if (elements != null) {
                    List<ConstrainedElement<T, D>> list = new ArrayList<>(observableList.size());
                    for (T value : observableList) {
                        list.add(ConstrainedElementHelper.newInstance(value, this));
                    }

                    elements.addAll(list);
                } else {
                    // Initializes the elements list and validates its elements.
                    getElements();
                }
            }
        }

        super.invalidated(dependency);
    }

    /**
     * Called when the source list has changed.
     *
     * This method invokes list validators (which are handled by the {@link ValidationHelper} superclass)
     * for the list as a whole, as well as element validators for all list elements.
     */
    public void invalidated(ListChangeListener.Change<? extends T> change) {
        beginQuiescence();

        ReadOnlyConstrainedListProperty<T, D> observable = getObservable();
        super.onStartValidation(observable, PropertyHelper.readValue(observable));

        if (elementConstraints.length == 0) {
            aggregateChanges(change);
        } else {
            validateElementsAndAggregateChanges(change);
        }

        endQuiescence();
    }

    @SuppressWarnings("unchecked")
    private void aggregateChanges(ListChangeListener.Change<? extends T> change) {
        while (change.next()) {
            int from = change.getFrom();
            int to = change.getTo();

            if (change.wasPermutated()) {
                listChangeAggregator.add(
                    new ListChange.ReplacedRange<>(from, to - from, (List<T>)change.getList().subList(from, to)));
            } else if (change.wasReplaced()) {
                if (change.getRemovedSize() == 1) {
                    T added = change.getList().get(from);
                    listChangeAggregator.add(new ListChange.ReplacedRange<>(from, 1, added));
                } else {
                    List<T> addedSubList = (List<T>)change.getAddedSubList();
                    listChangeAggregator.add(new ListChange.ReplacedRange<>(from, change.getRemovedSize(), addedSubList));
                }
            } else if (change.wasRemoved()) {
                listChangeAggregator.add(new ListChange.RemovedRange<>(from, change.getRemovedSize()));
            } else if (change.wasAdded()) {
                if (change.getAddedSize() == 1) {
                    T added = change.getList().get(from);
                    listChangeAggregator.add(new ListChange.AddedRange<>(from, added));
                } else {
                    List<T> addedSubList = (List<T>)change.getAddedSubList();
                    listChangeAggregator.add(new ListChange.AddedRange<>(from, addedSubList));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateElementsAndAggregateChanges(ListChangeListener.Change<? extends T> change) {
        ObservableList<ConstrainedElement<T, D>> elements = this.elements;
        if (elements == null) {
            elements = this.elements = FXCollections.observableArrayList();
        }

        while (change.next()) {
            int from = change.getFrom();
            int to = change.getTo();

            if (change.wasPermutated()) {
                listChangeAggregator.add(new ListChange.ReplacedRange<>(
                    from, to - from, (List<T>)change.getList().subList(from, to)));

                ConstrainedElement<T, D>[] constrainedElementRange = new ConstrainedElement[to - from];
                for (int oldIndex = from; oldIndex < to; oldIndex++) {
                    int newIndex = change.getPermutation(oldIndex) - from;
                    constrainedElementRange[newIndex] = elements.get(oldIndex);
                }

                elements.subList(from, to).clear();
                elements.addAll(from, Arrays.asList(constrainedElementRange));
            } else if (change.wasUpdated()) {
                for (int i = from; i < to; ++i) {
                    ConstrainedElementHelper.validate(elements.get(i));
                }
            } else if (change.wasReplaced()) {
                to = from;

                if (change.getRemovedSize() == 1) {
                    T added = change.getList().get(from);
                    listChangeAggregator.add(new ListChange.ReplacedRange<>(from, 1, added));
                    ConstrainedElement<T, D> removed = elements.get(from);
                    elements.remove(from);
                    elements.add(to++, ConstrainedElementHelper.newInstance(added, this));
                    ConstrainedElementHelper.dispose(removed);
                } else {
                    List<T> addedSubList = (List<T>)change.getAddedSubList();
                    listChangeAggregator.add(new ListChange.ReplacedRange<>(from, change.getRemovedSize(), addedSubList));

                    List<ConstrainedElement<T, D>> removed = elements.subList(from, from + change.getRemovedSize());
                    List<ConstrainedElement<T, D>> copy = new ArrayList<>(removed);
                    removed.clear();
                    copy.forEach(ConstrainedElementHelper::dispose);

                    for (T added : addedSubList) {
                        elements.add(to++, ConstrainedElementHelper.newInstance(added, this));
                    }
                }

                for (int i = from; i < to; ++i) {
                    ConstrainedElementHelper.validate(elements.get(i));
                }
            } else if (change.wasRemoved()) {
                if (change.getRemovedSize() == 1) {
                    listChangeAggregator.add(new ListChange.RemovedRange<>(from, change.getRemovedSize()));
                    ConstrainedElement<T, D> removed = elements.get(from);
                    elements.remove(from);
                    ConstrainedElementHelper.dispose(removed);
                } else {
                    listChangeAggregator.add(new ListChange.RemovedRange<>(from, change.getRemovedSize()));
                    List<ConstrainedElement<T, D>> removed = elements.subList(from, from + change.getRemovedSize());
                    List<ConstrainedElement<T, D>> copy = new ArrayList<>(removed);
                    removed.clear();
                    copy.forEach(ConstrainedElementHelper::dispose);
                }
            } else if (change.wasAdded()) {
                to = from;

                if (change.getAddedSize() == 1) {
                    T added = change.getList().get(from);
                    listChangeAggregator.add(new ListChange.AddedRange<>(from, added));
                    elements.add(to++, ConstrainedElementHelper.newInstance(added, this));
                } else {
                    List<T> addedSubList = (List<T>)change.getAddedSubList();
                    listChangeAggregator.add(new ListChange.AddedRange<>(from, addedSubList));
                    for (T added : addedSubList) {
                        elements.add(to++, ConstrainedElementHelper.newInstance(added, this));
                    }
                }

                for (int i = from; i < to; ++i) {
                    ConstrainedElementHelper.validate(elements.get(i));
                }
            }
        }
    }

}
