package com.emichal.imagediff;

import net.kroo.GifSequenceWriter;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class AnimationGenerator {

    private GifSequenceWriter gifWriter;
    private BufferedImage[] imageArray;
    private ImageOutputStream output;
    private int delay = 200;

    public AnimationGenerator createAnimatedGIF(BufferedImage... images) {
        imageArray = images;
        return this;
    }

    public AnimationGenerator withDelay(int seconds) {
        this.delay = seconds * 1000;
        return this;
    }

    public AnimationGenerator toStream(OutputStream stream) throws IOException {
        output = new MemoryCacheImageOutputStream(stream);
        gifWriter = getGifWriter(output);

        return this;
    }

    public void build() throws IOException {
        for (BufferedImage image : imageArray) {
            gifWriter.writeToSequence(image);
        }

        gifWriter.close();
        output.close();
    }

    private GifSequenceWriter getGifWriter(ImageOutputStream output) throws IOException {
        return new GifSequenceWriter(output, delay);
    }
}
