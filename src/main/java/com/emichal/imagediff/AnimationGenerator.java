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

    /**
     * @param images to be used in GIF sequence
     * @return this instance of AnimationGenerator
     */
    public AnimationGenerator createAnimatedGIF(BufferedImage... images) {
        imageArray = images;
        return this;
    }

    /**
     * @param seconds of pause between frames
     * @return this instance of AnimationGenerator
     */
    public AnimationGenerator withDelay(int seconds) {
        this.delay = seconds * 100;
        return this;
    }

    /**
     * @param stream to save output to
     * @return this instance of AnimationGenerator
     * @throws IOException
     */
    public AnimationGenerator toStream(OutputStream stream) throws IOException {
        output = new MemoryCacheImageOutputStream(stream);
        gifWriter = getGifWriter(output);

        return this;
    }

    /**
     * Builds GIF sequence to specified stream
     *
     * @throws IOException
     */
    public void build() throws IOException {
        for (BufferedImage image : imageArray) {
            gifWriter.writeToSequence(image);
        }

        gifWriter.close();
        output.close();
    }

    private GifSequenceWriter getGifWriter(ImageOutputStream output) throws IOException {
        return new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, delay, true);
    }
}
