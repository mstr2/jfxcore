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

package org.jfxcore.validation;

import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import java.util.Map;

public class MapEmptyProperty extends ReadOnlyBooleanPropertyBase {

    private final Map<?, ?> map;
    private boolean empty;

    public MapEmptyProperty(Map<?, ?> map) {
        this.map = map;
        this.empty = map.isEmpty();
    }

    @Override
    public boolean get() {
        return empty;
    }

    @Override
    public Object getBean() {
        return map;
    }

    @Override
    public String getName() {
        return "empty";
    }

    @Override
    public void fireValueChangedEvent() {
        boolean newEmpty = map.isEmpty();
        if (empty != newEmpty) {
            empty = newEmpty;
            super.fireValueChangedEvent();
        }
    }

}
