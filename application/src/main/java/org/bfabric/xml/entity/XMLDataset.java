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

package org.bfabric.xml.entity;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Dataset;
import org.bfabric.entity.DatasetAttribute;
import org.bfabric.entity.DatasetItem;
import org.bfabric.entity.Workunit;

@XmlRootElement(name = "dataset")
public class XMLDataset extends XMLContainerReferencingEntity {

    @XmlElement
    private List<XMLDatasetAttribute> attribute = new ArrayList<>();

    @XmlElement
    private String datasettemplateid;

    @XmlElement
    private List<XMLDatasetItem> item = new ArrayList<>();

    @XmlElement
    private String numberofattributes;

    @XmlElement
    private String numberofitems;

    @XmlElement
    private XMLRun run;

    @XmlElement
    private List<XMLWorkunit> succeedingworkunit = new ArrayList<>();

    @XmlElement
    private String typevalid;

    @XmlElement
    private XMLWorkunit workunit;

    public XMLDataset(Dataset entity) {
        super(entity);
        if (entity != null) {
            if (entity.getDatasetTemplate() != null) {
                setDatasettemplateid(String.valueOf(entity.getDatasetTemplate().getIdString()));
            }
            if (entity.getAttributes() != null) {
                setNumberofattributes(String.valueOf(entity.getAttributes().size()));
                for (DatasetAttribute aAttribute : entity.getAttributes()) {
                    getAttribute().add(new XMLDatasetAttribute(aAttribute));
                }
            }
            if (entity.getItems() != null) {
                setNumberofitems(String.valueOf(entity.getItems().size()));
                for (DatasetItem aItem : entity.getItems()) {
                    getItem().add(new XMLDatasetItem(aItem));
                }
            }
            if (entity.isChecked()) {
                setFullDetails(entity);
            }
            if (entity.getWorkunit() != null) {
                setWorkunit(new XMLWorkunit(entity.getWorkunit(), true));
            }
            if (entity.getSucceedingWorkunits() != null) {
                for (Workunit succeedingWorkunit : entity.getSucceedingWorkunits()) {
                    getSucceedingworkunits().add(new XMLWorkunit(succeedingWorkunit, true));
                }
            }
        }
    }

    public XMLDataset() {
    }

    public XMLDataset(Dataset entity, boolean reference) {
        super(entity, reference);
    }

    public List<XMLDatasetAttribute> getAttribute() {
        return attribute;
    }

    public String getDatasettemplateid() {
        return datasettemplateid;
    }

    public List<XMLDatasetItem> getItem() {
        return item;
    }

    public String getNumberofattributes() {
        return numberofattributes;
    }

    public String getNumberofitems() {
        return numberofitems;
    }

    public XMLRun getRun() {
        return run;
    }

    public List<XMLWorkunit> getSucceedingworkunits() {
        return succeedingworkunit;
    }

    public String getTypevalid() {
        return typevalid;
    }

    public XMLWorkunit getWorkunit() {
        return workunit;
    }

    public void setAttribute(List<XMLDatasetAttribute> attribute) {
        this.attribute = attribute;
    }

    public void setDatasettemplateid(String datasettemplateid) {
        this.datasettemplateid = datasettemplateid;
    }

    private void setFullDetails(@NotNull Dataset dataset) {
        if (dataset.getSucceedingWorkunits() != null) {
            for (Workunit succeedingWorkunit : dataset.getSucceedingWorkunits()) {
                getSucceedingworkunits().add(new XMLWorkunit(succeedingWorkunit, true));
            }
        }
        if (dataset.getWorkunit() != null) {
            setWorkunit(new XMLWorkunit(dataset.getWorkunit(), true));
        }
        if (dataset.getRun() != null) {
            setRun(new XMLRun(dataset.getRun(), true));
        }
        setTypevalid(String.valueOf(!dataset.isTypeInvalid()));
    }

    public void setItem(List<XMLDatasetItem> item) {
        this.item = item;
    }

    public void setNumberofattributes(String numberofattributes) {
        this.numberofattributes = numberofattributes;
    }

    public void setNumberofitems(String numberofitems) {
        this.numberofitems = numberofitems;
    }

    public void setRun(XMLRun run) {
        this.run = run;
    }

    public void setSucceedingworkunits(List<XMLWorkunit> succeedingworkunit) {
        this.succeedingworkunit = succeedingworkunit;
    }

    public void setTypevalid(String typevalid) {
        this.typevalid = typevalid;
    }

    public void setWorkunit(XMLWorkunit workunit) {
        this.workunit = workunit;
    }
}
