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

import org.jfxcore.validation.SetChangeAggregator;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SetChangeAggregatorTest {

    @Test
    public void testAddChange() {
        var aggregator = new SetChangeAggregator<String>();
        aggregator.addAdded("foo");
        aggregator.addAdded("foo");
        aggregator.addAdded("bar");

        var change = aggregator.completeAggregatedChange();
        assertEquals(Set.of("foo", "bar"), change.getAdded());
        assertEquals(Set.of(), change.getRemoved());
    }

    @Test
    public void testRemoveChange() {
        var aggregator = new SetChangeAggregator<String>();
        aggregator.addRemoved("foo");
        aggregator.addRemoved("foo");
        aggregator.addRemoved("bar");

        var change = aggregator.completeAggregatedChange();
        assertEquals(Set.of(), change.getAdded());
        assertEquals(Set.of("foo", "bar"), change.getRemoved());
    }

    @Test
    public void testAddAndRemoveChange() {
        var aggregator = new SetChangeAggregator<String>();
        aggregator.addAdded("foo");
        aggregator.addAdded("bar");
        aggregator.addAdded("baz");
        aggregator.addRemoved("foo");

        var change = aggregator.completeAggregatedChange();
        assertEquals(Set.of("bar", "baz"), change.getAdded());
        assertEquals(Set.of(), change.getRemoved());
    }

    @Test
    public void testRemoveAndAddChange() {
        var aggregator = new SetChangeAggregator<String>();
        aggregator.addRemoved("foo");
        aggregator.addAdded("bar");
        aggregator.addAdded("foo");

        var change = aggregator.completeAggregatedChange();
        assertEquals(Set.of("bar"), change.getAdded());
        assertEquals(Set.of(), change.getRemoved());
    }

}
