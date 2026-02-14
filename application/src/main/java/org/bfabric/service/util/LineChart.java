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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.inject.Named;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScaleTitle;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.data.NumericPoint;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;

@Named
public class LineChart extends Chart {

    private static final long serialVersionUID = 1;

    private CartesianLinearAxes createYAxis(String id, String position, Long min, Long max, String label, List<String> seriesColors, int colorIndex, boolean beginAtZero, boolean offset) {
        CartesianLinearAxes yAxis = new CartesianLinearAxes();
        yAxis.setId(id);
        yAxis.setPosition(position);
        yAxis.setBeginAtZero(beginAtZero);
        yAxis.setOffset(offset);
        yAxis.setMin(min);
        yAxis.setMax(max);
        yAxis.setScaleTitle(createYAxisTitle(label, seriesColors, colorIndex));
        return yAxis;
    }

    private CartesianScaleTitle createYAxisTitle(String label, List<String> seriesColors, int colorIndex) {
        CartesianScaleTitle yAxisTitle = new CartesianScaleTitle();
        yAxisTitle.setText(label);
        yAxisTitle.setDisplay(true);
        if (seriesColors != null && seriesColors.size() > colorIndex) {
            yAxisTitle.setFontColor("#" + seriesColors.get(colorIndex));
        }
        yAxisTitle.setFontStyle("bold");
        return yAxisTitle;
    }

    public LineChartModel get(List<List<Object[]>> series, String chartTitle, String xAxisLabel, String yAxisLabel, List<String> seriesColors, List<String> labels, boolean showLegend) {
        return getLineChartModel(new LineChartModel(), series, chartTitle, xAxisLabel, yAxisLabel, seriesColors, labels, showLegend);
    }

    // Overloaded to provide backward compatibility
    public LineChartModel get(List<List<Object[]>> series, String chartTitle, String xAxisLabel, String yAxisLabel, List<String> seriesColors, List<String> labels) {
        return get(series, chartTitle, xAxisLabel, yAxisLabel, seriesColors, labels, true);
    }

    public LineChartModel getLineChartModel(LineChartModel chartModel, List<List<Object[]>> series, String title, String xAxisLabel, String yAxisLabel, List<String> seriesColors, List<String> labels, boolean showLegend) {
        if (chartModel != null) {
            ChartData data = new ChartData();
            Long xAxisMin;
            Long xAxisMax;
            Long yAxisMin = null;
            Long yAxisMax = null;
            Long yAxis2Min = null;
            Long yAxis2Max = null;
            Long[] minMax = new Long[4];
            boolean dualAxis = series != null && series.size() == 2;
            if (series != null && labels != null && series.size() == labels.size()) {
                List<List<Object>> values = new ArrayList<>();
                List<Set<Number>> xAxisTicksPerSeries = new ArrayList<>();
                // Use border colors instead of background colors. If needed, override the background color method.
                List<String> colors = getBorderColors(seriesColors);
                int colorIndex = 0;
                for (int i = 0; i < series.size(); i++) {
                    LineChartDataSet dataSet = new LineChartDataSet();
                    List<Object> points = getValuesAndUpdateMinMax(series, i, minMax);
                    values.add(i, points);
                    xAxisTicksPerSeries.add(new HashSet<>());
                    for (Object point : points) {
                        xAxisTicksPerSeries.get(i).add(((NumericPoint) point).getX());
                    }
                    dataSet.setFill(false);
                    dataSet.setLabel(labels.get(i));
                    if (colorIndex == colors.size()) {
                        colorIndex = 0;
                    }
                    dataSet.setBackgroundColor(colors.get(colorIndex));
                    dataSet.setBorderColor(colors.get(colorIndex));
                    dataSet.setTension(0.2);
                    data.addChartDataSet(dataSet);
                    colorIndex++;
                }

                xAxisMin = minMax[0];
                xAxisMax = minMax[1];
                // For dual axis, calculate min/max per axis
                if (dualAxis) {
                    Long[] minMax1 = new Long[4];
                    Long[] minMax2 = new Long[4];
                    getValuesAndUpdateMinMax(series, 0, minMax1);
                    getValuesAndUpdateMinMax(series, 1, minMax2);
                    yAxisMin = minMax1[2];
                    yAxisMax = minMax1[3];
                    yAxis2Min = minMax2[2];
                    yAxis2Max = minMax2[3];
                } else {
                    yAxisMin = minMax[2];
                    yAxisMax = minMax[3];
                }

                if (xAxisMin != null && xAxisMax != null && yAxisMin != null && yAxisMax != null) {
                    List<Long> xAxisTicks = Arrays.stream(LongStream.rangeClosed(xAxisMin, xAxisMax).toArray()).boxed().collect(Collectors.toList());
                    for (int j = 0; j < xAxisTicks.size(); j++) {
                        for (int i = 0; i < series.size(); i++) {
                            if (!xAxisTicksPerSeries.get(i).contains(xAxisTicks.get(j))) {
                                values.get(i).add(j, new NumericPoint(null, null));
                            }
                        }
                    }
                    for (int i = 0; i < series.size(); i++) {
                        LineChartDataSet dataSet = (LineChartDataSet) data.getDataSet().get(i);
                        dataSet.setData(values.get(i));
                    }

                    setLabels(data, xAxisMin, xAxisMax);
                }
            }

            // Options
            LineChartOptions options = new LineChartOptions();
            CartesianScales scales = new CartesianScales();
            setTitle(options, title);
            if (!showLegend) {
                if (options.getLegend() == null) {
                    options.setLegend(new org.primefaces.model.charts.optionconfig.legend.Legend());
                }
                options.getLegend().setDisplay(false);
            }

            // x-axis
            setXAxis(xAxisLabel, scales);

            // y-axis
            String yLabel;
            if (dualAxis && labels != null && labels.size() > 1) {
                yLabel = labels.get(0);
            } else {
                yLabel = yAxisLabel;
            }
            CartesianLinearAxes yAxis = createYAxis("y", "left", yAxisMin, yAxisMax, yLabel, seriesColors, 0, true, true);
            scales.addYAxesData(yAxis);

            // Add a second y-axis if in dual axis mode
            if (dualAxis) {
                String y2Label = labels != null && labels.size() > 1 ? labels.get(1) : "";
                CartesianLinearAxes yAxis2 = createYAxis("y2", "right", yAxis2Min, yAxis2Max, y2Label, seriesColors, 1, true, true);
                scales.addYAxesData(yAxis2);
                // Assign the second dataset to y2
                if (data.getDataSet().size() > 1) {
                    ((LineChartDataSet) data.getDataSet().get(1)).setYaxisID("y2");
                }
            }

            options.setScales(scales);

            chartModel.setOptions(options);
            chartModel.setData(data);
        }
        return chartModel;
    }

