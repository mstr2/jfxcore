package test.com.sun.javafx.iio.svg;

import com.sun.javafx.iio.ImageFrame;
import com.sun.javafx.iio.ImageStorage;
import com.sun.javafx.iio.svg.SVGImageLoader;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

}
