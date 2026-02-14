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

package org.bfabric.xml;

import org.bfabric.util.StringHelper;

public class XmlLogRow {

    private String field;

    private String newValue;

    private String oldValue;

    public XmlLogRow(String field, String oldValue, String newValue) {
        setField(field);
        setOldValue(oldValue);
        setNewValue(newValue);
    }

    public void addNewValue(String value) {
        if (StringHelper.isNotEmpty(value)) {
            if (StringHelper.isNotEmpty(getNewValue())) {
                setNewValue(getNewValue() + "\n" + value);
            } else {
                setNewValue(value);
            }
        }
    }

    public void addOldValue(String value) {
        if (StringHelper.isNotEmpty(value)) {
            if (StringHelper.isNotEmpty(getOldValue())) {
                setOldValue(getOldValue() + "\n" + value);
            } else {
                setOldValue(value);
            }
        }
    }

    public String getField() {
        return field;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getNewValueSafeHtml() {
        return StringHelper.getSafeHtml(getNewValue());
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getOldValueSafeHtml() {
        return StringHelper.getSafeHtml(getOldValue());
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
}