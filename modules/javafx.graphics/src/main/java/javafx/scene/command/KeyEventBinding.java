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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Incubating;

/**
 * Binds a {@link Command} to {@link KeyEvent}.
 * <p>
 * By default, {@code KeyEventBinding} handles all key events.
 * It can also be configured to filter key events with the following filter properties:
 * <ul>
 *     <li>{@link #codeProperty() code}
 *     <li>{@link #characterProperty() character}
 *     <li>{@link #shiftDownProperty() shiftDown}
 *     <li>{@link #controlDownProperty() controlDown}
 *     <li>{@link #altDownProperty() altDown}
 *     <li>{@link #metaDownProperty() metaDown}
 * </ul>
 * If a value other than {@code null} is specified for any of these filter properties,
 * {@code KeyEventBinding} will only handle events that match the specified value.
 *
 * @since JFXcore 18
 */
@Incubating
public class KeyEventBinding extends InputEventBinding<KeyEvent> {

    /**
     * Initializes a new {@code KeyEventBinding} instance.
     */
    public KeyEventBinding() {}

    /**
     * Initializes a new {@code KeyEventBinding} instance.
     *
     * @param command the command that is bound to the {@code KeyEvent}
     */
    public KeyEventBinding(@NamedArg("command") Command command) {
        super(command);
    }

    /**
     * Initializes a new {@code KeyEventBinding} instance.
     *
     * @param command the command that is bound to the {@code KeyEvent}
     * @param parameter the parameter that is passed to the command
     */
    public KeyEventBinding(@NamedArg("command") Command command, @NamedArg("parameter") Object parameter) {
        super(command, parameter);
    }

    /**
     * Specifies a filter for the {@link KeyEvent#getCode() code} value.
     */
    private ObjectProperty<KeyCode> code;

    public final ObjectProperty<KeyCode> codeProperty() {
        return code != null ? code :
            (code = new SimpleObjectProperty<>(this, "code"));
    }

    public final KeyCode getCode() {
        return codeProperty().get();
    }

    public final void setCode(KeyCode keyCode) {
        codeProperty().set(keyCode);
    }

    /**
     * Specifies a filter for the {@link KeyEvent#getCharacter() character} value.
     */
    private ObjectProperty<String> character;

    public final ObjectProperty<String> characterProperty() {
        return character != null ? character :
            (character = new SimpleObjectProperty<>(this, "character"));
    }

    public final String getCharacter() {
        return characterProperty().get();
    }

    public final void setCharacter(String character) {
        characterProperty().set(character);
    }

    /**
     * Specifies a filter for the {@link KeyEvent#isShiftDown() shiftDown} value.
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
     * Specifies a filter for the {@link KeyEvent#isControlDown() controlDown} value.
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
     * Specifies a filter for the {@link KeyEvent#isAltDown() altDown} value.
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
     * Specifies a filter for the {@link KeyEvent#isMetaDown() metaDown} value.
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
    protected boolean handleEvent(KeyEvent event) {
        return super.handleEvent(event)
            && matches(code, event.getCode())
            && matches(character, event.getCharacter())
            && matches(shiftDown, event.isShiftDown())
            && matches(controlDown, event.isControlDown())
            && matches(altDown, event.isAltDown())
            && matches(metaDown, event.isMetaDown());
    }

}
