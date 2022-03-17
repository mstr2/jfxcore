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

import javafx.beans.property.ReadOnlyListProperty;
import javafx.validation.property.ReadOnlyConstrainedListProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates any number of {@link ListChange} events into a single {@link ListChange.ReplacedRange} event.
 *
 * The aggregated event is used to update {@link ReadOnlyConstrainedListProperty#constrainedValueProperty()}
 * when all constraint validators have successfully completed. Since there can be any number of changes to
 * the unconstrained source list before all constraints are satisfied, there is a risk that invalid intermediate
 * elements may be surfaced to user code when the recorded list changes are applied to update the constrained list.
 *
 * Aggregating all recorded list changes before updating the constrained list solves this problem, since
 * intermediate elements (i.e. elements that are added, but later removed) are not contained in the
 * aggregated change event.
 */
public class ListChangeAggregator<T> {

    private final ReadOnlyListProperty<T> source;
    private int from = -1;
    private int removeSize = 0;
    private List<T> added = new ArrayList<>(2);

    public ListChangeAggregator(ReadOnlyListProperty<T> source) {
        this.source = source;
    }

    /**
     * Returns the current aggregated change without completing the current aggregation run.
     * After calling this method, the resulting change must not be applied to the source list.
     */
    public ListChange.ReplacedRange<T> getAggregatedChange() {
        ListChange.ReplacedRange<T> change;

        if (source == null || removeSize == source.size() && added.size() == source.size() && source.equals(added)) {
            change = new ListChange.ReplacedRange<>(0, 0, Collections.emptyList());
        } else {
            change = new ListChange.ReplacedRange<>(from, removeSize, added);
        }

        return change;
    }

    /**
     * Completes the current aggregation run and returns the aggregated change.
     * After calling this method, the resulting change must be applied to the source list before
     * any new changes are added.
     */
    public ListChange.ReplacedRange<T> completeAggregatedChange() {
        ListChange.ReplacedRange<T> change = getAggregatedChange();
        removeSize = 0;
        from = -1;
        added = new ArrayList<>(2);
        return change;
    }

    /**
     * Adds a new list change to this aggregator.
     */
    public void add(ListChange<T> change) {
        if (source == null) {
            return;
        }

        if (change instanceof ListChange.AddedRange<T> c) {
            addRange(c.getFrom(), c.getElements());
        } else if (change instanceof ListChange.RemovedRange<T> c) {
            removeRange(c.getFrom(), c.getRemovedSize());
        } else if (change instanceof ListChange.ReplacedRange<T> c) {
            removeRange(c.getFrom(), c.getTo());
            addRange(c.getFrom(), c.getElements());
        }
    }

    private void addRange(int cFrom, List<T> elements) {
        if (from == -1) {
            from = cFrom;
            added.addAll(elements);
        } else if (cFrom <= from) {
            if (cFrom < from) {
                removeSize = Math.max(removeSize, from - cFrom + removeSize);
            }

            if (elements.size() == 1) {
                added.add(0, elements.get(0));
                added.addAll(1, source.subList(cFrom, from));
            } else {
                added.addAll(0, elements);
                added.addAll(elements.size(), source.subList(cFrom, from));
            }

            from = cFrom;
        } else if (cFrom <= from + added.size()) {
            if (elements.size() == 1) {
                added.add(cFrom - from, elements.get(0));
            } else {
                added.addAll(cFrom - from, elements);
            }
        } else {
            int sourceIndex = cFrom - added.size() + removeSize;
            added.addAll(source.subList(from + removeSize, sourceIndex));
            added.addAll(elements);
            removeSize = sourceIndex - from;
        }
    }

    private void removeRange(int cFrom, int cRemoveSize) {
        if (from == -1) {
            from = cFrom;
            removeSize = cRemoveSize;
        } else if (cFrom < from) {
            if (cFrom + cRemoveSize > from && added.size() > 0) {
                int addedFrom = from - cFrom;
                int addedTo = Math.min(added.size(), cRemoveSize - addedFrom);
                if (addedTo > 0) {
                    added.subList(0, addedTo).clear();
                    cRemoveSize -= addedTo;
                }
            }

            removeSize = Math.max(removeSize, cRemoveSize);
            from = cFrom;
        } else {
            int addedFrom = cFrom - from;
            int addedTo = Math.min(cRemoveSize + addedFrom, added.size());
            if (addedTo > addedFrom) {
                added.subList(addedFrom, addedTo).clear();
                cRemoveSize -= addedTo - addedFrom;
            }

            int sourceIndex = cFrom - added.size() + removeSize;
            int leading = sourceIndex - from + removeSize;
            if (leading > 0) {
                added.addAll(source.subList(from + removeSize, sourceIndex));
            }

            removeSize = Math.max(removeSize, sourceIndex + cRemoveSize - from);
        }
    }

}
