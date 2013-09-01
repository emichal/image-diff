import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.*;
import java.io.*;

import static com.emichal.imagediff.ImageDiff.image;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(JUnit4.class)
public class ImageDiffTest {

    private static InputStream image1() {
        return ClassLoader.getSystemResourceAsStream("screenshot1.png");
    }

    private static InputStream image2() {
        return ClassLoader.getSystemResourceAsStream("screenshot2.png");
    }

    private static File defaultDiffOutput() throws IOException {
        File tmpOutput = File.createTempFile("imagediff", "test");
        tmpOutput.deleteOnExit();

        FileOutputStream outputStream = new FileOutputStream(tmpOutput);
        IOUtils.copy(ClassLoader.getSystemResourceAsStream("output.gif"), outputStream);

        return tmpOutput;
    }

    @Test
    public void shouldConfirmDimensionEquality() throws IOException {
        assertThat(image(image2())
                .comparedTo(image1())
                .hasEqualDimensions()
        ).isTrue();
    }

    @Test
    public void shouldConfirmEquality() throws IOException {
        assertThat(image(image1())
                .comparedTo(image1())
                .isEqual()
        ).isTrue();
    }

    @Test
    public void shouldNotConfirmEquality() throws IOException {
        assertThat(image(image2())
                .comparedTo(image1())
                .isEqual()
        ).isFalse();
    }

    @Test
    public void shouldConfirmEqualityWithinRegion() throws IOException {
        assertThat(image(image1())
                .comparedTo(image2())
                .withinRectangle(new Point(45, 51), new Point(46, 42))
                .isEqual()
        ).isTrue();
    }

    @Test
    public void shouldNotConfirmEqualityWithinRegion() throws IOException {
        assertThat(image(image1())
                .comparedTo(image2())
                .withinRectangle(new Point(0, 0), new Point(200, 400))
                .isEqual()
        ).isFalse();
    }

    @Test
    public void shouldGenerateImageDiff() throws IOException {
        File output = File.createTempFile("imagediff", "generated");
        output.deleteOnExit();

        assertThat(image(image1())
                .comparedTo(image2())
                .withAnimatedOutput(new FileOutputStream(output))
                .isEqual()
        ).isFalse();

        assertThat(output)
                .isFile()
                .exists()
                .hasSameContentAs(defaultDiffOutput());
    }
}