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
import javafx.validation.ConstraintBase;
import javafx.validation.ListConstraint;
import javafx.validation.MapConstraint;
import javafx.validation.SetConstraint;

enum ConstraintType {

    SCALAR,
    LIST,
    SET,
    MAP;

    boolean checkType(ConstraintBase<?, ?> constraint) {
        if (constraint == null) {
            throw new NullPointerException("Constraint cannot be null.");
        }

        return switch (this) {
            case SCALAR -> {
                if (constraint instanceof Constraint) {
                    yield true;
                }

                throw new IllegalArgumentException(String.format(
                    "Illegal constraint type: expected = %s; actual = %s",
                    Constraint.class.getSimpleName(),
                    getClassName(constraint)));
            }

            case LIST -> constraint instanceof ListConstraint || checkConstraintClass(constraint, ListConstraint.class);

            case SET -> constraint instanceof SetConstraint || checkConstraintClass(constraint, SetConstraint.class);

            case MAP -> constraint instanceof MapConstraint || checkConstraintClass(constraint, MapConstraint.class);
        };
    }

    private static boolean checkConstraintClass(ConstraintBase<?, ?> constraint, Class<?> expectedClass) {
        if (!(constraint instanceof Constraint)) {
            throw new IllegalArgumentException(String.format(
                "Illegal constraint type: expected = %s; actual = %s",
                expectedClass.getSimpleName(),
                getClassName(constraint)));
        }

        return false;
    }

    private static String getClassName(ConstraintBase<?, ?> constraint) {
        if (constraint instanceof Constraint) {
            return Constraint.class.getSimpleName();
        }

        if (constraint instanceof ListConstraint) {
            return ListConstraint.class.getSimpleName();
        }

        if (constraint instanceof SetConstraint) {
            return SetConstraint.class.getSimpleName();
        }

        if (constraint instanceof MapConstraint) {
            return MapConstraint.class.getSimpleName();
        }

        return constraint.getClass().getName();
    }

}
