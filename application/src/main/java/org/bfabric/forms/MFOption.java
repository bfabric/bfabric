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
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOption;

public class MFOption extends AbstractMF {

    private final Option option;

    private final XMLRequestParameterSaveOption xmlRequestSaveOption;

    public MFOption(Option option, XMLRequestParameterSaveOption xmlRequestSaveOption) {
        this.option = option;
        this.xmlRequestSaveOption = xmlRequestSaveOption;
    }

    @Override
    public void apply() throws Exception {
        getOption().setName(getName());
        getOption().setParent(getIdentityService().getCurrentUser());
        getOption().setEnabled(getEnabled());
        getOption().setMultiple(getMultiple());
        getOption().setRequired(getRequired());
    }

    public Boolean getEnabled() throws InvalidDataException {
        if (getXmlRequestSaveOption().getEnabled() != null) {
            return MFHelper.booleanValueOf("enabled", getXmlRequestSaveOption().getEnabled());
        }
        return getOption().isEnabled();
    }

    public Boolean getMultiple() throws InvalidDataException {
        if (getXmlRequestSaveOption().getMultiple() != null) {
            return MFHelper.booleanValueOf("multiple", getXmlRequestSaveOption().getMultiple());
        }
        return getOption().isMultiple();
    }

    public String getName() {
        if (getXmlRequestSaveOption().getName() != null) {
            return getXmlRequestSaveOption().getName();
        }
        return getOption().getName();
    }

    public Option getOption() {
        return option;
    }

    public Boolean getRequired() throws InvalidDataException {
        if (getXmlRequestSaveOption().getRequired() != null) {
            return MFHelper.booleanValueOf("required", getXmlRequestSaveOption().getRequired());
        }
        return getOption().isRequired();
    }

    public XMLRequestParameterSaveOption getXmlRequestSaveOption() {
        return xmlRequestSaveOption;
    }
}
