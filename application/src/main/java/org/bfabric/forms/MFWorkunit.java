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

import org.bfabric.Messages;
import org.bfabric.entity.Application;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.WorkunitStatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkunit;

public class MFWorkunit<T extends XMLRequestParameterSaveWorkunit> extends AbstractMF {

    private final Workunit workunit;

    private final T xmlRequestSaveWorkunit;

    public MFWorkunit(Workunit workunit, T xmlRequestSaveWorkunit) {
        this.workunit = workunit;
        this.xmlRequestSaveWorkunit = xmlRequestSaveWorkunit;
    }

    @Override
    public synchronized void apply() throws Exception {
        getWorkunit().setContainer(getContainer());
        getWorkunit().setDataset(getDataset());
        getWorkunit().setInputDataset(getInputDataset());
        getWorkunit().setApplication(getApplication());
        getWorkunit().setArchiving(getArchiving());
        getWorkunit().setDescription(getDescription());
        getWorkunit().setInputResources(getInputResources());
        getWorkunit().setProgress(getProgress());
        getWorkunit().setStatus(getStatus());
        WorkunitStatusEnum status = getWorkunit().getStatus();
        if (!status.equals(WorkunitStatusEnum.FAILED)) {
            getWorkunit().resetStatus();
        }
        getWorkunit().setName(getName());
        getWorkunit().setCustomAttributes(getXmlRequestSaveWorkunit().getCustomattribute());
    }

    public Application getApplication() throws InvalidDataException {
        if (getXmlRequestSaveWorkunit().getApplicationid() != null) {
            return (Application) fetch(Application.class, MFHelper.positiveLongValueOf("applicationid", getXmlRequestSaveWorkunit().getApplicationid()));
        }
        if (getWorkunit().getApplication() == null) {
            throw new InvalidDataException(Messages.get("applicationRequired"));
        }
        return getWorkunit().getApplication();
    }

    public Boolean getArchiving() throws InvalidDataException {
        if (getXmlRequestSaveWorkunit().getArchiving() != null) {
            return MFHelper.booleanValueOf("archiving", getXmlRequestSaveWorkunit().getArchiving());
        }
        return getApplication().getArchiving();
    }

    public Container getContainer() throws InvalidDataException {
        Container container = null;
        if (getXmlRequestSaveWorkunit().getContainerid() != null) {
            container = (Container) fetch(Container.class, MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveWorkunit().getContainerid()));
            if (!container.isExtensible()) {
                throw new InvalidDataException("Container " + getXmlRequestSaveWorkunit().getContainerid() + " is not extensible!");
            }
        }

        if (getWorkunit().getId() == 0) {
            return container;
        } else if (container != null && !container.equals(getWorkunit().getContainer())) {
            throw new InvalidDataException("Changing workunit container not supported yet! " + getXmlRequestSaveWorkunit()
                .getContainerid() + " is different from the current container id " + getWorkunit().getContainer().getId());
        }
        return getWorkunit().getContainer();
    }

    public Dataset getDataset() throws InvalidDataException {
        if (getXmlRequestSaveWorkunit().getDatasetid() != null) {
            long datasetId = MFHelper.positiveLongValueOf("datasetid", getXmlRequestSaveWorkunit().getDatasetid());
            Dataset dataset = (Dataset) fetch(Dataset.class, datasetId);

            if (getWorkunit().getDataset() != null && !getWorkunit().getDataset().equals(dataset)) {
                throw new InvalidDataException("To unlink the current dataset " + getWorkunit().getDataset().getId() + " from the workunit and potentially remove it, use the dataset Web service!");
            }

            if (dataset.getWorkunit() != null && dataset.getWorkunit().getId() != getWorkunit().getId()) {
                throw new InvalidDataException("Dataset " + dataset.getId() + " is already assigned to another workunit!");
            }

            if (!getWorkunit().getContainer().equals(dataset.getContainer())) {
                throw new InvalidDataException("Dataset " + dataset.getId() + " is assigned to another container " + dataset.getContainer().getId());
            }

            if (!dataset.equals(getWorkunit().getDataset())) {
                dataset.setWorkunit(getWorkunit());
                getWorkunit().setDataset(dataset);
            }
        }
        return getWorkunit().getDataset();
    }

    public String getDescription() {
        if (getXmlRequestSaveWorkunit().getDescription() != null) {
            return getXmlRequestSaveWorkunit().getDescription();
        }
        return getWorkunit().getDescription();
    }

    public Dataset getInputDataset() throws Exception {
        if (getXmlRequestSaveWorkunit().getInputdatasetid() != null) {
            return (Dataset) fetch(Dataset.class, MFHelper.positiveLongValueOf("inputdatasetid", getXmlRequestSaveWorkunit().getInputdatasetid()));
        }
        return getWorkunit().getInputDataset();
    }

    public Set<Resource> getInputResources() throws InvalidDataException {
        if (getXmlRequestSaveWorkunit().getInputresourceid() != null && !getWorkunit().isAvailable()) {
            Set<Resource> inputResources = new HashSet<>();
            for (String inputResourceId : getXmlRequestSaveWorkunit().getInputresourceid()) {
                if (StringHelper.isNotEmpty(inputResourceId)) {
                    Resource inputResource = (Resource) fetch(Resource.class, MFHelper.positiveLongValueOf("inputresourceid", inputResourceId));
                    if (!inputResource.getWorkunit().getApplication().isPreceding(getWorkunit().getApplication())) {
                        throw new InvalidDataException("Input Resource " + inputResource.getId() + " was created with application " + inputResource.getWorkunit().getApplication().getName()
                            + ". This is not an input/preceding application of " + getWorkunit().getApplication().getName() + "!");
                    }
                    inputResources.add(inputResource);
                }
            }
            return inputResources;
        }
        return getWorkunit().getInputResources();
    }

    public String getName() {
        if (getXmlRequestSaveWorkunit().getName() != null) {
            return getXmlRequestSaveWorkunit().getName();
        }
        return getWorkunit().getName();
    }

    public String getProgress() {
        if (getXmlRequestSaveWorkunit().getProgress() != null) {
            return getXmlRequestSaveWorkunit().getProgress();
        }
        return getWorkunit().getProgress();
    }

    public WorkunitStatusEnum getStatus() throws InvalidEnumValueException {
        if (getXmlRequestSaveWorkunit().getStatus() != null) {
            WorkunitStatusEnum status = WorkunitStatusEnum.value(getXmlRequestSaveWorkunit().getStatus());
            if (status == null || WorkunitStatusEnum.INVALID.getLabel().equalsIgnoreCase(getXmlRequestSaveWorkunit().getStatus())) {
                throw new InvalidEnumValueException("status", getXmlRequestSaveWorkunit().getStatus(), CollectionHelper.print(WorkunitStatusEnum.getValidEnumValues()));
            }
            return status;
        }
        return getWorkunit().getStatus() != null ? getWorkunit().getStatus() : WorkunitStatusEnum.PENDING;
    }

    public Workunit getWorkunit() {
        return workunit;
    }

    public T getXmlRequestSaveWorkunit() {
        return xmlRequestSaveWorkunit;
    }
}
