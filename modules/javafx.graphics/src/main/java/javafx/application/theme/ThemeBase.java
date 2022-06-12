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

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.css.StylesheetList;
import com.sun.javafx.util.Utils;
import javafx.application.Platform;
import javafx.application.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.util.Incubating;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * {@link ThemeBase} is a base implementation for themes that can dynamically react to
 * {@link Platform#getPreferences() platform preferences}. {@code ThemeBase} uses the operating
 * system's preferences to determine the value of {@link #darkModeProperty()} and
 * {@link #accentColorProperty()}, with the option to override their values by setting
 * {@link #darkModeOverrideProperty()} or {@link #accentColorOverrideProperty()}.
 * <p>
 * Stylesheets that are added to this theme via {@link #addStylesheet(String)} always retain the
 * order in which they were added. The value of a stylesheet URL can be changed at any time with
 * the {@link WritableValue} wrapper that is returned by {@link #addStylesheet(String)}.
 * <p>
 * In this example, a custom theme is created that responds to dark mode changes:
 * <pre>{@code
 *     public class MyTheme extends ThemeBase {
 *         private final WritableValue<String> colors;
 *
 *         public MyTheme() {
 *             // The order of addStylesheet(...) calls is significant, as it corresponds
 *             // to the order of the stylesheets in the CSS cascade
 *
 *             addStylesheet("base.css");
 *             colors = addStylesheet("colors-light.css");
 *             addStylesheet("controls.css");
 *
 *             darkModeProperty().addListener(((observable, oldValue, newValue) -> {
 *                 if (newValue) {
 *                     colors.setValue("colors-dark.css");
 *                 } else {
 *                     colors.setValue("colors-light.css");
 *                 }
 *             }));
 *         }
 *     }
 * }</pre>
 * A theme can be extended by subclassing its theme class and adding more stylesheets:
 * <pre>{@code
 *     public class MyExtendedTheme extends MyTheme {
 *         public MyExtendedTheme() {
 *             addStylesheet("additional-stylesheet.css");
 *         }
 *     }
 * }</pre>
 *
 * @since JFXcore 18
 */
@Incubating
public abstract class ThemeBase implements Theme {

    /**
     * Indicates whether dark mode is requested by the operating system. When the platform does not
     * report dark mode information, the default value is {@code false}.
     * <p>
     * Use {@link #darkModeOverrideProperty()} to override the value of this property.
     */
    private final BooleanPropertyImpl darkMode = new BooleanPropertyImpl() {
        boolean currentValue;
        boolean newValue;

        @Override
        public Object getBean() {
            return ThemeBase.this;
        }

        @Override
        public String getName() {
            return "darkMode";
        }

        @Override
        public boolean get() {
            return currentValue;
        }

        @Override
        public void fireValueChangedEvent() {
            if (currentValue != newValue) {
                currentValue = newValue;
                super.fireValueChangedEvent();
            }
        }

        @Override
        void update() {
            newValue = isDarkModeEnabled();
        }

        private boolean isDarkModeEnabled() {
            Boolean override = darkModeOverride.get();
            if (override != null) {
                return override;
            }

            Map<String, String> preferences = PlatformImpl.getPreferences();
            String foreground = preferences.get("Windows.UIColor.Foreground");
            if (foreground != null) {
                return Utils.calculateBrightness(Color.valueOf(foreground)) > 0.5;
            }

            return false;
        }
    };

    @Override
    public final ReadOnlyBooleanProperty darkModeProperty() {
        return darkMode;
    }

    @Override
    public final boolean isDarkMode() {
        return darkMode.get();
    }

    /**
     * Overrides the value of {@link #darkModeProperty()}.
     * When set to {@code null}, the original value of {@link #darkModeProperty()} is restored.
     */
    private final ObjectProperty<Boolean> darkModeOverride =
            new SimpleObjectProperty<>(this, "darkModeOverride", null) {
                @Override
                protected void invalidated() {
                    darkMode.fireValueChangedEvent();
                }
            };

    public final ObjectProperty<Boolean> darkModeOverrideProperty() {
        return darkModeOverride;
    }

    public final void setDarkModeOverride(Boolean state) {
        darkModeOverride.set(state);
    }

    public final Boolean getDarkModeOverride() {
        return darkModeOverride.get();
    }

    /**
     * The accent color of this theme, which corresponds to the operating system's accent color
     * on supported platforms. When the platform does not report an accent color, the default
     * value is {@code #157EFB}.
     * <p>
     * Use {@link #accentColorProperty()} to override the value of this property.
     */
    private final ObjectPropertyImpl<Color> accentColor = new ObjectPropertyImpl<>() {
        static final Color DEFAULT_ACCENT_COLOR = Color.rgb(21, 126, 251);

        Color currentValue = DEFAULT_ACCENT_COLOR;
        Color newValue = DEFAULT_ACCENT_COLOR;

        @Override
        public Object getBean() {
            return ThemeBase.this;
        }

        @Override
        public String getName() {
            return "accentColor";
        }

        @Override
        public Color get() {
            return currentValue;
        }

        @Override
        public void fireValueChangedEvent() {
            if (!Objects.equals(currentValue, newValue)) {
                currentValue = newValue;
                super.fireValueChangedEvent();
            }
        }

        @Override
        void update() {
            newValue = getAccentColor();
        }

        private Color getAccentColor() {
            Color override = accentColorOverride.get();
            if (override != null) {
                return override;
            }

            String accentColor = PlatformImpl.getPreferences().get("Windows.UIColor.Accent");
            if (accentColor != null) {
                return Color.valueOf(accentColor);
            }

            return DEFAULT_ACCENT_COLOR;
        }
    };

    @Override
    public final ReadOnlyObjectProperty<Color> accentColorProperty() {
        return accentColor;
    }

    @Override
    public final Color getAccentColor() {
        return accentColor.get();
    }

    /**
     * Overrides the value of {@link #accentColorProperty()}.
     * When set to {@code null}, the original value of {@link #accentColorProperty()} is restored.
     */
    private final ObjectProperty<Color> accentColorOverride =
            new SimpleObjectProperty<>(this, "accentColorOverride", null) {
                @Override
                protected void invalidated() {
                    accentColor.fireValueChangedEvent();
                }
            };

    public final ObjectProperty<Color> accentColorOverrideProperty() {
        return accentColorOverride;
    }

    public final void setAccentColorOverride(Color color) {
        accentColorOverride.set(color);
    }

    public final Color getAccentColorOverride() {
        return accentColorOverride.get();
    }

    /**
     * The high-contrast theme is a special accessibility theme that is available on some platforms.
     * The name of this theme is platform-dependent, and may also depend on the platform locale.
     */
    private final StringPropertyImpl highContrastThemeName = new StringPropertyImpl() {
        String currentValue;
        String newValue;

        @Override
        public Object getBean() {
            return ThemeBase.this;
        }

        @Override
        public String getName() {
            return "highContrastThemeName";
        }

        @Override
        public String get() {
            return currentValue;
        }

        @Override
        public void fireValueChangedEvent() {
            if (!Objects.equals(currentValue, newValue)) {
                currentValue = newValue;
                super.fireValueChangedEvent();
            }
        }

        @Override
        void update() {
            newValue = getHighContrastThemeName();
        }

        private String getHighContrastThemeName() {
            String overrideThemeName = System.getProperty("com.sun.javafx.highContrastTheme");
            if (overrideThemeName != null) {
                return overrideThemeName;
            }

            Map<String, String> preferences = PlatformImpl.getPreferences();
            if (Boolean.parseBoolean(preferences.get("Windows.SPI.HighContrastOn"))) {
                return preferences.get("Windows.SPI.HighContrastColorScheme");
            }

            return null;
        }
    };

    public final ReadOnlyStringProperty highContrastThemeNameProperty() {
        return highContrastThemeName;
    }

    public final String getHighContrastThemeName() {
        return highContrastThemeName.get();
    }

    /**
     * Adds a new stylesheet URL to the list of stylesheets.
     * <p>
     * The returned {@link WritableValue} can be used to change the value of the URL.
     * If the value is set to {@code null}, the stylesheet will not be included in the CSS cascade.
     *
     * @param url the stylesheet URL
     * @return a {@code WritableValue} that represents the stylesheet URL in the list of stylesheets
     */
    protected WritableValue<String> addStylesheet(String url) {
        return stylesheetList.addStylesheet(url);
    }

    @Override
    public final ObservableList<String> getStylesheets() {
        return stylesheetList;
    }

    private final StylesheetList stylesheetList = new StylesheetList();

    private final Consumer<Map<String, String>> preferencesChanged = changedPreferences -> {
        updateProperties();
        onPreferencesChanged(changedPreferences);
    };

    /**
     * Creates a new instance of the {@code ThemeBase} class.
     */
    protected ThemeBase() {
        PlatformImpl.getPreferences().addBatchChangedListener(preferencesChanged);
        updateProperties();
    }

    private void updateProperties() {
        highContrastThemeName.update();
        accentColor.update();
        darkMode.update();

        highContrastThemeName.fireValueChangedEvent();
        accentColor.fireValueChangedEvent();
        darkMode.fireValueChangedEvent();
    }

    /**
     * Occurs when platform preferences have changed.
     * <p>
     * The supplied {@code preferences} map only includes platform preferences that have changed.
     * If a platform preference was removed, its key maps to {@code null}.
     * <p>
     * Use {@link Platform#getPreferences()} to get a list of all platform preferences.
     *
     * @param preferences the platform preferences that have changed
     */
    protected void onPreferencesChanged(Map<String, String> preferences) {}

    private static abstract class BooleanPropertyImpl extends ReadOnlyBooleanPropertyBase {
        abstract void update();

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private static abstract class ObjectPropertyImpl<T> extends ReadOnlyObjectPropertyBase<T> {
        abstract void update();

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private static abstract class StringPropertyImpl extends ReadOnlyStringPropertyBase {
        abstract void update();

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

}
