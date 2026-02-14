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

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
public class ControlSample extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Size(max = 256)
    @XmlElement
    private String organism;

    @OneToMany(mappedBy = "controlSample")
    @XmlIDREF
    private Set<Sample> samples;

    @Override
    public ControlSample clone() throws CloneNotSupportedException {
        return (ControlSample) super.clone();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.RUNMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getOrganism())) {
            addEntityInfoItem(summary, "organism", getOrganism());
        }
        return summary.toString();
    }

    public String getOrganism() {
        return organism;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    @Override
    public boolean isCreatable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) && super.isCreatable();
    }

    @Override
    public boolean isReadable() {
        return getConfiguration().isLabEnabled() && hasCurrentUserRoleEnum(RoleEnum.RUNREADER) && hasCurrentUserRoleEnum(RoleEnum.LABMANAGER) || super.isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable() && getSamples().isEmpty();
    }

    public void setOrganism(String organism) {
        this.organism = StringHelper.format(organism);
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }
}
