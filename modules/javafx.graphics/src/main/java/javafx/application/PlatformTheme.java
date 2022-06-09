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

package javafx.application;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import javafx.util.Incubating;

/**
 * {@code PlatformTheme} provides theming-related information about the platform that the
 * JavaFX application is running on.
 *
 * @since JFXcore 18
 */
@Incubating
public interface PlatformTheme {

    /**
     * Gets a property that indicates whether the current platform requests a dark UI.
     *
     * @return the {@code darkMode} property
     * @defaultValue false
     */
    ReadOnlyBooleanProperty darkModeProperty();

    /**
     * Indicates whether the current platform requests a dark UI.
     *
     * @return {@code true} if the platform requests a dark UI, {@code false} otherwise
     */
    boolean isDarkMode();

    /**
     * Gets a property that contains the accent color of the current platform.
     *
     * @return the {@code accentColor} property
     * @defaultValue {@code Color.BLACK}
     */
    ReadOnlyObjectProperty<Color> accentColorProperty();

    /**
     * Gets the accent color of the current platform.
     *
     * @return the accent color, or {@link Color#BLACK} if the platform doesn't support accent colors
     */
    Color getAccentColor();

    /**
     * Gets the theme properties of the current platform.
     * <p>
     * Currently, only Windows platforms report theme properties:
     * <ol>
     *     <li>High contrast color scheme (as reported by SystemParametersInfo):
     *     <pre>
     *     Windows.SPI.HighContrastOn                        "true" | "false
     *     Windows.SPI.HighContrastColorScheme               hex-color-string
     *     </pre>
     *     <li>System colors (as reported by GetSysColor):
     *     <pre>
     *     Windows.SysColor.COLOR_3DDKSHADOW                 hex-color-string
     *     Windows.SysColor.COLOR_3DFACE                     hex-color-string
     *     Windows.SysColor.COLOR_3DHIGHLIGHT                hex-color-string
     *     Windows.SysColor.COLOR_3DHILIGHT                  hex-color-string
     *     Windows.SysColor.COLOR_3DLIGHT                    hex-color-string
     *     Windows.SysColor.COLOR_3DSHADOW                   hex-color-string
     *     Windows.SysColor.COLOR_ACTIVEBORDER               hex-color-string
     *     Windows.SysColor.COLOR_ACTIVECAPTION              hex-color-string
     *     Windows.SysColor.COLOR_APPWORKSPACE               hex-color-string
     *     Windows.SysColor.COLOR_BACKGROUND                 hex-color-string
     *     Windows.SysColor.COLOR_BTNFACE                    hex-color-string
     *     Windows.SysColor.COLOR_BTNHIGHLIGHT               hex-color-string
     *     Windows.SysColor.COLOR_BTNHILIGHT                 hex-color-string
     *     Windows.SysColor.COLOR_BTNSHADOW                  hex-color-string
     *     Windows.SysColor.COLOR_BTNTEXT                    hex-color-string
     *     Windows.SysColor.COLOR_CAPTIONTEXT                hex-color-string
     *     Windows.SysColor.COLOR_DESKTOP                    hex-color-string
     *     Windows.SysColor.COLOR_GRADIENTACTIVECAPTION      hex-color-string
     *     Windows.SysColor.COLOR_GRADIENTINACTIVECAPTION    hex-color-string
     *     Windows.SysColor.COLOR_GRAYTEXT                   hex-color-string
     *     Windows.SysColor.COLOR_HIGHLIGHT                  hex-color-string
     *     Windows.SysColor.COLOR_HIGHLIGHTTEXT              hex-color-string
     *     Windows.SysColor.COLOR_HOTLIGHT                   hex-color-string
     *     Windows.SysColor.COLOR_INACTIVEBORDER             hex-color-string
     *     Windows.SysColor.COLOR_INACTIVECAPTION            hex-color-string
     *     Windows.SysColor.COLOR_INACTIVECAPTIONTEXT        hex-color-string
     *     Windows.SysColor.COLOR_INFOBK                     hex-color-string
     *     Windows.SysColor.COLOR_INFOTEXT                   hex-color-string
     *     Windows.SysColor.COLOR_MENU                       hex-color-string
     *     Windows.SysColor.COLOR_MENUHILIGHT                hex-color-string
     *     Windows.SysColor.COLOR_MENUBAR                    hex-color-string
     *     Windows.SysColor.COLOR_MENUTEXT                   hex-color-string
     *     Windows.SysColor.COLOR_SCROLLBAR                  hex-color-string
     *     Windows.SysColor.COLOR_WINDOW                     hex-color-string
     *     Windows.SysColor.COLOR_WINDOWFRAME                hex-color-string
     *     Windows.SysColor.COLOR_WINDOWTEXT                 hex-color-string
     *     </pre>
     *     <li>Theme colors (as reported by UISettings, introduced in Windows 10 build 10240):
     *     <pre>
     *     Windows.UIColor.Background                        hex-color-string
     *     Windows.UIColor.Foreground                        hex-color-string
     *     Windows.UIColor.AccentDark3                       hex-color-string
     *     Windows.UIColor.AccentDark2                       hex-color-string
     *     Windows.UIColor.AccentDark1                       hex-color-string
     *     Windows.UIColor.Accent                            hex-color-string
     *     Windows.UIColor.AccentLight1                      hex-color-string
     *     Windows.UIColor.AccentLight2                      hex-color-string
     *     Windows.UIColor.AccentLight3                      hex-color-string
     *     </pre>
     * </ol>
     * {@code hex-color-string} is a value that can be parsed by {@link Color#web(String)}
     */
    ObservableMap<String, String> getProperties();

    /**
     * Gets a {@code ReadOnlyStringProperty} that represents a single theme property.
     * <p>
     * The returned {@code ReadOnlyStringProperty} is only weakly referenced by {@code PlatformTheme};
     * it is therefore necessary for users of this API to store a strong reference to the property
     * to prevent it from being garbage-collected.
     *
     * @param name the name of the property, as reported by {@link #getProperties()}
     * @return a {@code ReadOnlyStringProperty} representing the theme property
     */
    ReadOnlyStringProperty getProperty(String name);

}
