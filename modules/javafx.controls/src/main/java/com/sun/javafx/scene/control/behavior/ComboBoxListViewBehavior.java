/*
 * Copyright (c) 2010, 2021, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022, JFXcore. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx.scene.control.behavior;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.SelectionModel;
import com.sun.javafx.scene.control.inputmap.InputMap;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.UP;

public class ComboBoxListViewBehavior<T> extends ComboBoxBaseBehavior<T> {

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     *
     */
    public ComboBoxListViewBehavior(final ComboBox<T> comboBox) {
        super(comboBox);

        // Add these bindings as a child input map, so they take precedence
        InputMap<ComboBoxBase<T>> comboBoxListViewInputMap = new InputMap<>(comboBox);
        comboBoxListViewInputMap.getMappings().addAll(
            new InputMap.KeyMapping(UP, e -> selectPrevious()),
            new InputMap.KeyMapping(DOWN, e -> selectNext())
        );
        addDefaultChildMap(getInputMap(), comboBoxListViewInputMap);
    }

    /***************************************************************************
     *                                                                         *
     * Key event handling                                                      *
     *                                                                         *
     **************************************************************************/

    private ComboBox<T> getComboBox() {
        return (ComboBox<T>) getNode();
    }

    private void selectPrevious() {
        SelectionModel<T> sm = getComboBox().getSelectionModel();
        if (sm == null) return;
        int oldIndex = sm.getSelectedIndex();
        sm.selectPrevious();
        if (oldIndex != sm.getSelectedIndex()) {
            getComboBox().setUserModified(true);
        }
    }

    private void selectNext() {
        SelectionModel<T> sm = getComboBox().getSelectionModel();
        if (sm == null) return;
        int oldIndex = sm.getSelectedIndex();
        sm.selectNext();
        if (oldIndex != sm.getSelectedIndex()) {
            getComboBox().setUserModified(true);
        }
    }
}
