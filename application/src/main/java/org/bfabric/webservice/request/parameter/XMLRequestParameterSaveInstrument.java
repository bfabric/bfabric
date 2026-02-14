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

public class XMLRequestParameterSaveInstrument extends XMLRequestParameterSaveAbstractNamedBaseEntity {

    @XmlElement
    private String adminid;

    @XmlElement
    private String annotationid;

    @XmlElement
    private String available;

    @XmlElement
    private String bookable;

    @XmlElement
    private String label;

    @XmlElement
    private String runenabled;

    @XmlElement
    private String serviceid;

    @XmlElement
    private List<String> servicetypeid;

    @XmlElement
    private String statuscomment;

    @XmlElement
    private String supervisorid;

    @XmlElement
    private List<String> technologyid;

    @XmlElement
    private String up;

    @XmlElement
    private String userbookable;

    @XmlElement
    private String uservisible;

    public String getAdminid() {
        return adminid;
    }

    public String getAnnotationid() {
        return annotationid;
    }

    public String getAvailable() {
        return available;
    }

    public String getBookable() {
        return bookable;
    }

    public String getLabel() {
        return label;
    }

    public String getRunenabled() {
        return runenabled;
    }

    public String getServiceid() {
        return serviceid;
    }

    public List<String> getServicetypeid() {
        return servicetypeid;
    }

    public String getStatuscomment() {
        return statuscomment;
    }

    public String getSupervisorid() {
        return supervisorid;
    }

    public List<String> getTechnologyid() {
        return technologyid;
    }

    public String getUp() {
        return up;
    }

    public String getUserbookable() {
        return userbookable;
    }

    public String getUservisible() {
        return uservisible;
    }

    public void setAdminid(String adminid) {
        this.adminid = adminid;
    }

    public void setAnnotationid(String annotationid) {
        this.annotationid = annotationid;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public void setBookable(String bookable) {
        this.bookable = bookable;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setRunenabled(String runenabled) {
        this.runenabled = runenabled;
    }

    public void setServiceid(String serviceid) {
        this.serviceid = serviceid;
    }

    public void setServicetypeid(List<String> servicetypeid) {
        this.servicetypeid = servicetypeid;
    }

    public void setStatuscomment(String statuscomment) {
        this.statuscomment = statuscomment;
    }

    public void setSupervisorid(String supervisorid) {
        this.supervisorid = supervisorid;
    }

    public void setTechnologyid(List<String> technologyid) {
        this.technologyid = technologyid;
    }

    public void setUp(String up) {
        this.up = up;
    }

    public void setUserbookable(String userbookable) {
        this.userbookable = userbookable;
    }

    public void setUservisible(String uservisible) {
        this.uservisible = uservisible;
    }
}