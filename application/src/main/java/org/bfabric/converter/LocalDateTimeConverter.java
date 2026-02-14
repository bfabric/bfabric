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

package org.bfabric.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.bfabric.Constants;
import org.bfabric.util.ConfigurationHelper;

@FacesConverter(forClass = LocalDateTime.class)
public class LocalDateTimeConverter implements Converter<Object> {

    private static DateTimeFormatter getFormatter(UIComponent component) {
        return DateTimeFormatter.ofPattern(getPattern(component));
    }

    private static String getPattern(UIComponent component) {
        String pattern = (String) component.getAttributes().get("pattern");
        if (pattern == null) {
            pattern = ConfigurationHelper.getConfiguration().getDefaultDateTimePattern();
        }
        return pattern;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String submittedValue) {
        if (submittedValue == null || submittedValue.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(submittedValue, getFormatter(component));
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(submittedValue, getFormatter(component)).atStartOfDay();
            } catch (DateTimeParseException e2) {
                throw new ConverterException(new FacesMessage(submittedValue + " is not a valid LocalDateTime value"), e2);
            }
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object modelValue) {
        if (modelValue == null) {
            return Constants.EMPTY_STRING;
        }
        if (modelValue instanceof LocalDate) {
            return getFormatter(component).format((LocalDate) modelValue);
        }
        if (modelValue instanceof LocalDateTime) {
            return getFormatter(component).format((LocalDateTime) modelValue);
        }
        throw new ConverterException(new FacesMessage(modelValue + " is not a valid LocalDateTime value"));
    }
}