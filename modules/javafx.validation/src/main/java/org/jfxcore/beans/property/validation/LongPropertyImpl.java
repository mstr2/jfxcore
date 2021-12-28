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

import javafx.beans.property.ReadOnlyLongPropertyBase;

public abstract class LongPropertyImpl
        extends ReadOnlyLongPropertyBase implements ValidationHelper.WritableProperty<Number> {

    private long value;

    public LongPropertyImpl(long initialValue) {
        this.value = initialValue;
    }

    @Override
    public long get() {
        return value;
    }

    public boolean set(long value) {
        if (this.value != value) {
            this.value = value;
            return true;
        }

        return false;
    }

    @Override
    public boolean setValue(Number value) {
        long newValue = value != null ? value.longValue() : 0;

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
