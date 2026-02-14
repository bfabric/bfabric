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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.bfabric.entity.Container;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.InstrumentReservationType;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.User;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.RepeaterHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstrumentReservation;

public class MFInstrumentReservation extends AbstractMF {

    private final InstrumentReservation instrumentReservation;

    private final XMLRequestParameterSaveInstrumentReservation xmlRequestSaveInstrumentReservation;

    public MFInstrumentReservation(InstrumentReservation instrumentReservation, XMLRequestParameterSaveInstrumentReservation xmlInstrumentReservationRequestSave) {
        this.instrumentReservation = instrumentReservation;
        xmlRequestSaveInstrumentReservation = xmlInstrumentReservationRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getInstrumentReservation().setOldValues();
        getInstrumentReservation().setContainers(getContainers());
        getInstrumentReservation().setInstrument(getInstrument());
        getInstrumentReservation().setInstrumentReservationType(getInstrumentReservationType());
        getInstrumentReservation().setBooker(getBooker());
        getInstrumentReservation().setServiceType(getServiceType());
        getInstrumentReservation().setUser(getUser());
        getInstrumentReservation().setChargeable(isChargeable());
        getInstrumentReservation().setSendMailNotification(isSendMailNotification());
        getInstrumentReservation().setStartDate(getStartDate());
        getInstrumentReservation().getInstrumentReservationSetting();
        getInstrumentReservation().setEndDate(getEndDate());
        if (getXmlRequestSaveInstrumentReservation().getRepeatinguntil() != null && getXmlRequestSaveInstrumentReservation().getRepeatingfrequency() != null) {
            getInstrumentReservation().setRepeaterHelper(getRepeaterHelper());
            if (getInstrumentReservation().getId() == 0 && getInstrumentReservation().isRepeatable()) {
                getInstrumentReservation().setRepeaterEvents(getRepeaterHelper());
            }
        }

        getInstrumentReservation().setDescription(getDescription());
    }

    private User getBooker() throws InvalidDataException {
        if (getInstrumentReservation().getId() == 0 || getXmlRequestSaveInstrumentReservation().getBookerid() != null) {
            MFHelper.checkNotNull("bookerid", getXmlRequestSaveInstrumentReservation().getBookerid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("bookerid", getXmlRequestSaveInstrumentReservation().getBookerid()));
        }
        return getInstrumentReservation().getBooker();
    }

    private Set<Container> getContainers() {
        if (getXmlRequestSaveInstrumentReservation().getContainerid() != null) {
            return getXmlRequestSaveInstrumentReservation().getContainerid().stream().filter(containerId -> !containerId.isEmpty()).map(containerId -> {
                    try {
                        return (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", containerId));
                    } catch (InvalidDataException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
        }
        return getInstrumentReservation().getContainers();
    }

    public String getDescription() {
        if (getXmlRequestSaveInstrumentReservation().getDescription() != null) {
            return getXmlRequestSaveInstrumentReservation().getDescription();
        }
        return getInstrumentReservation().getDescription();
    }

    public LocalDateTime getEndDate() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentReservation().getEnddate() != null) {
            return MFHelper.dateTimeValueOf("endDate", getXmlRequestSaveInstrumentReservation().getEnddate());
        }
        return getInstrumentReservation().getEndDate();
    }

    private Instrument getInstrument() throws InvalidDataException {
        if (getInstrumentReservation().getId() == 0 || getXmlRequestSaveInstrumentReservation().getInstrumentid() != null) {
            MFHelper.checkNotNull("instrumentid", getXmlRequestSaveInstrumentReservation().getInstrumentid());
            return (Instrument) fetch(Instrument.class, MFHelper.positiveLongValueOf("instrumentid", getXmlRequestSaveInstrumentReservation().getInstrumentid()));
        }
        return getInstrumentReservation().getInstrument();
    }

    public InstrumentReservation getInstrumentReservation() {
        return instrumentReservation;
    }

    private InstrumentReservationType getInstrumentReservationType() throws InvalidDataException {
        if (getInstrumentReservation().getId() == 0 || getXmlRequestSaveInstrumentReservation().getTypeid() != null) {
            MFHelper.checkNotNull("typeid", getXmlRequestSaveInstrumentReservation().getTypeid());
            return (InstrumentReservationType) fetch(InstrumentReservationType.class, MFHelper.positiveLongValueOf("typeid", getXmlRequestSaveInstrumentReservation().getTypeid()));
        }
        return getInstrumentReservation().getInstrumentReservationType();
    }

    private RepeaterHelper getRepeaterHelper() throws InvalidDataException {
        int frequency = MFHelper.integerValueOf("repeatingfrequency", getXmlRequestSaveInstrumentReservation().getRepeatingfrequency());
        LocalDate until = MFHelper.dateValueOf("repeatinguntil", getXmlRequestSaveInstrumentReservation().getRepeatinguntil());
        return new RepeaterHelper(true, until, frequency);
    }

    private ServiceType getServiceType() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentReservation().getServicetypeid() != null) {
            return (ServiceType) fetch(ServiceType.class, MFHelper.positiveLongValueOf("servicetypeid", getXmlRequestSaveInstrumentReservation().getServicetypeid()));
        }
        return getInstrumentReservation().getServiceType();
    }

    public LocalDateTime getStartDate() throws InvalidDataException {
        if (getXmlRequestSaveInstrumentReservation().getStartdate() != null) {
            return MFHelper.dateTimeValueOf("startDate", getXmlRequestSaveInstrumentReservation().getStartdate());
        }
        return getInstrumentReservation().getStartDate();
    }

    private User getUser() throws InvalidDataException {
        if (getInstrumentReservation().getId() == 0 || getXmlRequestSaveInstrumentReservation().getUserid() != null) {
            MFHelper.checkNotNull("userid", getXmlRequestSaveInstrumentReservation().getUserid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("userid", getXmlRequestSaveInstrumentReservation().getUserid()));
        }
        return getInstrumentReservation().getUser();
    }

    public XMLRequestParameterSaveInstrumentReservation getXmlRequestSaveInstrumentReservation() {
        return xmlRequestSaveInstrumentReservation;
    }

    private Boolean isChargeable() throws InvalidDataException {
        return MFHelper.booleanValueOf("chargeable", getXmlRequestSaveInstrumentReservation().getChargeable());
    }

    public Boolean isSendMailNotification() throws InvalidDataException {
        return MFHelper.booleanValueOf("sendmailnotification", getXmlRequestSaveInstrumentReservation().getSendmailnotification());
    }
}

