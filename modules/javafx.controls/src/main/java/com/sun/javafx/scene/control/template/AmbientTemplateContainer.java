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

package com.sun.javafx.scene.control.template;

import javafx.scene.Node;
import javafx.scene.control.Template;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * A container for ambient templates that are added to {@link Node#getProperties()}.
 * This class contains the logic to find a template that matches a given data object.
 */
public final class AmbientTemplateContainer extends HashSet<Template<?>> {

    private List<Template<?>> matches;

    /**
     * Finds a template that matches the specified data object by type and selector.
     * If several templates match the data object, this method returns the template with
     * the most derived data type.
     *
     * @see Template#getSelector()
     * @see Template#getDataType()
     * @param data the data object
     * @return a {@code Template} instance, or {@code null} if no matching template was found
     */
    @SuppressWarnings("unchecked")
    public Template<?> find(Object data) {
        Class<?> itemType = data.getClass();
        Template<?> match = null;
        boolean multipleMatches = false;

        for (Template<?> template : this) {
            if (!template.isAmbient() || !template.getDataType().isAssignableFrom(itemType)) {
                continue;
            }

            Predicate<Object> selector = (Predicate<Object>)template.getSelector();
            if (selector != null && !selector.test(data)) {
                continue;
            }

            if (match != null) {
                if (matches == null) {
                    matches = new ArrayList<>();
                }

                if (!multipleMatches) {
                    matches.add(match);
                }

                matches.add(template);
                multipleMatches = true;
            } else {
                match = template;
            }
        }

        Template<?> template = multipleMatches ? selectMostDerived(matches, matches.size()) : match;

        if (matches != null) {
            matches.clear();
        }

        return template;
    }

    private Template<?> selectMostDerived(List<Template<?>> list, int size) {
        if (size == 2) {
            Template<?> t1 = list.get(0);
            Template<?> t2 = list.get(1);
            return t1.getDataType().isAssignableFrom(t2.getDataType()) ? t2 : t1;
        }

        if (size == 1) {
            return list.get(0);
        }

        for (int i = 0, max = size - 1, insertPos = 0; i < max; i += 2, ++insertPos) {
            Template<?> t1 = list.get(i);
            Template<?> t2 = list.get(i + 1);
            list.set(insertPos, t1.getDataType().isAssignableFrom(t2.getDataType()) ? t2 : t1);
        }

        if (size % 2 == 1) {
            list.set(size / 2, list.get(size - 1));
        }

        return selectMostDerived(list, (int)Math.ceil(size / 2.0));
    }

}
