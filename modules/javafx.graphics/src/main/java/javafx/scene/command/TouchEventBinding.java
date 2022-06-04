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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.TouchEvent;
import javafx.util.Incubating;

/**
 * Binds a {@link Command} to {@link TouchEvent}.
 * <p>
 * By default, {@code TouchEventBinding} handles all touch events.
 * It can also be configured to filter touch events with the following filter properties:
 * <ul>
 *     <li>{@link #touchCountProperty() touchCount}
 *     <li>{@link #shiftDownProperty() shiftDown}
 *     <li>{@link #controlDownProperty() controlDown}
 *     <li>{@link #altDownProperty() altDown}
 *     <li>{@link #metaDownProperty() metaDown}
 * </ul>
 * If a value other than {@code null} is specified for any of these filter properties,
 * {@code TouchEventBinding} will only handle events that match the specified value.
 *
 * @since JFXcore 18
 */
@Incubating
public class TouchEventBinding extends InputEventBinding<TouchEvent> {

    /**
     * Initializes a new {@code TouchEventBinding} instance.
     */
    public TouchEventBinding() {}

    /**
     * Initializes a new {@code TouchEventBinding} instance.
     *
     * @param command the command that is bound to the {@code TouchEvent}
     */
    public TouchEventBinding(@NamedArg("command") Command command) {
        super(command);
    }

    /**
     * Initializes a new {@code TouchEventBinding} instance.
     *
     * @param command the command that is bound to the {@code TouchEvent}
     * @param parameter the parameter that is passed to the command
     */
    public TouchEventBinding(@NamedArg("command") Command command, @NamedArg("parameter") Object parameter) {
        super(command, parameter);
    }

    /**
     * Specifies a filter for the {@link TouchEvent#getTouchCount() touchCount} value.
     */
    private ObjectProperty<Integer> touchCount;

    public final ObjectProperty<Integer> touchCountProperty() {
        return touchCount != null ? touchCount :
            (touchCount = new SimpleObjectProperty<>(this, "touchCount"));
    }

    public final Integer getTouchCount() {
        return touchCount != null ? touchCount.get() : null;
    }

    public final void setTouchCount(Integer touchCount) {
        touchCountProperty().set(touchCount);
    }

    /**
     * Specifies a filter for the {@link TouchEvent#isShiftDown() shiftDown} value.
     */
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

    /**
     * Specifies a filter for the {@link TouchEvent#isControlDown() controlDown} value.
     */
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

    /**
     * Specifies a filter for the {@link TouchEvent#isAltDown() altDown} value.
     */
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

    /**
     * Specifies a filter for the {@link TouchEvent#isMetaDown() metaDown} value.
     */
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

    @Override
    protected boolean handleEvent(TouchEvent event) {
        return super.handleEvent(event)
            && matches(touchCount, event.getTouchCount())
            && matches(shiftDown, event.isShiftDown())
            && matches(controlDown, event.isControlDown())
            && matches(altDown, event.isAltDown())
            && matches(metaDown, event.isMetaDown());
    }

}
