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
import javafx.application.ConditionalFeature;
import javafx.beans.value.WritableValue;
import javafx.util.Incubating;
import java.util.Map;

/**
 * {@code Caspian} is a built-in JavaFX theme.
 *
 * @since JFXcore 18
 */
@Incubating
public class CaspianTheme extends ThemeBase {

    private final WritableValue<String> highContrastStylesheet;

    /**
     * Creates a new instance of the {@code CaspianTheme} class.
     */
    public CaspianTheme() {
        addStylesheet("com/sun/javafx/scene/control/skin/caspian/caspian.css");

        if (PlatformImpl.isSupported(ConditionalFeature.INPUT_TOUCH)) {
            addStylesheet("com/sun/javafx/scene/control/skin/caspian/embedded.css");

            if (com.sun.javafx.util.Utils.isQVGAScreen()) {
                addStylesheet("com/sun/javafx/scene/control/skin/caspian/embedded-qvga.css");
            }

            if (PlatformUtil.isAndroid()) {
                addStylesheet("com/sun/javafx/scene/control/skin/caspian/android.css");
            }

            if (PlatformUtil.isIOS()) {
                addStylesheet("com/sun/javafx/scene/control/skin/caspian/ios.css");
            }
        }

        if (PlatformImpl.isSupported(ConditionalFeature.TWO_LEVEL_FOCUS)) {
            addStylesheet("com/sun/javafx/scene/control/skin/caspian/two-level-focus.css");
        }

        if (PlatformImpl.isSupported(ConditionalFeature.VIRTUAL_KEYBOARD)) {
            addStylesheet("com/sun/javafx/scene/control/skin/caspian/fxvk.css");
        }

        if (!PlatformImpl.isSupported(ConditionalFeature.TRANSPARENT_WINDOW)) {
            addStylesheet("com/sun/javafx/scene/control/skin/caspian/caspian-no-transparency.css");
        }

        highContrastStylesheet = addStylesheet(null);
        updateHighContrastTheme();
    }

    @Override
    protected void onPreferencesChanged(Map<String, String> preferences) {
        updateHighContrastTheme();
    }

    private void updateHighContrastTheme() {
        boolean enabled = false;
        String overrideThemeName = System.getProperty("com.sun.javafx.highContrastTheme");
        if (overrideThemeName != null) {
            enabled = true;
        }

        if (!enabled) {
            Map<String, String> preferences = PlatformImpl.getPreferences();
            if (Boolean.parseBoolean(preferences.get("Windows.SPI.HighContrastOn"))) {
                enabled = preferences.get("Windows.SPI.HighContrastColorScheme") != null;
            }
        }

        if (enabled) {
            // caspian has only one high contrast theme, use it regardless of the user or platform theme.
            highContrastStylesheet.setValue("com/sun/javafx/scene/control/skin/caspian/highcontrast.css");
        } else {
            highContrastStylesheet.setValue(null);
        }
    }

}
