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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Executable;
import org.bfabric.entity.ImportResource;
import org.bfabric.entity.Parameter;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Workunit;

@XmlRootElement(name = "workunit")
public class XMLWorkunit extends XMLContainerReferencingEntity {

    @XmlElement
    private XMLApplication application;

    @XmlElement
    private XMLExecutable applicationexecutable;

    @XmlElement
    private String archiving;

    @XmlElement
    private XMLDataset dataset;

    @XmlElement
    private String exportable;

    @XmlElement
    private List<XMLImportResource> importresource = new ArrayList<>();

    @XmlElement
    private XMLDataset inputdataset;

    @XmlElement
    private List<XMLResource> inputresource = new ArrayList<>();

    @XmlElement
    private List<XMLParameter> parameter = new ArrayList<>();

    @XmlElement
    private String progress;

    @XmlElement
    private List<XMLResource> resource = new ArrayList<>();

    @XmlElement
    private String status;

    @XmlElement
    private XMLExecutable submitterexecutable;

    @XmlElement
    private List<XMLExecutable> workunitexecutable = new ArrayList<>();

    @XmlElement
    private XMLExecutable wrappercreatorexecutable;

    public XMLWorkunit(Workunit entity) {
        super(entity);
        if (entity != null) {
            boolean reference = !entity.isChecked();
            setExportable(Boolean.toString(entity.isExportable()));
            if (entity.getArchiving() != null) {
                setArchiving(entity.getArchiving().toString());
            }
            if (entity.getStatus() != null) {
                setStatus(entity.getStatus().toString());
            }
            if (entity.getProgress() != null) {
                setProgress(entity.getProgress());
            }
            if (entity.isChecked()) {
                setFullDetails(entity);
            } else {
                if (entity.getApplication() != null) {
                    setApplication(new XMLApplication(entity.getApplication(), reference));
                }
                if (entity.getApplicationExecutable() != null) {
                    setApplicationexecutable(new XMLExecutable(entity.getApplicationExecutable(), reference));
                }
                if (entity.getDataset() != null) {
                    setDataset(new XMLDataset(entity.getDataset(), reference));
                }
                if (entity.getImportResources() != null) {
                    for (ImportResource importResource : entity.getImportResources()) {
                        getImportresource().add(new XMLImportResource(importResource, reference));
                    }
                }
                if (entity.getInputDataset() != null) {
                    setInputdataset(new XMLDataset(entity.getInputDataset(), reference));
                }
                if (entity.getInputResources() != null) {
                    for (Resource inputResource : entity.getInputResources()) {
                        getInputresource().add(new XMLResource(inputResource, reference));
                    }
                }
                if (entity.getParameters() != null) {
                    for (Parameter aParameter : entity.getParameters()) {
                        getParameter().add(new XMLParameter(aParameter, reference));
                    }
                }
                if (entity.getResources() != null) {
                    for (Resource aResource : entity.getResources()) {
                        getResource().add(new XMLResource(aResource, reference));
                    }
                }
                if (entity.getSubmitterExecutable() != null) {
                    setSubmitterexecutable(new XMLExecutable(entity.getSubmitterExecutable(), reference));
                }
                if (entity.getWorkunitExecutables() != null) {
                    for (Executable workunitExecutable : entity.getWorkunitExecutables()) {
                        getWorkunitexecutable().add(new XMLExecutable(workunitExecutable, reference));
                    }
                }
                if (entity.getWrapperCreatorExecutable() != null) {
                    setWrappercreatorexecutable(new XMLExecutable(entity.getWrapperCreatorExecutable(), reference));
                }
            }
        }
    }

    public XMLWorkunit() {
    }

    public XMLWorkunit(Workunit entity, boolean reference) {
        super(entity, reference);
    }

    public XMLWorkunit(Workunit entity, List<XMLResource> xmlResourcesList) {
        super(entity, true);
        if (xmlResourcesList != null) {
            setResource(xmlResourcesList);
        }
    }

    public XMLApplication getApplication() {
        return application;
    }

    public XMLExecutable getApplicationexecutable() {
        return applicationexecutable;
    }

