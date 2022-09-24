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
import javafx.scene.Node;
import javafx.scene.control.Template;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Incubating;

/**
 * Template-based cell factory for {@link TreeView}.
 *
 * @param <T> the item type
 *
 * @since JFXcore 19
 */
@Incubating
public final class TemplatedTreeCellFactory<T> extends TemplatedCellFactory<T, TreeView<T>, TreeCell<T>> {

    /**
     * Initializes a new instance of {@code TemplatedTreeCellFactory}.
     */
    public TemplatedTreeCellFactory() {}

    /**
     * Initializes a new instance of {@code TemplatedTreeCellFactory}.
     *
     * @param cellTemplate the cell template for this {@code TemplatedTreeCellFactory}
     */
    public TemplatedTreeCellFactory(@NamedArg("cellTemplate") Template<T> cellTemplate) {
        super(cellTemplate);
    }

    @Override
    protected TreeCell<T> createCell(TreeView<T> listView) {
        return new TreeCell<>() {
            final CellWrapper<T> cellWrapper = new CellWrapper<>(this) {
                @Override
                protected Node getControl() {
                    return getTreeView();
                }

                @Override
                protected Template<? super T> getCellTemplate() {
                    return TemplatedTreeCellFactory.this.getCellTemplate();
                }
            };

            @Override
            public void startEdit() {
                super.startEdit();
                cellWrapper.startEdit();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                cellWrapper.cancelEdit();
            }

            @Override
            public void commitEdit(T newValue) {
                super.commitEdit(newValue);
                cellWrapper.commitEdit();
            }

            @Override
            public void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                cellWrapper.updateItem(item, empty);
            }
        };
    }

}
