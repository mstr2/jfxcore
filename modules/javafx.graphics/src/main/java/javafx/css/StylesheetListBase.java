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

package javafx.css;

import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.util.Incubating;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an {@link ObservableList} of stylesheet URLs with consistent ordering.
 * <p>
 * Implementations of {@code StylesheetListBase} should add all items in the list by calling
 * {@link #addStylesheet(String)}. The initial value of each item is either {@code null} or
 * a {@code String} value. When the list is enumerated, {@code null} values are not included.
 * <p>
 * In the following example, a stylesheet list is created that contains three items.
 * Each of the three items is exposed as a {@link StylesheetItem}, which can be modified
 * after the list is created.
 * <pre>{@code
 *     public class MyStylesheets extends StylesheetListBase {
 *         public final StylesheetItem base = addStylesheet("base.css");
 *         public final StylesheetItem colors = addStylesheet(null);
 *         public final StylesheetItem controls = addStylesheet("controls.css");
 *     }
 * }</pre>
 * Since the value of the {@code colors} item is {@code null}, the content of the
 * {@code MyStylesheets} list is {@code [base.css, controls.css]}.
 * <p>
 * When the value of the {@code colors} item is set, the content of the list is
 * {@code [base.css, colors.css, controls.css]}:
 * <pre>{@code
 *     var myStylesheets = new MyStylesheets();
 *     myStylesheets.colors.set("colors.css");
 * }</pre>
 *
 * @since JFXcore 18
 */
@Incubating
public abstract class StylesheetListBase extends ObservableListBase<String> {

    private final List<StylesheetItem> items;
    private final List<String> values;

    protected StylesheetListBase() {
        items = new ArrayList<>();
        values = new ArrayList<>();
    }

    protected StylesheetListBase(int initialCapacity) {
        items = new ArrayList<>(initialCapacity);
        values = new ArrayList<>(initialCapacity);
    }

    protected StylesheetItem addStylesheet(String value) {
        if (value != null) {
            values.add(value);
        }

        StylesheetItem item = new ItemImpl(value);
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

    private class ItemImpl implements StylesheetItem {
        String currentValue;

        ItemImpl(String initialValue) {
            currentValue = initialValue;
        }

        @Override
        public String get() {
            return currentValue;
        }

        @Override
        public void set(String newValue) {
            if (Objects.equals(currentValue, newValue)) {
                return;
            }

            int index = 0;
            for (int i = 0, max = items.size(); i < max; ++i) {
                StylesheetItem item = items.get(i);
                if (item == this) {
                    break;
                } else if (item.get() != null) {
                    ++index;
                }
            }

            beginChange();

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
            endChange();
        }
    }

}
