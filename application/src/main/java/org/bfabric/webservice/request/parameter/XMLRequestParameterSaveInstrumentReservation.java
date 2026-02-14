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

public class XMLRequestParameterSaveInstrumentReservation extends XMLRequestParameterSaveAbstractDescriptionBaseEntity {

    @XmlElement
    private String bookerid;

    @XmlElement
    private String chargeable;

    @XmlElement
    private List<String> containerid;

    @XmlElement
    private String enddate;

    @XmlElement
    private String instrumentid;

    @XmlElement
    private String repeatingfrequency;

    @XmlElement
    private String repeatinguntil;

    @XmlElement
    private String sendmailnotification;

    @XmlElement
    private String servicetypeid;

    @XmlElement
    private String startdate;

    @XmlElement
    private String typeid;

    @XmlElement
    private String userid;

    public String getBookerid() {
        return bookerid;
    }

    public String getChargeable() {
        return chargeable;
    }

    public List<String> getContainerid() {
        return containerid;
    }

    public String getEnddate() {
        return enddate;
    }

    public String getInstrumentid() {
        return instrumentid;
    }

    public String getRepeatingfrequency() {
        return repeatingfrequency;
    }

    public String getRepeatinguntil() {
        return repeatinguntil;
    }

    public String getSendmailnotification() {
        return sendmailnotification;
    }

    public String getServicetypeid() {
        return servicetypeid;
    }

    public String getStartdate() {
        return startdate;
    }

    public String getTypeid() {
        return typeid;
    }

    public String getUserid() {
        return userid;
    }

    public void setBookerid(String bookerid) {
        this.bookerid = bookerid;
    }

    public void setChargeable(String chargeable) {
        this.chargeable = chargeable;
    }

    public void setContainerid(List<String> containerid) {
        this.containerid = containerid;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public void setInstrumentid(String instrumentid) {
        this.instrumentid = instrumentid;
    }

    public void setRepeatingfrequency(String repeatingfrequency) {
        this.repeatingfrequency = repeatingfrequency;
    }

    public void setRepeatinguntil(String repeatinguntil) {
        this.repeatinguntil = repeatinguntil;
    }

    public void setSendmailnotification(String sendmailnotification) {
        this.sendmailnotification = sendmailnotification;
    }

    public void setServicetypeid(String servicetypeid) {
        this.servicetypeid = servicetypeid;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public void setTypeid(String typeid) {
        this.typeid = typeid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
