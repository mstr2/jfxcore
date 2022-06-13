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

import javafx.scene.paint.Color;
import javafx.util.Incubating;
import java.util.Map;

/**
 * Contains UI preferences of the current platform.
 * <p>
 * {@code PlatformPreferences} implements {@link Map} to expose platform preferences as key-value pairs.
 * For convenience, {@link #getString}, {@link #getBoolean} and {@link #getColor} are provided as typed
 * alternatives to the untyped {@link #get} method.
 * <p>
 * An application should not expect all preferences to be available on a particular platform.
 * For example, the {@code Windows.UIColor.*} preferences are only available starting with Windows 10 build 10240.
 * <p>
 * The following list contains all preferences that are potentially available on the specified platforms:
 * <ol>
 *     <li><span style="font-weight: bold">Windows</span>
 *     <p>
 *     <table border="1" style="table-layout: fixed; width: 100%; max-width: 40em">
 *         <caption style="text-align: left">High contrast color scheme, reported by {@code SystemParametersInfo}</caption>
 *         <tr><td>{@code Windows.SPI.HighContrastOn}</td><td style="width: 5em">{@link Boolean}</td></tr>
 *         <tr><td>{@code Windows.SPI.HighContrastColorScheme}</td><td>{@link String}</td></tr>
 *     </table>
 *     <p>
 *     <table border="1" style="table-layout: fixed; width: 100%; max-width: 40em">
 *         <caption style="text-align: left">System colors, reported by {@code GetSysColor}</caption>
 *         <tr><td>{@code Windows.SysColor.COLOR_3DDKSHADOW}</td><td style="width: 5em">{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_3DFACE}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_3DHIGHLIGHT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_3DHILIGHT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_3DLIGHT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_3DSHADOW}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_ACTIVEBORDER}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_ACTIVECAPTION}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_APPWORKSPACE}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_BACKGROUND}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_BTNFACE}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_BTNHIGHLIGHT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_BTNHILIGHT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_BTNSHADOW}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_BTNTEXT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_CAPTIONTEXT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_DESKTOP}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_GRADIENTACTIVECAPTION}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_GRADIENTINACTIVECAPTION}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_GRAYTEXT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_HIGHLIGHT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_HIGHLIGHTTEXT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_HOTLIGHT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_INACTIVEBORDER}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_INACTIVECAPTION}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_INACTIVECAPTIONTEXT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_INFOBK}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_INFOTEXT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_MENU}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_MENUHILIGHT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_MENUBAR}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_MENUTEXT}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_SCROLLBAR}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_WINDOW}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_WINDOWFRAME}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.SysColor.COLOR_WINDOWTEXT}</td><td>{@link Color}</td></tr>
 *     </table>
 *     <p>
 *     <table border="1" style="table-layout: fixed; width: 100%; max-width: 40em">
 *         <caption style="text-align: left">Theme colors, reported by {@code Windows.UI.ViewManagement.UISettings}</caption>
 *         <tr><td>{@code Windows.UIColor.Background}</td><td style="width: 5em">{@link Color}</td></tr>
 *         <tr><td>{@code Windows.UIColor.Foreground}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.UIColor.AccentDark3}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.UIColor.AccentDark2}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.UIColor.AccentDark1}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.UIColor.Accent}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.UIColor.AccentLight1}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.UIColor.AccentLight2}</td><td>{@link Color}</td></tr>
 *         <tr><td>{@code Windows.UIColor.AccentLight3}</td><td>{@link Color}</td></tr>
 *     </table>
 *     Note that {@code Windows.UI.ViewManagement.UISettings} is available since Windows 10 build 10240.
 *     <p>
 *
 *     <li><span style="font-weight: bold">macOS</span>
 *         <p>Platform preferences are currently not reported on macOS.
 *
 *     <li><span style="font-weight: bold">Linux</span>
 *         <p>Platform preferences are currently not reported on Linux.
 * </ol>
 *
 * @since JFXcore 18
 */
@Incubating
public interface PlatformPreferences extends Map<String, Object> {

    /**
     * Returns the {@link String} instance to which the specified key is mapped.
     *
     * @param key the key
     * @return the {@code String} instance to which the {@code key} is mapped, or
     *         {@code null} if the key is not mapped to a {@code String} instance
     */
    String getString(String key);

    /**
     * Returns the {@link String} instance to which the specified key is mapped,
     * or a fallback value if the key is not mapped to a {@code String} instance.
     *
     * @param key the key
     * @return the {@code String} instance to which the {@code key} is mapped, or
     *         {@code fallbackValue} if the key is not mapped to a {@code String}
     *         instance
     */
    String getString(String key, String fallbackValue);

    /**
     * Returns the {@link Boolean} instance to which the specified key is mapped.
     *
     * @param key the key
     * @return the {@code Boolean} instance to which the {@code key} is mapped, or
     *         {@code null} if the key is not mapped to a {@code Boolean} instance
     */
    Boolean getBoolean(String key);

    /**
     * Returns the {@code boolean} value to which the specified key is mapped,
     * or a fallback value if the key is not mapped to a {@code boolean} value.
     *
     * @param key the key
     * @return the {@code boolean} value to which the {@code key} is mapped, or
     *         {@code fallbackValue} if the key is not mapped to a {@code boolean}
     *         value
     */
    boolean getBoolean(String key, boolean fallbackValue);

    /**
     * Returns the {@link Color} instance to which the specified key is mapped.
     *
     * @param key the key
     * @return the {@code Color} instance to which the {@code key} is mapped, or
     *         {@code null} if the key is not mapped to a {@code Color} instance
     */
    Color getColor(String key);

    /**
     * Returns the {@link Color} instance to which the specified key is mapped,
     * or a fallback value if the key is not mapped to a {@code Color} instance.
     *
     * @param key the key
     * @return the {@code Color} instance to which the {@code key} is mapped, or
     *         {@code fallbackValue} if the key is not mapped to a {@code Color}
     *         instance
     */
    Color getColor(String key, Color fallbackValue);

    /**
     * Adds the specified listener to this {@code PlatformPreferences} instance.
     *
     * @param listener the {@code PlatformPreferencesListener}
     */
    void addListener(PlatformPreferencesListener listener);

    /**
     * Removes the specified listener from this {@code PlatformPreferences} instance.
     *
     * @param listener the {@code PlatformPreferencesListener}
     */
    void removeListener(PlatformPreferencesListener listener);

}
