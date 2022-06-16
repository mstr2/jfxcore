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

package javafx.scene.control.theme;

import com.sun.javafx.scene.control.theme.StylesheetList;
import com.sun.javafx.util.Utils;
import javafx.application.Platform;
import javafx.application.PlatformPreferencesListener;
import javafx.application.WeakPlatformPreferencesListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.css.StyleTheme;
import javafx.scene.paint.Color;
import javafx.util.Incubating;
import java.util.Map;
import java.util.Objects;

/**
 * {@link ThemeBase} is a base implementation for themes that can dynamically change at runtime, for example
 * by switching from light colors to dark colors, or by changing the accent color of UI controls.
 * <p>
 * {@code ThemeBase} uses the operating system's {@link Platform#getPreferences() preferences} to determine
 * the value of {@link #darkModeProperty()} and {@link #accentColorProperty()}, with the option to override
 * their values by setting {@link #darkModeOverrideProperty()} or {@link #accentColorOverrideProperty()}.
 * <p>
 * Stylesheet URIs can be added to this theme by calling {@link #addStylesheet(String)}.
 * The value of a stylesheet URI can be changed at any time with the {@link WritableValue} wrapper
 * that is returned by {@link #addStylesheet(String)}.
 * <p>
 * <h2>Examples</h2>
 * <h3>1. Creating a custom theme</h3>
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
 * Note that instead of loading stylesheets from disk, an application may also create a stylesheet
 * programmatically and load it via an RFC 2397 "data" URI. Among other things, this can be useful
 * to inject dynamically computed colors into themes.
 * <h3>2. Extending an existing theme</h3>
 * A theme can be extended by subclassing its theme class and adding more stylesheets:
 * <pre>{@code
 *     public class MyExtendedTheme extends MyTheme {
 *         public MyExtendedTheme() {
 *             addStylesheet("additional-stylesheet.css");
 *         }
 *     }
 * }</pre>
 *
 * @see StyleTheme
 * @since JFXcore 18
 */
@Incubating
public abstract class ThemeBase implements StyleTheme {

    /**
     * Indicates whether dark mode is requested by the operating system. When the platform does not
     * report dark mode information, the default value is {@code false}.
     * <p>
     * Use {@link #darkModeOverrideProperty()} to override the value of this property.
     */
    private final BooleanPropertyImpl darkMode = new BooleanPropertyImpl() {
        static final String[] KEYS = new String[] { "Windows.UIColor.Foreground" };

        boolean effectiveValue;
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
            return effectiveValue;
        }

        @Override
        public void fireValueChangedEvent() {
            Boolean overrideValue = darkModeOverride.getValue();
            if (overrideValue != null) {
                if (effectiveValue != overrideValue) {
                    currentValue = newValue;
                    effectiveValue = overrideValue;
                    super.fireValueChangedEvent();
                }
            } else if (effectiveValue != currentValue || currentValue != newValue) {
                effectiveValue = currentValue = newValue;
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

            for (String key : KEYS) {
                Color foreground = Platform.getPreferences().getColor(key);
                if (foreground != null) {
                    return Utils.calculateBrightness(foreground) > 0.5;
                }
            }

            return false;
        }
    };

    public final ReadOnlyBooleanProperty darkModeProperty() {
        return darkMode;
    }

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
                    try {
                        stylesheetList.lock();
                        darkMode.fireValueChangedEvent();
                    } finally {
                        stylesheetList.unlock();
                    }
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
        static final String[] KEYS = new String[] { "Windows.UIColor.Accent" };

        Color effectiveValue = DEFAULT_ACCENT_COLOR;
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
            return effectiveValue;
        }

        @Override
        public void fireValueChangedEvent() {
            Color overrideValue = accentColorOverride.getValue();
            if (overrideValue != null) {
                if (!Objects.equals(effectiveValue, overrideValue)) {
                    currentValue = newValue;
                    effectiveValue = overrideValue;
                    super.fireValueChangedEvent();
                }
            } else if (!Objects.equals(effectiveValue, currentValue) || !Objects.equals(currentValue, newValue)) {
                effectiveValue = currentValue = newValue;
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

            for (String key : KEYS) {
                Color accentColor = Platform.getPreferences().getColor(key);
                if (accentColor != null) {
                    return accentColor;
                }
            }

            return DEFAULT_ACCENT_COLOR;
        }
    };

    public final ReadOnlyObjectProperty<Color> accentColorProperty() {
        return accentColor;
    }

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
                    try {
                        stylesheetList.lock();
                        accentColor.fireValueChangedEvent();
                    } finally {
                        stylesheetList.unlock();
                    }
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

    private final PlatformPreferencesListener preferencesChanged = (preferences, changed) -> {
        try {
            stylesheetList.lock();
            updateProperties();
            onPreferencesChanged(changed);
        } finally {
            stylesheetList.unlock();
        }
    };

    /**
     * Creates a new instance of the {@code ThemeBase} class.
     */
    protected ThemeBase() {
        Platform.getPreferences().addListener(new WeakPlatformPreferencesListener(preferencesChanged));
        updateProperties();
    }

    private void updateProperties() {
        accentColor.update();
        darkMode.update();

        accentColor.fireValueChangedEvent();
        darkMode.fireValueChangedEvent();
    }

    /**
     * Occurs when platform preferences have changed.
     * <p>
     * The supplied {@code changed} map only includes platform preferences that have changed.
     * Use {@link Platform#getPreferences()} to get a list of all platform preferences.
     *
     * @param changed the platform preferences that have changed
     */
    protected void onPreferencesChanged(Map<String, Object> changed) {}

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

}
