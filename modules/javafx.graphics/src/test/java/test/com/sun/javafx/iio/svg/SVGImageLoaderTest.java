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

package test.com.sun.javafx.iio.svg;

import com.sun.javafx.iio.ImageFrame;
import com.sun.javafx.iio.ImageStorage;
import com.sun.javafx.iio.png.PNGImageLoader2;
import com.sun.javafx.iio.svg.SVGImageLoader;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SVGImageLoaderTest {

    private static SVGImageLoader newImageLoader(String content) throws IOException {
        return new SVGImageLoader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testEmptyFile() {
        assertThrows(IOException.class, () -> newImageLoader("").load(0, 0, 0, true, false, 1));
    }

    @Test
    public void testEmptySvg() throws IOException {
        ImageFrame imageFrame = newImageLoader("""
            <svg version="1.1" width="30" height="20" xmlns="http://www.w3.org/2000/svg"/>
            """).load(0, 0, 0, true, false, 1);

        assertEquals(30, imageFrame.getWidth());
        assertEquals(20, imageFrame.getHeight());
        assertEquals(1, imageFrame.getPixelScale(), 0.001);
    }

    @Test
    public void testSimpleSvg() throws IOException {
        ImageFrame imageFrame = newImageLoader("""
            <svg version="1.1" width="30" height="20" xmlns="http://www.w3.org/2000/svg">
               <rect width="30%" height="100%" fill="red" />
               <rect x="30%" width="30%" height="100%" fill="lime" />
               <rect x="60%" width="30%" height="100%" fill="blue" />
            </svg>
            """).load(0, 0, 0, true, false, 1);

        assertEquals(30, imageFrame.getWidth());
        assertEquals(20, imageFrame.getHeight());
        assertEquals(4 * 30, imageFrame.getStride());
        assertEquals(ImageStorage.ImageType.RGBA_PRE, imageFrame.getImageType());

        byte[] data = (byte[])imageFrame.getImageData().array();
        assertEquals(-1, data[5 * 4]);
        assertEquals(0, data[5 * 4 + 1]);
        assertEquals(0, data[5 * 4 + 2]);

        assertEquals(0, data[15 * 4]);
        assertEquals(-1, data[15 * 4 + 1]);
        assertEquals(0, data[15 * 4 + 2]);

        assertEquals(0, data[25 * 4]);
        assertEquals(0, data[25 * 4 + 1]);
        assertEquals(-1, data[25 * 4 + 2]);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testSvgReferenceFiles() throws IOException, URISyntaxException {
        String[] testFiles = new String[] { "a-color-002", "a-display-003", "a-display-004", "e-marker-001" };

        Path testsDir = Path.of(SVGImageLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI())
            .getParent().getParent().getParent()
            .resolve("modules/javafx.graphics/src/main/native-iio/resvg/tests");

        for (String testFile : testFiles) {
            ImageFrame pngImageFrame = new PNGImageLoader2(
                    new ByteArrayInputStream(Files.readAllBytes(testsDir.resolve("png").resolve(testFile + ".png"))))
                .load(0, 0, 0, true, false, 1);

            ImageFrame svgImageFrame = new SVGImageLoader(
                    new ByteArrayInputStream(Files.readAllBytes(testsDir.resolve("svg").resolve(testFile + ".svg"))))
                .load(0, pngImageFrame.getWidth(), 0, true, false, 1);

            byte[] pngImg = (byte[])pngImageFrame.getImageData().array();
            byte[] svgImg = (byte[])svgImageFrame.getImageData().array();
            assertEquals(pngImg.length, svgImg.length);
            assertEquals(0, svgImg.length % 4);
            assertEquals(pngImageFrame.getWidth(), svgImageFrame.getWidth());
            assertEquals(pngImageFrame.getHeight(), svgImageFrame.getHeight());

            for (int y = 0; y < pngImageFrame.getHeight(); ++y) {
                for (int x = 0; x < pngImageFrame.getWidth(); ++x) {
                    int offs = y * pngImageFrame.getStride();
                    int png_a = Byte.toUnsignedInt(pngImg[offs + x * 4 + 3]);
                    double alpha =  ((double)png_a / 255.0);
                    int png_r = (int)((double)Byte.toUnsignedInt(pngImg[offs + x * 4]) * alpha);
                    int png_g = (int)((double)Byte.toUnsignedInt(pngImg[offs + x * 4 + 1]) * alpha);
                    int png_b = (int)((double)Byte.toUnsignedInt(pngImg[offs + x * 4 + 2]) * alpha);
                    int svg_a = Byte.toUnsignedInt(svgImg[offs + x * 4 + 3]);
                    int svg_r = Byte.toUnsignedInt(svgImg[offs + x * 4]);
                    int svg_g = Byte.toUnsignedInt(svgImg[offs + x * 4 + 1]);
                    int svg_b = Byte.toUnsignedInt(svgImg[offs + x * 4 + 2]);

                    if (Math.abs(png_a - svg_a) > 1 || Math.abs(png_r - svg_r) > 1
                            || Math.abs(png_g - svg_g) > 1 || Math.abs(png_b - svg_b) > 1) {
                        fail(String.format("%s x=%d y=%d, png=[%d,%d,%d,%d], svg=[%d,%d,%d,%d]",
                            testFile, x, y, png_r, png_g, png_b, png_a, svg_r, svg_g, svg_b, svg_a));
                    }
                }
            }
        }
    }

}
