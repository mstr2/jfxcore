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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Template;
import javafx.scene.control.cell.TemplatedCellFactory;
import javafx.util.Callback;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@code TemplateManager} is installed on a {@link Node} to receive notifications when templates
 * need to be re-applied. Implementations must override the {@link #onApplyTemplate()} method and
 * provide the logic to apply templates.
 */
public abstract class TemplateManager {

    /**
     * Tries to find a template in the scene graph that matches the specified data object.
     */
    @SuppressWarnings("unchecked")
    public static <T> Template<? super T> find(Node node, T data) {
        return (Template<? super T>)TemplateObserver.findTemplate(node, data);
    }

    /**
     * Determines whether the template matches the specified item.
     */
    public static <T> boolean match(Template<? super T> template, T data) {
        Predicate<? super T> selector = template.getSelector();
        return selector == null || selector.test(data);
    }

    private final ChangeListener<Callback<?, ?>> cellFactoryChangeListener =
            (observable, oldValue, newValue) -> cellFactoryChanged(oldValue, newValue);

    private final WeakChangeListener<Callback<?, ?>> weakCellFactoryChangeListener =
            new WeakChangeListener<>(cellFactoryChangeListener);

    private final ChangeListener<Template<?>> templateChangeListener =
            (observable, oldValue, newValue) -> templateChanged(oldValue, newValue);

    private final WeakChangeListener<Template<?>> weakTemplateChangeListener =
            new WeakChangeListener<>(templateChangeListener);

    private final TemplateListener templateSubPropertyChangeListener =
            (template, observable) -> onApplyTemplate();

    private final WeakTemplateListener weakTemplateSubPropertyChangeListener =
            new WeakTemplateListener(templateSubPropertyChangeListener);

    private final ObservableValue<? extends Callback<?, ?>> cellFactory;
    private final TemplateReapplyListener reapplyHandler;
    private final TemplateObserver observer;
    private boolean disposed;

    /**
     * Initializes a new instance of {@code TemplateManager}.
     *
     * @param node the node on which the {@code TemplateManager} is installed
     * @param cellFactory the cell factory of the control
     */
    public TemplateManager(Node node, ObservableValue<? extends Callback<?, ?>> cellFactory) {
        this.observer = TemplateObserver.acquire(Objects.requireNonNull(node, "node cannot be null"));
        this.observer.addListener(reapplyHandler = () -> {
            // Applying an ambient template is only relevant if the control has a TemplatedCellFactory,
            // but no template is set. If a template is explicitly set on the TemplatedCellFactory, the
            // explicit template always takes precedence.
            if (cellFactory != null
                    && cellFactory.getValue() instanceof TemplatedCellFactory<?, ?, ?> templatedCellFactory
                    && templatedCellFactory.getCellTemplate() == null) {
                onApplyTemplate();
            }
        });

        // The following listeners observe the explicit template on the TemplatedCellFactory.
        // Ambient templates are observed by TemplateObserver.
        this.cellFactory = cellFactory;
        if (cellFactory != null) {
            cellFactory.addListener(weakCellFactoryChangeListener);
            if (cellFactory.getValue() instanceof TemplatedCellFactory<?, ?, ?> templatedCellFactory) {
                templatedCellFactory.cellTemplateProperty().addListener(weakTemplateChangeListener);
                Template<?> template = templatedCellFactory.getCellTemplate();
                if (template != null) {
                    TemplateHelper.addListener(template, weakTemplateSubPropertyChangeListener);
                }
            }
        }
    }

    public final void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;

        if (cellFactory instanceof TemplatedCellFactory<?, ?, ?> templatedCellFactory) {
            templatedCellFactory.cellTemplateProperty().removeListener(weakTemplateChangeListener);
            Template<?> template = templatedCellFactory.getCellTemplate();
            if (template != null) {
                TemplateHelper.removeListener(template, weakTemplateSubPropertyChangeListener);
            }
        }

        if (cellFactory != null) {
            cellFactory.removeListener(weakCellFactoryChangeListener);
        }

        observer.removeListener(reapplyHandler);
        TemplateObserver.release(observer);
    }

    /**
     * Occurs when a template (either the explicit {@link TemplatedCellFactory#cellTemplateProperty() cellTemplate}
     * or an ambient template in the scene graph) has changed in a way that makes it necessary to re-apply templates.
     */
    protected abstract void onApplyTemplate();

    private void cellFactoryChanged(Callback<?, ?> oldValue, Callback<?, ?> newValue) {
        Template<?> oldTemplate = null, newTemplate = null;

        if (oldValue instanceof TemplatedCellFactory<?, ?, ?> templatedCellFactory) {
            templatedCellFactory.cellTemplateProperty().removeListener(weakTemplateChangeListener);
            oldTemplate = templatedCellFactory.getCellTemplate();
        }

        if (newValue instanceof TemplatedCellFactory<?, ?, ?> templatedCellFactory) {
            templatedCellFactory.cellTemplateProperty().addListener(weakTemplateChangeListener);
            newTemplate = templatedCellFactory.getCellTemplate();
        }

        if (oldTemplate != newTemplate) {
            templateChanged(oldTemplate, newTemplate);
        }
    }

    private void templateChanged(Template<?> oldTemplate, Template<?> newTemplate) {
        if (oldTemplate != null) {
            TemplateHelper.removeListener(oldTemplate, weakTemplateSubPropertyChangeListener);
        }

        if (newTemplate != null) {
            TemplateHelper.addListener(newTemplate, weakTemplateSubPropertyChangeListener);
        }

        onApplyTemplate();
    }

}
