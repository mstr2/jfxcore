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

import javafx.validation.ConstrainedElement;

public class ConstrainedElementHelper {

    private static Accessor accessor;

    static {
        try {
            Class.forName(ConstrainedElement.class.getName(), true, ConstrainedElement.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public static void setAccessor(Accessor accessor) {
        ConstrainedElementHelper.accessor = accessor;
    }

    static <T, D> ConstrainedElement<T, D> newInstance(T value, ElementValidationHelper<T, D> validationHelper) {
        return accessor.newInstance(value, validationHelper);
    }

    static <T, D> void dispose(ConstrainedElement<T, D> element) {
        accessor.dispose(element);
    }

    static  <T, D> void validate(ConstrainedElement<T, D> element) {
        accessor.validate(element);
    }

    public interface Accessor {
        <T, D> ConstrainedElement<T, D> newInstance(T value, ElementValidationHelper<T, D> validationHelper);
        <T, D> void dispose(ConstrainedElement<T, D> element);
        <T, D> void validate(ConstrainedElement<T, D> element);
    }

}
