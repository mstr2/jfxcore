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

import javafx.validation.Constraint;
import javafx.validation.ListConstraint;
import javafx.validation.MapConstraint;
import javafx.validation.SetConstraint;

enum ConstraintType {

    SCALAR,
    LIST,
    SET,
    MAP;

    boolean checkType(Constraint<?, ?> constraint) {
        if (constraint == null) {
            throw new NullPointerException("Constraint cannot be null.");
        }

        return switch (this) {
            case SCALAR -> {
                if (constraint.getClass() == Constraint.class) {
                    yield true;
                }

                throw new IllegalArgumentException(String.format(
                    "Illegal constraint: expected = %s; actual = %s",
                    Constraint.class.getSimpleName(),
                    constraint.getClass().getSimpleName()));
            }

            case LIST -> checkConstraintClass(constraint, ListConstraint.class);

            case SET -> checkConstraintClass(constraint, SetConstraint.class);

            case MAP -> checkConstraintClass(constraint, MapConstraint.class);
        };
    }

    private static boolean checkConstraintClass(Constraint<?, ?> constraint, Class<?> expectedClass) {
        var clazz = constraint.getClass();

        if (clazz == expectedClass) {
            return true;
        }

        if (clazz != Constraint.class) {
            throw new IllegalArgumentException(String.format(
                "Illegal constraint: expected = %s; actual = %s",
                expectedClass.getSimpleName(),
                constraint.getClass().getSimpleName()));
        }

        return false;
    }

}
