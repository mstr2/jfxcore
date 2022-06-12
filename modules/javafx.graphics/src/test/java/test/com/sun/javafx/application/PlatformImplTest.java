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

package test.com.sun.javafx.application;

import com.sun.javafx.application.PlatformImpl;
import org.junit.jupiter.api.Test;
import javafx.application.Application;
import javafx.application.Theme;
import javafx.application.theme.CaspianTheme;
import javafx.application.theme.ModenaTheme;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import static org.junit.jupiter.api.Assertions.*;

public class PlatformImplTest {

    @Test
    public void testCaspianStylesheetNameSetsTheme() {
        PlatformImpl.platformUserAgentStylesheetProperty().set(null);
        PlatformImpl.platformThemeProperty().set(null);

        PlatformImpl.platformUserAgentStylesheetProperty().set(Application.STYLESHEET_CASPIAN);
        assertEquals(Application.STYLESHEET_CASPIAN, PlatformImpl.platformUserAgentStylesheetProperty().get());
        assertTrue(PlatformImpl.platformThemeProperty().get() instanceof CaspianTheme);

        PlatformImpl.platformThemeProperty().set(null);
        assertNull(PlatformImpl.platformUserAgentStylesheetProperty().get());
        assertNull(PlatformImpl.platformThemeProperty().get());
    }

    @Test
    public void testModenaStylesheetNameSetsTheme() {
        PlatformImpl.platformUserAgentStylesheetProperty().set(null);
        PlatformImpl.platformThemeProperty().set(null);

        PlatformImpl.platformUserAgentStylesheetProperty().set(Application.STYLESHEET_MODENA);
        assertEquals(Application.STYLESHEET_MODENA, PlatformImpl.platformUserAgentStylesheetProperty().get());
        assertTrue(PlatformImpl.platformThemeProperty().get() instanceof ModenaTheme);

        PlatformImpl.platformThemeProperty().set(null);
        assertNull(PlatformImpl.platformUserAgentStylesheetProperty().get());
        assertNull(PlatformImpl.platformThemeProperty().get());
    }

    @Test
    public void testThemeDoesNotSetUAStylesheet() {
        PlatformImpl.platformUserAgentStylesheetProperty().set(null);
        PlatformImpl.platformThemeProperty().set(null);
        PlatformImpl.platformThemeProperty().set(new Theme() {
            @Override public ReadOnlyBooleanProperty darkModeProperty() { return null; }
            @Override public boolean isDarkMode() { return false; }
            @Override public ReadOnlyObjectProperty<Color> accentColorProperty() { return null; }
            @Override public Color getAccentColor() { return null; }
            @Override public ObservableList<String> getStylesheets() { return null; }
        });

        assertNull(PlatformImpl.platformUserAgentStylesheetProperty().get());
    }

}
