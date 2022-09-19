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

import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.cell.CellTemplate;
import javafx.util.Incubating;

/**
 * Base class for behaviors that run when a {@link Cell}'s editing mode is started,
 * cancelled or when the changes are committed.
 * <p>
 * {@code CellBehavior} is a mechanism to customize the behavior of a control that is placed
 * in a {@link CellTemplate}. It can be added to any control within a {@code CellTemplate} by
 * adding it to the control's static {@link CellTemplate#getEditingBehaviors(Node)} list.
 *
 * @param <T> the type of the templated node
 * @since JFXcore 19
 */
@Incubating
public abstract class CellBehavior<T extends Node> {

    private final Class<?> nodeClass;

    /**
     * Initializes a new instance of {@code CellBehavior}.
     *
     * @param nodeClass the type of the templated node
     */
    protected CellBehavior(Class<T> nodeClass) {
        this.nodeClass = nodeClass;
    }

    /**
     * Gets the type of the templated node.
     *
     * @return the {@code Class} of the node
     */
    public final Class<?> getNodeClass() {
        return nodeClass;
    }

    /**
     * Occurs when cell editing has started.
     *
     * @param cell the cell
     * @param node the templated node
     */
    public void onStartEdit(Cell<Object> cell, T node) {}

    /**
     * Occurs when cell editing was cancelled.
     *
     * @param cell the cell
     * @param node the templated node
     */
    public void onCancelEdit(Cell<Object> cell, T node) {}

    /**
     * Occurs when cell editing was completed by committing the changes.
     *
     * @param cell the cell
     * @param node the templated node
     */
    public void onCommitEdit(Cell<Object> cell, T node) {}

}
