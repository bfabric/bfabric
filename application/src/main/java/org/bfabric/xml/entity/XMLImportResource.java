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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.ImportResource;
import org.bfabric.entity.Workunit;
import org.bfabric.util.DateUtils;

@XmlRootElement(name = "importresource")
public class XMLImportResource extends XMLAbstractContainerResource {

    @XmlElement
    private XMLApplication application;

    @XmlElement
    private String filedate;

    @XmlElement
    private List<XMLWorkunit> workunit = new ArrayList<>();

    public XMLImportResource() {
    }

    public XMLImportResource(ImportResource importResource) {
        super(importResource);
        if (importResource != null) {
            if (importResource.getApplication() != null) {
                setApplication(new XMLApplication(importResource.getApplication(), true));
            }
            if (importResource.getFileDate() != null) {
                setFiledate(DateUtils.getDateAsFormattedString(importResource.getFileDate()));
            }
            if (importResource.getWorkunits() != null) {
                for (Workunit aWorkunit : importResource.getWorkunits()) {
                    getWorkunit().add(new XMLWorkunit(aWorkunit, true));
                }
            }
            if (importResource.getUrl() != null) {
                setUrl(importResource.getUrl());
            }
        }
    }

    public XMLImportResource(ImportResource entity, boolean reference) {
        super(entity, reference);
    }

    public XMLApplication getApplication() {
        return application;
    }

    public String getFiledate() {
        return filedate;
    }

    public List<XMLWorkunit> getWorkunit() {
        return workunit;
    }

    public void setApplication(XMLApplication application) {
        this.application = application;
    }

    public void setFiledate(String filedate) {
        this.filedate = filedate;
    }

    public void setWorkunit(List<XMLWorkunit> workunit) {
        this.workunit = workunit;
    }
}
