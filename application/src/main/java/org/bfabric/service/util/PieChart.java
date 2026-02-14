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

import java.util.List;

import javax.inject.Named;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;

@Named
public class PieChart extends Chart {

    private static final long serialVersionUID = 1;

    public PieChartModel get(List<Object[]> values) {
        return get(values, null, null, null);
    }

    public PieChartModel get(List<Object[]> values, String title, String legendPosition, List<String> backgroundColors) {
        PieChartModel chartModel = new PieChartModel();
        if (values != null) {
            PieChartDataSet chartDataSet = new PieChartDataSet();
            chartDataSet.setData(getNumbers(values));
            chartDataSet.setBackgroundColor(getBorderColors(backgroundColors));

            ChartData data = new ChartData();
            data.addChartDataSet(chartDataSet);
            data.setLabels(getLabels(values));
            chartModel.setData(data);

            PieChartOptions options = new PieChartOptions();
            setTitle(options, title);
            Tooltip tooltip = new Tooltip();
            tooltip.setMode("index");
            tooltip.setIntersect(true);
            options.setTooltip(tooltip);
            setLegend(options, legendPosition);
            chartModel.setOptions(options);
        }
        return chartModel;
    }
}