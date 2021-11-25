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

package javafx.scene.control.template;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

@DefaultProperty("cellTemplate")
public abstract class TemplatedCellFactory<T> {

    private ObjectProperty<Template<T>> cellTemplate;

    protected TemplatedCellFactory() {}

    protected TemplatedCellFactory(Template<T> cellTemplate) {
        setCellTemplate(cellTemplate);
    }

    public final void setCellTemplate(Template<T> value) {
        cellTemplateProperty().set(value);
    }

    public final Template<T> getCellTemplate() {
        return cellTemplate == null ? null : cellTemplate.get();
    }

    public final ObjectProperty<Template<T>> cellTemplateProperty() {
        if (cellTemplate == null) {
            cellTemplate = new SimpleObjectProperty<>(this, "cellTemplate");
        }
        return cellTemplate;
    }

}
