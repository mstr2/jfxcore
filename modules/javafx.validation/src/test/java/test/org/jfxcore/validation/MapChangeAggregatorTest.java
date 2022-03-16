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

import org.jfxcore.validation.MapChangeAggregator;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MapChangeAggregatorTest {

    @Test
    public void testAddChange() {
        var aggregator = new MapChangeAggregator<Integer, String>();
        aggregator.addAdded(0, "foo");
        aggregator.addAdded(0, "foo");
        aggregator.addAdded(1, "bar");

        var change = aggregator.completeAggregatedChange();
        assertEquals(Map.of(0, "foo", 1, "bar"), change.getAdded());
        assertEquals(Set.of(), change.getRemoved());
    }

    @Test
    public void testRemoveChange() {
        var aggregator = new MapChangeAggregator<Integer, String>();
        aggregator.addRemoved(0);
        aggregator.addRemoved(0);
        aggregator.addRemoved(1);

        var change = aggregator.completeAggregatedChange();
        assertEquals(Map.of(), change.getAdded());
        assertEquals(Set.of(0, 1), change.getRemoved());
    }

    @Test
    public void testAddAndRemoveChange() {
        var aggregator = new MapChangeAggregator<Integer, String>();
        aggregator.addAdded(0, "foo");
        aggregator.addAdded(1, "bar");
        aggregator.addAdded(2, "baz");
        aggregator.addRemoved(0);

        var change = aggregator.completeAggregatedChange();
        assertEquals(Map.of(1, "bar", 2, "baz"), change.getAdded());
        assertEquals(Set.of(), change.getRemoved());
    }

    @Test
    public void testRemoveAndAddChange() {
        var aggregator = new MapChangeAggregator<Integer, String>();
        aggregator.addRemoved(0);
        aggregator.addAdded(1, "bar");
        aggregator.addAdded(0, "foo");

        var change = aggregator.completeAggregatedChange();
        assertEquals(Map.of(1, "bar"), change.getAdded());
        assertEquals(Set.of(), change.getRemoved());
    }

}
