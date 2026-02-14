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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@XmlRootElement
public class FeedbackQuestionType extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Min(0)
    @XmlElement
    private Integer columns;

    @Min(0)
    @XmlElement
    private Integer decimalPlaces;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "feedbackQuestionType")
    @OrderBy("id desc")
    private Set<FeedbackQuestion> feedbackQuestions = new HashSet<>();

    @XmlElement
    private BigDecimal maxValue;

    @XmlElement
    private BigDecimal minValue;

    @Column(columnDefinition = "boolean DEFAULT false")
    @XmlElement
    private boolean requiresCustomOptions = false;

    @Min(1)
    @XmlElement
    private Integer rows;

    @Min(0)
    @XmlElement
    private BigDecimal step;

    @XmlElement
    private String symbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widgetTypeId")
    @XmlIDREF
    private WidgetType widgetType;

    @Override
    public FeedbackQuestionType clone() throws CloneNotSupportedException {
        FeedbackQuestionType clone = (FeedbackQuestionType) super.clone();
        clone.feedbackQuestions = new HashSet<>();
        return clone;
    }

    public Integer getColumns() {
        return columns;
    }

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public String getDecimalPlacesString() {
        return getDecimalPlaces() != null ? getDecimalPlaces().toString() : "0";
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (!getFeedbackQuestions().isEmpty()) {
            addEntityInfoItem(summary, "feedbackQuestions", getFeedbackQuestions().size());
        }
        return summary.toString();
    }

    public Set<FeedbackQuestion> getFeedbackQuestions() {
        return feedbackQuestions;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public Integer getMaxValueInteger() {
        return getMaxValue() != null ? getMaxValue().intValue() : null;
    }

    public List<Integer> getMaxValueIntegerList() {
        List<Integer> maxValueIntegerList = new ArrayList<>();
        if (getMaxValue() != null) {
            for (int i = 1; i <= getMaxValue().intValue(); i++) {
                maxValueIntegerList.add(i);
            }
        }
        return maxValueIntegerList;
    }

    public String getMaxValueString() {
        return getMaxValue() != null ? getMaxValue().toString() : "10000000000000";
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public Integer getMinValueInteger() {
        return getMinValue() != null ? getMinValue().intValue() : null;
    }

    public String getMinValueString() {
        return getMinValue() != null ? getMinValue().toString() : "-10000000000000";
    }

    public Integer getRows() {
        return rows;
    }

    public BigDecimal getStep() {
        return step;
    }

    public String getSymbol() {
        return symbol;
    }

    public WidgetType getWidgetType() {
        return widgetType;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER);
    }

    public boolean isRequiresCustomOptions() {
        return requiresCustomOptions;
    }

    @Override
    public boolean isUpdatable() {
        return getFeedbackQuestions().isEmpty();
    }

    public void resetWidgetTypeSpecificAttributes() {
        setMinValue(null);
        setMaxValue(null);
        setDecimalPlaces(null);
        setStep(null);
        setColumns(null);
        setRows(null);
        setSymbol(null);
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    public void setDecimalPlaces(Integer decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public void setFeedbackQuestions(Set<FeedbackQuestion> feedbackQuestions) {
        this.feedbackQuestions = feedbackQuestions;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public void setRequiresCustomOptions(boolean requiresCustomOptions) {
        this.requiresCustomOptions = requiresCustomOptions;
    }

    public void setRows(Integer row) {
        this.rows = row;
    }

    public void setStep(BigDecimal step) {
        this.step = step;
    }

    public void setSymbol(String symbol) {
        this.symbol = StringHelper.format(symbol);
    }

    public void setWidgetType(WidgetType widgetType) {
        this.widgetType = widgetType;
    }

    public void widgetTypeChanged(ValueChangeEvent event) {
        WidgetType oldWidgetType = getWidgetType();
        setWidgetType((WidgetType) event.getNewValue());
        if (!getWidgetType().equals(oldWidgetType)) {
            resetWidgetTypeSpecificAttributes();
        }
    }
}
