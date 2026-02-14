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

package org.bfabric.webservice.request.parameter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.bfabric.Constants;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.forms.MFHelper;
import org.bfabric.util.StringHelper;

public class WhereClauseItem {

    public static final String alias = "entity.";

    private boolean attributeAlias = true;

    private String attributeName;

    private boolean caseInsensitive = false;

    private String joinClause;

    private boolean localDate = false;

    private String parameterName;

    private Boolean parameterValueBoolean;

    private LocalDate parameterValueDate;

    private LocalDate parameterValueDateAfter;

    private LocalDate parameterValueDateBefore;

    private LocalDateTime parameterValueDateTime;

    private LocalDateTime parameterValueDateTimeAfter;

    private LocalDateTime parameterValueDateTimeBefore;

    private Enum<?> parameterValueEnum;

    private Integer parameterValueInteger;

    private Long parameterValueLong;

    private Number parameterValueNumber;

    private String parameterValueString;

    public WhereClauseItem() {
    }

    public WhereClauseItem(String attributeName, String parameterNameSuffix) {
        setAttributeName(attributeName);
        if (attributeName != null) {
            setParameterName(attributeName.replace(".", Constants.EMPTY_STRING) + parameterNameSuffix);
        }
    }

    public WhereClauseItem(String attributeName, int index) {
        this(attributeName, String.valueOf(index));
    }

    public String getAttribute() {
        return isCaseInsensitive() ? "LOWER(" + getAttributeName() + ") " : getAttributeName() + " ";
    }

    public String getAttributeName() {
        return (isAttributeAlias() ? alias : Constants.EMPTY_STRING) + attributeName;
    }

    public String getJoinClause() {
        return joinClause;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Object getParameterValue() {
        if (parameterValueInteger != null) {
            return parameterValueInteger;
        }
        if (parameterValueLong != null) {
            return parameterValueLong;
        }
        if (parameterValueNumber != null) {
            return parameterValueNumber;
        }
        if (parameterValueString != null) {
            return parameterValueString;
        }
        if (parameterValueBoolean != null) {
            return parameterValueBoolean;
        }
        if (parameterValueDateTimeBefore != null) {
            return parameterValueDateTimeBefore;
        }
        if (parameterValueDateTimeAfter != null) {
            return parameterValueDateTimeAfter;
        }
        if (parameterValueDateBefore != null) {
            return parameterValueDateBefore;
        }
        if (parameterValueDateAfter != null) {
            return parameterValueDateAfter;
        }
        if (parameterValueEnum != null) {
            return parameterValueEnum;
        }
        return isLocalDate() ? parameterValueDate : parameterValueDateTime;
    }

    public Boolean getParameterValueBoolean() {
        return parameterValueBoolean;
    }

    public LocalDate getParameterValueDate() {
        return parameterValueDate;
    }

    public LocalDate getParameterValueDateAfter() {
        return parameterValueDateAfter;
    }

    public LocalDate getParameterValueDateBefore() {
        return parameterValueDateBefore;
    }

    public LocalDateTime getParameterValueDateTime() {
        return parameterValueDateTime;
    }

    public LocalDateTime getParameterValueDateTimeAfter() {
        return parameterValueDateTimeAfter;
    }

    public LocalDateTime getParameterValueDateTimeBefore() {
        return parameterValueDateTimeBefore;
    }

    public Enum<?> getParameterValueEnum() {
        return parameterValueEnum;
    }

    public Integer getParameterValueInteger() {
        return parameterValueInteger;
    }

    public Long getParameterValueLong() {
        return parameterValueLong;
    }

    public Number getParameterValueNumber() {
        return parameterValueNumber;
    }

    public String getParameterValueString() {
        return parameterValueString;
    }

    public String getParameterizedAttribute() {
        return ":" + getParameterName() + " ";
    }

    public String getPredicate() {
        StringBuilder predicate = new StringBuilder();
        predicate.append(getAttribute());
        if (getParameterValueString() != null) {
            predicate.append(Constants.OPERATOR_LIKE).append(getParameterizedAttribute());
        } else if (getParameterValueDateTimeBefore() != null || getParameterValueDateBefore() != null) {
            predicate.append(Constants.OPERATOR_LTE).append(getParameterizedAttribute());
        } else if (getParameterValueDateTimeAfter() != null || getParameterValueDateAfter() != null) {
            predicate.append(Constants.OPERATOR_GTE).append(getParameterizedAttribute());
        } else {
            predicate.append(Constants.OPERATOR_EQ).append(getParameterizedAttribute());
            if (getParameterValue() == null) {
                predicate.append(" OR ").append(getAttribute()).append(" IS NULL");
            }
        }
        return predicate.toString();
    }

    public boolean isAttributeAlias() {
        return attributeAlias;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public boolean isLocalDate() {
        return localDate;
    }

    public void setAttributeAlias(boolean attributeAlias) {
        this.attributeAlias = attributeAlias;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public void setJoinClause(String joinClause) {
        this.joinClause = joinClause;
        setAttributeAlias(false);
    }

    public void setLocalDate(boolean localDate) {
        this.localDate = localDate;
    }

    public void setLocalDate() {
        setLocalDate(true);
    }

    public void setParameterName(String key) {
        parameterName = key;
    }

    public void setParameterValueBoolean(String value) throws InvalidDataException {
        this.parameterValueBoolean = MFHelper.booleanValueOf(value);
    }

    public void setParameterValueBoolean(boolean value) {
        this.parameterValueBoolean = value;
    }

    public void setParameterValueDate(String parameterValueDate) throws InvalidDataException {
        setLocalDate();
        this.parameterValueDate = MFHelper.dateValueOf(parameterValueDate);
    }

    public void setParameterValueDateAfter(String parameterValueDateAfter) throws InvalidDataException {
        setLocalDate();
        this.parameterValueDateAfter = MFHelper.dateValueOf(parameterValueDateAfter);
    }

    public void setParameterValueDateBefore(String parameterValueDateBefore) throws InvalidDataException {
        setLocalDate();
        this.parameterValueDateBefore = MFHelper.dateValueOf(parameterValueDateBefore);
    }

    public void setParameterValueDateTime(String parameterValueDateTime) throws InvalidDataException {
        this.parameterValueDateTime = MFHelper.dateTimeValueOf(parameterValueDateTime);
    }

    public void setParameterValueDateTimeAfter(String parameterValueDateTimeAfter) throws InvalidDataException {
        this.parameterValueDateTimeAfter = MFHelper.dateTimeValueOf(parameterValueDateTimeAfter);
    }

    public void setParameterValueDateTimeBefore(String parameterValueDateTimeBefore) throws InvalidDataException {
        this.parameterValueDateTimeBefore = MFHelper.dateTimeValueOf(parameterValueDateTimeBefore);
    }

    public void setParameterValueEnum(Enum<?> value) {
        parameterValueEnum = value;
    }

    public void setParameterValueInteger(Integer value) {
        parameterValueInteger = value;
    }

    public void setParameterValueLong(Long value) {
        parameterValueLong = value;
    }

    public void setParameterValueNumber(Number parameterValueNumber) {
        this.parameterValueNumber = parameterValueNumber;
    }

    public void setParameterValueString(String value) {
        parameterValueString = StringHelper.trimBoth(value);
    }
}
