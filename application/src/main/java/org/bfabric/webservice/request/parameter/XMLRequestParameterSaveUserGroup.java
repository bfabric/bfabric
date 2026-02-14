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

public class XMLRequestParameterSaveUserGroup extends XMLRequestParameterSaveAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String divisionid;

    @XmlElement
    private String forEmployeesOnly;

    @XmlElement
    private String hidden;

    @XmlElement
    private String instituteid;

    @XmlElement
    private String internal;

    @XmlElement
    private String supervisorid;

    @XmlElement(name = "userid")
    private List<String> users;

    public String getDivisionid() {
        return divisionid;
    }

    public String getForEmployeesOnly() {
        return forEmployeesOnly;
    }

    public String getHidden() {
        return hidden;
    }

    public String getInstituteid() {
        return instituteid;
    }

    public String getInternal() {
        return internal;
    }

    public String getSupervisorid() {
        return supervisorid;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setDivisionid(String divisionid) {
        this.divisionid = divisionid;
    }

    public void setForEmployeesOnly(String forEmployeesOnly) {
        this.forEmployeesOnly = forEmployeesOnly;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public void setInstituteid(String instituteid) {
        this.instituteid = instituteid;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public void setSupervisorid(String supervisorid) {
        this.supervisorid = supervisorid;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
