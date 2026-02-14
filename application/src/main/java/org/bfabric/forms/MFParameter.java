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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.bfabric.entity.Application;
import org.bfabric.entity.Executable;
import org.bfabric.entity.Parameter;
import org.bfabric.entity.Storage;
import org.bfabric.entity.Submitter;
import org.bfabric.entity.Workunit;
import org.bfabric.entity.WrapperCreator;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.enums.ParameterTypeEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveParameter;

public class MFParameter extends AbstractMF {

    private final Parameter parameter;

    private final XMLRequestParameterSaveParameter xmlRequestSaveParameter;

    public MFParameter(Parameter parameter, XMLRequestParameterSaveParameter xmlRequestSaveParameter) {
        this.parameter = parameter;
        this.xmlRequestSaveParameter = xmlRequestSaveParameter;
    }

    @Override
    public synchronized void apply() throws Exception {
        if (getParameter().getWorkunit() != null) {
            throw new InvalidDataException("Unable to update parameter with id " + getParameter().getId() + " since it is attached to workunit " + getParameter().getWorkunit().getId() + ".");
        }
        Application application = getApplication();
        Executable executable = getExecutable();
        Storage storage = getStorage();
        Submitter submitter = getSubmitter();
        Workunit workunit = getWorkunit();
        WrapperCreator wrapperCreator = getWrapperCreator();
        validateParameterConditions(generateAllPairs(Arrays.asList(application, executable, storage, submitter, workunit, wrapperCreator)));
        if (application != null) {
            getParameter().setApplication(getApplication());
        }
        if (executable != null) {
            getParameter().setExecutable(getExecutable());
        }
        if (storage != null) {
            getParameter().setStorage(getStorage());
        }
        if (submitter != null) {
            getParameter().setSubmitter(getSubmitter());
        }
        if (workunit != null) {
            getParameter().setWorkunit(getWorkunit());
        }
        if (wrapperCreator != null) {
            getParameter().setWrapperCreator(getWrapperCreator());
        }
        getParameter().setDescription(getDescription());
        getParameter().setType(getType());
        getParameter().setEnumeration(getEnumeration());
        getParameter().setKey(getKey());
        getParameter().setLabel(getLabel());
        getParameter().setModifiable(isModifiable());
        getParameter().setRequired(isRequired());
        getParameter().setValue(getValue());
        getParameter().setContext(getContext());

        getWSValidationManager().isValid(getParameter());
    }

