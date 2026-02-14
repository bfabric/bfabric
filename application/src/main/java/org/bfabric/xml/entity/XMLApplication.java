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
import org.bfabric.entity.Technology;

@XmlRootElement(name = "application")
public class XMLApplication extends XMLAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String annotationrequired;

    @XmlElement
    private String archiving;

    @XmlElement
    private String category;

    @XmlElement
    private XMLDatasetTemplate datasettemplate;

    @XmlElement
    private String enabled;

    @XmlElement
    private XMLExecutable executable;

    @XmlElement
    private String foremployeesonly;

    @XmlElement
    private String help;

    @XmlElement
    private String hidden;

    @XmlElement
    private String importresourcesrequired;

    @XmlElement
    private XMLInstrument instrument;

    @XmlElement
    private String notifyapplicationsupervisor;

    @XmlElement
    private String notifycontainermember;

    @XmlElement
    private String outputfileformat;

    @XmlElement
    private String pageflow;

    @XmlElement
    private List<XMLApplication> precedingapplication = new ArrayList<>();

    @XmlElement
    private XMLApplication predecessor;

    @XmlElement
    private XMLStorage storage;

    @XmlElement
    private XMLSubmitter submitter;

    @XmlElement
    private List<XMLApplication> succeedingapplication = new ArrayList<>();

    @XmlElement
    private XMLApplication succeedingwebapp;

    @XmlElement
    private XMLUser supervisor;

    @XmlElement
    private List<String> technology = new ArrayList<>();

    @XmlElement
    private String type;

    @XmlElement
    private String valid;

    @XmlElement
    private String weburl;

    @XmlElement
    private XMLWrapperCreator wrappercreator;

    public XMLApplication() {
    }

    public XMLApplication(Application entity, boolean reference) {
        super(entity, reference);
    }

    public XMLApplication(Application entity) {
        super(entity);
        if (entity != null) {
            setAnnotationrequired(Boolean.toString(entity.isAnnotationRequired()));
            if (entity.getArchiving() != null) {
                setArchiving(entity.getArchiving().toString());
            }
            if (entity.getApplicationType() != null) {
                setType(entity.getApplicationType().getName());
            }
            if (entity.getApplicationCategory() != null) {
                setCategory(entity.getApplicationCategory().getName());
            }
            setEnabled(Boolean.toString(entity.isEnabled()));
            if (entity.getExecutable() != null) {
                setExecutable(new XMLExecutable(entity.getExecutable(), true));
            }
            setForemployeesonly(Boolean.toString(entity.isForEmployeesOnly()));
            if (entity.getHelp() != null) {
                setHelp(entity.getHelp());
            }
            setHidden(Boolean.toString(entity.isHidden()));
            setImportresourcesrequired(Boolean.toString(entity.isImportResourcesRequired()));
            if (entity.getOutputFileFormat() != null) {
                setOutputfileformat(entity.getOutputFileFormat());
            }
            if (entity.getPageflow() != null) {
                setPageflow(entity.getPageflow().getName());
            }
            if (entity.getPrecedingApplications() != null) {
                for (Application precedingApplication : entity.getPrecedingApplications()) {
                    XMLApplication xmlApplication = new XMLApplication(precedingApplication, true);
                    getPrecedingapplication().add(xmlApplication);
                }
            }
            if (entity.getPredecessor() != null) {
                setPredecessor(new XMLApplication(entity.getPredecessor(), true));
            }
            if (entity.getSucceedingWebApp() != null) {
                setSucceedingwebapp(new XMLApplication(entity.getSucceedingWebApp(), true));
            }
            if (entity.getWebUrl() != null) {
                setWeburl(entity.getWebUrl());
            }
            setNotifyapplicationsupervisor(Boolean.toString(entity.isNotifyApplicationSupervisor()));
            setNotifycontainermember(Boolean.toString(entity.isNotifyContainerMember()));
            if (entity.getStorage() != null) {
                setStorage(new XMLStorage(entity.getStorage(), true));
            }
            if (entity.getSucceedingApplications() != null) {
                for (Application succeedingApplication : entity.getSucceedingApplications()) {
                    XMLApplication xmlApplication = new XMLApplication(succeedingApplication, true);
                    getSucceedingapplication().add(xmlApplication);
                }
            }
            if (entity.getSubmitter() != null) {
                setSubmitter(new XMLSubmitter(entity.getSubmitter(), true));
            }
            if (entity.getSucceedingWebApp() != null) {
                setSucceedingwebapp(new XMLApplication(entity.getSucceedingWebApp(), true));
            }
            if (entity.getSupervisor() != null) {
                setSupervisor(new XMLUser(entity.getSupervisor(), true));
            }
            if (entity.getTechnology() != null) {
                for (Technology aTechnology : entity.getTechnologies()) {
                    getTechnology().add(aTechnology.getName());
                }
            }
            setValid(Boolean.toString(entity.isValid()));
            if (entity.getWrapperCreator() != null) {
                setWrappercreator(new XMLWrapperCreator(entity.getWrapperCreator(), true));
            }
            if (entity.getInstrument() != null) {
                setInstrument(new XMLInstrument(entity.getInstrument(), true));
            }
            if (entity.getDatasetTemplate() != null) {
                setDatasettemplate(new XMLDatasetTemplate(entity.getDatasetTemplate(), true));
            }
        }
    }

    public String getAnnotationrequired() {
        return annotationrequired;
    }

    public String getArchiving() {
        return archiving;
    }

    public String getCategory() {
        return category;
    }

    public XMLDatasetTemplate getDatasettemplate() {
        return datasettemplate;
    }

    public String getEnabled() {
        return enabled;
    }

    public XMLExecutable getExecutable() {
        return executable;
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

    public XMLInstrument getInstrument() {
        return instrument;
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

    public String getPageflow() {
        return pageflow;
    }

    public List<XMLApplication> getPrecedingapplication() {
        return precedingapplication;
    }

    public XMLApplication getPredecessor() {
        return predecessor;
    }

    public XMLStorage getStorage() {
        return storage;
    }

    public XMLSubmitter getSubmitter() {
        return submitter;
    }

    public List<XMLApplication> getSucceedingapplication() {
        return succeedingapplication;
    }

    public XMLApplication getSucceedingwebapp() {
        return succeedingwebapp;
    }

    public XMLUser getSupervisor() {
        return supervisor;
    }

    public List<String> getTechnology() {
        return technology;
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

    public XMLWrapperCreator getWrappercreator() {
        return wrappercreator;
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

    public void setDatasettemplate(XMLDatasetTemplate datasettemplate) {
        this.datasettemplate = datasettemplate;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public void setExecutable(XMLExecutable executable) {
        this.executable = executable;
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

    public void setInstrument(XMLInstrument instrument) {
        this.instrument = instrument;
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

    public void setPageflow(String pageflow) {
        this.pageflow = pageflow;
    }

    public void setPrecedingapplication(List<XMLApplication> precedingapplication) {
        this.precedingapplication = precedingapplication;
    }

    public void setPredecessor(XMLApplication predecessor) {
        this.predecessor = predecessor;
    }

    public void setStorage(XMLStorage storage) {
        this.storage = storage;
    }

    public void setSubmitter(XMLSubmitter submitter) {
        this.submitter = submitter;
    }

    public void setSucceedingapplication(List<XMLApplication> succeedingapplication) {
        this.succeedingapplication = succeedingapplication;
    }

    public void setSucceedingwebapp(XMLApplication succeedingwebapp) {
        this.succeedingwebapp = succeedingwebapp;
    }

    public void setSupervisor(XMLUser supervisor) {
        this.supervisor = supervisor;
    }

    public void setTechnology(List<String> technology) {
        this.technology = technology;
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

    public void setWrappercreator(XMLWrapperCreator wrappercreator) {
        this.wrappercreator = wrappercreator;
    }
}
