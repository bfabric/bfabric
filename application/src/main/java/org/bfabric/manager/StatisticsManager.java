/*
 *
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


package org.bfabric.manager;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Institute;
import org.bfabric.entity.Project;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.StatisticsList;
import org.bfabric.service.StatisticsService;
import org.bfabric.service.TechnologyService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class StatisticsManager extends AbstractManager {

    private static final long serialVersionUID = 1;

    private final String[] timeframes = { "day", "week", "month", "year" };

    @Param
    private Integer count;

    private String entityClassName;

    private String selectedTechnology;

    private Integer selectedYear;

    @Inject
    private StatisticsList statisticsList;

    @Inject
    private StatisticsService statisticsService;

    @Inject
    private TechnologyService technologyService;

    private String timeframe;

    private Integer timeframeMultiplicator;

    @Param
    private String unit;

    public void entityClassNameChanged(ValueChangeEvent event) {
        entityClassName = (String) event.getNewValue();
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public List<Object> getLastApprovedProjects() {
        return statisticsList.getLastApprovedProjects(getTimeframe(), getTimeframeMultiplicator());
    }

    @CachedMethodResult
    public List<Object> getLastCreatedInstitutes() {
        return statisticsList.getTimeframeElementsByClass(Institute.class, "created", "id, name, to_char(created, 'YYYY-MM-DD HH24:MI') as created", getTimeframe(), getTimeframeMultiplicator());
    }

    @CachedMethodResult
    public List<Object> getLastCreatedProjects() {
        return statisticsList.getTimeframeElementsByClass(Project.class, "created", "id, name, to_char(created, 'YYYY-MM-DD HH24:MI') as created", getTimeframe(), getTimeframeMultiplicator());
    }

    @CachedMethodResult
    public List<Object> getLastCreatedUsers() {
        return statisticsList
            .getTimeframeElementsByClass(User.class, "created", "id, login, to_char(created, 'YYYY-MM-DD HH24:MI') as created, title, firstname, lastname", getTimeframe(), getTimeframeMultiplicator());
    }

    public List<Object> getNumberOfOrdersPerYear() {
        return statisticsService.getNumberOfOrdersPerYearByTechnology(selectedTechnology);
    }

    public List<Object> getNumberOfProjectPerYear() {
        return statisticsService.getNumberOfProjectsPerYearByTechnology(selectedTechnology);
    }

    public List<Object> getOrderAcceptedToArrived() {
        return getOrderData("acceptedToArrived", selectedTechnology);
    }

    public List<Object> getOrderAnalyzedToFinished() {
        return getOrderData("analyzedToFinished", selectedTechnology);
    }

    public List<Object> getOrderAnalyzingToAnalyzed() {
        return getOrderData("analyzingToAnalyzed", selectedTechnology);
    }

    public List<Object> getOrderArrivedToProcessed() {
        return getOrderData("arrivedToProcessed", selectedTechnology);
    }

    public List<Object> getOrderArrivedToProcessing() {
        return getOrderData("arrivedToProcessing", selectedTechnology);
    }

    @CachedMethodResult
    public List<Object> getOrderData(String orderPhase, String technology) {
        return statisticsService.getOrderData(orderPhase, technology);
    }

    @CachedMethodResult
    public List<Object> getOrderDurationPieChart(String technology) {
        return statisticsService.getOrderDurationPieChartByTechnology(technology);
    }

    public List<Object> getOrderDurationPieChart() {
        return getOrderDurationPieChart(selectedTechnology);
    }

    public List<Object> getOrderFinishedToClosed() {
        return getOrderData("finishedToClosed", selectedTechnology);
    }

    public List<Object> getOrderProcessedToAnalyzed() {
        return getOrderData("processedToAnalyzed", selectedTechnology);
    }

    public List<Object> getOrderProcessedToAnalyzing() {
        return getOrderData("processedToAnalyzing", selectedTechnology);
    }

    public List<Object> getOrderProcessingToProcessed() {
        return getOrderData("processingToProcessed", selectedTechnology);
    }

    public List<Object> getOrderSubmittedToAccepted() {
        return getOrderData("submittedToAccepted", selectedTechnology);
    }

    public List<Object> getOrderSubmittedToAnalyzed() {
        return getOrderData("submittedToAnalyzed", selectedTechnology);
    }

    public List<Object> getOrderSubmittedToAnalyzing() {
        return getOrderData("submittedToAnalyzing", selectedTechnology);
    }

    public List<Object> getOrderSubmittedToArrived() {
        return getOrderData("submittedToArrived", selectedTechnology);
    }

    public List<Object> getOrderSubmittedToClosed() {
        return getOrderData("submittedToClosed", selectedTechnology);
    }

    public List<Object> getOrderSubmittedToFinished() {
        return getOrderData("submittedToFinished", selectedTechnology);
    }

    public List<Object> getOrderSubmittedToProcessed() {
        return getOrderData("submittedToProcessed", selectedTechnology);
    }

    public List<Object> getOrderSubmittedToProcessing() {
        return getOrderData("submittedToProcessing", selectedTechnology);
    }

    public List<Object> getProjectDurationPieChart() {
        return statisticsService.getProjectDurationPieChartByTechnology(selectedTechnology);
    }

    public List<Object> getProjectFinishedToClose() {
        return statisticsService.getProjectData("finishedtoclosed", selectedTechnology);
    }

    public List<Object> getProjectPendingToClose() {
        return statisticsService.getProjectData("pendingtoclosed", selectedTechnology);
    }

    public List<Object> getProjectPendingToReview() {
        return statisticsService.getProjectData("pendingtoreview", selectedTechnology);
    }

    public List<Object> getProjectReviewToRunning() {
        return statisticsService.getProjectData("reviewtorunning", selectedTechnology);
    }

    public List<Object> getProjectRunningToFinished() {
        return statisticsService.getProjectData("runningtofinished", selectedTechnology);
    }

    public String getSelectedTechnology() {
        return this.selectedTechnology;
    }

    public Integer getSelectedYear() {
        return selectedYear;
    }

    @CachedMethodResult
    public List<Technology> getTechnologiesHavingOrders() {
        return technologyService.getTechnologiesHavingOrders();
    }

    public String getTimeframe() {
        return timeframe;
    }

    public List<Object> getTimeframeEntities() {
        return statisticsList.getTimeframeEntities(getTimeframe(), getTimeframeMultiplicator());
    }

    public Integer getTimeframeMultiplicator() {
        return timeframeMultiplicator;
    }

    public String[] getTimeframes() {
        return timeframes != null ? timeframes.clone() : null;
    }

    @Override
    @PostConstruct
    public void init() {
        initTimeframe();
    }

    public void initTimeframe() {
        if (timeframe == null) {
            timeframe = unit != null && Arrays.asList(timeframes).contains(unit) ? unit : "day";
        }
        if (timeframeMultiplicator == null) {
            timeframeMultiplicator = count != null ? count : Integer.valueOf(1);
        }
    }

    public void selectedTechnologyChanged(ValueChangeEvent event) {
        selectedTechnology = (String) event.getNewValue();
    }

    public void selectedYearChanged(ValueChangeEvent event) {
        selectedYear = (Integer) event.getNewValue();
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public void setSelectedTechnology(String selectedTechnology) {
        this.selectedTechnology = selectedTechnology;
    }

    public void setSelectedYear(Integer selectedYear) {
        this.selectedYear = selectedYear;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public void setTimeframeMultiplicator(Integer timeframeMultiplicator) {
        this.timeframeMultiplicator = timeframeMultiplicator;
    }
}

