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

package com.sun.javafx.application;

import com.sun.javafx.util.Utils;
import javafx.application.PlatformTheme;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

final class PlatformThemeImpl implements PlatformTheme {

    private static final Color DEFAULT_ACCENT_COLOR = Color.rgb(21, 126, 251);

    public void update(Map<String, String> properties) {
        darkMode.set(isDarkMode(properties));
        accentColor.set(getAccentColor(properties));

        Iterator<Map.Entry<String, String>> it1 = properties.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry<String, String> entry = it1.next();
            WeakReference<StringPropertyImpl> wref = observableProperties.get(entry.getKey());
            if (wref != null) {
                StringPropertyImpl property = wref.get();
                if (property != null) {
                    property.set(entry.getValue());
                } else {
                    it1.remove();
                }
            }
        }

        this.properties.putAll(properties);
        this.darkMode.fireValueChangedEvent();
        this.accentColor.fireValueChangedEvent();

        Iterator<Map.Entry<String, WeakReference<StringPropertyImpl>>> it2 = observableProperties.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<String, WeakReference<StringPropertyImpl>> entry = it2.next();
            StringPropertyImpl property = entry.getValue().get();
            if (property != null) {
                property.fireValueChangedEvent();
            } else {
                it2.remove();
            }
        }
    }

    private boolean isDarkMode(Map<String, String> properties) {
        String foreground = properties.get("Windows.UIColor.Foreground");
        if (foreground != null) {
            return Utils.calculateBrightness(Color.valueOf(foreground)) > 0.5;
        }

        return false;
    }

    private Color getAccentColor(Map<String, String> properties) {
        String accentColor = properties.get("Windows.UIColor.Accent");
        if (accentColor != null) {
            return Color.valueOf(accentColor);
        }

        return DEFAULT_ACCENT_COLOR;
    }

    private final BooleanPropertyImpl darkMode = new BooleanPropertyImpl(false) {
        @Override
        public String getName() {
            return "darkMode";
        }
    };

    @Override
    public ReadOnlyBooleanProperty darkModeProperty() {
        return darkMode;
    }

    @Override
    public boolean isDarkMode() {
        return darkMode.get();
    }

    private final ObjectPropertyImpl<Color> accentColor = new ObjectPropertyImpl<>(DEFAULT_ACCENT_COLOR) {
        @Override
        public String getName() {
            return "accentColor";
        }
    };

    @Override
    public ReadOnlyObjectProperty<Color> accentColorProperty() {
        return accentColor;
    }

    @Override
    public Color getAccentColor() {
        return accentColor.get();
    }

    private final ObservableMap<String, String> properties =
            FXCollections.observableHashMap();
    private final ObservableMap<String, String> unmodifiableProperties =
            FXCollections.unmodifiableObservableMap(properties);

    @Override
    public ObservableMap<String, String> getProperties() {
        return unmodifiableProperties;
    }

    private final Map<String, WeakReference<StringPropertyImpl>> observableProperties = new HashMap<>();

    @Override
    public synchronized ReadOnlyStringProperty getProperty(String name) {
        WeakReference<StringPropertyImpl> ref = observableProperties.get(name);
        if (ref != null) {
            StringPropertyImpl property = ref.get();
            if (property != null) {
                return property;
            }
        }

        StringPropertyImpl property = new StringPropertyImpl(properties.get(name)) {
            @Override
            public String getName() {
                return name;
            }
        };

        observableProperties.put(name, new WeakReference<>(property));
        return property;
    }

    private abstract class BooleanPropertyImpl extends ReadOnlyBooleanPropertyBase {
        private boolean currentValue, newValue;

        public BooleanPropertyImpl(boolean initialValue) {
            currentValue = newValue = initialValue;
        }

        @Override
        public Object getBean() {
            return PlatformThemeImpl.this;
        }

        @Override
        public boolean get() {
            return currentValue;
        }

        public void set(boolean value) {
            newValue = value;
        }

        @Override
        public void fireValueChangedEvent() {
            if (currentValue != newValue) {
                currentValue = newValue;
                super.fireValueChangedEvent();
            }
        }
    }

    private abstract class ObjectPropertyImpl<T> extends ReadOnlyObjectPropertyBase<T> {
        private T currentValue, newValue;

        public ObjectPropertyImpl(T initialValue) {
            currentValue = newValue = initialValue;
        }

        @Override
        public Object getBean() {
            return PlatformThemeImpl.this;
        }

        @Override
        public T get() {
            return currentValue;
        }

        public void set(T value) {
            newValue = value;
        }

        @Override
        public void fireValueChangedEvent() {
            if (!Objects.equals(currentValue, newValue)) {
                currentValue = newValue;
                super.fireValueChangedEvent();
            }
        }
    }

    private abstract class StringPropertyImpl extends ReadOnlyStringPropertyBase {
        private String currentValue, newValue;

        public StringPropertyImpl(String initialValue) {
            currentValue = newValue = initialValue;
        }

        @Override
        public Object getBean() {
            return PlatformThemeImpl.this;
        }

        @Override
        public String get() {
            return currentValue;
        }

        public void set(String value) {
            newValue = value;
        }

        @Override
        public void fireValueChangedEvent() {
            if (!Objects.equals(currentValue, newValue)) {
                currentValue = newValue;
                super.fireValueChangedEvent();
            }
        }
    }

}
