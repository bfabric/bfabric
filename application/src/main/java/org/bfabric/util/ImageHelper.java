/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageHelper {

    public static Dimension getMaxScaledDimension(Dimension srcDimension, Dimension maxDimension) {
        int srcWidth = srcDimension.width;
        int srcHeight = srcDimension.height;
        int maxWidth = maxDimension.width;
        int maxHeight = maxDimension.height;
        int maxScaledWidth = srcWidth;
        int maxScaledHeight = srcHeight;
        // Check if it is necessary to scale the width.
        if (srcWidth > maxWidth) {
            // Scale width to fit.
            maxScaledWidth = maxWidth;
            // Scale height to maintain the aspect ratio.
            maxScaledHeight = maxScaledWidth * srcHeight / srcWidth;
        }
        // Check if it is necessary to scale even with the new height.
        if (maxScaledHeight > maxHeight) {
            // Scale height to fit instead.
            maxScaledHeight = maxHeight;
            // Scale width to maintain the aspect ratio
            maxScaledWidth = maxScaledHeight * srcWidth / srcHeight;
        }
        return new Dimension(maxScaledWidth, maxScaledHeight);
    }

    public static BufferedImage resizeToFit(BufferedImage bufferedImage, int maxWidth, int maxHeight) {
        int scaledWidth = bufferedImage.getWidth();
        int scaledHeight = bufferedImage.getHeight();
        if (scaledWidth > maxWidth || scaledHeight > maxHeight) {
            Dimension scaledDimension = getMaxScaledDimension(new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()), new Dimension(maxWidth, maxHeight));
            scaledWidth = scaledDimension.width;
            scaledHeight = scaledDimension.height;
        }
        Image scaledImaged = bufferedImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(scaledWidth, scaledHeight, bufferedImage.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : bufferedImage.getType());
        if (resized.getColorModel().hasAlpha()) {
            resized = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
            resized.getGraphics().drawImage(bufferedImage, 0, 0, null);
        }
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaledImaged, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        return resized;
    }
}
