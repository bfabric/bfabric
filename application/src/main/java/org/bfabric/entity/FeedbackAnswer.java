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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import static org.bfabric.Constants.DATETIME_FORMATTER;
import static org.bfabric.Constants.DATE_FORMATTER;
import static org.bfabric.Constants.TIME_FORMATTER;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
public class FeedbackAnswer extends AbstractOrderedEntity {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedbackid")
    @NotNull
    @XmlIDREF
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionid")
    @NotNull
    private FeedbackQuestion question;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Transient
    private LocalDate valueDate;

    @Transient
    private LocalDateTime valueDateTime;

    @Transient
    private Integer valueInteger;

    @Transient
    private Double valueNumber;

    @Transient
    private LocalTime valueTime;

    public FeedbackAnswer() {
        super();
    }

    @XmlElement(name = "answer")
    public String getAnswer() {
        // Note: For entity logging purposes.
        return getQuestion() != null ? " " + getQuestion().getValue() + " : " + getValue() : null;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public FeedbackQuestion getQuestion() {
        return question;
    }

    public String getValue() {
        return value;
    }

    public LocalDate getValueDate() {
        if (value != null) {
            valueDate = LocalDate.parse(value, DATE_FORMATTER);
        }
        return valueDate;
    }

    public LocalDateTime getValueDateTime() {
        if (value != null) {
            valueDateTime = LocalDateTime.parse(value, DATETIME_FORMATTER);
        }
        return valueDateTime;
    }

    public Integer getValueInteger() {
        if (valueInteger == null && getValue() != null) {
            valueInteger = Integer.valueOf(getValue());
        }
        return valueInteger;
    }

    public List<String> getValueList() {
        List<String> result = new ArrayList<>();
        if (this.value != null) {
            result.addAll(Arrays.asList(this.value.split(", ")));
        }
        return result;
    }

    public Double getValueNumber() {
        if (valueNumber == null && getValue() != null) {
            valueNumber = Double.valueOf(getValue());
        }
        return valueNumber;
    }

    public LocalTime getValueTime() {
        if (value != null) {
            valueTime = LocalTime.parse(value, TIME_FORMATTER);
        }
        return valueTime;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public void setQuestion(FeedbackQuestion question) {
        this.question = question;
    }

    public void setValue(String value) {
        this.value = StringHelper.formatText(value);
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
        if (valueDate != null) {
            setValue(DATE_FORMATTER.format(valueDate));
        } else {
            setValue(null);
        }
    }

    public void setValueDateTime(LocalDateTime valueDateTime) {
        this.valueDateTime = valueDateTime;
        if (valueDateTime != null) {
            setValue(DATETIME_FORMATTER.format(valueDateTime));
        } else {
            setValue(null);
        }
    }

    public void setValueInteger(Integer valueInteger) {
        this.valueInteger = valueInteger;
        if (valueInteger != null) {
            setValue(String.valueOf(valueInteger));
        } else {
            setValue(null);
        }
    }

    public void setValueList(List<String> values) {
        this.value = CollectionHelper.print(values, "getValue");
    }

    public void setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
        if (valueNumber != null) {
            setValue(String.valueOf(valueNumber));
        } else {
            setValue(null);
        }
    }

    public void setValueTime(LocalTime valueTime) {
        this.valueTime = valueTime;
        if (valueTime != null) {
            setValue(TIME_FORMATTER.format(valueTime));
        } else {
            setValue(null);
        }
    }
}