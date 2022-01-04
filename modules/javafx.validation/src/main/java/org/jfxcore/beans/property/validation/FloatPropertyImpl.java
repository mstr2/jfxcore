/*
 * Copyright (c) 2021, JFXcore. All rights reserved.
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

package org.jfxcore.beans.property.validation;

import javafx.beans.property.ReadOnlyFloatPropertyBase;

public abstract class FloatPropertyImpl
        extends ReadOnlyFloatPropertyBase implements WritableProperty<Number> {

    private float value;

    public FloatPropertyImpl(float initialValue) {
        this.value = initialValue;
    }

    @Override
    public float get() {
        return value;
    }

    public boolean set(float value) {
        if (this.value != value) {
            this.value = value;
            return true;
        }

        return false;
    }

    @Override
    public boolean setValue(Number value) {
        float newValue = value != null ? value.floatValue() : 0;

        if (this.value != newValue) {
            this.value = newValue;
            return true;
        }

        return false;
    }

    @Override
    public void fireValueChangedEvent() {
        super.fireValueChangedEvent();
    }

}
