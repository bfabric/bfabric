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

package org.bfabric.xml.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.InstrumentEvent;
import org.bfabric.util.DateUtils;

@XmlRootElement(name = "instrumentevent")
public class XMLInstrumentEvent extends XMLAbstractDescriptionBaseEntity {

    @XmlElement
    private String datetime;

    @XmlElement
    private XMLInstrument instrument;

    @XmlElement
    private XMLInstrumentEventType instrumenteventtype;

    @XmlElement
    private String status;

    @XmlElement
    private XMLUser user;

    public XMLInstrumentEvent() {
    }

    public XMLInstrumentEvent(InstrumentEvent entity, boolean reference) {
        super(entity, reference);
    }

    public XMLInstrumentEvent(InstrumentEvent entity) {
        super(entity);
        if (entity != null) {
            if (entity.getStatus() != null) {
                setStatus(entity.getStatus().name());
            }
            if (entity.getInstrumentEventType() != null) {
                setInstrumenteventtype(new XMLInstrumentEventType(entity.getInstrumentEventType(), true));
            }
            if (entity.getInstrument() != null) {
                setInstrument(new XMLInstrument(entity.getInstrument(), true));
            }
            if (entity.getDateTime() != null) {
                setDatetime(DateUtils.getDateAsFormattedString(entity.getDateTime()));
            }
            if (entity.getUser() != null) {
                setUser(new XMLUser(entity.getUser(), true));
            }

        }
    }

    public String getDatetime() {
        return datetime;
    }

    public XMLInstrument getInstrument() {
        return instrument;
    }

    public XMLInstrumentEventType getInstrumenteventtype() {
        return instrumenteventtype;
    }

    public String getStatus() {
        return status;
    }

    public XMLUser getUser() {
        return user;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setInstrument(XMLInstrument instrument) {
        this.instrument = instrument;
    }

    public void setInstrumenteventtype(XMLInstrumentEventType instrumenteventtype) {
        this.instrumenteventtype = instrumenteventtype;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUser(XMLUser user) {
        this.user = user;
    }
}
