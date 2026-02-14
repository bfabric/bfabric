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

package org.bfabric.webservice.request.parameter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveOffer extends XMLRequestParameterSaveAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String charges;

    @XmlElement
    private String coachbackupid;

    @XmlElement
    private String coachid;

    @XmlElement
    private List<String> containerid;

    @XmlElement
    private String discount;

    @XmlElement
    private String eugrant;

    @XmlElement
    private String locked;

    @XmlElement
    private String organizationtypeid;

    @XmlElement
    private String requesteraddress;

    @XmlElement
    private String requesterid;

    @XmlElement
    private String requestername;

    @XmlElement
    private String status;

    public String getCharges() {
        return charges;
    }

    public String getCoachbackupid() {
        return coachbackupid;
    }

    public String getCoachid() {
        return coachid;
    }

    public List<String> getContainerid() {
        return containerid;
    }

    public String getDiscount() {
        return discount;
    }

    public String getEugrant() {
        return eugrant;
    }

    public String getLocked() {
        return locked;
    }

    public String getOrganizationtypeid() {
        return organizationtypeid;
    }

    public String getRequesteraddress() {
        return requesteraddress;
    }

    public String getRequesterid() {
        return requesterid;
    }

    public String getRequestername() {
        return requestername;
    }

    public String getStatus() {
        return status;
    }

    public void setCharges(String charges) {
        this.charges = charges;
    }

    public void setCoachbackupid(String coachbackupid) {
        this.coachbackupid = coachbackupid;
    }

    public void setCoachid(String coachid) {
        this.coachid = coachid;
    }

    public void setContainerid(List<String> containerid) {
        this.containerid = containerid;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public void setEugrant(String eugrant) {
        this.eugrant = eugrant;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public void setOrganizationtypeid(String organizationtypeid) {
        this.organizationtypeid = organizationtypeid;
    }

    public void setRequesteraddress(String requesteraddress) {
        this.requesteraddress = requesteraddress;
    }

    public void setRequesterid(String requesterid) {
        this.requesterid = requesterid;
    }

    public void setRequestername(String requestername) {
        this.requestername = requestername;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
