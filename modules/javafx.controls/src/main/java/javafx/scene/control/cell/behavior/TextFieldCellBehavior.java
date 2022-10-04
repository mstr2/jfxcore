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

package javafx.scene.control.cell.behavior;

import javafx.scene.control.Cell;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CellTemplate;
import javafx.scene.input.KeyCode;
import javafx.util.Incubating;

/**
 * {@code TextFieldCellBehavior} adds several default behaviors to a {@link TextField}
 * that is placed in a {@link CellTemplate}.
 *
 * @since JFXcore 19
 */
@Incubating
public class TextFieldCellBehavior extends CellBehavior<TextField> {

    /**
     * Initializes a new instance of {@code TextFieldCellBehavior}.
     */
    public TextFieldCellBehavior() {
        super(TextField.class);
    }

    @Override
    public void onStartEdit(Cell<Object> cell, TextField textField) {
        textField.requestFocus();
        textField.selectAll();

        textField.setOnAction(event -> {
            cell.commitEdit(textField.getText());
            event.consume();
        });

        textField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                event.consume();
            }
        });
    }

}
