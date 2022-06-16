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

package javafx.scene.control.theme;

import javafx.beans.value.WritableValue;
import javafx.collections.ObservableListBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class StylesheetList extends ObservableListBase<String> {

    private final List<WritableValue<String>> items;
    private final List<String> values;
    private int lockCount;

    public StylesheetList() {
        items = new ArrayList<>();
        values = new ArrayList<>();
    }

    public StylesheetList(int initialCapacity) {
        items = new ArrayList<>(initialCapacity);
        values = new ArrayList<>(initialCapacity);
    }

    public void lock() {
        if (++lockCount == 1) {
            beginChange();
        };
    }

    public void unlock() {
        if (--lockCount == 0) {
            endChange();
        }
    }

    public WritableValue<String> addStylesheet(String value) {
        if (value != null) {
            values.add(value);
        }

        WritableValue<String> item = new ItemImpl(value);
        items.add(item);
        return item;
    }

    @Override
    public String get(int index) {
        return values.get(index);
    }

    @Override
    public int size() {
        return values.size();
    }

    private class ItemImpl implements WritableValue<String> {
        String currentValue;

        ItemImpl(String initialValue) {
            currentValue = initialValue;
        }

        @Override
        public String getValue() {
            return currentValue;
        }

        @Override
        public void setValue(String newValue) {
            if (Objects.equals(currentValue, newValue)) {
                return;
            }

            int index = 0;
            for (int i = 0, max = items.size(); i < max; ++i) {
                WritableValue<String> item = items.get(i);
                if (item == this) {
                    break;
                } else if (item.getValue() != null) {
                    ++index;
                }
            }

            if (lockCount == 0) {
                beginChange();
            }

            if (currentValue == null && newValue != null) {
                nextAdd(index, index + 1);
                values.add(index, newValue);
            } else if (currentValue != null && newValue == null) {
                nextRemove(index, currentValue);
                values.remove(index);
            } else if (currentValue != null) {
                nextReplace(index, index + 1, List.of(currentValue));
                values.set(index, newValue);
            }

            currentValue = newValue;

            if (lockCount == 0) {
                endChange();
            }
        }
    }

}
