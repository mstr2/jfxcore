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

package test.javafx.scene.command;

import javafx.scene.Node;
import javafx.scene.command.Command;
import javafx.scene.command.CommandHandler;
import java.util.List;

public class TestCommandHandler implements CommandHandler {

    final List<String> trace;
    final String name;

    public TestCommandHandler(List<String> trace, String name) {
        this.trace = trace;
        this.name = name;
    }

    @Override
    public void onAttached(Node node, Command command) {
        if (trace != null) {
            trace.add("+" + name);
        }
    }

    @Override
    public void onDetached(Node node, Command command) {
        if (trace != null) {
            trace.add("-" + name);
        }
    }

}
