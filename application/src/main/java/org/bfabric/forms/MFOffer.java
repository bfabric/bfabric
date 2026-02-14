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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.bfabric.entity.Container;
import org.bfabric.entity.Offer;
import org.bfabric.entity.OrganizationType;
import org.bfabric.entity.User;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOffer;

public class MFOffer extends AbstractMF {

    private final Offer offer;

    private final XMLRequestParameterSaveOffer xmlRequestSaveOffer;

    public MFOffer(Offer offer, XMLRequestParameterSaveOffer xmlRequestSaveOffer) {
        this.offer = offer;
        this.xmlRequestSaveOffer = xmlRequestSaveOffer;
    }

    @Override
    public void apply() throws Exception {
        getOffer().setOrganizationType(getOrganizationType());
        getOffer().setDescription(getDescription());
        getOffer().setEuGrant(getEuGrant());
        getOffer().setRequester(getRequester());
        getOffer().setRequesterAddress(getRequesterAddress());
        getOffer().setRequesterName(getRequesterName());
        getOffer().setCoach(getCoach());
        getOffer().setCoachBackup(getCoachBackup());
        getOffer().setDiscount(getDiscount());
        getOffer().setStatus(getStatus());
    }

    public User getCoach() throws InvalidDataException {
        if (getXmlRequestSaveOffer().getCoachid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("coachid", getXmlRequestSaveOffer().getCoachid()));
        }
        return getOffer().getCoach();
    }

    public User getCoachBackup() throws InvalidDataException {
        if (getXmlRequestSaveOffer().getCoachbackupid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("coachbackupid", getXmlRequestSaveOffer().getCoachbackupid()));
        }
        return getOffer().getCoachBackup();
    }

    public Set<Container> getContainers() throws InvalidDataException {
        if (getXmlRequestSaveOffer().getContainerid() != null) {
            Set<Container> containers = new HashSet<>();
            for (String containerId : getXmlRequestSaveOffer().getContainerid()) {
                if (!containerId.isEmpty()) {
                    Container container = (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", containerId));
                    containers.add(container);
                }
            }
            return CollectionHelper.asSet(containers);
        }
        return getOffer().getContainers();
    }

    public String getDescription() {
        if (getXmlRequestSaveOffer().getDescription() != null) {
            return getXmlRequestSaveOffer().getDescription();
        }
        return getOffer().getDescription();
    }

    private BigDecimal getDiscount() throws InvalidDataException {
        if (getXmlRequestSaveOffer().getDiscount() != null) {
            MFHelper.checkNotNull("discount", getXmlRequestSaveOffer().getDiscount());
            return MFHelper.bigDecimalValueOf("discount", getXmlRequestSaveOffer().getDiscount());
        }
        return getOffer().getDiscount();
    }

    public Boolean getEuGrant() throws InvalidDataException {
        if (getXmlRequestSaveOffer().getEugrant() != null) {
            return MFHelper.booleanValueOf("eugrant", getXmlRequestSaveOffer().getEugrant());
        }
        return getOffer().getEuGrant();
    }

    public Offer getOffer() {
        return offer;
    }

    private OrganizationType getOrganizationType() throws InvalidDataException {
        if (getXmlRequestSaveOffer().getOrganizationtypeid() != null) {
            MFHelper.checkNotNull("organizationtypeid", getXmlRequestSaveOffer().getOrganizationtypeid());
            return (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("organizationtypeid", getXmlRequestSaveOffer().getOrganizationtypeid()));
        }
        return getOffer().getOrganizationType();
    }

    public User getRequester() throws InvalidDataException {
        if (getXmlRequestSaveOffer().getRequesterid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("requesterid", getXmlRequestSaveOffer().getRequesterid()));
        }
        return getOffer().getRequester();
    }

    public String getRequesterAddress() {
        if (getXmlRequestSaveOffer().getRequesteraddress() != null) {
            return getXmlRequestSaveOffer().getRequesteraddress();
        }
        return getOffer().getRequesterAddress();
    }

    public String getRequesterName() {
        if (getXmlRequestSaveOffer().getRequestername() != null) {
            return getXmlRequestSaveOffer().getRequestername();
        }
        return getOffer().getRequesterName();
    }

    public StatusEnum getStatus() throws InvalidEnumValueException, InvalidDataException {
        if (getXmlRequestSaveOffer().getStatus() != null) {
            return StatusEnum.value(getXmlRequestSaveOffer().getStatus());
        }
        return getOffer().getStatus();
    }

    public XMLRequestParameterSaveOffer getXmlRequestSaveOffer() {
        return xmlRequestSaveOffer;
    }
}

