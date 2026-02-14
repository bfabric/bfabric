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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.InstrumentReservation;

@XmlRootElement(name = "instrumentreservation")
public class XMLInstrumentReservation extends XMLAbstractDescriptionBaseEntity {

    @XmlElement
    private XMLUser booker;

    @XmlElement
    private List<XMLCharge> charge = new ArrayList<>();

    @XmlElement
    private String chargeable;

    @XmlElement
    private List<XMLContainer> container = new ArrayList<>();

    @XmlElement
    private String duration;

    @XmlElement
    private String enddate;

    @XmlElement
    private String instrument;

    @XmlElement
    private String repeatingfrequency;

    @XmlElement
    private String repeatinguntil;

    @XmlElement
    private XMLServiceType servicetype;

    @XmlElement
    private String startdate;

    @XmlElement
    private String type;

    @XmlElement
    private XMLUser user;

    public XMLInstrumentReservation() {
    }

    public XMLInstrumentReservation(InstrumentReservation entity, boolean reference) {
        super(entity, reference);
    }

    public XMLInstrumentReservation(InstrumentReservation instrumentReservation) {
        super(instrumentReservation);
        if (instrumentReservation != null) {
            if (instrumentReservation.getInstrument() != null) {
                setInstrument(instrumentReservation.getInstrument().getDisplayName());
            }
            if (instrumentReservation.getInstrumentReservationType() != null) {
                setType(instrumentReservation.getInstrumentReservationType().getName());
            }
            if (instrumentReservation.getUser() != null) {
                setUser(new XMLUser(instrumentReservation.getUser(), true));
            }
            if (instrumentReservation.getBooker() != null) {
                setBooker(new XMLUser(instrumentReservation.getBooker(), true));
            }
            if (instrumentReservation.getServiceType() != null) {
                setServicetype(new XMLServiceType(instrumentReservation.getServiceType(), true));
            }
            if (instrumentReservation.getEndDate() != null) {
                setEnddate(instrumentReservation.getEndDate().toString());
            }
            if (instrumentReservation.getStartDate() != null) {
                setStartdate(instrumentReservation.getStartDate().toString());
            }
            setChargeable(String.valueOf(instrumentReservation.isChargeable()));
            if (instrumentReservation.getDuration() != null) {
                setDuration(instrumentReservation.getDuration().toString());
            }
            if (instrumentReservation.getContainers() != null) {
                container = instrumentReservation.getContainers().stream()
                    .map(c -> new XMLContainer(c, true))
                    .collect(Collectors.toList());
            }
            if (instrumentReservation.getCharges() != null) {
                charge = instrumentReservation.getCharges().stream()
                    .map(c -> new XMLCharge(c, true))
                    .collect(Collectors.toList());
            }
        }
    }

    public XMLUser getBooker() {
        return booker;
    }

    public List<XMLCharge> getCharge() {
        return charge;
    }

    public String getChargeable() {
        return chargeable;
    }

    public List<XMLContainer> getContainer() {
        return container;
    }

    public String getDuration() {
        return duration;
    }

    public String getEnddate() {
        return enddate;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getRepeatingfrequency() {
        return repeatingfrequency;
    }

    public String getRepeatinguntil() {
        return repeatinguntil;
    }

    public XMLServiceType getServicetype() {
        return servicetype;
    }

    public String getStartdate() {
        return startdate;
    }

    public String getType() {
        return type;
    }

    public XMLUser getUser() {
        return user;
    }

    public void setBooker(XMLUser booker) {
        this.booker = booker;
    }

    public void setCharge(List<XMLCharge> charge) {
        this.charge = charge;
    }

    public void setChargeable(String chargeable) {
        this.chargeable = chargeable;
    }

    public void setContainer(List<XMLContainer> container) {
        this.container = container;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public void setRepeatingfrequency(String repeatingfrequency) {
        this.repeatingfrequency = repeatingfrequency;
    }

    public void setRepeatinguntil(String repeatinguntil) {
        this.repeatinguntil = repeatinguntil;
    }

    public void setServicetype(XMLServiceType servicetype) {
        this.servicetype = servicetype;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUser(XMLUser user) {
        this.user = user;
    }
}


