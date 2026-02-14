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

import org.bfabric.entity.Executable;
import org.bfabric.entity.Parameter;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.enums.ExecutableStatusEnum;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.WorkunitStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveExecutable;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveParameter;

public class MFExecutable extends AbstractMF {

    private final Executable executable;

    private final XMLRequestParameterSaveExecutable xmlRequestSaveExecutable;

    public MFExecutable(Executable executable, XMLRequestParameterSaveExecutable xmlRequestSaveExecutable) {
        this.executable = executable;
        this.xmlRequestSaveExecutable = xmlRequestSaveExecutable;
    }

    @Override
    public synchronized void apply() throws Exception {
        getExecutable().setName(getName());
        getExecutable().setContext(getContext());
        getExecutable().setProgram(getProgram());
        getExecutable().setSupervisor(getSupervisor());
        getExecutable().setEnabled(isEnabled());
        getExecutable().setValid(isValid());
        getExecutable().setWorkunit(getWorkunit());
        getExecutable().setDescription(getDescription());
        getExecutable().setMasterExecutable(getMasterExecutable());
        getExecutable().setWrapperCreatorExecutable(getWrapperCreatorExecutable());
        getExecutable().setVersion(getVersion());
        getExecutable().setPredecessor(getPredecessor());
        getExecutable().setUploadedFile(getUploadedFile());
        getExecutable().changeStatus(getStatus());
        setParameters();
        getWSValidationManager().isValid(getExecutable());
    }

    public ExecutableContextEnum getContext() throws InvalidEnumValueException, InvalidDataException {
        if (getExecutable().getId() == 0) {
            MFHelper.checkNotNull("context", getXmlRequestSaveExecutable().getContext());
            ExecutableContextEnum executableContext = ExecutableContextEnum.value(getXmlRequestSaveExecutable().getContext());

            if (executableContext != null) {
                if (executableContext.equals(ExecutableContextEnum.WORKUNIT)) {
                    if (getWorkunit() == null) {
                        throw new InvalidDataException("Context " + executableContext.name() + " requires specification of workunitid.");
                    }
                } else if (getXmlRequestSaveExecutable().getWorkunitid() != null) {
                    throw new InvalidDataException(executableContext.name() + " executable cannot be linked with a workunit!");
                }

                if (executableContext.equals(ExecutableContextEnum.MASTER) && !executable.hasCurrentUserRoleEnum(RoleEnum.MASTEREXECUTABLEMANAGER)) {
                    throw new InvalidDataException("Upload of master executable requires role admin or masterExecutableManager.");
                }
            }

            return executableContext;
        }
        return getExecutable().getExecutableContext();
    }

    public String getDescription() {
        if (getXmlRequestSaveExecutable().getDescription() != null) {
            return getXmlRequestSaveExecutable().getDescription();
        }
        return getExecutable().getDescription();
    }

    public Executable getExecutable() {
        return executable;
    }

    public Executable getMasterExecutable() throws InvalidDataException {
        if (getXmlRequestSaveExecutable().getMasterexecutableid() != null) {
            Executable masterExecutable = (Executable) fetch(Executable.class, MFHelper.positiveLongValueOf("masterexecutableid", getXmlRequestSaveExecutable().getMasterexecutableid()));
            if (!masterExecutable.isContextMaster()) {
                throw new InvalidDataException("Executable " + getXmlRequestSaveExecutable().getMasterexecutableid() + " has not the context " + ExecutableContextEnum.MASTER);
            }
            return masterExecutable;
        }
        return getExecutable().getMasterExecutable();
    }

    public String getName() {
        if (getXmlRequestSaveExecutable().getName() != null) {
            return getXmlRequestSaveExecutable().getName();
        }
        return getExecutable().getName();
    }

    public Executable getPredecessor() throws InvalidDataException {
        if (getXmlRequestSaveExecutable().getPredecessorid() != null) {
            Executable predecessor = (Executable) fetch(Executable.class, MFHelper.positiveLongValueOf("predecessorid", getXmlRequestSaveExecutable().getPredecessorid()));
            if (!predecessor.getContext().equals(getExecutable().getContext())) {
                throw new InvalidDataException("The context of predecessor " + getXmlRequestSaveExecutable().getPredecessorid() + " is not equal to the context of the executable " + getExecutable()
                    .getName());
            }
            return predecessor;
        }
        return getExecutable().getPredecessor();
    }

    public String getProgram() {
        if (getXmlRequestSaveExecutable().getProgram() != null) {
            return getXmlRequestSaveExecutable().getProgram();
        }
        return getExecutable().getProgram();
    }

    public ResourceStatusEnum getStatus() throws InvalidEnumValueException {
        if (getXmlRequestSaveExecutable().getStatus() != null) {
            return ResourceStatusEnum.value(ExecutableStatusEnum.value(getXmlRequestSaveExecutable().getStatus()).getLabel());
        }
        return getExecutable().getStatus();
    }

