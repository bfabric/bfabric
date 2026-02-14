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

package org.bfabric.service.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import org.bfabric.util.ColorHelper;
import org.bfabric.util.StringHelper;
import org.primefaces.model.charts.ChartOptions;
import org.primefaces.model.charts.axes.cartesian.CartesianAxes;
import org.primefaces.model.charts.axes.cartesian.CartesianScaleTitle;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.title.Title;

@Named
public class Chart implements Serializable {

    private static final Logger logger = Logger.getLogger(Chart.class.getName());

    private static final long serialVersionUID = 1;

    public static List<String> getBackgroundColors(List<String> colors) {
        List<String> backgroundColors = new ArrayList<>();
        if (colors != null && !colors.isEmpty()) {
            for (String color : colors) {
                backgroundColors.add("#" + color);
            }
        } else {
            backgroundColors.addAll(getDefaultBackgroundColors());
        }
        return backgroundColors;
    }

    public static List<String> getBorderColors(List<String> colors) {
        List<String> borderColors = new ArrayList<>();
        if (colors != null && !colors.isEmpty()) {
            for (String color : colors) {
                borderColors.add("#" + color);
            }
        } else {
            borderColors.addAll(getDefaultColors());
        }
        return borderColors;
    }

    public static List<String> getDefaultBackgroundColors() {
        return ColorHelper.getDefaultColorsRgba();
    }

    public static List<String> getDefaultColors() {
        return ColorHelper.getDefaultColorsRgb();
    }

    public static void setAxisLabel(CartesianAxes cartesianAxes, String axisLabel) {
        if (StringHelper.isNotEmpty(axisLabel)) {
            CartesianScaleTitle cartesianScaleTitle = new CartesianScaleTitle();
            cartesianScaleTitle.setText(axisLabel);
            cartesianScaleTitle.setDisplay(true);
            cartesianAxes.setScaleTitle(cartesianScaleTitle);
        }
    }

    public static void setLegend(ChartOptions chartOptions, String legendPosition) {
        if (chartOptions != null && StringHelper.isNotEmpty(legendPosition)) {
            Legend legend = new Legend();
            legend.setPosition(legendPosition);
            chartOptions.setLegend(legend);
        }
    }

    public static void setTitle(ChartOptions chartOptions, String title) {
        if (StringHelper.isNotEmpty(title)) {
            Title optionalTitle = new Title();
            optionalTitle.setDisplay(true);
            optionalTitle.setText(title);
            chartOptions.setTitle(optionalTitle);
        }
    }

    public List<String> getLabels(List<Object[]> values) {
        List<String> labels = new ArrayList<>();
        if (values != null) {
            for (Object[] objectArray : values) {
                labels.add(String.valueOf(objectArray[1]));
            }
        }
        return labels;
    }

    public List<Number> getNumbers(List<Object[]> values) {
        List<Number> numbers = new ArrayList<>();
        if (values != null) {
            for (Object[] objectArray : values) {
                try {
                    numbers.add(Double.valueOf(String.valueOf(objectArray[0])).intValue());
                } catch (Exception e) {
                    logger.fine(e.getMessage());
                }
            }
        }
        return numbers;
    }
}