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

import org.bfabric.entity.ServiceType;
import org.bfabric.entity.WorkflowTemplate;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowTemplate;

public class MFWorkflowTemplate extends AbstractMF {

    private final WorkflowTemplate workflowTemplate;

    private final XMLRequestParameterSaveWorkflowTemplate xmlRequestSaveWorkflowTemplate;

    public MFWorkflowTemplate(WorkflowTemplate workflowTemplate, XMLRequestParameterSaveWorkflowTemplate xmlRequestSaveWorkflowTemplate) {
        this.workflowTemplate = workflowTemplate;
        this.xmlRequestSaveWorkflowTemplate = xmlRequestSaveWorkflowTemplate;
    }

    @Override
    public synchronized void apply() throws Exception {
        getWorkflowTemplate().setName(getName());
        getWorkflowTemplate().setEnabled(getEnabled());
        getWorkflowTemplate().setDescription(getDescription());
        getWorkflowTemplate().setServiceTypes(getServiceTypes());
    }

    public String getDescription() {
        String ret = getWorkflowTemplate().getDescription();
        if (getXmlRequestSaveWorkflowTemplate().getDescription() != null) {
            ret = getXmlRequestSaveWorkflowTemplate().getDescription();
        }
        return ret;
    }

    public boolean getEnabled() throws InvalidDataException {
        boolean ret = getWorkflowTemplate().isEnabled();
        if (getXmlRequestSaveWorkflowTemplate().getEnabled() != null) {
            ret = MFHelper.booleanValueOf("enabled", getXmlRequestSaveWorkflowTemplate().getEnabled());
        }
        return ret;
    }

    public String getName() {
        if (getXmlRequestSaveWorkflowTemplate().getName() != null) {
            return getXmlRequestSaveWorkflowTemplate().getName();
        }
        return getWorkflowTemplate().getName();
    }

    public Set<ServiceType> getServiceTypes() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowTemplate().getServicetypeid() != null) {
            Set<ServiceType> serviceTypes = new HashSet<>();
            for (String serviceTypeId : getXmlRequestSaveWorkflowTemplate().getServicetypeid()) {
                if (StringHelper.isNotEmpty(serviceTypeId)) {
                    serviceTypes.add((ServiceType) fetch(ServiceType.class, MFHelper.positiveLongValueOf("servicetypeid", serviceTypeId)));
                }
            }
            return serviceTypes;
        }
        return getWorkflowTemplate().getServiceTypes();
    }

    public WorkflowTemplate getWorkflowTemplate() {
        return workflowTemplate;
    }

    public XMLRequestParameterSaveWorkflowTemplate getXmlRequestSaveWorkflowTemplate() {
        return xmlRequestSaveWorkflowTemplate;
    }
}
