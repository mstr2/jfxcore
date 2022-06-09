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

package javafx.application.theme;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.application.HighContrastScheme;
import javafx.application.ConditionalFeature;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.css.StylesheetItem;
import javafx.css.StylesheetListBase;
import java.util.ResourceBundle;

public class ModenaTheme extends StylesheetListBase {

    private final InvalidationListener highContrastSchemeChanged =
            observable -> updateHighContrastStylesheet();

    private final ReadOnlyStringProperty highContrastScheme =
            PlatformImpl.getPlatformTheme().getProperty("Windows.SPI.HighContrastColorScheme");

    private final ReadOnlyStringProperty highContrastOn =
            PlatformImpl.getPlatformTheme().getProperty("Windows.SPI.HighContrastOn");

    private final StylesheetItem highContrastStylesheet;

    public ModenaTheme() {
        highContrastScheme.addListener(new WeakInvalidationListener(highContrastSchemeChanged));
        highContrastOn.addListener(new WeakInvalidationListener(highContrastSchemeChanged));

        addStylesheet("com/sun/javafx/scene/control/skin/modena/modena.css");

        if (PlatformImpl.isSupported(ConditionalFeature.INPUT_TOUCH)) {
            addStylesheet("com/sun/javafx/scene/control/skin/modena/touch.css");
        }

        if (PlatformUtil.isEmbedded()) {
            addStylesheet("com/sun/javafx/scene/control/skin/modena/modena-embedded-performance.css");
        }

        if (PlatformUtil.isAndroid()) {
            addStylesheet("com/sun/javafx/scene/control/skin/modena/android.css");
        }

        if (PlatformUtil.isIOS()) {
            addStylesheet("com/sun/javafx/scene/control/skin/modena/ios.css");
        }

        if (PlatformImpl.isSupported(ConditionalFeature.TWO_LEVEL_FOCUS)) {
            addStylesheet("com/sun/javafx/scene/control/skin/modena/two-level-focus.css");
        }

        if (PlatformImpl.isSupported(ConditionalFeature.VIRTUAL_KEYBOARD)) {
            addStylesheet("com/sun/javafx/scene/control/skin/caspian/fxvk.css");
        }

        if (!PlatformImpl.isSupported(ConditionalFeature.TRANSPARENT_WINDOW)) {
            addStylesheet("com/sun/javafx/scene/control/skin/modena/modena-no-transparency.css");
        }

        highContrastStylesheet = addStylesheet(null);
        updateHighContrastStylesheet();
    }

    private void updateHighContrastStylesheet() {
        boolean enabled = Boolean.parseBoolean(highContrastOn.get());
        if (enabled) {
            String highContrastStylesheet = null;
            String userTheme = System.getProperty("com.sun.javafx.highContrastTheme");

            if (userTheme != null) {
                highContrastStylesheet = switch (userTheme.toUpperCase()) {
                    case "BLACKONWHITE" -> "com/sun/javafx/scene/control/skin/modena/blackOnWhite.css";
                    case "WHITEONBLACK" -> "com/sun/javafx/scene/control/skin/modena/whiteOnBlack.css";
                    case "YELLOWONBLACK" -> "com/sun/javafx/scene/control/skin/modena/yellowOnBlack.css";
                    default -> null;
                };
            }

            if (highContrastStylesheet == null) {
                ResourceBundle bundle = ResourceBundle.getBundle("com/sun/glass/ui/win/themes");
                String enumValue = HighContrastScheme.fromThemeName(bundle::getString, highContrastScheme.get());

                highContrastStylesheet = enumValue != null ? switch (HighContrastScheme.valueOf(enumValue)) {
                    case HIGH_CONTRAST_WHITE -> "com/sun/javafx/scene/control/skin/modena/blackOnWhite.css";
                    case HIGH_CONTRAST_BLACK -> "com/sun/javafx/scene/control/skin/modena/whiteOnBlack.css";
                    case HIGH_CONTRAST_1, HIGH_CONTRAST_2 -> "com/sun/javafx/scene/control/skin/modena/yellowOnBlack.css";
                } : null;
            }

            this.highContrastStylesheet.set(highContrastStylesheet);
        } else {
            this.highContrastStylesheet.set(null);
        }
    }

}
