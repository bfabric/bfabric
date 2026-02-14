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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Application;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.User;
import org.bfabric.util.DateUtils;

@XmlRootElement(name = "instrument")
public class XMLInstrument extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private XMLUser admin;

    @XmlElement
    private XMLAnnotation annotation;

    @XmlElement
    private List<XMLApplication> application = new ArrayList<>();

    @XmlElement
    private String available;

    @XmlElement
    private String availabletime;

    @XmlElement
    private String availabletimetotal;

    @XmlElement
    private String bookable;

    @XmlElement
    private String bookabletime;

    @XmlElement
    private String bookabletimetotal;

    @XmlElement
    private List<XMLUser> booker = new ArrayList<>();

    @XmlElement
    private List<XMLInstrument> child = new ArrayList<>();

    @XmlElement
    private String computer;

    @XmlElement
    private XMLService defaultserviceforcharging;

    @XmlElement
    private String installationdate;

    @XmlElement
    private String inventorynumber;

    @XmlElement
    private String label;

    @XmlElement
    private String location;

    @XmlElement
    private XMLInstrument parent;

    @XmlElement
    private String purchaseddate;

    @XmlElement
    private String purchasedprice;

    @XmlElement
    private String purchasedpricecurrency;

    @XmlElement
    private String reservations;

    @XmlElement
    private String runenabled;

    @XmlElement
    private String runenabledtime;

    @XmlElement
    private String runenabledtimetotal;

    @XmlElement
    private String serialnumber;

    @XmlElement
    private List<XMLServiceType> servicetype = new ArrayList<>();

    @XmlElement
    private String states;

    @XmlElement
    private String statuscomment;

    @XmlElement
    private String statusmodified;

    @XmlElement
    private XMLUser supervisor;

    @XmlElement
    private String supplier;

    @XmlElement
    private String suppliercontact;

    @XmlElement
    private String technology;

    @XmlElement
    private String up;

    @XmlElement
    private String uptime;

    @XmlElement
    private String uptimetotal;

    @XmlElement
    private String userbookable;

    @XmlElement
    private String userbookabletime;

    @XmlElement
    private String userbookabletimetotal;

    @XmlElement
    private String uservisible;

    @XmlElement
    private String uservisibletime;

    @XmlElement
    private String uservisibletimetotal;

    public XMLInstrument() {
    }

    public XMLInstrument(Instrument entity, boolean reference) {
        super(entity, reference);
    }

    public XMLInstrument(Instrument entity) {
        super(entity);
        if (entity != null) {
            if (entity.getAnnotation() != null) {
                setAnnotation(new XMLAnnotation(entity.getAnnotation(), true));
            }
            if (entity.getAdmin() != null) {
                setAdmin(new XMLUser(entity.getAdmin(), true));
            }
            if (entity.getSupervisor() != null) {
                setSupervisor(new XMLUser(entity.getSupervisor(), true));
            }
            if (entity.getLabel() != null) {
                setLabel(entity.getLabel());
            }
            if (entity.getInstallationDate() != null) {
                setInstallationdate(DateUtils.getDateAsFormattedString(entity.getInstallationDate()));
            }
            if (entity.getPurchasedDate() != null) {
                setPurchaseddate(DateUtils.getDateAsFormattedString(entity.getPurchasedDate()));
            }
            if (entity.getPurchasedPrice() != null) {
                setPurchasedprice(String.valueOf(entity.getPurchasedPrice()));
            }
            if (entity.getCurrency() != null) {
                setPurchasedpricecurrency(entity.getCurrency().getCode());
            }
            setComputer(entity.getComputer());
            setSerialnumber(entity.getSerialNumber());
            setSupplier(entity.getSeller());
            setSuppliercontact(entity.getSellerContact());
            setInventorynumber(entity.getInventoryNumber());
            setTechnology(entity.getTechnologiesAsString());
            setAvailable(String.valueOf(entity.isAvailable()));
            setAvailabletime(entity.getDurationAvailableTimeNowAsText());
            setAvailabletimetotal(entity.getDurationAvailableTimeTotalNowAsText());
            setUp(String.valueOf(entity.isUp()));
            setUptime(entity.getDurationUpTimeNowAsText());
            setUptimetotal(entity.getDurationUpTimeTotalNowAsText());
            setBookable(String.valueOf(entity.isBookable()));
            setBookabletime(entity.getDurationBookableTimeNowAsText());
            setBookabletimetotal(entity.getDurationBookableTimeTotalNowAsText());
            setUservisible(String.valueOf(entity.isUserVisible()));
            setUservisibletime(entity.getDurationUserVisibleTimeNowAsText());
            setUservisibletimetotal(entity.getDurationUserVisibleTimeTotalNowAsText());
            setUserbookable(String.valueOf(entity.isUserBookable()));
            setUserbookabletime(entity.getDurationUserBookableTimeNowAsText());
            setUserbookabletimetotal(entity.getDurationUserBookableTimeTotalNowAsText());
            setRunenabled(String.valueOf(entity.isRunEnabled()));
            setRunenabledtime(entity.getDurationRunEnabledTimeNowAsText());
            setRunenabledtimetotal(entity.getDurationRunEnabledTimeTotalNowAsText());
            if (entity.getInstrumentStatusInfo() != null) {
                setStatuscomment(entity.getInstrumentStatusInfo().getStatusComment());
            }
            setDescription(entity.getDescription());
            if (entity.getBookers() != null) {
                for (User user : entity.getBookers()) {
                    getBooker().add(new XMLUser(user, true));
                }
            }
            if (entity.getApplications() != null) {
                for (Application app : entity.getApplications()) {
                    getApplication().add(new XMLApplication(app, true));
                }
            }
            if (entity.getServiceTypes() != null) {
                for (ServiceType serviceType : entity.getServiceTypes()) {
                    getServicetype().add(new XMLServiceType(serviceType, true));
                }
            }
            if (entity.getService() != null) {
                setDefaultserviceforcharging(new XMLService(entity.getService(), true));
            }
            if (entity.getChildInstruments() != null) {
                for (Instrument aChild : entity.getChildInstruments()) {
                    getChild().add(new XMLInstrument(aChild, true));
                }
            }
            if (entity.getParent() != null) {
                setParent(new XMLInstrument(entity.getParent(), true));
            }
            if (entity.getReservations() != null && !entity.getReservations().isEmpty()) {
                setReservations(String.valueOf(entity.getReservations().size()));
            }
            if (entity.getStates() != null && !entity.getStates().isEmpty()) {
                setStates(String.valueOf(entity.getStates().size()));
            }
            if (entity.getStatusModified() != null) {
                setStatusmodified(String.valueOf(entity.getStatusModified()));
            }
        }
    }

    public XMLUser getAdmin() {
        return admin;
    }

    public XMLAnnotation getAnnotation() {
        return annotation;
    }

    public List<XMLApplication> getApplication() {
        return application;
    }

    public String getAvailable() {
        return available;
    }

    public String getAvailabletime() {
        return availabletime;
    }

    public String getAvailabletimetotal() {
        return availabletimetotal;
    }

    public String getBookable() {
        return bookable;
    }

    public String getBookabletime() {
        return bookabletime;
    }

    public String getBookabletimetotal() {
        return bookabletimetotal;
    }

    public List<XMLUser> getBooker() {
        return booker;
    }

    public List<XMLInstrument> getChild() {
        return child;
    }

    public String getComputer() {
        return computer;
    }

    public XMLService getDefaultserviceforcharging() {
        return defaultserviceforcharging;
    }

    public String getInstallationdate() {
        return installationdate;
    }

    public String getInventorynumber() {
        return inventorynumber;
    }

    public String getLabel() {
        return label;
    }

    public String getLocation() {
        return location;
    }

    public XMLInstrument getParent() {
        return parent;
    }

    public String getPurchaseddate() {
        return purchaseddate;
    }

    public String getPurchasedprice() {
        return purchasedprice;
    }

    public String getPurchasedpricecurrency() {
        return purchasedpricecurrency;
    }

    public String getReservations() {
        return reservations;
    }

    public String getRunenabled() {
        return runenabled;
    }

    public String getRunenabledtime() {
        return runenabledtime;
    }

    public String getRunenabledtimetotal() {
        return runenabledtimetotal;
    }

    public String getSerialnumber() {
        return serialnumber;
    }

    public List<XMLServiceType> getServicetype() {
        return servicetype;
    }

    public String getStates() {
        return states;
    }

    public String getStatuscomment() {
        return statuscomment;
    }

    public String getStatusmodified() {
        return statusmodified;
    }

    public XMLUser getSupervisor() {
        return supervisor;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getSuppliercontact() {
        return suppliercontact;
    }

    public String getTechnology() {
        return technology;
    }

    public String getUp() {
        return up;
    }

    public String getUptime() {
        return uptime;
    }

    public String getUptimetotal() {
        return uptimetotal;
    }

    public String getUserbookable() {
        return userbookable;
    }

    public String getUserbookabletime() {
        return userbookabletime;
    }

    public String getUserbookabletimetotal() {
        return userbookabletimetotal;
    }

    public String getUservisible() {
        return uservisible;
    }

    public String getUservisibletime() {
        return uservisibletime;
    }

    public String getUservisibletimetotal() {
        return uservisibletimetotal;
    }

    public void setAdmin(XMLUser admin) {
        this.admin = admin;
    }

    public void setAnnotation(XMLAnnotation annotation) {
        this.annotation = annotation;
    }

    public void setApplication(List<XMLApplication> application) {
        this.application = application;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public void setAvailabletime(String availabletime) {
        this.availabletime = availabletime;
    }

    public void setAvailabletimetotal(String availabletimetotal) {
        this.availabletimetotal = availabletimetotal;
    }

    public void setBookable(String bookable) {
        this.bookable = bookable;
    }

    public void setBookabletime(String bookabletime) {
        this.bookabletime = bookabletime;
    }

    public void setBookabletimetotal(String bookabletimetotal) {
        this.bookabletimetotal = bookabletimetotal;
    }

    public void setBooker(List<XMLUser> booker) {
        this.booker = booker;
    }

    public void setChild(List<XMLInstrument> child) {
        this.child = child;
    }

    public void setComputer(String computer) {
        this.computer = computer;
    }

    public void setDefaultserviceforcharging(XMLService defaultserviceforcharging) {
        this.defaultserviceforcharging = defaultserviceforcharging;
    }

    public void setInstallationdate(String installationdate) {
        this.installationdate = installationdate;
    }

    public void setInventorynumber(String inventorynumber) {
        this.inventorynumber = inventorynumber;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setParent(XMLInstrument parent) {
        this.parent = parent;
    }

    public void setPurchaseddate(String purchaseddate) {
        this.purchaseddate = purchaseddate;
    }

    public void setPurchasedprice(String purchasedprice) {
        this.purchasedprice = purchasedprice;
    }

    public void setPurchasedpricecurrency(String purchasedpricecurrency) {
        this.purchasedpricecurrency = purchasedpricecurrency;
    }

    public void setReservations(String reservations) {
        this.reservations = reservations;
    }

    public void setRunenabled(String runenabled) {
        this.runenabled = runenabled;
    }

    public void setRunenabledtime(String runenabledtime) {
        this.runenabledtime = runenabledtime;
    }

    public void setRunenabledtimetotal(String runenabledtimetotal) {
        this.runenabledtimetotal = runenabledtimetotal;
    }

    public void setSerialnumber(String serialnumber) {
        this.serialnumber = serialnumber;
    }

    public void setServicetype(List<XMLServiceType> servicetype) {
        this.servicetype = servicetype;
    }

    public void setStates(String states) {
        this.states = states;
    }

    public void setStatuscomment(String statuscomment) {
        this.statuscomment = statuscomment;
    }

    public void setStatusmodified(String statusmodified) {
        this.statusmodified = statusmodified;
    }

    public void setSupervisor(XMLUser supervisor) {
        this.supervisor = supervisor;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setSuppliercontact(String suppliercontact) {
        this.suppliercontact = suppliercontact;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public void setUp(String up) {
        this.up = up;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public void setUptimetotal(String uptimetotal) {
        this.uptimetotal = uptimetotal;
    }

    public void setUserbookable(String userbookable) {
        this.userbookable = userbookable;
    }

    public void setUserbookabletime(String userbookabletime) {
        this.userbookabletime = userbookabletime;
    }

    public void setUserbookabletimetotal(String userbookabletimetotal) {
        this.userbookabletimetotal = userbookabletimetotal;
    }

    public void setUservisible(String uservisible) {
        this.uservisible = uservisible;
    }

    public void setUservisibletime(String uservisibletime) {
        this.uservisibletime = uservisibletime;
    }

    public void setUservisibletimetotal(String uservisibletimetotal) {
        this.uservisibletimetotal = uservisibletimetotal;
    }
}