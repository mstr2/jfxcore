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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapChangeAggregator<K, V> {

    private Set<K> removed;
    private Map<K, V> added;

    public void addRemoved(K key) {
        if (added instanceof HashMap<K, V> map && map.containsKey(key)) {
            map.remove(key);
            return;
        }

        if (added != null && added.containsKey(key)) {
            added = null;
            return;
        }

        if (removed == null) {
            removed = Set.of(key);
        } else if (removed instanceof HashSet<K> set) {
            set.add(key);
        } else {
            Set<K> removed = new HashSet<>(2);
            removed.addAll(this.removed);
            removed.add(key);
            this.removed = removed;
        }
    }

    public void addAdded(K key, V value) {
        if (removed instanceof HashSet<K> set && set.contains(key)) {
            set.remove(key);
            return;
        }

        if (removed != null && removed.contains(key)) {
            removed = null;
            return;
        }

        if (added == null) {
            added = Map.of(key, value);
        } else if (added instanceof HashMap<K, V> map) {
            map.put(key, value);
        } else {
            Map<K, V> added = new HashMap<>(2);
            added.putAll(this.added);
            added.put(key, value);
            this.added = added;
        }
    }

    public MapChange<K, V> completeAggregatedChange() {
        var change = new MapChange<>(
            removed != null ? removed : Collections.emptySet(),
            added != null ? added : Collections.emptyMap());

        removed = null;
        added = null;
        return change;
    }

}
