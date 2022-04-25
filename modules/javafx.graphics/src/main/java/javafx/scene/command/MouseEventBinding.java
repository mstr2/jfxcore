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

package javafx.scene.command;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Incubating;

/**
 * Executes a command when a {@link MouseEvent} is received.
 *
 * @since JFXcore 18
 */
@Incubating
public class MouseEventBinding extends InputEventBinding<MouseEvent> {

    /**
     * The mouse button that is handled by this binding.
     */
    private final ObjectProperty<MouseButton> button = new SimpleObjectProperty<>(this, "button");

    /**
     * The number of clicks that are required to invoke the command.
     */
    private IntegerProperty clickCount;

    /**
     * Whether the {@code alt} key must be pressed to invoke the command.
     */
    private BooleanProperty altDown;

    /**
     * Whether the {@code control} key must be pressed to invoke the command.
     */
    private BooleanProperty controlDown;

    /**
     * Whether the {@code shift} key must be pressed to invoke the command.
     */
    private BooleanProperty shiftDown;

    /**
     * Whether the {@code meta} key must be pressed to invoke the command.
     */
    private BooleanProperty metaDown;

    /**
     * Whether the {@code shortcut} key must be pressed to invoke the command.
     * This corresponds to the {@code control} key on Windows, and the {@code meta} key on macOS.
     */
    private BooleanProperty shortcutDown;

    public MouseEventBinding(@NamedArg("command") Command command) {
        super(MouseEvent.MOUSE_CLICKED, command);
    }

    public MouseEventBinding(
            @NamedArg("eventType") EventType<MouseEvent> eventType,
            @NamedArg("command") Command command) {
        super(eventType, command);
    }

    public ObjectProperty<MouseButton> buttonProperty() {
        return button;
    }

    public MouseButton getButton() {
        return button.get();
    }

    public void setButton(MouseButton button) {
        this.button.set(button);
    }

    public IntegerProperty clickCountProperty() {
        return clickCount != null ?
                clickCount : (clickCount = new SimpleIntegerProperty(this, "clickCount", 1));
    }

    public int getClickCount() {
        return clickCount != null ? clickCount.get() : 1;
    }

    public void setClickCount(int value) {
        if (clickCount != null || value != 1) {
            clickCountProperty().set(value);
        }
    }

    public BooleanProperty altDownProperty() {
        return altDown != null ?
                altDown : (altDown = new SimpleBooleanProperty(this, "altDown"));
    }

    public boolean isAltDown() {
        return altDown != null && altDown.get();
    }

    public void setAltDown(boolean value) {
        if (altDown != null || value) {
            altDownProperty().set(value);
        }
    }

    public BooleanProperty controlDownProperty() {
        return controlDown != null ?
                controlDown : (controlDown = new SimpleBooleanProperty(this, "controlDown"));
    }

    public boolean isControlDown() {
        return controlDown != null && controlDown.get();
    }

    public void setControlDown(boolean value) {
        if (controlDown != null || value) {
            controlDownProperty().set(value);
        }
    }

    public BooleanProperty shiftDownProperty() {
        return shiftDown != null ?
                shiftDown : (shiftDown = new SimpleBooleanProperty(this, "shiftDown"));
    }

    public boolean isShiftDown() {
        return shiftDown != null && shiftDown.get();
    }

    public void setShiftDown(boolean value) {
        if (shiftDown != null || value) {
            shiftDownProperty().set(value);
        }
    }

    public BooleanProperty metaDownProperty() {
        return metaDown != null ?
                metaDown : (metaDown = new SimpleBooleanProperty(this, "metaDown"));
    }

    public boolean isMetaDown() {
        return metaDown != null && metaDown.get();
    }

    public void setMetaDown(boolean value) {
        if (metaDown != null || value) {
            metaDownProperty().set(value);
        }
    }

    public BooleanProperty shortcutDownProperty() {
        return shortcutDown != null ?
                shortcutDown : (shortcutDown = new SimpleBooleanProperty(this, "shortcutDown"));
    }

    public boolean isShortcutDown() {
        return shortcutDown != null && shortcutDown.get();
    }

    public void setShortcutDown(boolean value) {
        if (shortcutDown != null || value) {
            shortcutDownProperty().set(value);
        }
    }

    @Override
    protected void handleEvent(MouseEvent event) {
        if (event.getButton() == getButton()
                && event.getClickCount() == getClickCount()
                && event.isAltDown() == isAltDown()
                && event.isControlDown() == isControlDown()
                && event.isMetaDown() == isMetaDown()
                && event.isShiftDown() == isShiftDown()
                && event.isShortcutDown() == isShortcutDown()) {
            fireCommand(event);
        }
    }

}
