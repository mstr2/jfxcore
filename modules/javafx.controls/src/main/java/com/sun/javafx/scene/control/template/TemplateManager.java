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
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Template;

/**
 * {@code TemplateManager} receives notifications when templates need to be re-applied by observing changes of
 * templates in the scene graph, or of explicitly specified template dependencies.
 */
public final class TemplateManager {

    private final Node control;
    private final ObservableValue<Boolean> active;
    private final ObservableValue<Template<?>>[] templates;
    private final Runnable applyHandler;
    private TemplateObserver.ReapplyListener reapplyListener;
    private InvalidationListener activeListener;
    private WeakChangeListener<Template<?>> weakTemplateChangeListener;
    private ChangeListener<Template<?>> templateChangeListener;
    private WeakTemplateListener weakTemplateListener;
    private TemplateListener templateListener;
    private TemplateObserver observer;
    private boolean disposed;

    /**
     * Initializes a new instance of {@code TemplateManager}.
     *
     * @param control the control that is observed for the presence of templates, or {@code null}
     * @param active when the {@code active} value is {@code true}, the scene graph is observed
     *               for the presence of templates; when {@code null} is specified, the value is
     *               assumed to be {@code true}
     * @param templates additional templates that are observed, or {@code null}
     * @param handler the handler that is invoked when templates need to be re-applied, or {@code null}
     */
    public TemplateManager(Node control,
                           ObservableValue<Boolean> active,
                           ObservableValue<Template<?>>[] templates,
                           Runnable handler) {
        this.applyHandler = handler != null ? handler : () -> {};
        this.control = control;
        this.templates = templates;
        this.active = active;

        if (active != null) {
            active.addListener(activeListener = observable -> onActiveChanged(this.active.getValue() == Boolean.TRUE));
        }

        if (templates != null && templates.length > 0) {
            templateChangeListener = this::onTemplateChanged;
            weakTemplateChangeListener = new WeakChangeListener<>(templateChangeListener);
            templateListener = (template, observable) -> applyHandler.run();
            weakTemplateListener = new WeakTemplateListener(templateListener);

            for (ObservableValue<Template<?>> template : templates) {
                template.addListener(weakTemplateChangeListener);
                Template<?> value = template.getValue();
                if (value != null) {
                    onTemplateChanged(template, null, value);
                }
            }
        }

        onActiveChanged(active == null || active.getValue() == Boolean.TRUE);
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

        if (templates != null) {
            for (ObservableValue<Template<?>> template : templates) {
                Template<?> value = template.getValue();
                if (value != null) {
                    TemplateHelper.removeListener(value, weakTemplateListener);
                }

                template.removeListener(weakTemplateChangeListener);
            }
        }

        if (activeListener != null) {
            active.removeListener(activeListener);
        }

        disposed = true;
    }

    private void onActiveChanged(boolean active) {
        if (active && control != null) {
            reapplyListener = applyHandler::run;
            observer = TemplateObserver.acquire(control);
            observer.addListener(reapplyListener);
        } else if (observer != null) {
            TemplateObserver.release(observer);
            observer.removeListener(reapplyListener);
            observer = null;
        }
    }

    private void onTemplateChanged(ObservableValue<? extends Template<?>> observable,
                                   Template<?> oldValue, Template<?> newValue) {
        if (oldValue != null) {
            TemplateHelper.removeListener(oldValue, weakTemplateListener);
        }

        if (newValue != null) {
            TemplateHelper.addListener(newValue, weakTemplateListener);
        }

        applyHandler.run();
    }

}
