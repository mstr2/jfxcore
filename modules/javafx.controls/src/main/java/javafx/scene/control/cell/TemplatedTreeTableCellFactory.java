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
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableView;
import javafx.util.Incubating;

/**
 * Template-based cell factory for {@link TreeTableView}.
 *
 * @param <S> The type of the TreeTableView generic type
 * @param <T> The type of the item contained in the cell
 *
 * @since JFXcore 19
 */
@Incubating
public class TemplatedTreeTableCellFactory<S, T> extends TemplatedCellFactory<T, TreeTableView<T>, TreeTableCell<S, T>> {

    /**
     * Initializes a new instance of {@code TemplatedTreeTableCellFactory}.
     */
    public TemplatedTreeTableCellFactory() {}

    /**
     * Initializes a new instance of {@code TemplatedTreeTableCellFactory}.
     *
     * @param cellTemplate the cell template for this {@code TemplatedTreeTableCellFactory}
     */
    public TemplatedTreeTableCellFactory(@NamedArg("cellTemplate") Template<T> cellTemplate) {
        super(cellTemplate);
    }

    @Override
    public TreeTableCell<S, T> createCell(TreeTableView<T> listView) {
        return new TreeTableCell<>() {
            final CellWrapper<T> cellWrapper = new CellWrapper<>(this) {
                @Override
                protected Node getControl() {
                    return getTreeTableView();
                }

                @Override
                protected Template<? super T> getCellTemplate() {
                    return TemplatedTreeTableCellFactory.this.getCellTemplate();
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
