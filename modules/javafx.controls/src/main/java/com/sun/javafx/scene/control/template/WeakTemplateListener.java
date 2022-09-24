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

import javafx.beans.WeakListener;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.control.Template;
import java.lang.ref.WeakReference;
import java.util.Objects;

public final class WeakTemplateListener implements TemplateListener, WeakListener {

    private final WeakReference<TemplateListener> ref;

    public WeakTemplateListener(TemplateListener listener) {
        this.ref = new WeakReference<>(
            Objects.requireNonNull(listener, "Listener must be specified."));
    }

    @Override
    public boolean wasGarbageCollected() {
        return ref.get() == null;
    }

    @Override
    public void onTemplateChanged(Template<?> template, ReadOnlyProperty<?> observable) {
        TemplateListener listener = ref.get();
        if (listener != null) {
            listener.onTemplateChanged(template, observable);
        } else {
            TemplateHelper.removeListener(template, this);
        }
    }


}
