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

#pragma once

#include <jni.h>

namespace ABI { namespace Windows { namespace UI { struct Color; } } }

class ThemeSupport final
{
public:
    ThemeSupport(JNIEnv*);
    ~ThemeSupport();
    ThemeSupport(ThemeSupport const&) = delete;
    ThemeSupport& operator=(ThemeSupport const&) = delete;

    void querySystemColors(jobject properties) const;
    void queryHighContrastScheme(jobject properties) const;
    void queryUIColors(jobject properties) const;

private:
    JNIEnv* env_;
    jclass mapClass_;
    jclass colorClass_;
    jclass booleanClass_;
    jmethodID putMethod_;
    jmethodID rgbMethod_;
    jfieldID trueField_;
    jfieldID falseField_;

    void putString(jobject properties, const char* key, const char* value) const;
    void putString(jobject properties, const char* key, const wchar_t* value) const;
    void putBoolean(jobject properties, const char* key, const bool value) const;
    void putColor(jobject properties, const char* colorName, int colorValue) const;
    void putColor(jobject properties, const char* colorName, ABI::Windows::UI::Color colorValue) const;
};
