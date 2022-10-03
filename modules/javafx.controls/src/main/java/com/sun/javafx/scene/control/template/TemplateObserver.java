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

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Template;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code TemplateObserver} is installed on a node (and all of its parents) to observe the scene graph
 * and notify downstream {@code TemplateObservers} when templates are added or removed from the node's
 * {@link Node#getProperties()} map, when a template was changed, or when the parent of a node in the
 * scene graph has changed. {@link TemplateManager} listens to these notifications and potentially
 * re-applies templates.
 */
public final class TemplateObserver implements MapChangeListener<Object, Object>,
                                               ChangeListener<Parent>,
                                               TemplateListener {

    /**
     * Acquires the {@code TemplateObserver} for the specified node.
     * If a {@code TemplateObserver} already exists, it is returned and its use count is increased.
     * Otherwise, a new {@code TemplateObserver} instance will be created.
     *
     * @param node the node for which a {@code TemplateObserver} should be acquired
     */
    public static TemplateObserver acquire(Node node) {
        TemplateObserver observer = getTemplateObserver(node);
        if (observer != null) {
            observer.useCount += 1;
            return observer;
        }

        observer = new TemplateObserver(node);
        observer.useCount  = 1;
        return observer;
    }

    /**
     * Releases a {@code TemplateObserver} that was acquired with {@link #acquire(Node)}.
     * This decreases the use count of the {@code TemplateObserver}.
     * If the use count reaches zero, the {@code TemplateObserver} is disposed.
     *
     * @param observer the {@code TemplateObserver} returned by {@link #acquire(Node)}
     */
    public static void release(TemplateObserver observer) {
        if (observer.useCount > 0) {
            observer.useCount -= 1;

            if (observer.useCount == 0) {
                observer.dispose();
            }
        }
    }

    /**
     * Tries to find a template that matches the specified data object on the specified node
     * or any of its parents.
     *
     * @param node the {@code Node}
     * @param data the data object
     * @return a {@code Template} instance or {@code null}
     */
    public static Template<?> findTemplate(Node node, Object data) {
        Node parent = node;
        while (parent != null) {
            TemplateObserver observer = getTemplateObserver(parent);
            Template<?> template =
                observer != null && observer.container != null ?
                observer.containerHelper.selectTemplate(observer.container, data) : null;

            if (template != null) {
                return template;
            } else {
                parent = parent.getParent();
            }
        }

        return null;
    }

    private final Node node;
    private final List<TemplateObserver> children = new ArrayList<>(2);
    private final TemplateContainerHelper containerHelper = new TemplateContainerHelper();
    private List<ReapplyListener> reapplyListeners;
    private Set<Template<?>> container;
    private int useCount;

    private TemplateObserver(Node node) {
        this.node = node;
        this.container = null;

        for (Map.Entry<Object, Object> entry : node.getProperties().entrySet()) {
            if (entry.getValue() instanceof Template<?> template) {
                if (container == null) {
                    container = new HashSet<>();
                }

                container.add(template);
                TemplateHelper.addListener(template, this);
            }
        }

        node.parentProperty().addListener(this);
        node.getProperties().addListener(this);

        if (node.getProperties().put(TemplateObserver.class, this) != null) {
            throw new IllegalStateException("TemplateObserver is already installed on " + node);
        }

        Node parent = node.getParent();
        if (parent != null) {
            connectToParent(parent);
        }
    }

    // package-private for testing
    int getUseCount() {
        return useCount;
    }

    // package-private for testing
    List<TemplateObserver> getChildren() {
        return children;
    }

    /**
     * Adds a listener that is invoked when a template in the scene graph may need to be reapplied.
     */
    public void addListener(ReapplyListener listener) {
        if (reapplyListeners == null) {
            reapplyListeners = new ArrayList<>(2);
        }

        reapplyListeners.add(listener);
    }

    /**
     * Removes a listener that was added via {@link #addListener(ReapplyListener)}.
     */
    public void removeListener(ReapplyListener listener) {
        if (reapplyListeners != null) {
            reapplyListeners.remove(listener);

            if (reapplyListeners.isEmpty()) {
                reapplyListeners = null;
            }
        }
    }

    private void dispose() {
        node.parentProperty().removeListener(this);
        node.getProperties().removeListener(this);
        node.getProperties().remove(TemplateObserver.class);

        for (Map.Entry<Object, Object> entry : node.getProperties().entrySet()) {
            if (entry.getValue() instanceof Template<?> template) {
                TemplateHelper.removeListener(template, this);
            }
        }

        Node parent = node.getParent();
        if (parent != null) {
            disconnectFromParent(parent);
        }
    }

    private void connectToParent(Node parent) {
        TemplateObserver parentObserver = getTemplateObserver(parent);
        if (parentObserver == null) {
            parentObserver = new TemplateObserver(parent);
            parent.getProperties().put(TemplateObserver.class, parentObserver);
        }

        parentObserver.children.add(this);
        parentObserver.useCount += 1;
    }

    private void disconnectFromParent(Node parent) {
        TemplateObserver parentObserver = getTemplateObserver(parent);
        parentObserver.children.remove(this);
        parentObserver.useCount -= 1;

        if (parentObserver.useCount == 0) {
            parentObserver.dispose();
        }
    }

    /**
     * Invoked when the {@link Node#getProperties()} map of the node that corresponds to this
     * {@code TemplateObserver has changed.
     */
    @Override
    public void onChanged(Change<?, ?> change) {
        boolean templatesChanged = false;

        if (container != null && change.wasRemoved() && change.getValueRemoved() instanceof Template<?> template) {
            container.remove(template);
            TemplateHelper.removeListener(template, this);
            templatesChanged = true;
        }

        if (change.wasAdded() && change.getValueAdded() instanceof Template<?> template) {
            if (container == null) {
                container = new HashSet<>();
            }

            container.add(template);
            TemplateHelper.addListener(template, this);
            templatesChanged = true;
        }

        if (templatesChanged) {
            if (container != null && container.isEmpty()) {
                container = null;
            }

            fireReapplyEvent();
        }
    }

    /**
     * Invoked when the {@link Node#parentProperty()} of the node that corresponds to this
     * {@code TemplateObserver} has changed.
     */
    @Override
    public void changed(ObservableValue<? extends Parent> observable, Parent oldParent, Parent newParent) {
        if (oldParent != null) {
            disconnectFromParent(oldParent);
        }

        if (newParent != null) {
            connectToParent(newParent);

            if (isAnyTemplateInSceneGraph()) {
                fireReapplyEvent();
            }
        }
    }

    /**
     * Invoked when a template has changed (i.e. one of its properties has changed).
     */
    @Override
    public void onTemplateChanged(Template<?> template, ReadOnlyProperty<?> observable) {
        fireReapplyEvent();
    }

    private void fireReapplyEvent() {
        if (reapplyListeners != null) {
            for (ReapplyListener listener : reapplyListeners) {
                listener.reapply();
            }
        }

        for (TemplateObserver childObserver : children) {
            childObserver.fireReapplyEvent();
        }
    }

    private boolean isAnyTemplateInSceneGraph() {
        if (container != null && !container.isEmpty()) {
            return true;
        }

        Node parent = node;
        while (parent != null) {
            TemplateObserver observer = getTemplateObserver(parent);
            if (observer != null && observer.container != null && !observer.container.isEmpty()) {
                return true;
            }

            parent = parent.getParent();
        }

        return false;
    }

    private static TemplateObserver getTemplateObserver(Node node) {
        if (node == null || !node.hasProperties()) {
            return null;
        }

        return (TemplateObserver)node.getProperties().get(TemplateObserver.class);
    }

    public interface ReapplyListener {
        void reapply();
    }

}
