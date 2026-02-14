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

package org.bfabric.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Application;
import org.bfabric.entity.Executable;
import org.bfabric.entity.Parameter;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ApplicationService;
import org.bfabric.service.TechnologyService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class ApplicationManager extends AbstractEntityManager<Application> {

    private static final long serialVersionUID = 1;

    private final Set<Application> checkedPrecedingApplications = new HashSet<>();

    private final Set<Application> checkedSucceedingApplications = new HashSet<>();

    @Param
    protected Long containerId;

    @Inject
    private ApplicationService applicationService;

    @Param
    private String applicationTypeName;

    @Param
    private Long executableId;

    private List<Application> potentialPrecedingApplications;

    private List<Application> potentialSucceedingApplications;

    @Inject
    private TechnologyService technologyService;

    public ApplicationManager() {
        super(Application.class);
    }

    @Override
    protected Application createInstance() {
        final Application application = super.createInstance();
        if (application != null) {
            if (getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.APPLICATIONMANAGER)) {
                application.setSupervisor(getCurrentUser());
            }
            if (executableId != null) {
                try {
                    application.setExecutable((Executable) entityService.fetch(Executable.class, executableId));
                } catch (final InvalidDataException e) {
                    e.printStackTrace();
                }
            }
        }
        return application;
    }

    public void deselectExecutable() {
        getApplication().setExecutable(null);
    }

    public void deselectStorage() {
        getApplication().setStorage(null);
    }

    public void deselectSubmitter() {
        getApplication().setSubmitterAndParameters(null);
    }

    public void deselectWrapperCreator() {
        getApplication().setWrapperCreatorAndParameters(null);
    }

    @Produces
    @Named("app")
    public Application getApplication() {
        return getInstance();
    }

    public String getApplicationTypeName() {
        return applicationTypeName;
    }

    public List<Application> getAvailableSucceedingWebApps(String filterString) {
        return applicationService.getAvailableSucceedingWebApps(filterString, getApplication().getSucceedingWebApp());
    }

    public Set<Application> getCheckedPrecedingApplications() {
        return checkedPrecedingApplications;
    }

    public Set<Application> getCheckedSucceedingApplications() {
        return checkedSucceedingApplications;
    }

    public Long getContainerId() {
        return containerId;
    }

    public List<Application> getPotentialPrecedingApplications() {
        if (potentialPrecedingApplications == null) {
            return potentialPrecedingApplications = applicationService.getPotentialPrecedingApplications();
        }
        return potentialPrecedingApplications;
    }

    public List<Application> getPotentialPredecessorApplicationsFiltered(String filterString) {
        return applicationService.getPotentialPredecessorApplicationsFiltered(filterString, getApplication());
    }

    public List<Application> getPotentialSucceedingApplications() {
        if (potentialSucceedingApplications == null) {
            return potentialSucceedingApplications = applicationService.getPotentialSucceedingApplications();
        }
        return potentialSucceedingApplications;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (containerId != null) {
            getContextManager().setContextContainerById(containerId);
        }
        initPotentialPrecedingSucceedingApplications();
    }

    private void initPotentialPrecedingSucceedingApplications() {
        for (final Application application : getPotentialPrecedingApplications()) {
            if (getApplication().getPrecedingApplications().contains(application)) {
                application.check();
                getCheckedPrecedingApplications().add(application);
            }
        }

        for (final Application application : getPotentialSucceedingApplications()) {
            if (getApplication().getSucceedingApplications().contains(application)) {
                application.check();
                getCheckedSucceedingApplications().add(application);
            }
        }
    }

    public String removeParameter(Parameter parameter) {
        applicationService.removeParameter(getApplication(), parameter);
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted"));
        return createRedirectShowScreenURL(getApplication(), "parameters", null);
    }

    public String removeParameters() {
        applicationService.removeParameters(getApplication());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted"));
        return createRedirectShowScreenURL(getApplication(), "parameters", null);
    }

    @Override
    public String save() {
        getApplication().setPrecedingApplications(getCheckedPrecedingApplications());
        getApplication().setSucceedingApplications(getCheckedSucceedingApplications());
        setCreated(!isManaged());
        if (getApplication().isWebApp() && getApplication().isEnabled()) {
            getApplication().checkValidity();
        }
        applicationService.save(getApplication());
        String ret = postSave(true, false);
        if (getApplication().isWebApp() && getApplication().isEnabled() && !getApplication().isValid()) {
            getFacesMessagesManager().bufferWarning(Messages.get("webApp") + " " + Messages.get("urlNotFoundHint"));
        }
        return ret;
    }

    public void setApplicationTypeName(String applicationTypeName) {
        this.applicationTypeName = applicationTypeName;
    }

    public void setExecutableId(Long executableId) {
        this.executableId = executableId;
    }

    public void updatePrecedingApplications() {
        for (final Application application : getPotentialPrecedingApplications()) {
            if (application.isChecked()) {
                getCheckedPrecedingApplications().add(application);
            } else {
                getCheckedPrecedingApplications().remove(application);
            }
        }
    }

    public void updateSucceedingApplications() {
        for (final Application application : getPotentialSucceedingApplications()) {
            if (application.isChecked()) {
                getCheckedSucceedingApplications().add(application);
            } else {
                getCheckedSucceedingApplications().remove(application);
            }
        }
    }
}
