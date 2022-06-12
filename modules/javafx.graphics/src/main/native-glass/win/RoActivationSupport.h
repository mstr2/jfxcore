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

#define _ROAPI_
#include <roapi.h>
#include <wrl.h>
#include <hstring.h>

#define RO_CHECKED(NAME, FUNC) \
    { HRESULT res = FUNC; if (FAILED(res)) throw RoException(NAME ## " failed: ", res); }

struct hstring
{
    hstring(const char* str);
    ~hstring();
    operator HSTRING();

private:
    HSTRING hstr_;
};

void tryInitializeRoActivationSupport();
void uninitializeRoActivationSupport();
bool isRoActivationSupported();

class RoException
{
public:
    RoException(const char* message);
    RoException(const char* message, HRESULT);
    RoException(const RoException&);
    RoException(RoException&&);
    ~RoException();

    RoException& operator=(const RoException&);
    RoException& operator=(RoException&&);

    const char* message() const;

private:
    const char* message_;
};
