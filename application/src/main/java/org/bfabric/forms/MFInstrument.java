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

import java.util.Set;
import java.util.stream.Collectors;

import org.bfabric.entity.Annotation;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstrument;

public class MFInstrument extends AbstractMF {

    private final Instrument instrument;

    private final XMLRequestParameterSaveInstrument xmlRequestSaveInstrument;

    public MFInstrument(Instrument instrument, XMLRequestParameterSaveInstrument xmlInstrumentRequestSave) {
        this.instrument = instrument;
        this.xmlRequestSaveInstrument = xmlInstrumentRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getInstrument().setOldValues();
        getInstrument().setName(getName());
        getInstrument().setLabel(getLabel());
        getInstrument().setService(getService());
        getInstrument().setServiceTypes(getServiceTypes());
        getInstrument().setSupervisor(getSupervisor());
        getInstrument().setAdmin(getAdmin());
        getInstrument().setAnnotation(getAnnotation());
        getInstrument().setTechnologies(getTechnologies());
        getInstrument().getInstrumentStatusInfo().setAvailable(isAvailable());
        getInstrument().getInstrumentStatusInfo().setUp(isUp());
        getInstrument().getInstrumentStatusInfo().setBookable(isBookable());
        getInstrument().getInstrumentStatusInfo().setUserBookable(isUserBookable());
        getInstrument().getInstrumentStatusInfo().setUserVisible(isUserVisible());
        getInstrument().getInstrumentStatusInfo().setRunEnabled(isRunEnabled());
        getInstrument().getInstrumentStatusInfo().setStatusComment(getStatusComment());
        if (!getInstrument().isManaged()) {
            getInstrument().setStatusModified();
        }
        getInstrument().setCustomAttributes(getXmlRequestSaveInstrument().getCustomattribute());
    }

    private User getAdmin() throws InvalidDataException {
        if (getInstrument().getId() == 0 || getXmlRequestSaveInstrument().getAdminid() != null) {
            MFHelper.checkNotNull("adminid", getXmlRequestSaveInstrument().getAdminid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("adminid", getXmlRequestSaveInstrument().getAdminid()));
        }
        return getInstrument().getAdmin();
    }

    private Annotation getAnnotation() throws InvalidDataException {
        if (getXmlRequestSaveInstrument().getAnnotationid() != null) {
            return (Annotation) fetch(Annotation.class, MFHelper.positiveLongValueOf("annotationid", getXmlRequestSaveInstrument().getAnnotationid()));
        }
        return getInstrument().getAnnotation();
    }

    public Instrument getInstrument() {
        return instrument;
    }

    private String getLabel() {
        if (getXmlRequestSaveInstrument().getLabel() != null) {
            return getXmlRequestSaveInstrument().getLabel();
        }
        return getInstrument().getLabel();
    }

    private String getName() {
        if (getXmlRequestSaveInstrument().getName() != null) {
            return getXmlRequestSaveInstrument().getName();
        }
        return getInstrument().getName();
    }

    private Service getService() throws InvalidDataException {
        if (getXmlRequestSaveInstrument().getServiceid() != null) {
            return (Service) fetch(Service.class, MFHelper.positiveLongValueOf("serviceid", getXmlRequestSaveInstrument().getServiceid()));
        }
        return getInstrument().getService();
    }

    private Set<ServiceType> getServiceTypes() {
        if (getXmlRequestSaveInstrument().getServicetypeid() != null) {
            return getXmlRequestSaveInstrument().getServicetypeid().stream()
                .filter(serviceTypeId -> !serviceTypeId.isEmpty())
                .map(serviceTypeId -> {
                    try {
                        return (ServiceType) fetch(ServiceType.class, MFHelper.positiveLongValueOf("servicetypeid", serviceTypeId));
                    } catch (InvalidDataException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
        }
        return getInstrument().getServiceTypes();
    }

    private String getStatusComment() {
        if (getXmlRequestSaveInstrument().getStatuscomment() != null) {
            return getXmlRequestSaveInstrument().getStatuscomment();
        }
        return null;
    }

    private User getSupervisor() throws InvalidDataException {
        if (getInstrument().getId() == 0 || getXmlRequestSaveInstrument().getSupervisorid() != null) {
            MFHelper.checkNotNull("supervisorid", getXmlRequestSaveInstrument().getSupervisorid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("supervisorid", getXmlRequestSaveInstrument().getSupervisorid()));
        }
        return getInstrument().getSupervisor();
    }

    private Set<Technology> getTechnologies() {
        if (getXmlRequestSaveInstrument().getTechnologyid() != null) {
            return getXmlRequestSaveInstrument().getTechnologyid().stream()
                .filter(technologyId -> !technologyId.isEmpty())
                .map(technologyId -> {
                    try {
                        return (Technology) fetch(Technology.class, MFHelper.positiveLongValueOf("technologyid", technologyId));
                    } catch (InvalidDataException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
        }
        return getInstrument().getTechnologies();
    }

    public XMLRequestParameterSaveInstrument getXmlRequestSaveInstrument() {
        return xmlRequestSaveInstrument;
    }

    public boolean isAvailable() throws InvalidDataException {
        if (getXmlRequestSaveInstrument().getAvailable() != null) {
            return MFHelper.booleanValueOf("available", getXmlRequestSaveInstrument().getAvailable());
        }
        return getInstrument().isAvailable();
    }

    public boolean isBookable() throws InvalidDataException {
        if (getXmlRequestSaveInstrument().getBookable() != null) {
            return MFHelper.booleanValueOf("bookable", getXmlRequestSaveInstrument().getBookable());
        }
        return getInstrument().isBookable();
    }

    public boolean isRunEnabled() throws InvalidDataException {
        if (getXmlRequestSaveInstrument().getRunenabled() != null) {
            return MFHelper.booleanValueOf("runenabled", getXmlRequestSaveInstrument().getRunenabled());
        }
        return getInstrument().isRunEnabled();
    }

    public boolean isUp() throws InvalidDataException {
        if (getXmlRequestSaveInstrument().getUp() != null) {
            return MFHelper.booleanValueOf("up", getXmlRequestSaveInstrument().getUp());
        }
        return getInstrument().isUp();
    }

    public boolean isUserBookable() throws InvalidDataException {
        if (getXmlRequestSaveInstrument().getUserbookable() != null) {
            return MFHelper.booleanValueOf("userbookable", getXmlRequestSaveInstrument().getUserbookable());
        }
        return getInstrument().isUserBookable();
    }

    public boolean isUserVisible() throws InvalidDataException {
        if (getXmlRequestSaveInstrument().getUservisible() != null) {
            return MFHelper.booleanValueOf("Uservisible", getXmlRequestSaveInstrument().getUservisible());
        }
        return getInstrument().isUserVisible();
    }
}