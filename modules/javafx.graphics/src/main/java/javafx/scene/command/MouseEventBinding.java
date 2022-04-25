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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Incubating;

/**
 * {@code Bindings} associate scene graph events with commands.
 *
 * @since JFXcore 18
 */
@Incubating
public class MouseEventBinding extends EventBinding<MouseEvent> {

    public MouseEventBinding() {}

    private ObjectProperty<MouseButton> button;

    public final ObjectProperty<MouseButton> buttonProperty() {
        return button != null ? button :
            (button = new SimpleObjectProperty<>(this, "button"));
    }

    public final MouseButton getButton() {
        return button != null ? button.get() : null;
    }

    public final void setButton(MouseButton button) {
        buttonProperty().set(button);
    }

    private ObjectProperty<Integer> clickCount;

    public final ObjectProperty<Integer> clickCountProperty() {
        return clickCount != null ? clickCount :
            (clickCount = new SimpleObjectProperty<>(this, "clickCount"));
    }

    public final Integer getClickCount() {
        return clickCount != null ? clickCount.get() : null;
    }

    public final void setClickCount(Integer clickCount) {
        clickCountProperty().set(clickCount);
    }

    private ObjectProperty<Boolean> shiftDown;

    public final ObjectProperty<Boolean> shiftDownProperty() {
        return shiftDown != null ? shiftDown :
            (shiftDown = new SimpleObjectProperty<>(this, "shiftDown"));
    }

    public final Boolean isShiftDown() {
        return shiftDown != null ? shiftDown.get() : null;
    }

    public final void setShiftDown(Boolean shiftDown) {
        shiftDownProperty().set(shiftDown);
    }

    private ObjectProperty<Boolean> controlDown;

    public final ObjectProperty<Boolean> controlDownProperty() {
        return controlDown != null ? controlDown :
            (controlDown = new SimpleObjectProperty<>(this, "controlDown"));
    }

    public final Boolean isControlDown() {
        return controlDown != null ? controlDown.get() : null;
    }

    public final void setControlDown(Boolean controlDown) {
        controlDownProperty().set(controlDown);
    }

    private ObjectProperty<Boolean> altDown;

    public final ObjectProperty<Boolean> altDownProperty() {
        return altDown != null ? altDown :
            (altDown = new SimpleObjectProperty<>(this, "altDown"));
    }

    public final Boolean isAltDown() {
        return altDown != null ? altDown.get() : null;
    }

    public final void setAltDown(Boolean altDown) {
        altDownProperty().set(altDown);
    }

    private ObjectProperty<Boolean> metaDown;

    public final ObjectProperty<Boolean> metaDownProperty() {
        return metaDown != null ? metaDown :
            (metaDown = new SimpleObjectProperty<>(this, "metaDown"));
    }

    public final Boolean isMetaDown() {
        return metaDown != null ? metaDown.get() : null;
    }

    public final void setMetaDown(Boolean metaDown) {
        metaDownProperty().set(metaDown);
    }

    private ObjectProperty<Boolean> primaryButtonDown;

    public final ObjectProperty<Boolean> primaryButtonDownProperty() {
        return primaryButtonDown != null ? primaryButtonDown :
            (primaryButtonDown = new SimpleObjectProperty<>(this, "primaryButtonDown"));
    }

    public final Boolean isPrimaryButtonDown() {
        return primaryButtonDown != null ? primaryButtonDown.get() : null;
    }

    public final void setPrimaryButtonDown(Boolean primaryButtonDown) {
        primaryButtonDownProperty().set(primaryButtonDown);
    }

    private ObjectProperty<Boolean> middleButtonDown;

    public final ObjectProperty<Boolean> middleButtonDownProperty() {
        return middleButtonDown != null ? middleButtonDown :
            (middleButtonDown = new SimpleObjectProperty<>(this, "middleButtonDown"));
    }

    public final Boolean isMiddleButtonDown() {
        return middleButtonDown != null ? middleButtonDown.get() : null;
    }

    public final void setMiddleButtonDown(Boolean middleButtonDown) {
        middleButtonDownProperty().set(middleButtonDown);
    }

    private ObjectProperty<Boolean> secondaryButtonDown;

    public final ObjectProperty<Boolean> secondaryButtonDownProperty() {
        return secondaryButtonDown != null ? secondaryButtonDown :
            (secondaryButtonDown = new SimpleObjectProperty<>(this, "secondaryButtonDown"));
    }

    public final Boolean isSecondaryButtonDown() {
        return secondaryButtonDown != null ? secondaryButtonDown.get() : null;
    }

    public final void setSecondaryButtonDown(Boolean secondaryButtonDown) {
        secondaryButtonDownProperty().set(secondaryButtonDown);
    }
    
    private ObjectProperty<Boolean> backButtonDown;

    public final ObjectProperty<Boolean> backButtonDownProperty() {
        return backButtonDown != null ? backButtonDown :
            (backButtonDown = new SimpleObjectProperty<>(this, "backButtonDown"));
    }

    public final Boolean isBackButtonDown() {
        return backButtonDown != null ? backButtonDown.get() : null;
    }

    public final void setBackButtonDown(Boolean backButtonDown) {
        backButtonDownProperty().set(backButtonDown);
    }

    private ObjectProperty<Boolean> forwardButtonDown;

    public final ObjectProperty<Boolean> forwardButtonDownProperty() {
        return forwardButtonDown != null ? forwardButtonDown :
                (forwardButtonDown = new SimpleObjectProperty<>(this, "forwardButtonDown"));
    }

    public final Boolean isForwardButtonDown() {
        return forwardButtonDown != null ? forwardButtonDown.get() : null;
    }

    public final void setForwardButtonDown(Boolean forwardButtonDown) {
        forwardButtonDownProperty().set(forwardButtonDown);
    }

    @Override
    protected boolean handleEvent(MouseEvent event) {
        return matches(button, event.getButton())
            && matches(shiftDown, event.isShiftDown())
            && matches(controlDown, event.isControlDown())
            && matches(altDown, event.isAltDown())
            && matches(metaDown, event.isMetaDown())
            && matches(primaryButtonDown, event.isPrimaryButtonDown())
            && matches(middleButtonDown, event.isMiddleButtonDown())
            && matches(secondaryButtonDown, event.isSecondaryButtonDown())
            && matches(backButtonDown, event.isBackButtonDown())
            && matches(forwardButtonDown, event.isForwardButtonDown());
    }

    private <T> boolean matches(ObjectProperty<T> property, T value) {
        return property == null || property.get() == value;
    }

}
