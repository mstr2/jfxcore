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
import javafx.scene.Parent;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.Template;
import javafx.scene.control.TemplateContent;
import javafx.scene.control.cell.behavior.CellBehavior;
import javafx.util.Callback;
import javafx.util.Incubating;
import java.util.Collections;
import java.util.List;

/**
 * Base class for template-based cell factories.
 *
 * @see TemplatedListCellFactory
 * @see TemplatedTreeCellFactory
 *
 * @param <T> the item type
 * @param <V> the view type
 * @param <C> the cell type
 *
 * @since JFXcore 19
 */
@Incubating
public abstract class TemplatedCellFactory<T, V extends Control, C extends Cell<T>> implements Callback<V, C> {

    /**
     * Initializes a new instance of {@code TemplatedCellFactory}.
     */
    protected TemplatedCellFactory() {}

    @Override
    public final C call(V param) {
        return createCell(param);
    }

    /**
     * Creates a new cell instance that represents the specified data item.
     *
     * @param item the data item
     * @return a new cell instance
     */
    protected abstract C createCell(V item);

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void onStartEdit(Cell<?> cell, Node node) {
        List<CellBehavior<?>> behaviorList = node.hasProperties() ?
            (List<CellBehavior<?>>)node.getProperties().get(CellTemplate.EDITING_BEHAVIORS_KEY) :
            Collections.emptyList();

        for (CellBehavior behavior : behaviorList) {
            if (behavior != null && behavior.getNodeClass().isInstance(node)) {
                behavior.onStartEdit(cell, node);
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                onStartEdit(cell, child);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void onCancelEdit(Cell<?> cell, Node node) {
        List<CellBehavior<?>> behaviorList = node.hasProperties() ?
            (List<CellBehavior<?>>)node.getProperties().get(CellTemplate.EDITING_BEHAVIORS_KEY) :
            Collections.emptyList();

        for (CellBehavior behavior : behaviorList) {
            if (behavior != null && behavior.getNodeClass().isInstance(node)) {
                behavior.onCancelEdit(cell, node);
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                onCancelEdit(cell, child);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void onCommitEdit(Cell<?> cell, Node node) {
        List<CellBehavior<?>> behaviorList = node.hasProperties() ?
            (List<CellBehavior<?>>)node.getProperties().get(CellTemplate.EDITING_BEHAVIORS_KEY) :
            Collections.emptyList();

        for (CellBehavior behavior : behaviorList) {
            if (behavior != null && behavior.getNodeClass().isInstance(node)) {
                behavior.onCommitEdit(cell, node);
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                onCommitEdit(cell, child);
            }
        }
    }

    /**
     * Implementations of {@code TemplatedCellFactory} should use {@code CellWrapper} to support
     * cell templating by delegating to {@code CellWrapper} from the cells produced by the factory.
     * <p>
     * An implementation for {@link javafx.scene.control.ListView} looks like this:
     * <pre>{@code
     * public class TemplatedListCellFactory<T> extends TemplatedCellFactory<T, ListView<T>, ListCell<T>> {
     *     @Override
     *     protected ListCell<T> createCell(ListView<T> listView) {
     *         return new ListCell<>() {
     *             final CellWrapper<T> cellWrapper = new CellWrapper<>(this) {
     *                 @Override
     *                 protected Node getControl() {
     *                     return getListView();
     *                 }
     *             };
     *
     *             @Override
     *             public void startEdit() {
     *                 super.startEdit();
     *                 cellWrapper.startEdit();
     *             }
     *
     *             @Override
     *             public void cancelEdit() {
     *                 super.cancelEdit();
     *                 cellWrapper.cancelEdit();
     *             }
     *
     *             @Override
     *             public void commitEdit(T newValue) {
     *                 super.commitEdit(newValue);
     *                 cellWrapper.commitEdit();
     *             }
     *
     *             @Override
     *             public void updateItem(T item, boolean empty) {
     *                 super.updateItem(item, empty);
     *                 cellWrapper.updateItem(item, empty);
     *             }
     *         };
     *     }
     * }
     * }</pre>
     */
    protected abstract static class CellWrapper<T> {
        private final Cell<T> cell;
        private TemplateContent<? super T> currentTemplateContent;
        private Node currentTemplateNode;
        private T currentItem;

        /**
         * Initializes a new instance of {@code CellWrapper}.
         *
         * @param cell the wrapped cell
         */
        protected CellWrapper(Cell<T> cell) {
            this.cell = cell;
        }

        /**
         * Returns the control that hosts the cells.
         *
         * @return the control
         */
        protected abstract Node getControl();

        /**
         * {@link Cell} implementations should delegate to this method from {@link Cell#startEdit()}:
         *
         * <pre>{@code
         *     @Override
         *     public void startEdit() {
         *         super.startEdit();
         *         cellWrapper.startEdit();
         *     }
         * }</pre>
         */
        public final void startEdit() {
            if (!cell.isEditing()) {
                return;
            }

            T item = cell.getItem();
            if (item != null && applyTemplate(item, true)) {
                onStartEdit(cell, currentTemplateNode);
            }
        }

        /**
         * {@link Cell} implementations should delegate to this method from {@link Cell#cancelEdit()}:
         *
         * <pre>{@code
         *     @Override
         *     public void cancelEdit() {
         *         super.cancelEdit();
         *         cellWrapper.cancelEdit();
         *     }
         * }</pre>
         */
        public final void cancelEdit() {
            T item = cell.getItem();
            if (item != null) {
                Node lastTemplateNode = currentTemplateNode;
                if (lastTemplateNode != null && applyTemplate(item, false)) {
                    onCancelEdit(cell, lastTemplateNode);
                }
            }
        }

        /**
         * {@link Cell} implementations should delegate to this method from {@link Cell#commitEdit(Object)}:
         *
         * <pre>{@code
         *     @Override
         *     public void commitEdit(T newValue) {
         *         super.commitEdit(newValue);
         *         cellWrapper.commitEdit(newValue);
         *     }
         * }</pre>
         */
        public final void commitEdit() {
            T item = cell.getItem();
            if (item != null) {
                Node lastTemplateNode = currentTemplateNode;
                if (lastTemplateNode != null && applyTemplate(item, false)) {
                    onCommitEdit(cell, lastTemplateNode);
                }
            }
        }

        /**
         * {@link Cell} implementations should delegate to this method from {@link Cell#updateItem(Object, boolean)}:
         *
         * <pre>{@code
         *     @Override
         *     public void updateItem(T item, boolean empty) {
         *         super.updateItem(item, empty);
         *         cellWrapper.updateItem(item, empty);
         *     }
         * }</pre>
         */
        public final void updateItem(T item, boolean empty) {
            if (item == null || empty) {
                cell.setText(null);
                cell.setGraphic(null);
                currentTemplateContent = null;
                currentTemplateNode = null;
            } else {
                if (!applyTemplate(item, cell.isEditing())) {
                    if (item instanceof Node newNode) {
                        cell.setText(null);
                        Node currentNode = cell.getGraphic();
                        if (currentNode == null || !currentNode.equals(newNode)) {
                            cell.setGraphic(newNode);
                        }
                    } else {
                        cell.setText(item.toString());
                        cell.setGraphic(null);
                    }
                }
            }

            currentItem = item;
        }

        private boolean applyTemplate(T item, boolean editing) {
            Node listView = getControl();
            Template<? super T> selectedTemplate = listView != null ? Template.find(listView, item) : null;
            if (selectedTemplate == null) {
                currentTemplateContent = null;
                currentTemplateNode = null;
                return false;
            }

            TemplateContent<? super T> content = CellTemplate.getContent(selectedTemplate, editing);
            if (content == null) {
                currentTemplateContent = null;
                currentTemplateNode = null;
                return false;
            }

            if (content != currentTemplateContent || item != currentItem) {
                currentTemplateContent = content;
                currentTemplateNode = content.newInstance(item);
                cell.setGraphic(currentTemplateNode);
                cell.setText(null);
            }

            return true;
        }
    }

}
