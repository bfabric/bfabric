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

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Executable;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ExecutableService;

@MeasureCalls
@Named
@ViewScoped
public class ExecutableManager extends AbstractEntityManager<Executable> {

    private static final long serialVersionUID = 1;

    @Inject
    private ExecutableService executableService;

    public ExecutableManager() {
        super(Executable.class);
    }

    public String changeStatus(StatusEnum statusEnum) {
        printFacesMessagesClear(executableService.changeStatus(getExecutable(), ResourceStatusEnum.get(statusEnum)));
        return getShowScreenRedirectURL();
    }

    @Override
    protected Executable createInstance() {
        Executable executable = super.createInstance();
        if (getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.EXECUTABLEMANAGER)) {
            executable.setSupervisor(getCurrentUser());
        }
        return executable;
    }

    public List<Executable> getEnabledValidMasterExecutablesFiltered(String filterString) {
        return executableService.getEnabledValidMasterExecutablesFiltered(filterString, getExecutable().getMasterExecutable());
    }

    @Produces
    @Named("executable")
    public Executable getExecutable() {
        return getInstance();
    }

    public List<Executable> getPotentialPredecessorExecutablesFiltered(String filterString) {
        return executableService.getPotentialPredecessorExecutablesFiltered(filterString, getExecutable());
    }

    public String rollbackStatus() {
        executableService.rollbackStatus(getExecutable());
        getFacesMessagesManager().bufferWarningClear(Messages.get("statusRolledBack"));
        return getShowScreenRedirectURL();
    }

    @Override
    public String save() {
        setCreated(!isManaged());
        executableService.save(getExecutable());
        return postSave(true, false);
    }
}
