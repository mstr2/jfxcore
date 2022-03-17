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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetChangeAggregator<T> {

    private Set<T> removed;
    private Set<T> added;

    public void addRemoved(T element) {
        if (added instanceof HashSet<T> set && set.remove(element)) {
            return;
        }

        if (added != null && added.contains(element)) {
            added = null;
            return;
        }

        if (removed == null) {
            removed = Set.of(element);
        } else if (removed instanceof HashSet<T> set) {
            set.add(element);
        } else {
            Set<T> removed = new HashSet<>(2);
            removed.addAll(this.removed);
            removed.add(element);
            this.removed = removed;
        }
    }

    public void addAdded(T element) {
        if (removed instanceof HashSet<T> set && set.remove(element)) {
            return;
        }

        if (removed != null && removed.contains(element)) {
            removed = null;
            return;
        }

        if (added == null) {
            added = Set.of(element);
        } else if (added instanceof HashSet<T> set) {
            set.add(element);
        } else {
            Set<T> added = new HashSet<>(2);
            added.addAll(this.added);
            added.add(element);
            this.added = added;
        }
    }

    public SetChange<T> completeAggregatedChange() {
        var change = new SetChange<T>(
            removed != null ? removed : Collections.emptySet(),
            added != null ? added : Collections.emptySet());

        removed = null;
        added = null;
        return change;
    }

}
