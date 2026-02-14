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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
@XmlRootElement
public class WidgetType extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @XmlElement
    private boolean columns;

    @XmlElement
    private boolean decimalPlaces;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "widgetType")
    @OrderBy("id desc")
    private Set<FeedbackQuestionType> feedbackQuestionTypes = new HashSet<>();

    @XmlElement
    private boolean maxValue = false;

    @XmlElement
    private boolean minValue = false;

    @XmlElement
    private boolean rows = false;

    @XmlElement
    private boolean step = false;

    @XmlElement
    private boolean symbol = false;

    @Override
    public WidgetType clone() throws CloneNotSupportedException {
        WidgetType clone = (WidgetType) super.clone();
        clone.feedbackQuestionTypes = new HashSet<>();
        return clone;
    }

    public Set<FeedbackQuestionType> getFeedbackQuestionTypes() {
        return feedbackQuestionTypes;
    }

    public boolean isColumns() {
        return columns;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isDecimalPlaces() {
        return decimalPlaces;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    public boolean isMaxValue() {
        return maxValue;
    }

    public boolean isMinValue() {
        return minValue;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.FEEDBACKREADER);
    }

    public boolean isRows() {
        return rows;
    }

    public boolean isStep() {
        return step;
    }

    public boolean isSymbol() {
        return symbol;
    }

    @Override
    public boolean isUpdatable() {
        return getFeedbackQuestionTypes().isEmpty();
    }

    public void setColumns(boolean columns) {
        this.columns = columns;
    }

    public void setDecimalPlaces(boolean decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public void setFeedbackQuestionTypes(Set<FeedbackQuestionType> feedbackQuestionTypes) {
        this.feedbackQuestionTypes = feedbackQuestionTypes;
    }

    public void setMaxValue(boolean maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(boolean minValue) {
        this.minValue = minValue;
    }

    public void setRows(boolean row) {
        this.rows = row;
    }

    public void setStep(boolean step) {
        this.step = step;
    }

    public void setSymbol(boolean symbol) {
        this.symbol = symbol;
    }
}
