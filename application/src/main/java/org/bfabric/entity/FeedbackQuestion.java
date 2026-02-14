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

package org.bfabric.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.util.BarChart;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;

@Entity
@XmlRootElement
@NamedQuery(name = "FeedbackQuestion.findMaxPosition", query = "SELECT MAX(a.orderPosition) FROM FeedbackTemplateQuestion a WHERE a.feedbackTemplate = :template")
public class FeedbackQuestion extends AbstractBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "question", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<FeedbackAnswer> answers = new HashSet<>();

    @OneToMany(mappedBy = "feedbackQuestion", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("orderPosition")
    // @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlElement(name = "customOption")
    private List<FeedbackQuestionCustomOption> customOptions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedbackQuestionTypeId")
    @XmlIDREF
    private FeedbackQuestionType feedbackQuestionType;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean required = true;

    @OneToMany(mappedBy = "feedbackQuestion", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("orderPosition")
    @XmlElement(name = "template")
    private List<FeedbackTemplateQuestion> templateQuestions = new ArrayList<>();

    @NotNull
    @Column(columnDefinition = "TEXT")
    @XmlElement
    private String value;

    public FeedbackQuestion() {
        super();
    }

    private static void addAnswersToChartResult(Map<String, Number> result, String answer) {
        if (result.get(answer) == null) {
            result.put(answer, 1);
        } else {
            result.put(answer, result.get(answer).intValue() + 1);
        }
    }

    private static String[] getMultipleOptions(String option) {
        return option != null ? option.trim().split(",") : null;
    }

    public static boolean isAnswersNumeric(List<FeedbackAnswer> feedbackAnswers) {
        if (feedbackAnswers != null) {
            boolean answersNumeric = false;
            for (FeedbackAnswer feedbackAnswer : feedbackAnswers) {
                try {
                    if (StringHelper.isNotEmpty(feedbackAnswer.getValue())) {
                        Double.parseDouble(feedbackAnswer.getValue());
                        answersNumeric = true;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return answersNumeric;
        }
        return false;
    }

    private static void setUpClonedCustomOptions(FeedbackQuestion feedbackQuestion) {
        List<FeedbackQuestionCustomOption> clonedTemplateQuestions = new ArrayList<>();
        for (FeedbackQuestionCustomOption customOption : feedbackQuestion.getCustomOptions()) {
            FeedbackQuestionCustomOption clone = new FeedbackQuestionCustomOption();
            clone.setFeedbackQuestion(feedbackQuestion);
            clone.setValue(customOption.getValue());
            clone.setOrderPosition(customOption.getOrderPosition());
            clonedTemplateQuestions.add(clone);
        }
        feedbackQuestion.setCustomOptions(clonedTemplateQuestions);
    }

    private static void setUpClonedTemplates(FeedbackQuestion feedbackQuestion) throws CloneNotSupportedException {
        List<FeedbackTemplateQuestion> clonedTemplateQuestions = new ArrayList<>();
        for (FeedbackTemplateQuestion feedbackTemplateQuestion : feedbackQuestion.getTemplateQuestions()) {
            FeedbackTemplateQuestion clone = feedbackTemplateQuestion.clone();
            clone.setFeedbackQuestion(feedbackQuestion);
            clonedTemplateQuestions.add(clone);
        }
        feedbackQuestion.setTemplateQuestions(clonedTemplateQuestions);
    }

    public void addCustomOption() {
        FeedbackQuestionCustomOption customOption = new FeedbackQuestionCustomOption();
        customOption.setFeedbackQuestion(this);
        customOption.setValue("");

        // Keep order (add first to collection) to calculate the order position!
        getCustomOptions().add(customOption);
        customOption.setOrderPosition(customOption.getNextOrderPosition(getCustomOptions()));
    }

    @SuppressWarnings("unused")
    public void addTemplate(FeedbackTemplate template) {
        new FeedbackTemplateQuestion(this, template);
    }

    @Override
    public FeedbackQuestion clone() throws CloneNotSupportedException {
        FeedbackQuestion clone = (FeedbackQuestion) super.clone();
        clone.setAnswers(new HashSet<>());
        setUpClonedTemplates(clone);
        setUpClonedCustomOptions(clone);
        return clone;
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        // Clean custom options if the type is not (anymore) a select variant.
        if (!getFeedbackQuestionType().isRequiresCustomOptions()) {
            getCustomOptions().clear();
        }
    }

    public Set<FeedbackAnswer> getAnswers() {
        return answers;
    }

    public Set<FeedbackAnswer> getAnswers(FeedbackTemplate feedbackTemplate) {
        Set<FeedbackAnswer> feedbackAnswers = new HashSet<>();
        for (FeedbackAnswer feedbackAnswer : getAnswers()) {
            if (feedbackAnswer.getFeedback().getTemplate().equals(feedbackTemplate)) {
                feedbackAnswers.add(feedbackAnswer);
            }
        }
        return feedbackAnswers;
    }

    public BarChartModel getBarChartModel() {
        return getBarChartModelByFeedbackTemplate(null);
    }

    public BarChartModel getBarChartModelByFeedbackTemplate(FeedbackTemplate feedbackTemplate) {
        HorizontalBarChartModel chartModel = new HorizontalBarChartModel();

        BarChartDataSet chartDataSet = new BarChartDataSet();
        chartDataSet.setBackgroundColor(BarChart.getDefaultBackgroundColors());
        chartDataSet.setBorderColor(BarChart.getDefaultColors());
        chartDataSet.setBorderWidth(1);
        Map<String, Number> chartValues = getChartData(feedbackTemplate);
        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();
        for (Map.Entry<String, Number> mapEntry : chartValues.entrySet()) {
            labels.add(mapEntry.getKey());
            values.add(mapEntry.getValue());
        }
        chartDataSet.setData(values);
        chartDataSet.setLabel(getValue());

        ChartData data = new ChartData();
        data.addChartDataSet(chartDataSet);
        data.setLabels(labels);
        chartModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        linearAxes.setTicks(ticks);
        linearAxes.setBeginAtZero(true);
        cScales.addXAxesData(linearAxes);
        options.setScales(cScales);
        chartModel.setOptions(options);
        return chartModel;
    }

    public Map<String, Number> getChartData() {
        return getChartData(null);
    }

    public Map<String, Number> getChartData(FeedbackTemplate feedbackTemplate) {
        Map<String, Number> result = new LinkedHashMap<>();
        Set<FeedbackAnswer> chartAnswers;
        if (feedbackTemplate != null) {
            chartAnswers = getAnswers(feedbackTemplate);
        } else {
            chartAnswers = getAnswers();
        }

        // If the answers have only numeric values, then sort the list accordingly. Otherwise, the result will be ordered alpha-numerically.
        List<FeedbackAnswer> chartAnswersList = new ArrayList<>(chartAnswers);
        if (isAnswersNumeric(chartAnswersList)) {
            chartAnswersList.sort(getValueOrderComparator());
            Collections.reverse(chartAnswersList);
        }

        for (FeedbackAnswer answer : chartAnswersList) {
            if (answer.getValue() != null && !answer.getValue().isEmpty()) {
                if (getFeedbackQuestionType().getName().equals("Select Many")) {
                    String[] options = getMultipleOptions(answer.getValue());
                    for (String option : options) {
                        addAnswersToChartResult(result, option);
                    }
                } else {
                    addAnswersToChartResult(result, answer.getValue());
                }
            }
        }
        return result;
    }

    @Override
    public FeedbackQuestion getClone() {
        return (FeedbackQuestion) super.getClone();
    }

    public List<FeedbackQuestionCustomOption> getCustomOptions() {
        return customOptions;
    }

    public List<String> getCustomOptionsList() {
        List<String> result = new ArrayList<>();
        for (FeedbackQuestionCustomOption option : getCustomOptions()) {
            result.add(option.getValue());
        }
        return result;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.FEEDBACKMANAGER;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getValue();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getValue())) {
            addEntityInfoItem(summary, "question", getValue());
        }
        if (getFeedbackQuestionType() != null) {
            addEntityInfoItem(summary, "feedbackQuestionType", getFeedbackQuestionType());
        }
        addEntityInfoItem(summary, "required", isRequired());
        return summary.toString();
    }

    public FeedbackQuestionType getFeedbackQuestionType() {
        return feedbackQuestionType;
    }

    public FeedbackTemplateQuestion getTemplateQuestion(FeedbackTemplate template) {
        for (FeedbackTemplateQuestion feedbackTemplateQuestion : getTemplateQuestions()) {
            if (feedbackTemplateQuestion.getFeedbackTemplate().equals(template)) {
                return feedbackTemplateQuestion;
            }
        }
        return null;
    }

    public List<FeedbackTemplateQuestion> getTemplateQuestions() {
        return templateQuestions;
    }

    public Set<FeedbackTemplate> getTemplates() {
        Set<FeedbackTemplate> feedbackTemplates = new HashSet<>();
        for (FeedbackTemplateQuestion feedbackTemplateQuestion : getTemplateQuestions()) {
            feedbackTemplates.add(feedbackTemplateQuestion.getFeedbackTemplate());
        }
        return feedbackTemplates;
    }

    public String getValue() {
        return value;
    }

    public Comparator<FeedbackAnswer> getValueOrderComparator() {
        return (entity1, entity2) -> {
            if (entity1 == null && entity2 == null ||
                entity1 == null && entity2.getValue() == null ||
                entity2 == null && entity1.getValue() == null ||
                entity1 != null && entity2 != null && entity1.getValue() == null && entity2.getValue() == null ||
                entity1 != null && entity2 != null && entity1.getValue() != null && entity1.getValue().equals(entity2.getValue()) ||
                entity1 != null && entity2 != null && entity2.getValue() != null && entity2.getValue().equals(entity1.getValue())) {
                return 0;
            }
            if (entity1 == null || entity1.getValue() == null) {
                return -1;
            }
            if (entity2 == null || entity2.getValue() == null) {
                return 1;
            }
            try {
                return Long.compare(Long.parseLong(entity1.getValue()), Long.parseLong(entity2.getValue()));
            } catch (NumberFormatException e) {
                return entity1.getValue().compareTo(entity2.getValue());
            }
        };
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN);
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER);
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable() && getAnswers().isEmpty();
    }

    public void removeCustomOption(FeedbackQuestionCustomOption option) {
        getCustomOptions().remove(option);
        if (getCustomOptions().isEmpty()) {
            addCustomOption();
        }
    }

    public void removeTemplate(FeedbackTemplate template) {
        FeedbackTemplateQuestion templateQuestion = getTemplateQuestion(template);
        if (template != null && templateQuestion != null) {
            getTemplateQuestions().remove(templateQuestion);
            template.getTemplateQuestions().remove(templateQuestion);
        }
    }

    public void setAnswers(Set<FeedbackAnswer> answers) {
        this.answers = answers;
    }

    public void setCustomOptions(List<FeedbackQuestionCustomOption> customOptions) {
        this.customOptions = customOptions;
    }

    public void setFeedbackQuestionType(FeedbackQuestionType feedbackQuestionType) {
        this.feedbackQuestionType = feedbackQuestionType;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setTemplateQuestions(List<FeedbackTemplateQuestion> templates) {
        templateQuestions = templates;
    }

    public void setValue(String value) {
        this.value = StringHelper.formatText(value);
    }
}