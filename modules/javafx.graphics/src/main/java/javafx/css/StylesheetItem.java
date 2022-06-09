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

import javafx.util.Incubating;
import java.io.File;

/**
 * Represents a single stylesheet URL in a {@link StylesheetListBase}.
 * <p>
 * The URLs are hierarchical URIs of the form [scheme:][//authority][path]. If the URL
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
 * @since JFXcore 18
 */
@Incubating
public interface StylesheetItem {

    /**
     * Gets the stylesheet URL.
     *
     * @return the stylesheet URL
     */
    String get();

    /**
     * Sets the stylesheet URL.
     *
     * @param value the stylesheet URL
     */
    void set(String value);

}
