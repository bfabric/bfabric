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

import org.bfabric.entity.Resource;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.enums.WorkunitStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterChangeWorkunitStatus;

public class MFWorkunitChangeStatus extends AbstractMF {

    private final Workunit workunit;

    private final XMLRequestParameterChangeWorkunitStatus xmlRequestChangeStatusWorkunit;

    public MFWorkunitChangeStatus(Workunit workunit, XMLRequestParameterChangeWorkunitStatus xmlRequestChangeStatusWorkunit) {
        this.workunit = workunit;
        this.xmlRequestChangeStatusWorkunit = xmlRequestChangeStatusWorkunit;
    }

    @Override
    public synchronized void apply() throws Exception {
        getWorkunit().setDescription(getDescription());
        getWorkunit().setProgress(getProgress());
        getWorkunit().setStatus(getStatus());
        getWorkunit().setName(getName());
    }

    public String getDescription() {
        if (getXmlRequestChangeStatusWorkunit().getDescription() != null) {
            return getXmlRequestChangeStatusWorkunit().getDescription();
        }
        return getWorkunit().getDescription();
    }

    public String getName() {
        if (getXmlRequestChangeStatusWorkunit().getName() != null) {
            return getXmlRequestChangeStatusWorkunit().getName();
        }
        return getWorkunit().getName();
    }

    public String getProgress() {
        if (getXmlRequestChangeStatusWorkunit().getProgress() != null) {
            return getXmlRequestChangeStatusWorkunit().getProgress();
        }
        return getWorkunit().getProgress();
    }

    public Set<Resource> getResources() throws InvalidDataException {
        if (getXmlRequestChangeStatusWorkunit().getResourceid() != null && !getXmlRequestChangeStatusWorkunit().getResourceid().isEmpty()) {
            Set<Resource> resources = new HashSet<>();
            for (Long resourceId : getXmlRequestChangeStatusWorkunit().getResourceid()) {
                Resource resource = getIdentityService().find(Resource.class, resourceId);
                if (resource == null || resource.getWorkunit().getId() != getWorkunit().getId()) {
                    throw new InvalidDataException("The workunit does not contain a resource with id " + resourceId);
                }
                for (Resource workunitResource : getWorkunit().getResources()) {
                    if (workunitResource.getId() == resourceId) {
                        resources.add(workunitResource);
                    }
                }
            }
            return resources;
        }
        return getWorkunit().getResources();
    }

    public WorkunitStatusEnum getStatus() throws InvalidEnumValueException, InvalidDataException {
        if (getXmlRequestChangeStatusWorkunit().getStatus() != null) {
            if (getXmlRequestChangeStatusWorkunit().getStatus().toLowerCase().equals(ResourceStatusEnum.INVALID.getLabel())) {
                throw new InvalidEnumValueException("status", getXmlRequestChangeStatusWorkunit().getStatus(), CollectionHelper.print(ResourceStatusEnum.getValidEnumValues()));
            }
            ResourceStatusEnum status = ResourceStatusEnum.value(getXmlRequestChangeStatusWorkunit().getStatus());
            if (status != null) {
                for (Resource resource : getResources()) {
                    resource.setStatus(status);
                }
                getWorkunit().resetStatus();
                return getWorkunit().getStatus();
            }
        }
        return getWorkunit().getStatus() != null ? getWorkunit().getStatus() : WorkunitStatusEnum.PENDING;
    }

    public Workunit getWorkunit() {
        return workunit;
    }

    public XMLRequestParameterChangeWorkunitStatus getXmlRequestChangeStatusWorkunit() {
        return xmlRequestChangeStatusWorkunit;
    }
}