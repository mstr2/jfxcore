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

package test.org.jfxcore.validation;

import org.jfxcore.validation.ListChange;
import org.jfxcore.validation.ListChangeAggregator;
import org.junit.jupiter.api.Test;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ListChangeAggregatorTest {

    private <T> void assertState(ListChangeAggregator<T> aggregator, int from, int removeSize, List<T> added) {
        ListChange.ReplacedRange<T> change = aggregator.getAggregatedChange();
        assertEquals(from, change.getFrom(), "from");
        assertEquals(removeSize, change.getRemovedSize(), "removeSize");
        assertEquals(added, change.getElements(), "added");
    }

    private <T> void assertAppliedChange(ListChangeAggregator<T> aggregator, List<T> source, List<T> expected) {
        List<T> sourceCopy = new ArrayList<>(source);
        ListChange.ReplacedRange<T> change = aggregator.getAggregatedChange();
        sourceCopy.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
        sourceCopy.addAll(change.getFrom(), change.getElements());
        assertEquals(expected, sourceCopy);
    }

    private ReadOnlyListProperty<String> listProperty(String list) {
        return new SimpleListProperty<>(FXCollections.observableList(list(list)));
    }

    private List<String> list(String list) {
        return list.isEmpty() ? Collections.emptyList() : Arrays.asList(list.split(" "));
    }

    @Test
    public void testAddChange() {
        // [0 1 2 3 4 5]
        var source = listProperty("0 1 2 3 4 5");
        var aggregator = new ListChangeAggregator<>(source);

        // 0 1 2 +[a b c] 3 4 5
        aggregator.add(new ListChange.AddedRange<>(3, list("a b c")));
        assertState(aggregator, 3, 0, list("a b c"));
        assertAppliedChange(aggregator, source, list("0 1 2 a b c 3 4 5"));

        // 0 +[d] 1 2 a b c 3 4 5
        aggregator.add(new ListChange.AddedRange<>(1, "d"));
        assertState(aggregator, 1, 2, list("d 1 2 a b c"));
        assertAppliedChange(aggregator, source, list("0 d 1 2 a b c 3 4 5"));

        // 0 d 1 2 +[x y] a b c 3 4 5
        aggregator.add(new ListChange.AddedRange<>(4, list("x y")));
        assertState(aggregator, 1, 2, list("d 1 2 x y a b c"));
        assertAppliedChange(aggregator, source, list("0 d 1 2 x y a b c 3 4 5"));

        // 0 d 1 2 x y a b c 3 4 +[q] 5
        aggregator.add(new ListChange.AddedRange<>(11, "q"));
        assertState(aggregator, 1, 4, list("d 1 2 x y a b c 3 4 q"));
        assertAppliedChange(aggregator, source, list("0 d 1 2 x y a b c 3 4 q 5"));

        // 0 d 1 2 x y a b c 3 4 q 5 +[z]
        aggregator.add(new ListChange.AddedRange<>(13, list("z")));
        assertState(aggregator, 1, 5, list("d 1 2 x y a b c 3 4 q 5 z"));
        assertAppliedChange(aggregator, source, list("0 d 1 2 x y a b c 3 4 q 5 z"));
    }

    @Test
    public void testRemoveLeftOfPartlyCoveredSublist() {
        // 0 1 2 3 4 5
        var source = listProperty("0 1 2 3 4 5");
        var aggregator = new ListChangeAggregator<>(source);

        // 0 1 2 +[a b] 3 4 5
        aggregator.add(new ListChange.AddedRange<>(3, list("a b")));
        assertState(aggregator, 3, 0, list("a b"));
        assertAppliedChange(aggregator, source, list("0 1 2 a b 3 4 5"));

        // 0 -[1 2 a] b 3 4 5
        aggregator.add(new ListChange.RemovedRange<>(1, 3));
        assertState(aggregator, 1, 2, list("b"));
        assertAppliedChange(aggregator, source, list("0 b 3 4 5"));
    }

    @Test
    public void testRemoveLeftOfFullyCoveredSublist() {
        // 0 1 2 3 4 5
        var source = listProperty("0 1 2 3 4 5");
        var aggregator = new ListChangeAggregator<>(source);

        // 0 1 2 +[a b] 3 4 5
        aggregator.add(new ListChange.AddedRange<>(3, list("a b")));
        assertState(aggregator, 3, 0, list("a b"));
        assertAppliedChange(aggregator, source, list("0 1 2 a b 3 4 5"));

        // 0 -[1 2 a b 3] 4 5
        aggregator.add(new ListChange.RemovedRange<>(1, 5));
        assertState(aggregator, 1, 3, list(""));
        assertAppliedChange(aggregator, source, list("0 4 5"));
    }

    @Test
    public void testRemoveSublistSections() {
        // Remove the leading part of a sublist
        var source = listProperty("0 1 2 3");
        var aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b c d e] 2 3
        aggregator.add(new ListChange.AddedRange<>(2, list("a b c d e")));
        assertState(aggregator, 2, 0, list("a b c d e"));
        assertAppliedChange(aggregator, source, list("0 1 a b c d e 2 3"));

        // 0 1 -[a b c d] e 2 3
        aggregator.add(new ListChange.RemovedRange<>(2, 4));
        assertState(aggregator, 2, 0, list("e"));
        assertAppliedChange(aggregator, source, list("0 1 e 2 3"));

        // Remove the middle part of a sublist
        source = listProperty("0 1 2 3");
        aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b c d e] 2 3
        aggregator.add(new ListChange.AddedRange<>(2, list("a b c d e")));
        assertState(aggregator, 2, 0, list("a b c d e"));
        assertAppliedChange(aggregator, source, list("0 1 a b c d e 2 3"));

        // 0 1 a -[b c d] e 2 3
        aggregator.add(new ListChange.RemovedRange<>(3, 3));
        assertState(aggregator, 2, 0, list("a e"));
        assertAppliedChange(aggregator, source, list("0 1 a e 2 3"));

        // Remove the trailing part of a sublist
        source = listProperty("0 1 2 3");
        aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b c d e] 2 3
        aggregator.add(new ListChange.AddedRange<>(2, list("a b c d e")));
        assertState(aggregator, 2, 0, list("a b c d e"));
        assertAppliedChange(aggregator, source, list("0 1 a b c d e 2 3"));

        // 0 1 a b -[c d e] 2 3
        aggregator.add(new ListChange.RemovedRange<>(4, 3));
        assertState(aggregator, 2, 0, list("a b"));
        assertAppliedChange(aggregator, source, list("0 1 a b 2 3"));
    }

    @Test
    public void testRemoveSublistEntirely() {
        // 0 1 2 3
        var source = listProperty("0 1 2 3");
        var aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b c d e] 2 3
        aggregator.add(new ListChange.AddedRange<>(2, list("a b c d e")));
        assertState(aggregator, 2, 0, list("a b c d e"));
        assertAppliedChange(aggregator, source, list("0 1 a b c d e 2 3"));

        // 0 1 -[a b c d e] 2 3
        aggregator.add(new ListChange.RemovedRange<>(2, 5));
        assertState(aggregator, 2, 0, list(""));
        assertAppliedChange(aggregator, source, list("0 1 2 3"));
    }

    @Test
    public void testRemoveRightOfPartlyCoveredSublist() {
        // Remove the trailing element of a sublist, including adjacent elements
        // 0 1 2 3 4 5
        var source = listProperty("0 1 2 3 4 5");
        var aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b] 2 3 4 5
        aggregator.add(new ListChange.AddedRange<>(2, list("a b")));
        assertState(aggregator, 2, 0, list("a b"));
        assertAppliedChange(aggregator, source, list("0 1 a b 2 3 4 5"));

        // 0 1 a -[b 2 3 4] 5
        aggregator.add(new ListChange.RemovedRange<>(3, 4));
        assertState(aggregator, 2, 3, list("a"));
        assertAppliedChange(aggregator, source, list("0 1 a 5"));

        // Remove several trailing elements of a sublist, including adjacent elements
        source = listProperty("0 1 2 3 4 5");
        aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b c d] 2 3 4 5
        aggregator.add(new ListChange.AddedRange<>(2, list("a b c d")));
        assertState(aggregator, 2, 0, list("a b c d"));
        assertAppliedChange(aggregator, source, list("0 1 a b c d 2 3 4 5"));

        // 0 1 a -[b c d 2] 3 4 5
        aggregator.add(new ListChange.RemovedRange<>(3, 4));
        assertState(aggregator, 2, 1, list("a"));
        assertAppliedChange(aggregator, source, list("0 1 a 3 4 5"));
    }

    @Test
    public void testRemoveRightOfSublist() {
        // Remove some elements to the right of a sublist
        var source = listProperty("0 1 2 3 4 5");
        var aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b] 2 3 4 5
        aggregator.add(new ListChange.AddedRange<>(2, list("a b")));
        assertState(aggregator, 2, 0, list("a b"));
        assertAppliedChange(aggregator, source, list("0 1 a b 2 3 4 5"));

        // 0 1 a b 2 -[3 4] 5
        aggregator.add(new ListChange.RemovedRange<>(5, 2));
        assertState(aggregator, 2, 3, list("a b 2"));
        assertAppliedChange(aggregator, source, list("0 1 a b 2 5"));

        // Remove all elements to the right of a sublist
        source = listProperty("0 1 2 3 4 5");
        aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b] 2 3 4 5
        aggregator.add(new ListChange.AddedRange<>(2, list("a b")));
        assertState(aggregator, 2, 0, list("a b"));
        assertAppliedChange(aggregator, source, list("0 1 a b 2 3 4 5"));

        // 0 1 a b -[2 3 4 5]
        aggregator.add(new ListChange.RemovedRange<>(4, 4));
        assertState(aggregator, 2, 4, list("a b"));
        assertAppliedChange(aggregator, source, list("0 1 a b"));
    }

    @Test
    public void testRepeatedAddAndRemoveChanges() {
        var source = listProperty("0 1 2 3 4 5");
        var aggregator = new ListChangeAggregator<>(source);

        // 0 1 +[a b] 2 3 4 5
        aggregator.add(new ListChange.AddedRange<>(2, list("a b")));
        assertState(aggregator, 2, 0, list("a b"));
        assertAppliedChange(aggregator, source, list("0 1 a b 2 3 4 5"));

        // -[0 1 a b 2 3] 4 5
        aggregator.add(new ListChange.RemovedRange<>(0, 6));
        assertState(aggregator, 0, 4, list(""));
        assertAppliedChange(aggregator, source, list("4 5"));

        // 4 +[x y z] 5
        aggregator.add(new ListChange.AddedRange<>(1, list("x y z")));
        assertState(aggregator, 0, 5, list("4 x y z"));
        assertAppliedChange(aggregator, source, list("4 x y z 5"));

        // 4 x -[y z 5]
        aggregator.add(new ListChange.RemovedRange<>(2, 3));
        assertState(aggregator, 0, 6, list("4 x"));
        assertAppliedChange(aggregator, source, list("4 x"));

        // -[4 x]
        aggregator.add(new ListChange.RemovedRange<>(0, 2));
        assertState(aggregator, 0, 6, list(""));
        assertAppliedChange(aggregator, source, list(""));

        // +[0 1 2 3 4 5]
        aggregator.add(new ListChange.AddedRange<>(0, list("0 1 2 3 4 5")));
        assertState(aggregator, 0, 0, list(""));
        assertAppliedChange(aggregator, source, list("0 1 2 3 4 5"));
    }

}
