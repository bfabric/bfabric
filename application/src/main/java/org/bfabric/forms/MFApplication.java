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

import java.util.HashSet;
import java.util.Set;

import org.bfabric.entity.Application;
import org.bfabric.entity.ApplicationCategory;
import org.bfabric.entity.ApplicationType;
import org.bfabric.entity.Executable;
import org.bfabric.entity.Pageflow;
import org.bfabric.entity.Storage;
import org.bfabric.entity.Submitter;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.entity.WrapperCreator;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveApplication;

public class MFApplication extends AbstractMF {

    private final Application application;

    private final XMLRequestParameterSaveApplication xmlRequestSaveApplication;

    public MFApplication(Application application, XMLRequestParameterSaveApplication xmlRequestSaveApplication) {
        this.application = application;
        this.xmlRequestSaveApplication = xmlRequestSaveApplication;
    }

    @Override
    public synchronized void apply() throws Exception {
        getApplication().setName(getName());
        getApplication().setApplicationType(getApplicationType());
        getApplication().setApplicationCategory(getApplicationCategory());
        getApplication().setTechnologies(getTechnologies());
        getApplication().setAnnotationRequired(isAnnotationRequired());
        getApplication().setDescription(getDescription());
        getApplication().setForEmployeesOnly(isForEmployeesOnly());
        getApplication().setHelp(getHelp());
        getApplication().setEnabled(isEnabled());
        getApplication().setValid(isValid());
        getApplication().setHidden(isHidden());
        getApplication().setImportResourcesRequired(isImportResourcesRequired());
        getApplication().setOutputFileFormat(getOutputFileFormat());
        getApplication().setPageflow(getPageflow());
        getApplication().setPrecedingApplications(getPrecedingApplications());
        getApplication().setPredecessor(getPredecessor());
        getApplication().setWebUrl(getWeburl());
        getApplication().setNotifyApplicationSupervisor(isNotifyApplicationSupervisor());
        getApplication().setNotifyContainerMember(isNotifyContainerMember());
        getApplication().setArchiving(getArchiving());
        getApplication().setStorage(getStorage());
        getApplication().setSucceedingWebApp(getSucceeding());
        getApplication().setSucceedingApplications(getSucceedingApplications());
        getApplication().setSupervisor(getSupervisor());
        Submitter oldSubmitter = getApplication().getSubmitter();
        if (oldSubmitter == null || !oldSubmitter.equals(getSubmitter())) {
            getApplication().setSubmitterAndParameters(getSubmitter());
        }
        WrapperCreator oldWrapperCreator = getApplication().getWrapperCreator();
        if (oldWrapperCreator == null || !oldWrapperCreator.equals(getWrapperCreator())) {
            getApplication().setWrapperCreatorAndParameters(getWrapperCreator());
        }
        Executable oldExecutable = getApplication().getExecutable();
        if (oldExecutable == null || !oldExecutable.equals(getExecutable())) {
            getApplication().setExecutableAndParameters(getExecutable());
        }
        getApplication().setCustomAttributes(getXmlRequestSaveApplication().getCustomattribute());
    }

    public Application getApplication() {
        return application;
    }