    public User getSupervisor() throws InvalidDataException {
        if (getXmlRequestSaveExecutable().getSupervisorid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("supervisorid", getXmlRequestSaveExecutable().getSupervisorid()));
        }
        return getExecutable().getSupervisor() != null ? getExecutable().getSupervisor() : getIdentityService().getCurrentUser();
    }

    public BfabricUploadedFile getUploadedFile() throws InvalidDataException {
        if (getXmlRequestSaveExecutable().getBase64() != null) {
            BfabricUploadedFile bfabricUploadedFile = decodeAndCreateFile(getXmlRequestSaveExecutable().getBase64(), getName());
            if (bfabricUploadedFile == null && getExecutable().getRelativePath() != null) {
                RepositoryHelper.removeImport(getExecutable());
            }
            return bfabricUploadedFile;
        }
        return null;
    }

    public String getVersion() {
        if (getXmlRequestSaveExecutable().getVersion() != null) {
            return getXmlRequestSaveExecutable().getVersion();
        }
        return getExecutable().getVersion();
    }

    public Workunit getWorkunit() throws InvalidDataException {
        if (getXmlRequestSaveExecutable().getWorkunitid() != null) {
            Workunit workunit = (Workunit) fetch(Workunit.class, MFHelper.positiveLongValueOf("workunitid", getXmlRequestSaveExecutable().getWorkunitid()));

            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("The executable cannot be attached to the workunit with id ").append(getXmlRequestSaveExecutable().getWorkunitid());

            if (!workunit.getContainer().isExtensible()) {
                errorMsg.append(" since it belongs to a non-extensible container");
                throw new InvalidDataException(errorMsg.toString());
            }

            if (workunit.isAvailable()) {
                errorMsg.append(" since its status is ").append(WorkunitStatusEnum.AVAILABLE).append(".");
                throw new InvalidDataException(errorMsg.toString());
            }
            return workunit;
        }
        return getExecutable().getWorkunit();
    }

    public Executable getWrapperCreatorExecutable() throws InvalidDataException {
        if (getXmlRequestSaveExecutable().getWrappercreatorexecutableid() != null) {
            Executable wrapperCreatorExecutable = (Executable) fetch(Executable.class, MFHelper.positiveLongValueOf("wrappercreatorexecutableid", getXmlRequestSaveExecutable().getWrappercreatorexecutableid()));
            if (!wrapperCreatorExecutable.isContextWrapperCreator()) {
                throw new InvalidDataException("Executable " + getXmlRequestSaveExecutable().getWrappercreatorexecutableid() + " has not the context " + ExecutableContextEnum.WRAPPERCREATOR);
            }
            return wrapperCreatorExecutable;
        }
        return getExecutable().getWrapperCreatorExecutable();
    }

    public XMLRequestParameterSaveExecutable getXmlRequestSaveExecutable() {
        return xmlRequestSaveExecutable;
    }

    public boolean isEnabled() throws InvalidDataException {
        if (getXmlRequestSaveExecutable().getEnabled() != null) {
            return MFHelper.booleanValueOf("enabled", getXmlRequestSaveExecutable().getEnabled());
        }
        return getExecutable().isEnabled();
    }

    public boolean isValid() throws InvalidDataException {
        if (getXmlRequestSaveExecutable().getValid() != null) {
            return MFHelper.booleanValueOf("valid", getXmlRequestSaveExecutable().getValid());
        }
        return getExecutable().isValid();
    }

    public void setParameters() throws Exception {
        if (getXmlRequestSaveExecutable().getParameter() != null) {
            Set<Parameter> parameters = new HashSet<>();
            for (XMLRequestParameterSaveParameter xmlRequestSaveParameter : getXmlRequestSaveExecutable().getParameter()) {
                if (!xmlRequestSaveParameter.isEmpty()) {
                    Parameter parameterToAdd;
                    MFParameter mfParameter;
                    if (xmlRequestSaveParameter.getId() != null) {
                        Parameter parameter = (Parameter) fetch(Parameter.class, MFHelper.positiveLongValueOf("parameterid", String.valueOf(xmlRequestSaveParameter.getId())));
                        parameterToAdd = parameter.clone();
                        parameterToAdd.setExecutable(getExecutable());
                        xmlRequestSaveParameter.setContext(getContext().toString());
                    } else {
                        parameterToAdd = new Parameter(getContext(), getExecutable());
                    }
                    mfParameter = new MFParameter(parameterToAdd, xmlRequestSaveParameter);
                    mfParameter.apply();
                    parameters.add(parameterToAdd);
                }
            }
            getExecutable().clearAndSetParameters(parameters);
        }
    }
}
