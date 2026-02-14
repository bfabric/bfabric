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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianAxes;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;

@Named
public class BarChart extends Chart {

    private static final long serialVersionUID = 1;

    public BarChartModel get(List<Object[]> values, String title, String label, String legendPosition, String xAxisLabel, String yAxisLabel, List<String> colors) {
        return getBarChartModel(new BarChartModel(), values, title, label, legendPosition, xAxisLabel, yAxisLabel, colors);
    }

    public BarChartModel getBarChartModel(BarChartModel chartModel, List<Object[]> values, String title, String label, String legendPosition, String xAxisLabel, String yAxisLabel, List<String> colors) {
        if (chartModel != null) {
            if (values != null) {
                List<String> labels = new ArrayList<>();
                List<Number> numbers = new ArrayList<>();
                for (Object[] objectArray : values) {
                    labels.add(String.valueOf(objectArray[0]));
                    numbers.add(Double.valueOf(String.valueOf(objectArray[1])).intValue());
                }
                BarChartDataSet chartDataSet = new BarChartDataSet();
                chartDataSet.setData(numbers);
                chartDataSet.setLabel(label);
                ChartData data = new ChartData();
                data.addChartDataSet(chartDataSet);
                data.setLabels(labels);
                chartModel.setData(data);

                chartDataSet.setBackgroundColor(getBackgroundColors(colors));
                chartDataSet.setBorderColor(getBorderColors(colors));
                chartDataSet.setBorderWidth(1);
            }

            BarChartOptions options = new BarChartOptions();
            CartesianScales scales = new CartesianScales();
            CartesianLinearTicks ticks = new CartesianLinearTicks();

            // x-axis
            CartesianLinearAxes xAxis = new CartesianLinearAxes();
            xAxis.setTicks(ticks);
            xAxis.setBeginAtZero(true);
            xAxis.setStacked(true);
            xAxis.setOffset(true);
            setAxisLabel(xAxis, xAxisLabel);
            scales.addXAxesData(xAxis);

            // y-axis
            CartesianLinearAxes yAxis = new CartesianLinearAxes();
            yAxis.setTicks(ticks);
            yAxis.setBeginAtZero(true);
            yAxis.setOffset(true);
            setAxisLabel(yAxis, yAxisLabel);
            scales.addYAxesData(yAxis);

            options.setScales(scales);

            setLegend(options, legendPosition);
            setTitle(options, title);

            chartModel.setOptions(options);
        }
        return chartModel;
    }

    public BarChartModel getHorizontal(List<Object[]> values, String title, String label, String legendPosition, String yAxisLabel, String xAxisLabel, List<String> colors) {
        HorizontalBarChartModel horizontalBarChartModel = (HorizontalBarChartModel) getBarChartModel(new HorizontalBarChartModel(), values, title, label, legendPosition, xAxisLabel, yAxisLabel, colors);
        List<CartesianAxes> xAxes = horizontalBarChartModel.getOptions().getScales().getXAxes();
        if (xAxes != null && !xAxes.isEmpty()) {
            xAxes.get(0).setOffset(false);
        }
        return horizontalBarChartModel;
    }

    public BarChartModel getHorizontal(List<Object[]> values, String title, String label, String legendPosition, String yAxisLabel, String xAxisLabel) {
        return getHorizontal(values, title, label, legendPosition, xAxisLabel, yAxisLabel, null);
    }

    public BarChartModel getHorizontalStacked(List<List<Object[]>> series, List<String> labels, String title, String xAxisLabel, String yAxisLabel, List<String> colors) {
        HorizontalBarChartModel chartModel = new HorizontalBarChartModel();
        if (series != null && labels != null && series.size() == labels.size()) {
            Set<Long> labelsSet = new HashSet<>();
            ChartData data = new ChartData();
            List<String> backgroundColors = getBackgroundColors(colors);
            int colorIndex = 0;
            for (int i = 0; i < series.size(); i++) {
                BarChartDataSet chartDataSet = new BarChartDataSet();
                chartDataSet.setLabel(labels.get(i));
                List<Number> numbers = new ArrayList<>();
                for (Object[] objectArray : series.get(i)) {
                    labelsSet.add(Long.valueOf(String.valueOf(objectArray[0])));
                    numbers.add(Double.valueOf(String.valueOf(objectArray[1])).intValue());
                }
                chartDataSet.setData(numbers);
                if (colorIndex == backgroundColors.size()) {
                    colorIndex = 0;
                }
                chartDataSet.setBackgroundColor(backgroundColors.get(colorIndex));
                chartDataSet.setBorderColor(getDefaultColors().get(colorIndex));
                chartDataSet.setBorderWidth(1);
                data.addChartDataSet(chartDataSet);
                colorIndex++;
            }

            List<Long> labelsList = new ArrayList<>(labelsSet);
            labelsList.sort(Long::compare);
            List<String> labelsStringList = labelsList.stream().map(Object::toString).collect(Collectors.toList());
            data.setLabels(labelsStringList);
            chartModel.setData(data);
        }

        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setStacked(true);
        linearAxes.setOffset(true);
        cScales.addXAxesData(linearAxes);
        cScales.addYAxesData(linearAxes);
        options.setScales(cScales);

        setTitle(options, title);

        Tooltip tooltip = new Tooltip();
        tooltip.setMode("index");
        tooltip.setIntersect(true);
        options.setTooltip(tooltip);

        chartModel.setOptions(options);
        return chartModel;
    }
}
