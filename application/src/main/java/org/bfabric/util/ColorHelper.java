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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import org.bfabric.Constants;

@Named
public class ColorHelper implements Serializable {

    private static final long serialVersionUID = 1;

    public static String convertHexToRgb(String hex) {
        if (StringHelper.isNotEmpty(hex)) {
            Color color = Color.decode(hex);
            return "rgb(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")";
        }
        return null;
    }

    public static String generateRowStyleClassCoupledStyleSheet(List<String> generatedRowStyleClassesCoupled, List<String> generatedRowStyleColorsCoupled) {
        if (generatedRowStyleClassesCoupled != null && generatedRowStyleColorsCoupled != null && generatedRowStyleClassesCoupled.size() == generatedRowStyleColorsCoupled.size()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < generatedRowStyleClassesCoupled.size(); i++) {
                builder.append(i > 0 ? "\n" : Constants.EMPTY_STRING).append(".").append(generatedRowStyleClassesCoupled.get(i)).append(" {").append(generatedRowStyleColorsCoupled.get(i))
                    .append(" }");
            }
            return builder.toString();
        }
        return null;
    }

    public static List<String> getColorsRgba(Collection<String> colorsRgb, Double alphaChannel) {
        List<String> backgroundColors = new ArrayList<>();
        for (String color : colorsRgb != null && !colorsRgb.isEmpty() ? colorsRgb : getDefaultColorsRgb()) {
            backgroundColors.add(color.replace("rgb", "rgba").replace(")", ", " + (alphaChannel != null ? String.valueOf(alphaChannel) : "0.2") + ")"));
        }
        return backgroundColors;
    }

    public static List<String> getDefaultColorsRgb() {
        List<String> colors = new ArrayList<>();
        colors.add("rgb(255, 0, 0)");
        colors.add("rgb(255, 153, 0)");
        colors.add("rgb(255, 255, 0)");
        colors.add("rgb(153, 255, 0)");
        colors.add("rgb(0, 153, 0)");
        colors.add("rgb(0, 255, 255)");
        colors.add("rgb(0, 153, 255)");
        colors.add("rgb(153, 0, 255)");
        colors.add("rgb(255, 0, 255)");
        colors.add("rgb(153, 153, 153)");
        return colors;
    }

    public static List<String> getDefaultColorsRgba() {
        return getColorsRgba(null, 0.2);
    }

    public static List<String> getDistinctColorsRgb() {
        List<String> colors = new ArrayList<>();
        colors.add("rgb(0, 255, 0)");
        colors.add("rgb(0, 0, 255)");
        colors.add("rgb(255, 0, 0)");
        colors.add("rgb(1, 255, 254)");
        colors.add("rgb(255, 166, 254)");
        colors.add("rgb(255, 219, 102)");
        colors.add("rgb(0, 100, 1)");
        colors.add("rgb(1, 0, 103)");
        colors.add("rgb(149, 0, 58)");
        colors.add("rgb(0, 125, 181)");
        colors.add("rgb(255, 0, 246)");
        colors.add("rgb(255, 238, 232)");
        colors.add("rgb(119, 77, 0)");
        colors.add("rgb(144, 251, 146)");
        colors.add("rgb(0, 118, 255)");
        colors.add("rgb(213, 255, 0)");
        colors.add("rgb(255, 147, 126)");
        colors.add("rgb(106, 130, 108)");
        colors.add("rgb(255, 2, 157)");
        colors.add("rgb(254, 137, 0)");
        colors.add("rgb(122, 71, 130)");
        colors.add("rgb(126, 45, 210)");
        colors.add("rgb(133, 169, 0)");
        colors.add("rgb(255, 0, 86)");
        colors.add("rgb(164, 36, 0)");
        colors.add("rgb(0, 174, 126)");
        colors.add("rgb(104, 61, 59)");
        colors.add("rgb(189, 198, 255)");
        colors.add("rgb(38, 52, 0)");
        colors.add("rgb(189, 211, 147)");
        colors.add("rgb(0, 185, 23)");
        colors.add("rgb(158, 0, 142)");
        colors.add("rgb(0, 21, 68)");
        colors.add("rgb(194, 140, 159)");
        colors.add("rgb(255, 116, 163)");
        colors.add("rgb(1, 208, 255)");
        colors.add("rgb(0, 71, 84)");
        colors.add("rgb(229, 111, 254)");
        colors.add("rgb(120, 130, 49)");
        colors.add("rgb(14, 76, 161)");
        colors.add("rgb(145, 208, 203)");
        colors.add("rgb(190, 153, 112)");
        colors.add("rgb(150, 138, 232)");
        colors.add("rgb(187, 136, 0)");
        colors.add("rgb(67, 0, 44)");
        colors.add("rgb(222, 255, 116)");
        colors.add("rgb(0, 255, 198)");
        colors.add("rgb(255, 229, 2)");
        colors.add("rgb(98, 14, 0)");
        colors.add("rgb(0, 143, 156)");
        colors.add("rgb(152, 255, 82)");
        colors.add("rgb(117, 68, 177)");
        colors.add("rgb(181, 0, 255)");
        colors.add("rgb(0, 255, 120)");
        colors.add("rgb(255, 110, 65)");
        colors.add("rgb(0, 95, 57)");
        colors.add("rgb(107, 104, 130)");
        colors.add("rgb(95, 173, 78)");
        colors.add("rgb(167, 87, 64)");
        colors.add("rgb(165, 255, 210)");
        colors.add("rgb(255, 177, 103)");
        colors.add("rgb(0, 155, 255)");
        colors.add("rgb(232, 94, 190)");
        return colors;
    }

    public static List<String> getDistinctColorsRgba() {
        return getColorsRgba(getDistinctColorsRgb(), 0.2);
    }
}
