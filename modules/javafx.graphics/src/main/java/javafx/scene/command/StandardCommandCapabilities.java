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

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.util.Incubating;

/**
 * Contains a set of standard capabilities that can be reported by commands.
 * <p>
 * Using standard capabilities allows {@link CommandHandler} implementations to depend on
 * standard interfaces, rather than concrete command implementations.
 *
 * @since JFXcore 18
 */
@Incubating
public final class StandardCommandCapabilities {

    private StandardCommandCapabilities() {}

    /**
     * Marks a command that implements the <em>message</em> capability.
     */
    public interface Message {
        /**
         * Gets a property that represents the reported message of the command.
         *
         * @defaultValue null
         */
        ReadOnlyStringProperty messageProperty();

        /**
         * Gets the reported message of the command.
         *
         * @return the message or {@code null}
         */
        String getMessage();
    }

    /**
     * Marks a command that implements the <em>title</em> capability.
     */
    public interface Title {
        /**
         * Gets a property that represents the reported title of the command.
         *
         * @defaultValue null
         */
        ReadOnlyStringProperty titleProperty();

        /**
         * Gets the reported title of the command.
         *
         * @return the title or {@code null}
         */
        String getTitle();
    }

}
