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

package test.javafx.scene.control.theme;

import com.sun.javafx.application.PlatformImpl;
import org.junit.jupiter.api.Test;
import javafx.application.Application;
import javafx.css.Theme;
import javafx.scene.control.theme.ModenaTheme;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ModenaThemeTest {

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
    public void testHighContrastThemeWithSystemProperty() {
        Theme theme = new ModenaTheme();
        assertFalse(theme.getStylesheets().stream().anyMatch(fileName -> fileName.contains("blackOnWhite.css")));
        System.setProperty("com.sun.javafx.highContrastTheme", "BLACKONWHITE");
        theme = new ModenaTheme();
        assertTrue(theme.getStylesheets().stream().anyMatch(fileName -> fileName.contains("blackOnWhite.css")));
        System.clearProperty("com.sun.javafx.highContrastTheme");
    }

    @Test
    public void testHighContrastThemeWithPlatformPreference() {
        Theme theme = new ModenaTheme();
        assertFalse(theme.getStylesheets().stream().anyMatch(fileName -> fileName.contains("blackOnWhite.css")));

        Map<String, Object> map = ((PlatformImpl.PlatformPreferencesImpl)PlatformImpl.getPlatformPreferences()).getModifiableMap();
        Object originalOn = map.put("Windows.SPI.HighContrastOn", true);
        Object originalName = map.put("Windows.SPI.HighContrastColorScheme", "BLACKONWHITE");

        theme = new ModenaTheme();
        assertTrue(theme.getStylesheets().stream().anyMatch(fileName -> fileName.contains("blackOnWhite.css")));

        map.put("Windows.SPI.HighContrastOn", originalOn);
        map.put("Windows.SPI.HighContrastColorScheme", originalName);
    }

}
