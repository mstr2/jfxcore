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

package javafx.scene.control.cell;

import javafx.scene.Node;
import javafx.scene.Template;
import javafx.scene.TemplateContent;
import javafx.scene.control.cell.behavior.CellBehavior;
import java.util.ArrayList;
import java.util.List;

public class CellTemplate<T> extends Template<T> {

    static final String EDITING_BEHAVIORS_KEY = CellBehavior.class.getName();

    @SuppressWarnings("unchecked")
    public static List<CellBehavior<? extends Node>> getEditingBehaviors(Node node) {
        var result = (List<CellBehavior<? extends Node>>)node.getProperties().get(EDITING_BEHAVIORS_KEY);
        if (result == null) {
            result = new ArrayList<>(1);
            node.getProperties().put(EDITING_BEHAVIORS_KEY, result);
        }

        return result;
    }

    private TemplateContent<T> editingContent;

    public TemplateContent<T> getEditingContent() {
        return editingContent;
    }

    public void setEditingContent(TemplateContent<T> editingContent) {
        this.editingContent = editingContent;
    }

    static <T> TemplateContent<T> getContent(Template<T> template, boolean editing) {
        if (editing && template instanceof CellTemplate<T> cellTemplate) {
            TemplateContent<T> content = cellTemplate.getEditingContent();
            if (content != null) {
                return content;
            }
        }

        return template.getContent();
    }

}
