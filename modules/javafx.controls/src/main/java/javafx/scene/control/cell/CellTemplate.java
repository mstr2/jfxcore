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

import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.Node;
import javafx.scene.control.Template;
import javafx.scene.control.TemplateContent;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.behavior.CellBehavior;
import javafx.scene.control.cell.behavior.TextFieldCellBehavior;
import javafx.util.Incubating;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code CellTemplate} is a specialized template for cell-based controls.
 * <p>
 * It allows authors to specify an {@link #editingContentProperty() editingContent} that is used
 * in place of the regular {@link #contentProperty() content} when the cell is being edited.
 * <p>
 * The cell editing logic can be customized by adding a {@link CellBehavior} implementation to
 * {@link #getEditingBehaviors(Node)}. For example, {@link TextFieldCellBehavior} contains
 * logic to select all text of a {@link TextField} when cell editing is started.
 *
 * @param <T> the type of objects that can be visualized by this template
 * @since JFXcore 19
 */
@Incubating
public class CellTemplate<T> extends Template<T> {

    static final String EDITING_BEHAVIORS_KEY = CellBehavior.class.getName();

    /**
     * Gets the list of {@link CellBehavior cell behaviors} for the specified node.
     *
     * @param node the node which contains the cell behaviors
     * @return the list of cell behaviors for the specified node
     */
    @SuppressWarnings("unchecked")
    public static List<CellBehavior<? extends Node>> getEditingBehaviors(Node node) {
        var result = (List<CellBehavior<? extends Node>>)node.getProperties().get(EDITING_BEHAVIORS_KEY);
        if (result == null) {
            result = new ArrayList<>(1);
            node.getProperties().put(EDITING_BEHAVIORS_KEY, result);
        }

        return result;
    }

    /**
     * Initializes a new instance of {@code CellTemplate}.
     *
     * @param dataType the type of objects that can be visualized by this template
     */
    public CellTemplate(@NamedArg("dataType") Class<T> dataType) {
        super(dataType);
    }

    /**
     * Represents the editing content of the template as a function that creates the visual representation
     * for a given data object. The editing content represents the data object only if the cell is in edit mode;
     * if the cell is not in edit mode, {@link #contentProperty() content} is used instead.
     */
    private final ObjectProperty<TemplateContent<T>> editingContent = new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
            return CellTemplate.this;
        }

        @Override
        public String getName() {
            return "editingContent";
        }

        @Override
        protected void invalidated() {
            get(); // validate the property
            notifyTemplateChanged(this);
        }
    };

    public final ObjectProperty<TemplateContent<T>> editingContentProperty() {
        return editingContent;
    }

    public final TemplateContent<T> getEditingContent() {
        return editingContent.get();
    }

    public final void setEditingContent(TemplateContent<T> editingContent) {
        this.editingContent.set(editingContent);
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