    public String getArchiving() {
        return archiving;
    }

    public XMLDataset getDataset() {
        return dataset;
    }

    public String getExportable() {
        return exportable;
    }

    public List<XMLImportResource> getImportresource() {
        return importresource;
    }

    public XMLDataset getInputdataset() {
        return inputdataset;
    }

    public List<XMLResource> getInputresource() {
        return inputresource;
    }

    public List<XMLParameter> getParameter() {
        return parameter;
    }

    public String getProgress() {
        return progress;
    }

    public List<XMLResource> getResource() {
        return resource;
    }

    public String getStatus() {
        return status;
    }

    public XMLExecutable getSubmitterexecutable() {
        return submitterexecutable;
    }

    public List<XMLExecutable> getWorkunitexecutable() {
        return workunitexecutable;
    }

    public XMLExecutable getWrappercreatorexecutable() {
        return wrappercreatorexecutable;
    }

    public void setApplication(XMLApplication application) {
        this.application = application;
    }

    public void setApplicationexecutable(XMLExecutable applicationexecutable) {
        this.applicationexecutable = applicationexecutable;
    }

    public void setArchiving(String archiving) {
        this.archiving = archiving;
    }

    public void setDataset(XMLDataset dataset) {
        this.dataset = dataset;
    }

    public void setExportable(String exportable) {
        this.exportable = exportable;
    }

    private void setFullDetails(@NotNull Workunit workunit) {
        if (workunit.getApplication() != null) {
            setApplication(new XMLApplication(workunit.getApplication()));
        }
        if (workunit.getApplicationExecutable() != null) {
            setApplicationexecutable(new XMLExecutable(workunit.getApplicationExecutable()));
        }
        if (workunit.getDataset() != null) {
            setDataset(new XMLDataset(workunit.getDataset()));
        }
        if (workunit.getImportResources() != null) {
            for (ImportResource importResource : workunit.getImportResources()) {
                getImportresource().add(new XMLImportResource(importResource));
            }
        }
        if (workunit.getInputDataset() != null) {
            setInputdataset(new XMLDataset(workunit.getInputDataset()));
        }
        if (workunit.getInputResources() != null) {
            for (Resource inputResource : workunit.getInputResources()) {
                getInputresource().add(new XMLResource(inputResource));
            }
        }
        if (workunit.getParameters() != null) {
            for (Parameter aParameter : workunit.getParameters()) {
                getParameter().add(new XMLParameter(aParameter));
            }
        }
        if (workunit.getResources() != null) {
            for (Resource aResource : workunit.getResources()) {
                getResource().add(new XMLResource(aResource));
            }
        }
        if (workunit.getSubmitterExecutable() != null) {
            setSubmitterexecutable(new XMLExecutable(workunit.getSubmitterExecutable()));
        }
        if (workunit.getWorkunitExecutables() != null) {
            for (Executable workunitExecutable : workunit.getWorkunitExecutables()) {
                getWorkunitexecutable().add(new XMLExecutable(workunitExecutable));
            }
        }
        if (workunit.getWrapperCreatorExecutable() != null) {
            setWrappercreatorexecutable(new XMLExecutable(workunit.getWrapperCreatorExecutable()));
        }
    }

    public void setImportresource(List<XMLImportResource> importresource) {
        this.importresource = importresource;
    }

    public void setInputdataset(XMLDataset inputdataset) {
        this.inputdataset = inputdataset;
    }

    public void setInputresource(List<XMLResource> inputresource) {
        this.inputresource = inputresource;
    }

    public void setParameter(List<XMLParameter> parameter) {
        this.parameter = parameter;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setResource(List<XMLResource> resource) {
        this.resource = resource;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSubmitterexecutable(XMLExecutable submitterexecutable) {
        this.submitterexecutable = submitterexecutable;
    }

    public void setWorkunitexecutable(List<XMLExecutable> workunitexecutable) {
        this.workunitexecutable = workunitexecutable;
    }

    public void setWrappercreatorexecutable(XMLExecutable wrappercreatorexecutable) {
        this.wrappercreatorexecutable = wrappercreatorexecutable;
    }
}