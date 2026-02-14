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

import org.bfabric.entity.InstrumentEventType;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstrumentEventType;

public class MFInstrumentEventType extends AbstractMF {

    private final InstrumentEventType instrumentEventType;

    private final XMLRequestParameterSaveInstrumentEventType xmlRequestSaveInstrumentEventType;

    public MFInstrumentEventType(InstrumentEventType instrumentEventType, XMLRequestParameterSaveInstrumentEventType xmlRequestSaveInstrumentEventType) {
        this.instrumentEventType = instrumentEventType;
        this.xmlRequestSaveInstrumentEventType = xmlRequestSaveInstrumentEventType;
    }

    @Override
    public synchronized void apply() throws Exception {
        getInstrumentEventType().setName(getName());
        getInstrumentEventType().setDescription(getDescription());
        getInstrumentEventType().setEnabled(getEnabled());
        getInstrumentEventType().setUserVisible(getUserVisible());
        getInstrumentEventType().setUserCreatable(getUserCreatable());
    }

    public String getDescription() {
        if (getXmlRequestSaveInstrumentEventType().getDescription() != null) {
            return getXmlRequestSaveInstrumentEventType().getDescription();
        }
        return getInstrumentEventType().getDescription();
    }

    public Boolean getEnabled() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentEventType().getEnabled() != null) {
            return MFHelper.booleanValueOf("enabled", getXmlRequestSaveInstrumentEventType().getEnabled());
        }
        return getInstrumentEventType().isEnabled();
    }

    public InstrumentEventType getInstrumentEventType() {
        return instrumentEventType;
    }

    public String getName() {
        if (getXmlRequestSaveInstrumentEventType().getName() != null) {
            return getXmlRequestSaveInstrumentEventType().getName();
        }
        return getInstrumentEventType().getName();
    }

    public Boolean getUserCreatable() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentEventType().getUsercreatable() != null) {
            return MFHelper.booleanValueOf("usercreatable", getXmlRequestSaveInstrumentEventType().getUsercreatable());
        }
        return getInstrumentEventType().isUserCreatable();
    }

    public Boolean getUserVisible() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentEventType().getUservisible() != null) {
            return MFHelper.booleanValueOf("uservisible", getXmlRequestSaveInstrumentEventType().getUservisible());
        }
        return getInstrumentEventType().isUserVisible();
    }

    public XMLRequestParameterSaveInstrumentEventType getXmlRequestSaveInstrumentEventType() {
        return xmlRequestSaveInstrumentEventType;
    }
}