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
import com.sun.javafx.application.HighContrastScheme;
import com.sun.javafx.application.PlatformImpl;
import javafx.application.ConditionalFeature;
import javafx.beans.value.WritableValue;
import javafx.util.Incubating;
import java.util.ResourceBundle;

/**
 * {@code Modena} is a built-in JavaFX theme.
 *
 * @since JFXcore 18
 */
@Incubating
public class ModenaTheme extends ThemeBase {

    private final WritableValue<String> highContrastStylesheet;

    /**
     * Creates a new instance of the {@code ModenaTheme} class.
     */
    public ModenaTheme() {
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

        highContrastThemeNameProperty().addListener(
            ((observable, oldValue, newValue) -> onHighContrastThemeChanged(newValue)));

        onHighContrastThemeChanged(getHighContrastThemeName());
    }

    private void onHighContrastThemeChanged(String themeName) {
        if (themeName != null) {
            String stylesheet = switch (themeName.toUpperCase()) {
                case "BLACKONWHITE" -> "com/sun/javafx/scene/control/skin/modena/blackOnWhite.css";
                case "WHITEONBLACK" -> "com/sun/javafx/scene/control/skin/modena/whiteOnBlack.css";
                case "YELLOWONBLACK" -> "com/sun/javafx/scene/control/skin/modena/yellowOnBlack.css";
                default -> null;
            };

            if (stylesheet == null) {
                ResourceBundle bundle = ResourceBundle.getBundle("com/sun/glass/ui/win/themes");
                String enumValue = HighContrastScheme.fromThemeName(bundle::getString, themeName);

                stylesheet = enumValue != null ? switch (HighContrastScheme.valueOf(enumValue)) {
                    case HIGH_CONTRAST_WHITE -> "com/sun/javafx/scene/control/skin/modena/blackOnWhite.css";
                    case HIGH_CONTRAST_BLACK -> "com/sun/javafx/scene/control/skin/modena/whiteOnBlack.css";
                    case HIGH_CONTRAST_1, HIGH_CONTRAST_2 -> "com/sun/javafx/scene/control/skin/modena/yellowOnBlack.css";
                } : null;
            }

            highContrastStylesheet.setValue(stylesheet);
        } else {
            highContrastStylesheet.setValue(null);
        }
    }

}
