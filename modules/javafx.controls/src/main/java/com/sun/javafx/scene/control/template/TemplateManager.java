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

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Template;
import java.util.Objects;

/**
 * {@code TemplateManager} is installed on a {@link Node} to receive notifications when templates
 * need to be re-applied. Implementations must override the {@link #onApplyTemplate()} method and
 * provide the logic to apply templates.
 */
public abstract class TemplateManager {

    /**
     * Tries to find a template in the scene graph above the specified node that matches the data object.
     * <p>
     * This method will inspect the {@link Node#getProperties()} map of the specified node and potentially
     * all of its parents to find a template that matches the data object.
     *
     * @param node the {@code Node} that will be inspected
     */
    @SuppressWarnings("unchecked")
    public static <T> Template<? super T> findTemplate(Node node, T data) {
        return (Template<? super T>)TemplateObserver.findTemplate(node, data);
    }

    private static final ObservableValue<Boolean> TRUE = new ObservableValue<>() {
        @Override public void addListener(ChangeListener<? super Boolean> listener) {}
        @Override public void removeListener(ChangeListener<? super Boolean> listener) {}
        @Override public Boolean getValue() { return Boolean.TRUE; }
        @Override public void addListener(InvalidationListener listener) {}
        @Override public void removeListener(InvalidationListener listener) {}
    };

    private final TemplateObserver.ReapplyListener reapplyListener = this::onApplyTemplate;
    private final InvalidationListener activeListener;
    private ObservableValue<Boolean> active;
    private TemplateObserver observer;
    private Node control;
    private boolean disposed;

    /**
     * Initializes a new instance of {@code TemplateManager}.
     *
     * @param control the control on which the {@code TemplateManager} is installed
     */
    public TemplateManager(Node control) {
        this(control, TRUE);
    }

    /**
     * Initializes a new instance of {@code TemplateManager}.
     *
     * @param control the control on which the {@code TemplateManager} is installed
     * @param active when the {@code active} value is {@code true}, the scene graph is observed
     *               for the presence of templates
     */
    public TemplateManager(Node control, ObservableValue<Boolean> active) {
        this.control = Objects.requireNonNull(control, "control cannot be null");
        this.active = Objects.requireNonNull(active, "active cannot be null");
        this.active.addListener(activeListener = observable -> onActiveChanged(this.active.getValue() == Boolean.TRUE));
        onActiveChanged(active.getValue() == Boolean.TRUE);
    }

    /**
     * Disposes all resources of this {@code TemplateManager}.
     */
    public void dispose() {
        if (disposed) {
            return;
        }

        if (observer != null) {
            TemplateObserver.release(observer);
            observer.removeListener(reapplyListener);
        }

        active.removeListener(activeListener);
        observer = null;
        control = null;
        active = null;
        disposed = true;
    }

    /**
     * Occurs when a template has been invalidated and needs to be re-applied.
     * <p>
     * Implementations of {@code TemplateManager} must override this method and provide the logic to
     * apply a template to the control. Often, this involves calling {@link #findTemplate(Node, Object)}
     * to select a template from the scene graph that can visualize the data object.
     */
    protected abstract void onApplyTemplate();

    private void onActiveChanged(boolean active) {
        if (active) {
            observer = TemplateObserver.acquire(control);
            observer.addListener(reapplyListener);
        } else if (observer != null) {
            TemplateObserver.release(observer);
            observer.removeListener(reapplyListener);
            observer = null;
        }
    }

}
