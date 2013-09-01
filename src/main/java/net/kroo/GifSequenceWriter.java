package net.kroo;
//
//  GifSequenceWriter.java
//  
//  Created by Elliot Kroo on 2009-04-25.
//
// This work is licensed under the Creative Commons Attribution 3.0 Unported
// License. To view a copy of this license, visit
// http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
// Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;

public class GifSequenceWriter {
    private ImageWriter gifWriter;
    private ImageWriteParam imageWriteParam;
    private IIOMetadata imageMetaData;

    /**
     * Creates a new GifSequenceWriter
     *
     * @param outputStream the ImageOutputStream to be written to
     * @param time         the time between frames in miliseconds
     * @throws IIOException if no gif ImageWriters are found
     */
    public GifSequenceWriter(ImageOutputStream outputStream, int time) throws IOException {
        // my method to create a writer
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(time));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");
        child.setUserObject(new byte[]{0x1, (byte) (0), (byte) ((0 >> 8))});

        IIOMetadataNode appEntensionsNode = getNode(root, "ApplicationExtensions");
        appEntensionsNode.appendChild(child);

        imageMetaData.setFromTree(metaFormatName, root);

        gifWriter.setOutput(outputStream);
        gifWriter.prepareWriteSequence(null);
    }

    /**
     * Returns the first available GIF ImageWriter using
     * ImageIO.getImageWritersBySuffix("gif").
     *
     * @return a GIF ImageWriter object
     * @throws IIOException if no GIF image writers are returned
     */
    private static ImageWriter getWriter() throws IIOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IIOException("No GIF Image Writers Exist");
        } else {
            return iter.next();
        }
    }

    /**
     * Returns an existing child node, or creates and returns a new child node (if
     * the requested node does not exist).
     *
     * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
     * @param nodeName the name of the child node.
     * @return the child node, if found or a new node created with the given name.
     */
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }

        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);

        return (node);
    }

    public void writeToSequence(RenderedImage img) throws IOException {
        gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
    }

    /**
     * Close this GifSequenceWriter object. This does not close the underlying
     * stream, just finishes off the GIF.
     */
    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }
}