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
import javafx.event.ActionEvent;
import javafx.util.Incubating;

/**
 * Binds a {@link Command} to {@link ActionEvent}.
 *
 * @since JFXcore 18
 */
@Incubating
public class ActionEventBinding extends EventBinding<ActionEvent> {

    /**
     * Initializes a new {@code ActionEventBinding} instance.
     */
    public ActionEventBinding() {}

    /**
     * Initializes a new {@code ActionEventBinding} instance.
     *
     * @param command the command that is bound to the {@code ActionEvent}
     */
    public ActionEventBinding(@NamedArg("command") Command command) {
        super(command);
    }

    /**
     * Initializes a new {@code ActionEventBinding} instance.
     *
     * @param command the command that is bound to the {@code ActionEvent}
     * @param parameter the parameter that is passed to the command
     */
    public ActionEventBinding(@NamedArg("command") Command command, @NamedArg("parameter") Object parameter) {
        super(command, parameter);
    }

}
