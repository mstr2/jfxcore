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

import com.sun.javafx.util.Utils;
import javafx.scene.control.Template;

public final class TemplateHelper {

    static {
        Utils.forceInit(Template.class);
    }

    private static Accessor accessor;

    public static void setAccessor(Accessor accessor) {
        TemplateHelper.accessor = accessor;
    }

    public static void addListener(Template<?> template, TemplateListener listener) {
        accessor.addListener(template, listener);
    }

    public static void removeListener(Template<?> template, TemplateListener listener) {
        accessor.removeListener(template, listener);
    }

    public interface Accessor {
        void addListener(Template<?> template, TemplateListener listener);
        void removeListener(Template<?> template, TemplateListener listener);
    }

}