    public ApplicationCategory getApplicationCategory() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getCategory() != null) {
            ApplicationCategory applicationCategory = getIdentityService().findByName(ApplicationCategory.class, getXmlRequestSaveApplication().getCategory());
            if (applicationCategory == null) {
                throw new InvalidDataException("Invalid application category: " + getXmlRequestSaveApplication().getCategory());
            }
            return applicationCategory;
        }
        return getApplication().getApplicationCategory();
    }

    public ApplicationType getApplicationType() throws InvalidDataException {
        if (getApplication().getId() == 0) {
            MFHelper.checkNotNull("type", getXmlRequestSaveApplication().getType());
            ApplicationType applicationType = getIdentityService().findByName(ApplicationType.class, getXmlRequestSaveApplication().getType());
            if (applicationType == null) {
                throw new InvalidDataException("Invalid application type: " + getXmlRequestSaveApplication().getType());
            }
            return applicationType;
        }
        return getApplication().getApplicationType();
    }

    public Boolean getArchiving() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getArchiving() != null) {
            return MFHelper.booleanValueOf("archiving", getXmlRequestSaveApplication().getArchiving());
        }
        return getApplication().getArchiving();
    }

    public String getDescription() {
        if (getXmlRequestSaveApplication().getDescription() != null) {
            return getXmlRequestSaveApplication().getDescription();
        }
        return getApplication().getDescription();
    }

    public Executable getExecutable() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getExecutableid() != null) {
            Executable executable = (Executable) fetch(Executable.class, MFHelper.positiveLongValueOf("executableid", getXmlRequestSaveApplication().getExecutableid()));
            if (!executable.getExecutableContext().equals(ExecutableContextEnum.APPLICATION)) {
                throw new InvalidDataException("Executable " + getXmlRequestSaveApplication().getExecutableid() + " is not intended to be used as an application executable.");
            }
            return executable;
        }
        return getApplication().getExecutable();
    }

    public String getHelp() {
        if (getXmlRequestSaveApplication().getHelp() != null) {
            return getXmlRequestSaveApplication().getHelp();
        }
        return getApplication().getHelp();
    }

    public String getName() {
        if (getXmlRequestSaveApplication().getName() != null) {
            return getXmlRequestSaveApplication().getName();
        }
        return getApplication().getName();
    }

    public String getOutputFileFormat() {
        if (getXmlRequestSaveApplication().getOutputfileformat() != null) {
            return getXmlRequestSaveApplication().getOutputfileformat();
        }
        return getApplication().getOutputFileFormat();
    }

    public Pageflow getPageflow() throws Exception {
        if (getXmlRequestSaveApplication().getPageflowname() != null) {
            Pageflow pageflow = getIdentityService().findByName(Pageflow.class, getXmlRequestSaveApplication().getPageflowname());
            if (pageflow == null) {
                throw new InvalidDataException("Invalid pageflow: " + getXmlRequestSaveApplication().getPageflowname());
            }
            if (!getApplication().getApplicationType().getPageflows().contains(pageflow)) {
                throw new InvalidDataException("Pageflow " + pageflow.getName() + " is not valid for application type " + getApplication().getApplicationType().getName());
            }
            return pageflow;
        }
        return getApplication().getId() == 0 ? getApplication().getApplicationType().getDefaultPageflow() : getApplication().getPageflow();
    }

    public Set<Application> getPrecedingApplications() throws Exception {
        if (getXmlRequestSaveApplication().getPrecedingApplications() != null) {
            Set<Application> precedingApplications = new HashSet<>();
            for (String inputApplicationid : getXmlRequestSaveApplication().getPrecedingApplications()) {
                if (getApplication().isAnalysis()) {
                    if (!inputApplicationid.isEmpty()) {
                        Long applicationid = MFHelper.positiveLongValueOf("preceding applicationid", inputApplicationid);
                        if (getXmlRequestSaveApplication().getId() != null && getXmlRequestSaveApplication().getId().equals(applicationid)) {
                            throw new InvalidDataException("Invalid preceding application id " + applicationid);
                        }
                        precedingApplications.add((Application) fetch(Application.class, applicationid));
                    }
                } else {
                    throw new InvalidDataException("No preceding applications for application type " + getApplication().getApplicationType().getName() + " allowed");
                }
            }
            return precedingApplications;
        }
        return getApplication().getPrecedingApplications();
    }

    public Application getPredecessor() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getPredecessorid() != null) {
            return (Application) fetch(Application.class, MFHelper.positiveLongValueOf("predecessorid", getXmlRequestSaveApplication().getPredecessorid()));
        }
        return getApplication().getPredecessor();
    }

    public Storage getStorage() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getStorageid() != null) {
            if (getApplication().isWebApp()) {
                throw new InvalidDataException("No storage for application type " + getApplication().getApplicationType().getName() + " allowed");
            }
            return (Storage) fetch(Storage.class, MFHelper.positiveLongValueOf("storageid", getXmlRequestSaveApplication().getStorageid()));
        }
        return getApplication().getStorage();
    }

    public Submitter getSubmitter() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getSubmitterid() != null) {
            if (getApplication().isWebApp()) {
                throw new InvalidDataException("No submitter for application type " + getApplication().getApplicationType().getName() + " allowed");
            }
            Submitter ret = (Submitter) fetch(Submitter.class, MFHelper.positiveLongValueOf("submitterid", getXmlRequestSaveApplication().getSubmitterid()));
            if (!ret.isValid() && ret.getExecutable() == null) {
                throw new InvalidDataException("Submitter with id " + ret.getId() + " is not available");
            }
            return ret;
        }
        return getApplication().getSubmitter();
    }

    public Application getSucceeding() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getSucceedingwebappid() != null) {
            return (Application) fetch(Application.class, MFHelper.positiveLongValueOf("succeedingwebappid", getXmlRequestSaveApplication().getSucceedingwebappid()));
        }
        return getApplication().getSucceedingWebApp();
    }

    public Set<Application> getSucceedingApplications() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getSucceedingApplications() != null) {
            Set<Application> ret = new HashSet<>();
            if (getXmlRequestSaveApplication().getSucceedingApplications() != null) {
                for (String inputApplicationid : getXmlRequestSaveApplication().getSucceedingApplications()) {
                    if (!inputApplicationid.isEmpty()) {
                        Long applicationid = MFHelper.positiveLongValueOf("succeeding applicationid", inputApplicationid);
                        ret.add((Application) fetch(Application.class, applicationid));
                    }
                }
            }
            return ret;
        }
        return getApplication().getSucceedingApplications();
    }

    public User getSupervisor() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getSupervisorid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("supervisorid", getXmlRequestSaveApplication().getSupervisorid()));
        }
        return getApplication().getSupervisor() != null ? getApplication().getSupervisor() : getIdentityService().getCurrentUser();
    }

    public Set<Technology> getTechnologies() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getTechnologies() != null) {
            Set<Technology> technologies = new HashSet<>();
            for (String technologyId : getXmlRequestSaveApplication().getTechnologies()) {
                Technology technology = (Technology) fetch(Technology.class, MFHelper.positiveLongValueOf("technologyid", technologyId));
                if (technology != null) {
                    technologies.add(technology);
                } else {
                    throw new InvalidDataException("Invalid technologyid: " + technologyId);
                }

            }
            return technologies;
        }
        return getApplication().getTechnologies();
    }

    public String getWeburl() throws InvalidDataException {
        if (getApplication().isWebApp() && getXmlRequestSaveApplication().getWeburl() != null) {
            MFHelper.checkUri("weburl", getXmlRequestSaveApplication().getWeburl());
            return getXmlRequestSaveApplication().getWeburl();
        }
        return getApplication().getWebUrl();
    }

    public WrapperCreator getWrapperCreator() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getWrappercreatorid() != null) {
            if (getApplication().isWebApp()) {
                throw new InvalidDataException("No wrapper creator for application type " + getApplication().getApplicationType().getName() + " allowed");
            }
            WrapperCreator ret = (WrapperCreator) fetch(WrapperCreator.class, MFHelper.positiveLongValueOf("wrappercreatorid", getXmlRequestSaveApplication().getWrappercreatorid()));
            if (!ret.isValid() && ret.getExecutable() == null) {
                throw new InvalidDataException("Wrapper creator with id " + ret.getId() + " is not available");
            }
            return ret;
        }
        return getApplication().getWrapperCreator();
    }

    public XMLRequestParameterSaveApplication getXmlRequestSaveApplication() {
        return xmlRequestSaveApplication;
    }

    public boolean isAnnotationRequired() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getAnnotationrequired() != null) {
            return MFHelper.booleanValueOf("annotationRequired", getXmlRequestSaveApplication().getAnnotationrequired());
        }
        return getApplication().isAnnotationRequired();
    }

    public boolean isEnabled() throws Exception {
        if (getXmlRequestSaveApplication().getEnabled() != null) {
            return MFHelper.booleanValueOf("enabled", getXmlRequestSaveApplication().getEnabled());
        }
        return getApplication().isEnabled();
    }

    public boolean isForEmployeesOnly() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getForemployeesonly() != null) {
            return MFHelper.booleanValueOf("forEmployeesOnly", getXmlRequestSaveApplication().getForemployeesonly());
        }
        return getApplication().isForEmployeesOnly();
    }

    public boolean isHidden() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getHidden() != null) {
            return MFHelper.booleanValueOf("hidden", getXmlRequestSaveApplication().getHidden());
        }
        return getApplication().isHidden();
    }

    public boolean isImportResourcesRequired() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getImportresourcesrequired() != null) {
            return MFHelper.booleanValueOf("importresourcerequired", getXmlRequestSaveApplication().getImportresourcesrequired());
        }
        return getApplication().isImportResourcesRequired();
    }

    public boolean isNotifyApplicationSupervisor() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getNotifyapplicationsupervisor() != null) {
            return MFHelper.booleanValueOf("NotifyApplicationSupervisor", getXmlRequestSaveApplication().getNotifyapplicationsupervisor());
        }
        return getApplication().isNotifyApplicationSupervisor();
    }

    public boolean isNotifyContainerMember() throws InvalidDataException {
        if (getXmlRequestSaveApplication().getNotifycontainermember() != null) {
            return MFHelper.booleanValueOf("NotifyContainerMember", getXmlRequestSaveApplication().getNotifycontainermember());
        }
        return getApplication().isNotifyContainerMember();
    }

    public boolean isValid() throws Exception {
        if (getXmlRequestSaveApplication().getValid() != null) {
            return MFHelper.booleanValueOf("valid", getXmlRequestSaveApplication().getValid());
        }
        return getApplication().isValid();
    }
}