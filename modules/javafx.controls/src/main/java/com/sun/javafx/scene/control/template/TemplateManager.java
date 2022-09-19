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

import javafx.scene.Node;
import javafx.scene.control.Template;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@code TemplateManager} is installed on a {@link Node} to receive notifications when templates
 * need to be re-applied. Implementations must override the {@link #onApplyTemplate()} method and
 * provide the logic to apply templates.
 */
public abstract class TemplateManager {

    /**
     * Tries to find a template in the scene graph that matches the specified item.
     */
    @SuppressWarnings("unchecked")
    public static <T> Template<? super T> find(Node node, T item) {
        return (Template<? super T>)TemplateObserver.findTemplate(node, item);
    }

    /**
     * Determines whether the template matches the specified item.
     */
    public static <T> boolean match(Template<? super T> template, T item) {
        Predicate<? super T> selector = template.getSelector();
        return selector == null || selector.test(item);
    }

    private final Runnable applyHandler = this::onApplyTemplate;
    private final TemplateObserver observer;
    private boolean disposed;

    public TemplateManager(Node node) {
        this.observer = TemplateObserver.acquire(Objects.requireNonNull(node, "node cannot be null"));
        this.observer.addApplyListener(applyHandler);
    }

    public final void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        observer.removeApplyListener(applyHandler);
        TemplateObserver.release(observer);
    }

    protected abstract void onApplyTemplate();

}
