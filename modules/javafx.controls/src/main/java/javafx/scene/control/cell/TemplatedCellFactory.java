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

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Template;
import javafx.scene.TemplateContent;
import javafx.scene.control.Cell;
import javafx.scene.control.cell.behavior.CellBehavior;
import javafx.util.Callback;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

@DefaultProperty("cellTemplate")
public abstract class TemplatedCellFactory<T, V, C> implements Callback<V, C> {

    private final ObjectProperty<Template<T>> cellTemplate = new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
            return TemplatedCellFactory.this;
        }

        @Override
        public String getName() {
            return "cellTemplate";
        }

        @Override
        protected void invalidated() {
            get(); // needed to validate the property

            if (cache != null) {
                cache.refresh();
            }
        }
    };

    private Cache<V> cache;

    protected TemplatedCellFactory(Consumer<V> refreshMethod) {
        cache = new Cache<>(refreshMethod);
    }

    protected TemplatedCellFactory(Consumer<V> refreshMethod, Template<T> cellTemplate) {
        cache = new Cache<>(refreshMethod);
        setCellTemplate(cellTemplate);
    }

    public final ObjectProperty<Template<T>> cellTemplateProperty() {
        return cellTemplate;
    }

    public final void setCellTemplate(Template<T> value) {
        cellTemplate.set(value);
    }

    public final Template<T> getCellTemplate() {
        return cellTemplate.get();
    }

    @Override
    public final C call(V param) {
        cache = cache.add(param);
        return createCell(param);
    }

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

    abstract static class CellWrapper<T> {
        private final Cell<T> cell;
        private TemplateContent<T> currentTemplateContent;
        private Node currentTemplateNode;
        private T currentItem;

        CellWrapper(Cell<T> cell) {
            this.cell = cell;
        }

        protected abstract Node getControl();

        protected abstract Template<T> getTemplate();

        public void startEdit() {
            if (!cell.isEditing()) {
                return;
            }

            T item = cell.getItem();
            if (item != null && applyTemplate(item, true)) {
                onStartEdit(cell, currentTemplateNode);
            }
        }

        public void cancelEdit() {
            T item = cell.getItem();
            if (item != null) {
                Node lastTemplateNode = currentTemplateNode;
                if (lastTemplateNode != null && applyTemplate(item, false)) {
                    onCancelEdit(cell, lastTemplateNode);
                }
            }
        }

        public void commitEdit() {
            T item = cell.getItem();
            if (item != null) {
                Node lastTemplateNode = currentTemplateNode;
                if (lastTemplateNode != null && applyTemplate(item, false)) {
                    onCommitEdit(cell, lastTemplateNode);
                }
            }
        }

        public void updateItem(T item, boolean empty) {
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
                        if (currentNode == null || ! currentNode.equals(newNode)) {
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
            Template<T> template = getTemplate();

            if (template == null || !Template.match(template, item)) {
                template = listView != null ? Template.find(listView, item) : null;
            }

            if (template == null) {
                currentTemplateContent = null;
                currentTemplateNode = null;
                return false;
            }

            TemplateContent<T> content = CellTemplate.getContent(template, editing);

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

    static class Cache<U> {
        final Consumer<U> refreshMethod;

        public Cache(Consumer<U> refreshMethod) {
            this.refreshMethod = refreshMethod;
        }

        public void refresh() {}

        Cache<U> add(U newItem) {
            return new SingleCacheItem<>(refreshMethod, newItem);
        }
    }

    private static class SingleCacheItem<U> extends Cache<U> {
        private WeakReference<U> item;

        SingleCacheItem(Consumer<U> refreshMethod, U item) {
            super(refreshMethod);
            this.item = new WeakReference<>(item);
        }

        @Override
        public void refresh() {
            U ref = item.get();
            if (ref != null) {
                refreshMethod.accept(ref);
            }
        }

        @Override
        Cache<U> add(U newItem) {
            U existingItem = this.item.get();
            if (existingItem == null) {
                this.item = new WeakReference<>(newItem);
                return this;
            }

            if (existingItem != newItem) {
                var cache = new MultipleCacheItems<>(refreshMethod);
                cache.add(existingItem);
                cache.add(newItem);
                return cache;
            }

            return this;
        }
    }

    private static class MultipleCacheItems<U> extends Cache<U> {
        private final Set<U> items = Collections.newSetFromMap(new WeakHashMap<>(2));

        MultipleCacheItems(Consumer<U> refreshMethod) {
            super(refreshMethod);
        }

        @Override
        public void refresh() {
            for (U item : items) {
                refreshMethod.accept(item);
            }
        }

        @Override
        Cache<U> add(U item) {
            items.add(item);
            return this;
        }
    }

}
