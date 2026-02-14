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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Offer;
import org.bfabric.entity.OfferedCharge;

@XmlRootElement(name = "offer")
public class XMLOffer extends XMLAbstractDescriptionBaseEntity {

    @XmlElement
    protected Boolean eugrant;

    @XmlElement
    protected Boolean locked;

    @XmlElement
    private List<XMLOfferedCharge> charge;

    @XmlElement
    private Integer charges;

    @XmlElement
    private XMLUser coach;

    @XmlElement
    private XMLUser coachbackup;

    @XmlElement
    private Integer comments;

    @XmlElement
    private List<XMLContainer> container = new ArrayList<>();

    @XmlElement
    private BigDecimal discount;

    @XmlElement
    private Integer orders;

    @XmlElement
    private XMLOrganizationType organizationtype;

    @XmlElement
    private XMLUser requester;

    @XmlElement
    private String requesteraddress;

    @XmlElement
    private String requestername;

    @XmlElement
    private String status;

    public XMLOffer() {
    }

    public XMLOffer(Offer entity, boolean reference) {
        super(entity, reference);
    }

    public XMLOffer(Offer entity) {
        super(entity);
        if (entity != null) {
            if (entity.getStatus() != null) {
                setStatus(entity.getStatus().getLabel());
            }
            if (entity.getEuGrant() != null) {
                setEugrant(entity.getEuGrant());
            }
            setLocked(entity.isLocked());
            if (entity.getDiscount() != null) {
                setDiscount(entity.getDiscount());
            }
            if (!entity.getCharges().isEmpty()) {
                setCharges(entity.getCharges().size());
            }
            if (entity.getCharges() != null) {
                List<XMLOfferedCharge> offeredCharges = new ArrayList<>();
                for (OfferedCharge offeredCharge : entity.getCharges()) {
                    offeredCharges.add(new XMLOfferedCharge(offeredCharge, true));
                }
                setCharge(offeredCharges);
            }
            if (!entity.getReferencingOrders().isEmpty()) {
                setOrders(getOrders());
            }
            if (!entity.getComments().isEmpty()) {
                setComments(entity.getComments().size());
            }
            if (entity.getOrganizationType() != null) {
                setOrganizationtype(new XMLOrganizationType(entity.getOrganizationType(), true));
            }
            if (entity.getCoach() != null) {
                setCoach(new XMLUser(entity.getCoach(), true));
            }
            if (entity.getCoachBackup() != null) {
                setCoachbackup(new XMLUser(entity.getCoachBackup(), true));
            }
            if (entity.getRequester() != null) {
                setRequester(new XMLUser(entity.getRequester(), true));
            }
            if (entity.getRequesterName() != null) {
                setRequestername(entity.getRequesterName());
            }
            if (entity.getRequesterAddress() != null) {
                setRequesteraddress(entity.getRequesterAddress());
            }
        }
    }

    public List<XMLOfferedCharge> getCharge() {
        return charge;
    }

    public Integer getCharges() {
        return charges;
    }

    public XMLUser getCoach() {
        return coach;
    }

    public XMLUser getCoachbackup() {
        return coachbackup;
    }

    public Integer getComments() {
        return comments;
    }

    public List<XMLContainer> getContainer() {
        return container;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public Boolean getEugrant() {
        return eugrant;
    }

    public Boolean getLocked() {
        return locked;
    }

    public Integer getOrders() {
        return orders;
    }

    public XMLOrganizationType getOrganizationtype() {
        return organizationtype;
    }

    public XMLUser getRequester() {
        return requester;
    }

    public String getRequesteraddress() {
        return requesteraddress;
    }

    public String getRequestername() {
        return requestername;
    }

    public String getStatus() {
        return status;
    }

    public void setCharge(List<XMLOfferedCharge> charge) {
        this.charge = charge;
    }

    public void setCharges(Integer charges) {
        this.charges = charges;
    }

    public void setCoach(XMLUser coach) {
        this.coach = coach;
    }

    public void setCoachbackup(XMLUser coachbackup) {
        this.coachbackup = coachbackup;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public void setContainer(List<XMLContainer> container) {
        this.container = container;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public void setEugrant(Boolean eugrant) {
        this.eugrant = eugrant;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public void setOrders(Integer orders) {
        this.orders = orders;
    }

    public void setOrganizationtype(XMLOrganizationType organizationtype) {
        this.organizationtype = organizationtype;
    }

    public void setRequester(XMLUser requester) {
        this.requester = requester;
    }

    public void setRequesteraddress(String requesteraddress) {
        this.requesteraddress = requesteraddress;
    }

    public void setRequestername(String requestername) {
        this.requestername = requestername;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}