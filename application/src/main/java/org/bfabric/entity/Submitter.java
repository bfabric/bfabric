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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "Submitter.findAvailable", query = "SELECT a FROM Submitter a WHERE a.valid = true and a.executable IS NOT NULL ORDER BY a.name")
public class Submitter extends AbstractAssociatedToExecutableEntity {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "submitter")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Application> applications = new HashSet<>();

    @OneToMany(mappedBy = "submitter", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Parameter> parameters = new HashSet<>();

    public Set<Application> getApplications() {
        return applications;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.SUBMITTERMANAGER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "valid", isValid());
        addEntityInfoItem(summary, "enabled", isEnabled());
        if (getParameters() != null && !getParameters().isEmpty()) {
            addEntityInfoItem(summary, "parameters", getParameters().size());
        }
        return summary.toString();
    }

    @Override
    public Set<Parameter> getParameters() {
        return parameters;
    }

    public Map<String, String> getParametersKeyValueMap() {
        Map<String, String> parametersKeyValueMap = new HashMap<>();
        for (Parameter parameter : getParameters()) {
            parametersKeyValueMap.put(parameter.getKey(), parameter.getValue());
        }
        return parametersKeyValueMap;
    }

    @Override
    public boolean hasNoDependents() {
        return getApplications().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.SUBMITTERREADER);
    }

    public void setApplications(Set<Application> applications) {
        this.applications = applications;
    }
}
