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

package org.bfabric.entity;

import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.entity.api.HasSupervisor;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.UserService;

@MappedSuperclass
public abstract class AbstractSupervisorDescriptionNamedBaseEntity extends AbstractDescriptionNamedBaseEntity implements HasSupervisor {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisorid")
    @XmlIDREF
    private User supervisor;

    @Transient
    private boolean supervisorChanged;

    public AbstractSupervisorDescriptionNamedBaseEntity() {
    }

    public List<User> getEmployeesIncludingSupervisor(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getSupervisor());
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getSupervisor() != null) {
            addEntityInfoItem(summary, "supervisor", getSupervisor().getFullLastFirstName());
        }
        return summary.toString();
    }

    @Override
    public User getSupervisor() {
        return supervisor;
    }

    @Override
    public boolean isAdminOrSupervisor() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || getSupervisor() != null && getSupervisor().isIdentityUser();
    }

    public boolean isSupervisorChanged() {
        return supervisorChanged;
    }

    @Override
    public boolean isSupervisorEditable() {
        return !isManaged() || isAdminOrSupervisor();
    }

    @Override
    public boolean isSupervisorValid() {
        return getSupervisor() != null && getSupervisor().hasRoleImplicit(getDefaultRequiredRole());
    }

    public void setSupervisor(User supervisor) {
        this.supervisor = supervisor;
    }

    public void setSupervisorChanged(boolean supervisorChanged) {
        this.supervisorChanged = supervisorChanged;
    }

    public void supervisorChangedListener(ValueChangeEvent event) {
        setSupervisorChanged(!(getSupervisor() == null && event.getNewValue() == null || getSupervisor() != null && getSupervisor()
            .equals(event.getNewValue())));
    }
}
