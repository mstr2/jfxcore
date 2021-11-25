/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
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

package javafx.scene.control.template;

import javafx.scene.Node;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

@SuppressWarnings("DuplicatedCode")
public class TemplatedTreeTableCellFactory<S, T>
        extends TemplatedCellFactory<T>
        implements Callback<TreeTableView<S>, TreeTableCell<S, T>> {

    public TemplatedTreeTableCellFactory() {
        super();
    }

    public TemplatedTreeTableCellFactory(Template<T> cellTemplate) {
        super(cellTemplate);
    }
    
    @Override
    public TreeTableCell<S, T> call(TreeTableView<S> treeTableView) {
        return new TreeTableCell<>() {
            @Override
            public void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Node templateNode = applyTemplate(item, isEditing());

                    if (templateNode != null) {
                        setText(null);
                        setGraphic(templateNode);
                    } else if (item instanceof Node newNode) {
                        setText(null);
                        Node currentNode = getGraphic();
                        if (currentNode == null || ! currentNode.equals(newNode)) {
                            setGraphic(newNode);
                        }
                    } else {
                        setText(item.toString());
                        setGraphic(null);
                    }
                }
            }

            @SuppressWarnings("DuplicatedCode")
            private Node applyTemplate(T item, boolean editing) {
                TreeTableView<S> treeTableView = getTreeTableView();
                Template<T> template = getCellTemplate();

                if (template == null || !Template.match(template, item)) {
                    template = treeTableView != null ? Template.find(treeTableView, item) : null;
                }

                if (template == null) {
                    return null;
                }

                TemplateContent<T> content = Template.getContent(template, editing);
                if (content != null) {
                    return content.newInstance(item);
                }

                return null;
            }
        };
    }

}