    public LineChartModel getLineChartModelStacked(LineChartModel chartModel, List<List<Object[]>> series, String title, String xAxisLabel, String yAxisLabel, List<String> seriesColors, List<String> labels) {
        if (chartModel != null) {
            ChartData data = new ChartData();
            Long xAxisMin;
            Long xAxisMax;
            Long[] minMax = new Long[4];
            if (series != null && labels != null && series.size() == labels.size()) {
                List<String> colors = getBorderColors(seriesColors);
                int colorIndex = 0;
                for (int i = 0; i < series.size(); i++) {
                    LineChartDataSet dataSet = new LineChartDataSet();
                    dataSet.setData(getValuesAndUpdateMinMax(series, i, minMax));
                    dataSet.setLabel(labels.get(i));
                    if (colorIndex == colors.size()) {
                        colorIndex = 0;
                    }
                    dataSet.setBackgroundColor(colors.get(colorIndex));
                    dataSet.setBorderColor(colors.get(colorIndex));
                    dataSet.setBorderWidth(1);
                    dataSet.setFill(true);
                    data.addChartDataSet(dataSet);
                    colorIndex++;
                }

                xAxisMin = minMax[0];
                xAxisMax = minMax[1];
                setLabels(data, xAxisMin, xAxisMax);
                chartModel.setData(data);
            }

            // Options
            LineChartOptions options = new LineChartOptions();
            CartesianScales scales = new CartesianScales();
            setTitle(options, title);

            // x-axis
            setXAxis(xAxisLabel, scales);

            // y-axis
            CartesianLinearAxes yAxis = new CartesianLinearAxes();
            yAxis.setBeginAtZero(true);
            yAxis.setStacked(true);
            yAxis.setOffset(true);
            setAxisLabel(yAxis, yAxisLabel);
            scales.addYAxesData(yAxis);

            options.setScales(scales);

            Tooltip tooltip = new Tooltip();
            tooltip.setMode("index");
            tooltip.setIntersect(true);
            options.setTooltip(tooltip);

            chartModel.setOptions(options);
        }
        return chartModel;
    }

    public LineChartModel getLineChartStacked(List<List<Object[]>> series, String title, String xAxisLabel, String yAxisLabel, List<String> seriesColors, List<String> labels) {
        return getLineChartModelStacked(new LineChartModel(), series, title, xAxisLabel, yAxisLabel, seriesColors, labels);
    }

    public List<Object> getValuesAndUpdateMinMax(List<List<Object[]>> series, int index, Long[] minMax) {
        List<Object> values = new ArrayList<>();
        for (Object[] objectArray : series.get(index)) {
            long x = Double.valueOf(String.valueOf(objectArray[0])).longValue();
            long y = Double.valueOf(String.valueOf(objectArray[1])).longValue();
            if (minMax[0] == null || minMax[0] > x) {
                minMax[0] = x;
            }
            if (minMax[1] == null || minMax[1] < x) {
                minMax[1] = x;
            }
            if (minMax[2] == null || minMax[2] > y) {
                minMax[2] = y;
            }
            if (minMax[3] == null || minMax[3] < y) {
                minMax[3] = y;
            }
            values.add(new NumericPoint(x, y));
        }
        return values;
    }

    public void setLabels(ChartData data, Long xAxisMin, Long xAxisMax) {
        if (data != null && xAxisMin != null && xAxisMax != null) {
            List<String> xAxisLabels = Arrays.stream(LongStream.rangeClosed(xAxisMin, xAxisMax).toArray()).boxed().collect(Collectors.toList()).stream().map(Object::toString)
                .collect(Collectors.toList());
            data.setLabels(xAxisLabels);
        }
    }

    public CartesianLinearAxes setXAxis(String xAxisLabel, CartesianScales scales) {
        CartesianLinearAxes xAxis = new CartesianLinearAxes();
        setAxisLabel(xAxis, xAxisLabel);
        scales.addXAxesData(xAxis);
        return xAxis;
    }
}
