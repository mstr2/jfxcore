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

package com.sun.javafx.iio.svg;

import com.sun.glass.utils.NativeLibLoader;
import com.sun.javafx.iio.ImageFrame;
import com.sun.javafx.iio.ImageMetadata;
import com.sun.javafx.iio.ImageStorage;
import com.sun.javafx.iio.common.ImageLoaderImpl;
import com.sun.javafx.iio.common.ImageTools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class SVGImageLoader extends ImageLoaderImpl {

    static {
        @SuppressWarnings("removal")
        var dummy = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            NativeLibLoader.loadLibrary("javafx_iio");
            return null;
        });
    }

    private final long documentHandle;

    protected SVGImageLoader(InputStream input) throws IOException {
        super(SVGDescriptor.getInstance());
        documentHandle = parseDocument(input.readAllBytes());
    }

    @Override
    public void dispose() {
        freeDocument(documentHandle);
    }

    @Override
    public ImageFrame load(int imageIndex, double width, double height, boolean preserveAspectRatio,
                           boolean smooth, float pixelScale) throws IOException {
        if (imageIndex > 0) {
            return null;
        }

        double[] imageSize = getImageSize(documentHandle);
        int sourceWidth = (int)(imageSize[0] * pixelScale);
        int sourceHeight = (int)(imageSize[1] * pixelScale);
        int[] widthHeight = ImageTools.computeDimensions(
            sourceWidth, sourceHeight, (int)(width * pixelScale), (int)(height * pixelScale), preserveAspectRatio);
        double scaleX = (double)widthHeight[0] / (double)sourceWidth;
        double scaleY = (double)widthHeight[1] / (double)sourceHeight;

        SVGImageData imageData = renderDocument(documentHandle, widthHeight[0], widthHeight[1], scaleX, scaleY);

        ImageMetadata metadata = new ImageMetadata(null, true,
            null, null, null, null, null,
            imageData.getWidth(), imageData.getHeight(), null, null, null);

        return new ImageFrame(
            ImageStorage.ImageType.RGBA_PRE, ByteBuffer.wrap(imageData.getPixels()), imageData.getWidth(),
            imageData.getHeight(), imageData.getWidth() * 4, null, pixelScale, metadata);
    }

    private static native long parseDocument(byte[] data);
    private static native long freeDocument(long handle);
    private static native SVGImageData renderDocument(long handle, int width, int height, double scaleX, double scaleY);
    private static native double[] getImageSize(long handle);

}
