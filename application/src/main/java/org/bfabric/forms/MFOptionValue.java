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

package org.bfabric.forms;

import org.bfabric.entity.Option;
import org.bfabric.entity.OptionValue;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOptionValue;

public class MFOptionValue extends AbstractMF {

    private final OptionValue optionValue;

    private final XMLRequestParameterSaveOptionValue xmlRequestSaveOptionValue;

    public MFOptionValue(OptionValue optionValue, XMLRequestParameterSaveOptionValue xmlRequestSaveOptionValue) {
        this.optionValue = optionValue;
        this.xmlRequestSaveOptionValue = xmlRequestSaveOptionValue;
    }

    @Override
    public void apply() throws Exception {
        getOptionValue().setName(getName());
        getOptionValue().setOption(getOption());
    }

    public String getName() {
        if (getXmlRequestSaveOptionValue().getName() != null) {
            return getXmlRequestSaveOptionValue().getName();
        }
        return getOptionValue().getName();
    }

    public Option getOption() throws InvalidDataException {
        if (getXmlRequestSaveOptionValue().getOptionid() != null) {
            return (Option) fetch(Option.class, MFHelper.positiveLongValueOf("optionid", getXmlRequestSaveOptionValue().getOptionid()));
        }
        return getOptionValue().getOption();
    }

    public OptionValue getOptionValue() {
        return optionValue;
    }

    public XMLRequestParameterSaveOptionValue getXmlRequestSaveOptionValue() {
        return xmlRequestSaveOptionValue;
    }
}