    private List<Pair<Object, Object>> generateAllPairs(List<Object> items) {
        List<Pair<Object, Object>> pairs = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                pairs.add(Pair.of(items.get(i), items.get(j)));
            }
        }
        return pairs;
    }

    public Application getApplication() throws InvalidDataException {
        if (getXmlRequestSaveParameter().getApplicationid() != null) {
            return (Application) fetch(Application.class, MFHelper.positiveLongValueOf("applicationid", getXmlRequestSaveParameter().getApplicationid()));
        }
        return getParameter().getApplication();
    }

    public ExecutableContextEnum getContext() throws InvalidEnumValueException, InvalidDataException {
        if (getParameter().getId() == 0) {
            MFHelper.checkNotNull("context", getXmlRequestSaveParameter().getContext());
            ExecutableContextEnum context = ExecutableContextEnum.value(getXmlRequestSaveParameter().getContext());
            if (context != null && context.equals(ExecutableContextEnum.WORKUNIT) && getWorkunit() == null) {
                throw new InvalidDataException("Context " + context.name() + " requires specification of workunitid.");
            }
            return context;
        }
        return getParameter().getContext();
    }

    public String getDescription() {
        if (getXmlRequestSaveParameter().getDescription() != null) {
            return getXmlRequestSaveParameter().getDescription();
        }
        return getParameter().getDescription();
    }

    public List<String> getEnumeration() throws Exception {
        if (getXmlRequestSaveParameter().getEnumeration() != null) {
            if (getParameter().isBoolean()) {
                for (String enumeration : getXmlRequestSaveParameter().getEnumeration()) {
                    if (!Boolean.FALSE.toString().equalsIgnoreCase(enumeration) && !Boolean.TRUE.toString().equalsIgnoreCase(enumeration)) {
                        throw new InvalidDataException("Invalid enumeration: " + enumeration + " for type " + ParameterTypeEnum.BOOLEAN);
                    }
                }
            }
            return getXmlRequestSaveParameter().getEnumeration();
        }
        return getParameter().getEnumeration();
    }

    public Executable getExecutable() throws InvalidDataException {
        if (getXmlRequestSaveParameter().getExecutableid() != null) {
            return (Executable) fetch(Executable.class, MFHelper.positiveLongValueOf("executableid", getXmlRequestSaveParameter().getExecutableid()));
        }
        return getParameter().getExecutable();
    }

    public String getKey() {
        if (getXmlRequestSaveParameter().getKey() != null) {
            return getXmlRequestSaveParameter().getKey();
        }
        return getParameter().getKey();
    }

    public String getLabel() {
        if (getXmlRequestSaveParameter().getLabel() != null) {
            return getXmlRequestSaveParameter().getLabel();
        }
        return getParameter().getLabel();
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Storage getStorage() throws InvalidDataException {
        if (getXmlRequestSaveParameter().getStorageid() != null) {
            return (Storage) fetch(Storage.class, MFHelper.positiveLongValueOf("storageid", getXmlRequestSaveParameter().getStorageid()));
        }
        return getParameter().getStorage();
    }

    public Submitter getSubmitter() throws InvalidDataException {
        if (getXmlRequestSaveParameter().getSubmitterid() != null) {
            return (Submitter) fetch(Submitter.class, MFHelper.positiveLongValueOf("submitterid", getXmlRequestSaveParameter().getSubmitterid()));
        }
        return getParameter().getSubmitter();
    }

    public ParameterTypeEnum getType() throws InvalidEnumValueException {
        if (getXmlRequestSaveParameter().getType() != null) {
            ParameterTypeEnum ret = ParameterTypeEnum.value(getXmlRequestSaveParameter().getType());
            if (getXmlRequestSaveParameter().getValue() == null) {
                getXmlRequestSaveParameter().setValue(getParameter().getValue());
            }
            return ret;
        }
        return getParameter().getType();
    }

    public String getValue() throws Exception {
        if (getXmlRequestSaveParameter().getValue() != null) {
            String value = getXmlRequestSaveParameter().getValue();
            switch (getType()) {
            case BOOLEAN:
                if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
                    return Boolean.FALSE.toString();
                } else if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                    return Boolean.TRUE.toString();
                } else {
                    throw new InvalidDataException("Invalid value: " + value + " for type " + ParameterTypeEnum.BOOLEAN + "!");
                }
            case STRING:
            default:
                return value;
            }
        }
        return getParameter().getValue();
    }

    public Workunit getWorkunit() throws InvalidDataException {
        if (getXmlRequestSaveParameter().getWorkunitid() != null) {
            return (Workunit) fetch(Workunit.class, MFHelper.positiveLongValueOf("workunitid", getXmlRequestSaveParameter().getWorkunitid()));
        }
        return getParameter().getWorkunit();
    }

    public WrapperCreator getWrapperCreator() throws InvalidDataException {
        if (getXmlRequestSaveParameter().getWrappercreatorid() != null) {
            return (WrapperCreator) fetch(WrapperCreator.class, MFHelper.positiveLongValueOf("wrappercreatorid", getXmlRequestSaveParameter().getWrappercreatorid()));
        }
        return getParameter().getWrapperCreator();
    }

    public XMLRequestParameterSaveParameter getXmlRequestSaveParameter() {
        return xmlRequestSaveParameter;
    }

    public boolean isModifiable() throws InvalidDataException {
        if (getXmlRequestSaveParameter().getModifiable() != null) {
            return MFHelper.booleanValueOf("modifiable", getXmlRequestSaveParameter().getModifiable());
        }
        return getParameter().isModifiable();
    }

    public boolean isRequired() throws InvalidDataException {
        if (getXmlRequestSaveParameter().getRequired() != null) {
            return MFHelper.booleanValueOf("required", getXmlRequestSaveParameter().getRequired());
        }
        return getParameter().isRequired();
    }

    private void validateParameterConditions(List<Pair<Object, Object>> conditions) throws InvalidDataException {
        for (Pair<Object, Object> condition : conditions) {
            if (condition.getLeft() != null && condition.getRight() != null) {
                throw new InvalidDataException(String.format("Parameter cannot be associated with both: %s and %s.", condition.getLeft(), condition.getRight()));
            }
        }
    }
}
