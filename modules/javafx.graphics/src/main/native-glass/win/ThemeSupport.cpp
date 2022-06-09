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

#include "common.h"
#include "ThemeSupport.h"
#include "RoActivationSupport.h"
#include <windows.ui.viewmanagement.h>

using namespace Microsoft::WRL;
using namespace ABI::Windows::UI;
using namespace ABI::Windows::UI::ViewManagement;

ThemeSupport::ThemeSupport(JNIEnv* env) : env_(env), mapClass_((jclass)env->FindClass("java/util/Map"))
{
    putMethod_ = env->GetMethodID(mapClass_, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
}

ThemeSupport::~ThemeSupport() {
    env_->DeleteLocalRef(mapClass_);
}

void ThemeSupport::queryHighContrastScheme(jobject properties) const
{
    HIGHCONTRAST contrastInfo;
    contrastInfo.cbSize = sizeof(HIGHCONTRAST);
    ::SystemParametersInfo(SPI_GETHIGHCONTRAST, sizeof(HIGHCONTRAST), &contrastInfo, 0);
    if (contrastInfo.dwFlags & HCF_HIGHCONTRASTON) {
        putValue(properties, "Windows.SPI.HighContrastOn", "true");
        putValue(properties, "Windows.SPI.HighContrastColorScheme", contrastInfo.lpszDefaultScheme);
    } else {
        putValue(properties, "Windows.SPI.HighContrastOn", "false");
        putValue(properties, "Windows.SPI.HighContrastColorScheme", "");
    }
}

void ThemeSupport::querySystemColors(jobject properties) const
{
    putColorValue(properties, "Windows.SysColor.COLOR_3DDKSHADOW", GetSysColor(COLOR_3DDKSHADOW));
    putColorValue(properties, "Windows.SysColor.COLOR_3DFACE", GetSysColor(COLOR_3DFACE));
    putColorValue(properties, "Windows.SysColor.COLOR_3DHIGHLIGHT", GetSysColor(COLOR_3DHIGHLIGHT));
    putColorValue(properties, "Windows.SysColor.COLOR_3DHILIGHT", GetSysColor(COLOR_3DHILIGHT));
    putColorValue(properties, "Windows.SysColor.COLOR_3DLIGHT", GetSysColor(COLOR_3DLIGHT));
    putColorValue(properties, "Windows.SysColor.COLOR_3DSHADOW", GetSysColor(COLOR_3DSHADOW));
    putColorValue(properties, "Windows.SysColor.COLOR_ACTIVEBORDER", GetSysColor(COLOR_ACTIVEBORDER));
    putColorValue(properties, "Windows.SysColor.COLOR_ACTIVECAPTION", GetSysColor(COLOR_ACTIVECAPTION));
    putColorValue(properties, "Windows.SysColor.COLOR_APPWORKSPACE", GetSysColor(COLOR_APPWORKSPACE));
    putColorValue(properties, "Windows.SysColor.COLOR_BACKGROUND", GetSysColor(COLOR_BACKGROUND));
    putColorValue(properties, "Windows.SysColor.COLOR_BTNFACE", GetSysColor(COLOR_BTNFACE));
    putColorValue(properties, "Windows.SysColor.COLOR_BTNHIGHLIGHT", GetSysColor(COLOR_BTNHIGHLIGHT));
    putColorValue(properties, "Windows.SysColor.COLOR_BTNHILIGHT", GetSysColor(COLOR_BTNHILIGHT));
    putColorValue(properties, "Windows.SysColor.COLOR_BTNSHADOW", GetSysColor(COLOR_BTNSHADOW));
    putColorValue(properties, "Windows.SysColor.COLOR_BTNTEXT", GetSysColor(COLOR_BTNTEXT));
    putColorValue(properties, "Windows.SysColor.COLOR_CAPTIONTEXT", GetSysColor(COLOR_CAPTIONTEXT));
    putColorValue(properties, "Windows.SysColor.COLOR_DESKTOP", GetSysColor(COLOR_DESKTOP));
    putColorValue(properties, "Windows.SysColor.COLOR_GRADIENTACTIVECAPTION", GetSysColor(COLOR_GRADIENTACTIVECAPTION));
    putColorValue(properties, "Windows.SysColor.COLOR_GRADIENTINACTIVECAPTION", GetSysColor(COLOR_GRADIENTINACTIVECAPTION));
    putColorValue(properties, "Windows.SysColor.COLOR_GRAYTEXT", GetSysColor(COLOR_GRAYTEXT));
    putColorValue(properties, "Windows.SysColor.COLOR_HIGHLIGHT", GetSysColor(COLOR_HIGHLIGHT));
    putColorValue(properties, "Windows.SysColor.COLOR_HIGHLIGHTTEXT", GetSysColor(COLOR_HIGHLIGHTTEXT));
    putColorValue(properties, "Windows.SysColor.COLOR_HOTLIGHT", GetSysColor(COLOR_HOTLIGHT));
    putColorValue(properties, "Windows.SysColor.COLOR_INACTIVEBORDER", GetSysColor(COLOR_INACTIVEBORDER));
    putColorValue(properties, "Windows.SysColor.COLOR_INACTIVECAPTION", GetSysColor(COLOR_INACTIVECAPTION));
    putColorValue(properties, "Windows.SysColor.COLOR_INACTIVECAPTIONTEXT", GetSysColor(COLOR_INACTIVECAPTIONTEXT));
    putColorValue(properties, "Windows.SysColor.COLOR_INFOBK", GetSysColor(COLOR_INFOBK));
    putColorValue(properties, "Windows.SysColor.COLOR_INFOTEXT", GetSysColor(COLOR_INFOTEXT));
    putColorValue(properties, "Windows.SysColor.COLOR_MENU", GetSysColor(COLOR_MENU));
    putColorValue(properties, "Windows.SysColor.COLOR_MENUHILIGHT", GetSysColor(COLOR_MENUHILIGHT));
    putColorValue(properties, "Windows.SysColor.COLOR_MENUBAR", GetSysColor(COLOR_MENUBAR));
    putColorValue(properties, "Windows.SysColor.COLOR_MENUTEXT", GetSysColor(COLOR_MENUTEXT));
    putColorValue(properties, "Windows.SysColor.COLOR_SCROLLBAR", GetSysColor(COLOR_SCROLLBAR));
    putColorValue(properties, "Windows.SysColor.COLOR_WINDOW", GetSysColor(COLOR_WINDOW));
    putColorValue(properties, "Windows.SysColor.COLOR_WINDOWFRAME", GetSysColor(COLOR_WINDOWFRAME));
    putColorValue(properties, "Windows.SysColor.COLOR_WINDOWTEXT", GetSysColor(COLOR_WINDOWTEXT));
}

void ThemeSupport::queryWindows10ThemeColors(jobject properties) const
{
    if (!isRoActivationSupported()) {
        return;
    }

    try {
        ComPtr<IUISettings> settings;
        RO_CHECKED("RoActivateInstance",
                   RoActivateInstance(hstring("Windows.UI.ViewManagement.UISettings"), (IInspectable**)&settings));

        ComPtr<IUISettings3> settings3;
        RO_CHECKED("IUISettings::QueryInterface<IUISettings3>",
                   settings->QueryInterface<IUISettings3>(&settings3));

        Color background, foreground, accentDark3, accentDark2, accentDark1, accent,
              accentLight1, accentLight2, accentLight3;

        settings3->GetColorValue(UIColorType::UIColorType_Background, &background);
        settings3->GetColorValue(UIColorType::UIColorType_Foreground, &foreground);
        settings3->GetColorValue(UIColorType::UIColorType_AccentDark3, &accentDark3);
        settings3->GetColorValue(UIColorType::UIColorType_AccentDark2, &accentDark2);
        settings3->GetColorValue(UIColorType::UIColorType_AccentDark1, &accentDark1);
        settings3->GetColorValue(UIColorType::UIColorType_Accent, &accent);
        settings3->GetColorValue(UIColorType::UIColorType_AccentLight1, &accentLight1);
        settings3->GetColorValue(UIColorType::UIColorType_AccentLight2, &accentLight2);
        settings3->GetColorValue(UIColorType::UIColorType_AccentLight3, &accentLight3);

        putColorValue(properties, "Windows.UIColor.Background", background);
        putColorValue(properties, "Windows.UIColor.Foreground", foreground);
        putColorValue(properties, "Windows.UIColor.AccentDark3", accentDark3);
        putColorValue(properties, "Windows.UIColor.AccentDark2", accentDark2);
        putColorValue(properties, "Windows.UIColor.AccentDark1", accentDark1);
        putColorValue(properties, "Windows.UIColor.Accent", accent);
        putColorValue(properties, "Windows.UIColor.AccentLight1", accentLight1);
        putColorValue(properties, "Windows.UIColor.AccentLight2", accentLight2);
        putColorValue(properties, "Windows.UIColor.AccentLight3", accentLight3);
    } catch (RoException const& ex) {
        // If an activation exception occurs, it probably means that we're on a Windows system
        // that doesn't support the UISettings API. This is not a problem, it simply means that
        // we don't report the UISettings properties back to the JavaFX application.
        return;
    }
}

jobject ThemeSupport::newJavaColorString(int r, int g, int b, int a) const
{
    char value[9];
    sprintf_s(value, 9, "%02X%02X%02X%02X", r, g, b, a);
    return env_->NewStringUTF(value);
}

void ThemeSupport::putValue(jobject properties, const char* key, const char* value) const
{
    env_->CallObjectMethod(properties, putMethod_, env_->NewStringUTF(key), env_->NewStringUTF(value));
}

void ThemeSupport::putValue(jobject properties, const char* key, const wchar_t* value) const
{
    env_->CallObjectMethod(properties, putMethod_, env_->NewStringUTF(key), env_->NewString((jchar*)value, wcslen(value)));
}

void ThemeSupport::putColorValue(jobject properties, const char* colorName, int colorValue) const
{
    env_->CallObjectMethod(properties, putMethod_,
        env_->NewStringUTF(colorName),
        newJavaColorString(GetRValue(colorValue), GetGValue(colorValue), GetBValue(colorValue), 255));
}

void ThemeSupport::putColorValue(jobject properties, const char* colorName, Color colorValue) const
{
    env_->CallObjectMethod(properties, putMethod_,
        env_->NewStringUTF(colorName),
        newJavaColorString(colorValue.R, colorValue.G, colorValue.B, colorValue.A));
}