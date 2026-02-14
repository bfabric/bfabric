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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.entity.api.HasParameters;
import org.bfabric.entity.api.ShowScreen;

@MappedSuperclass
public abstract class AbstractAssociatedToExecutableEntity extends AbstractSupervisorNamedBaseEntity implements ShowScreen, HasParameters {

    private static final long serialVersionUID = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executableid")
    @XmlIDREF
    protected Executable executable;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    protected boolean valid = true;

    public AbstractAssociatedToExecutableEntity() {
    }

    public Set<Parameter> calculateParameters(Executable executable, Set<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            parameter.setInUse(false);
        }
        if (executable != null) {
            for (Parameter executableParameter : executable.getParameters()) {
                boolean equivalentFound = false;
                for (Parameter parameter : parameters) {
                    if (parameter.isEquivalent(executableParameter)) {
                        equivalentFound = true;
                        parameter.setInUse(true);
                        if (!executableParameter.isModifiable()) {
                            parameter.setEnumeration(executableParameter.getEnumeration());
                            parameter.setLabel(executableParameter.getLabel());
                            parameter.setType(executableParameter.getType());
                            parameter.setValue(executableParameter.getValue());
                            parameter.setRequired(executableParameter.isRequired());
                            parameter.setDescription(executableParameter.getDescription());
                            parameter.setModifiable(executableParameter.isModifiable());
                        }
                        parameter.setParentAllowsModification(executableParameter.isModifiable());
                        break;
                    }
                }
                if (!equivalentFound) {
                    // The clone operation sets the inUse flag to true.
                    parameters.add(executableParameter.clonePartial(this));
                }
            }
        }
        return parameters;
    }

    @Override
    public Set<Parameter> cloneParameters(Set<Parameter> parameters) {
        Set<Parameter> clonedParameters = new HashSet<>();
        for (Parameter parameter : parameters) {
            Parameter clonedParameter = parameter.clonePartial(this);
            clonedParameters.add(clonedParameter);
        }
        return clonedParameters;
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        for (Parameter parameter : getParameters()) {
            parameter.setParent(this);
        }
    }

    public Executable getExecutable() {
        return executable;
    }

    public Set<Parameter> getParametersInUse() {
        Set<Parameter> parametersInUse = new HashSet<>();
        for (Parameter parameter : getParameters()) {
            if (parameter.isInUse()) {
                parametersInUse.add(parameter);
            }
        }
        return parametersInUse;
    }

    public abstract boolean hasNoDependents();

    @Override
    public boolean isDeletable() {
        return super.isDeletable() && isAdminOrSupervisor() && hasNoDependents();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isValid() {
        return valid;
    }

    public void setExecutable(Executable executable) {
        this.executable = executable;
        getParameters().addAll(calculateParameters(executable, getParameters()));
    }

    @Override
    public void setParameters(Set<Parameter> parameters) {
        getParameters().clear();
        if (parameters != null && !parameters.isEmpty()) {
            getParameters().addAll(cloneParameters(parameters));
        }
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
