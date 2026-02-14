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
import org.bfabric.entity.Resource;
import org.bfabric.entity.Workunit;

@XmlRootElement(name = "resource")
public class XMLResource extends XMLAbstractContainerResource {

    @XmlElement
    private XMLResource inputresource;

    @XmlElement
    private String junk;

    @XmlElement
    private String junkcomment;

    @XmlElement
    private List<XMLDataset> succeedingdataset = new ArrayList<>();

    @XmlElement
    private List<XMLWorkunit> succeedingworkunit = new ArrayList<>();

    @XmlElement
    private XMLWorkunit workunit;

    public XMLResource() {
    }

    public XMLResource(Resource entity, boolean reference) {
        super(entity, reference);
    }

    public XMLResource(Resource resource) {
        super(resource);
        if (resource != null) {
            if (resource.getInputResource() != null) {
                setInputresource(new XMLResource(resource.getInputResource(), true));
            }
            setJunk(Boolean.toString(resource.isJunk()));
            if (resource.getJunkComment() != null) {
                setJunkcomment(resource.getJunkComment());
            }
            if (resource.getWorkunit() != null) {
                setWorkunit(new XMLWorkunit(resource.getWorkunit(), true));
            }
            if (resource.isChecked()) {
                setFullDetails(resource);
            }
        }
    }

    public XMLResource getInputresource() {
        return inputresource;
    }

    public String getJunk() {
        return junk;
    }

    public String getJunkcomment() {
        return junkcomment;
    }

    public List<XMLDataset> getSucceedingdataset() {
        return succeedingdataset;
    }

    public List<XMLWorkunit> getSucceedingworkunit() {
        return succeedingworkunit;
    }

    public XMLWorkunit getWorkunit() {
        return workunit;
    }

    private void setFullDetails(@NotNull Resource resource) {
        if (resource.getSucceedingDatasets() != null) {
            for (Dataset dataset : resource.getSucceedingDatasets()) {
                getSucceedingdataset().add(new XMLDataset(dataset, true));
            }
        }
        if (resource.getSucceedingWorkunits() != null) {
            for (Workunit succeedingWorkunit : resource.getSucceedingWorkunits()) {
                getSucceedingworkunit().add(new XMLWorkunit(succeedingWorkunit, true));
            }
        }
    }

    public void setInputresource(XMLResource inputresource) {
        this.inputresource = inputresource;
    }

    public void setJunk(String junk) {
        this.junk = junk;
    }

    public void setJunkcomment(String junkcomment) {
        this.junkcomment = junkcomment;
    }

    public void setSucceedingdataset(List<XMLDataset> succeedingdataset) {
        this.succeedingdataset = succeedingdataset;
    }

    public void setSucceedingworkunit(List<XMLWorkunit> succeedingworkunit) {
        this.succeedingworkunit = succeedingworkunit;
    }

    public void setWorkunit(XMLWorkunit workunit) {
        this.workunit = workunit;
    }
}
