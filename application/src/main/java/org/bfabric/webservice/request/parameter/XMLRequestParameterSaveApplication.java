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

public class XMLRequestParameterSaveApplication extends XMLRequestParameterSaveAbstractEnabledBaseEntity {

    @XmlElement
    private String annotationrequired;

    @XmlElement
    private String archiving;

    @XmlElement
    private String category;

    @XmlElement
    private String executableid;

    @XmlElement
    private String foremployeesonly;

    @XmlElement
    private String help;

    @XmlElement
    private String hidden;

    @XmlElement
    private String importresourcesrequired;

    @XmlElement
    private String notifyapplicationsupervisor;

    @XmlElement
    private String notifycontainermember;

    @XmlElement
    private String outputfileformat;

    @XmlElement
    private String pageflowname;

    @XmlElement(name = "precedingapplicationid")
    private List<String> precedingApplications;

    @XmlElement
    private String predecessorid;

    @XmlElement
    private String storageid;

    @XmlElement
    private String submitterid;

    @XmlElement(name = "succeedingapplicationid")
    private List<String> succeedingApplications;

    @XmlElement
    private String succeedingwebappid;

    @XmlElement
    private String supervisorid;

    @XmlElement(required = true, name = "technologyid")
    private List<String> technologies;

    @XmlElement(required = true)
    private String type;

    @XmlElement
    private String valid;

    @XmlElement
    private String weburl;

    @XmlElement
    private String wrappercreatorid;

    public String getAnnotationrequired() {
        return annotationrequired;
    }

    public String getArchiving() {
        return archiving;
    }

    public String getCategory() {
        return category;
    }

    public String getExecutableid() {
        return executableid;
    }

    public String getForemployeesonly() {
        return foremployeesonly;
    }

    public String getHelp() {
        return help;
    }

    public String getHidden() {
        return hidden;
    }

    public String getImportresourcesrequired() {
        return importresourcesrequired;
    }

    public String getNotifyapplicationsupervisor() {
        return notifyapplicationsupervisor;
    }

    public String getNotifycontainermember() {
        return notifycontainermember;
    }

    public String getOutputfileformat() {
        return outputfileformat;
    }

    public String getPageflowname() {
        return pageflowname;
    }

    public List<String> getPrecedingApplications() {
        return precedingApplications;
    }

    public String getPredecessorid() {
        return predecessorid;
    }

    public String getStorageid() {
        return storageid;
    }

    public String getSubmitterid() {
        return submitterid;
    }

    public List<String> getSucceedingApplications() {
        return succeedingApplications;
    }

    public String getSucceedingwebappid() {
        return succeedingwebappid;
    }

    public String getSupervisorid() {
        return supervisorid;
    }

    public List<String> getTechnologies() {
        return technologies;
    }

    public String getType() {
        return type;
    }

    public String getValid() {
        return valid;
    }

    public String getWeburl() {
        return weburl;
    }

    public String getWrappercreatorid() {
        return wrappercreatorid;
    }

    public void setAnnotationrequired(String annotationrequired) {
        this.annotationrequired = annotationrequired;
    }

    public void setArchiving(String archiving) {
        this.archiving = archiving;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setExecutableid(String executableid) {
        this.executableid = executableid;
    }

    public void setForemployeesonly(String foremployeesonly) {
        this.foremployeesonly = foremployeesonly;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public void setImportresourcesrequired(String importresourcesrequired) {
        this.importresourcesrequired = importresourcesrequired;
    }

    public void setNotifyapplicationsupervisor(String notifyapplicationsupervisor) {
        this.notifyapplicationsupervisor = notifyapplicationsupervisor;
    }

    public void setNotifycontainermember(String notifycontainermember) {
        this.notifycontainermember = notifycontainermember;
    }

    public void setOutputfileformat(String outputfileformat) {
        this.outputfileformat = outputfileformat;
    }

    public void setPageflowname(String pageflowname) {
        this.pageflowname = pageflowname;
    }

    public void setPrecedingApplications(List<String> precedingApplications) {
        this.precedingApplications = precedingApplications;
    }

    public void setPredecessorid(String predecessorid) {
        this.predecessorid = predecessorid;
    }

    public void setStorageid(String storageid) {
        this.storageid = storageid;
    }

    public void setSubmitterid(String submitterid) {
        this.submitterid = submitterid;
    }

    public void setSucceedingApplications(List<String> succeedingApplications) {
        this.succeedingApplications = succeedingApplications;
    }

    public void setSucceedingwebappid(String succeedingwebappid) {
        this.succeedingwebappid = succeedingwebappid;
    }

    public void setSupervisorid(String supervisorid) {
        this.supervisorid = supervisorid;
    }

    public void setTechnologies(List<String> technologies) {
        this.technologies = technologies;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public void setWeburl(String weburl) {
        this.weburl = weburl;
    }

    public void setWrappercreatorid(String wrappercreatorid) {
        this.wrappercreatorid = wrappercreatorid;
    }

}
