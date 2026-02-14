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

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.DeployerContextProperty;
import org.bfabric.entity.EnvironmentContextProperty;
import org.bfabric.entity.InstanceContextProperty;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ConfService;

@MeasureCalls
@Named
@ViewScoped
public class ContextPropertyManager implements Serializable {

    private static final Logger logger = Logger.getLogger(ContextPropertyManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    FacesMessagesManager facesMessagesManager;

    @Inject
    private ConfManager confManager;

    @Inject
    private ConfService confService;

    @Inject
    private SessionManager sessionManager;

    public void deployerChanged(ValueChangeEvent event) {
        DeployerContextProperty deployer = (DeployerContextProperty) event.getNewValue();
        try {
            for (DeployerContextProperty dcp : getAllDeployer()) {
                if (dcp.getValue().equals(deployer.getValue())) {
                    confService.setContext(dcp.getValue(), Boolean.TRUE, DeployerContextProperty.class);
                } else {
                    confService.setContext(dcp.getValue(), Boolean.FALSE, DeployerContextProperty.class);
                }
            }
            confManager.setConfiguration();
            facesMessagesManager.bufferWarning(Messages.get("successfullyUpdated"));
        } catch (Exception e) {
            facesMessagesManager.bufferWarning(Messages.get("cannotSaveCurrentDeployer"));
            logger.severe(Messages.get("cannotSaveCurrentDeployer") + ": " + e);
        }
        sessionManager.redirectRelative("/systemproperty/list.xhtml");
    }

    public void environmentChanged(ValueChangeEvent event) {
        EnvironmentContextProperty environment = (EnvironmentContextProperty) event.getNewValue();
        try {
            for (EnvironmentContextProperty ecp : getAllEnvironment()) {
                if (ecp.getValue().equals(environment.getValue())) {
                    confService.setContext(ecp.getValue(), Boolean.TRUE, EnvironmentContextProperty.class);
                } else {
                    confService.setContext(ecp.getValue(), Boolean.FALSE, EnvironmentContextProperty.class);
                }
            }
            confManager.setConfiguration();
            facesMessagesManager.bufferWarning(Messages.get("successfullyUpdated"));
        } catch (Exception e) {
            facesMessagesManager.bufferWarning(Messages.get("cannotSaveCurrentEnvironment"));
            logger.severe(Messages.get("cannotSaveCurrentEnvironment") + ": " + e);
        }
        sessionManager.redirectRelative("/systemproperty/list.xhtml");
    }

    @CachedMethodResult
    public List<DeployerContextProperty> getAllDeployer() {
        return (List<DeployerContextProperty>) confService.getContexts(DeployerContextProperty.class);
    }

    @CachedMethodResult
    public List<DeployerContextProperty> getAllEnabledDeployer() {
        return (List<DeployerContextProperty>) confService.getEnabledContexts(DeployerContextProperty.class);
    }

    @CachedMethodResult
    public List<EnvironmentContextProperty> getAllEnabledEnvironment() {
        return (List<EnvironmentContextProperty>) confService.getEnabledContexts(EnvironmentContextProperty.class);
    }

    @CachedMethodResult
    public List<InstanceContextProperty> getAllEnabledInstance() {
        return (List<InstanceContextProperty>) confService.getEnabledContexts(InstanceContextProperty.class);
    }

    @CachedMethodResult
    public List<EnvironmentContextProperty> getAllEnvironment() {
        return (List<EnvironmentContextProperty>) confService.getContexts(EnvironmentContextProperty.class);
    }

    @CachedMethodResult
    public List<InstanceContextProperty> getAllInstance() {
        return (List<InstanceContextProperty>) confService.getContexts(InstanceContextProperty.class);
    }

    public void instanceChanged(ValueChangeEvent event) {
        InstanceContextProperty instance = (InstanceContextProperty) event.getNewValue();
        try {
            for (InstanceContextProperty icp : getAllInstance()) {
                if (icp.getValue().equals(instance.getValue())) {
                    confService.setContext(icp.getValue(), Boolean.TRUE, InstanceContextProperty.class);
                } else {
                    confService.setContext(icp.getValue(), Boolean.FALSE, InstanceContextProperty.class);
                }
            }
            confManager.setConfiguration();
            facesMessagesManager.bufferWarning(Messages.get("successfullyUpdated"));
        } catch (Exception e) {
            facesMessagesManager.bufferWarning(Messages.get("cannotSaveCurrentInstance"));
            logger.severe("Cannot save current instance: " + e);
        }
        sessionManager.redirectRelative("/systemproperty/list.xhtml");
    }
}
