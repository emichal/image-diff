package com.emichal.imagediff;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class ImageDiff {

    private final BufferedImage actualImage;
    private BufferedImage expectedImage;
    private Set<Point> differences;
    private Point rectangleStart;
    private Point rectangleEnd;

    private ImageDiff(BufferedImage actual) {
        actualImage = actual;
    }

    public static ImageDiff image(InputStream actual) throws IOException {
        return new ImageDiff(ImageIO.read(actual));
    }

    public ImageDiff comparedTo(InputStream expected) throws IOException {
        expectedImage = ImageIO.read(expected);
        return this;
    }

    public ImageDiff withinRectangle(Point start, Point end) {
        rectangleStart = start;
        rectangleEnd = end;
        return this;
    }

    public ImageDiff withAnimatedOutput(OutputStream output) throws IOException {
        if (isEqual()) return this;

        AnimationGenerator animationGenerator = new AnimationGenerator();
        animationGenerator
                .createAnimatedGIF(actualImage, expectedImage, getDiffImage())
                .toStream(output)
                .withDelay(2)
                .build();

        return this;
    }

    public boolean isEqual() {
        return hasEqualDimensions() && getDiffPoints().size() == 0;
    }

    public boolean hasEqualDimensions() {
        return actualImage.getWidth() == expectedImage.getWidth() &&
                actualImage.getHeight() == expectedImage.getHeight();
    }

    private BufferedImage getDiffImage() {
        BufferedImage diff = getDeepCopy(actualImage);

        for (Point point : getDiffPoints()) {
            Color actualPx = new Color(actualImage.getRGB(point.x, point.y));
            Color expectedPx = new Color(expectedImage.getRGB(point.x, point.y));
            Color diffPx = getDiffPx(actualPx, expectedPx);

            diff.setRGB(point.x, point.y, diffPx.getRGB());
        }

        return diff;
    }

    private Color getDiffPx(Color actual, Color expected) {
        return new Color(
                (actual.getRed() + expected.getRed()) / 2,
                (actual.getGreen() + expected.getGreen()) / 2,
                (actual.getBlue() + expected.getBlue()) / 2
        );
    }

    private Set<Point> getDiffPoints() {
        if (differences != null) {
            return differences;
        }

        differences = new HashSet<>();
        for (int x = 0; x < actualImage.getWidth(); x++) {
            for (int y = 0; y < actualImage.getHeight(); y++) {
                if ((actualImage.getRGB(x, y) != expectedImage.getRGB(x, y)) &&
                        isWithinConsideredRectangle(new Point(x, y))) {

                    differences.add(new Point(x, y));
                }
            }
        }

        return differences;
    }

    private boolean isWithinConsideredRectangle(Point point) {
        return !(rectangleStart != null && rectangleEnd != null) ||
                point.x >= rectangleStart.x && point.y >= rectangleStart.y &&
                        point.x <= rectangleEnd.x && point.y <= rectangleEnd.y;
    }

    private BufferedImage getDeepCopy(BufferedImage bufferedImage) {
        ColorModel colorModel = bufferedImage.getColorModel();
        WritableRaster writableRaster = bufferedImage.copyData(null);

        return new BufferedImage(
                colorModel,
                writableRaster,
                colorModel.isAlphaPremultiplied(),
                null
        );
    }
}
