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

package javafx.css;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.util.Incubating;
import java.io.File;

/**
 * {@code StyleTheme} is a collection of stylesheets that specify the appearance of UI controls and other
 * nodes in the application. Like a user-agent stylesheet, a {@code StyleTheme} is implicitly used by all
 * JavaFX nodes in the scene graph.
 * <p>
 * The list of stylesheets that comprise a {@code StyleTheme} can be modified while the application is running,
 * enabling applications to create dynamic themes that respond to changing user preferences.
 * <p>
 * In the CSS subsystem, stylesheets that comprise a {@code StyleTheme} are classified as
 * {@link StyleOrigin#USER_AGENT} stylesheets, but have a higher precedence in the CSS cascade
 * than a stylesheet referenced by {@link Application#userAgentStylesheetProperty()}.
 *
 * @since JFXcore 18
 */
@Incubating
public interface StyleTheme {

    /**
     * Gets the list of stylesheet URLs that comprise this {@code StyleTheme}.
     * <p>
     * The URL is a hierarchical URI of the form [scheme:][//authority][path]. If the URL
     * does not have a [scheme:] component, the URL is considered to be the [path] component only.
     * Any leading '/' character of the [path] is ignored and the [path] is treated as a path relative to
     * the root of the application's classpath.
     * <p>
     * The RFC 2397 "data" scheme for URLs is supported in addition to the protocol handlers that
     * are registered for the application.
     * If a URL uses the "data" scheme and the MIME type is either empty, "text/plain", or "text/css",
     * the payload will be interpreted as a CSS file.
     * If the MIME type is "application/octet-stream", the payload will be interpreted as a binary
     * CSS file (see {@link Stylesheet#convertToBinary(File, File)}).
     *
     * @implNote Implementations of this method are encouraged to minimize the number of subsequent
     *           list change notifications that are fired by the returned {@link ObservableList},
     *           as each change notification causes the CSS subsystem to re-apply the referenced
     *           stylesheets.
     *
     * @return the list of stylesheet URLs
     */
    ObservableList<String> getStylesheets();

}
