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

import java.time.LocalDateTime;
import java.util.Arrays;

import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentEvent;
import org.bfabric.entity.InstrumentEventType;
import org.bfabric.entity.User;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstrumentEvent;

public class MFInstrumentEvent extends AbstractMF {

    private final InstrumentEvent instrumentEvent;

    private final XMLRequestParameterSaveInstrumentEvent xmlRequestSaveInstrumentEvent;

    public MFInstrumentEvent(InstrumentEvent instrumentEvent, XMLRequestParameterSaveInstrumentEvent xmlRequestSaveInstrumentEvent) {
        this.instrumentEvent = instrumentEvent;
        this.xmlRequestSaveInstrumentEvent = xmlRequestSaveInstrumentEvent;
    }

    @Override
    public synchronized void apply() throws Exception {
        getInstrumentEvent().setInstrument(getInstrument());
        getInstrumentEvent().setInstrumentEventType(getInstrumentEventType());
        getInstrumentEvent().setStatus(getStatus());
        getInstrumentEvent().setDescription(getDescription());
        getInstrumentEvent().setDateTime(getDateTime());
        getInstrumentEvent().setUser(getUser());
    }

    public LocalDateTime getDateTime() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentEvent().getDatetime() != null) {
            return MFHelper.dateTimeValueOf("datetime", getXmlRequestSaveInstrumentEvent().getDatetime());
        }
        return getInstrumentEvent().getDateTime();
    }

    public String getDescription() {
        if (getXmlRequestSaveInstrumentEvent().getDescription() != null) {
            return getXmlRequestSaveInstrumentEvent().getDescription();
        }
        return getInstrumentEvent().getDescription();
    }

    public Instrument getInstrument() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentEvent().getInstrumentid() != null) {
            return (Instrument) fetch(Instrument.class, MFHelper.positiveLongValueOf("instrumentid", getXmlRequestSaveInstrumentEvent().getInstrumentid()));
        }
        return getInstrumentEvent().getInstrument();
    }

    public InstrumentEvent getInstrumentEvent() {
        return instrumentEvent;
    }

    public InstrumentEventType getInstrumentEventType() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentEvent().getInstrumenteventtypeid() != null) {
            return (InstrumentEventType) fetch(InstrumentEventType.class, MFHelper.positiveLongValueOf("instrumenttypeid", getXmlRequestSaveInstrumentEvent().getInstrumenteventtypeid()));
        }
        return getInstrumentEvent().getInstrumentEventType();
    }

    public LogStatusEnum getStatus() throws InvalidEnumValueException {
        if (getXmlRequestSaveInstrumentEvent().getStatus() != null) {
            LogStatusEnum status = LogStatusEnum.value(getXmlRequestSaveInstrumentEvent().getStatus());
            if (status == null) {
                throw new InvalidEnumValueException("status", getXmlRequestSaveInstrumentEvent().getStatus(), CollectionHelper.print(Arrays.asList(LogStatusEnum.values())));
            }
            return status;
        }
        return getInstrumentEvent().getStatus();
    }

    private User getUser() throws InvalidDataException {
        if (getInstrumentEvent().getId() == 0 || getXmlRequestSaveInstrumentEvent().getUserid() != null) {
            MFHelper.checkNotNull("userid", getXmlRequestSaveInstrumentEvent().getUserid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("userid", getXmlRequestSaveInstrumentEvent().getUserid()));
        }
        return getInstrumentEvent().getUser();
    }

    public XMLRequestParameterSaveInstrumentEvent getXmlRequestSaveInstrumentEvent() {
        return xmlRequestSaveInstrumentEvent;
    }
}