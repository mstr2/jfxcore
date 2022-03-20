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

package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.input.InputNode;
import javafx.util.Incubating;

/**
 * Base class for all button-like controls that support data input.
 *
 * @since JFXcore 18
 */
@Incubating
public abstract class InputButtonBase extends ButtonBase implements InputNode {

    /**
     * Creates a default InputButtonBase with empty text.
     */
    public InputButtonBase() { }

    /**
     * Creates a InputButtonBase with the given text.
     * @param text null text is treated as the empty string
     */
    public InputButtonBase(String text) {
        super(text);
    }

    /**
     * Creates a InputButtonBase with the given text and graphic.
     * @param text null text is treated as the empty string
     * @param graphic a null graphic is acceptable
     */
    public InputButtonBase(String text, Node graphic) {
        super(text, graphic);
    }

    /**
     * Indicates whether the value represented by the control was changed as a result of user input.
     *
     * @defaultValue false
     */
    private BooleanProperty userModified;

    public final BooleanProperty userModifiedProperty() {
        if (userModified == null) {
            userModified = new SimpleBooleanProperty(this, "userModified");
        }
        return userModified;
    }

    public final boolean isUserModified() {
        return userModified != null && userModified.get();
    }

    public final void setUserModified(boolean value) {
        if (value || userModified != null) {
            userModifiedProperty().set(value);
        }
    }

}
