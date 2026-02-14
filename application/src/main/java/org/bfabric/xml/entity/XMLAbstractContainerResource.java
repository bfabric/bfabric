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

import org.bfabric.entity.AbstractContainerResource;
import org.bfabric.util.DateUtils;

public class XMLAbstractContainerResource extends XMLAbstractResource {

    @XmlElement
    private String archiveexpirationdate;

    @XmlElement
    private XMLContainer container;

    @XmlElement
    private String expirationdate;

    @XmlElement
    private String report;

    @XmlElement
    private XMLSample sample;

    @XmlElement
    private List<String> uri = new ArrayList<>();

    @XmlElement
    private String url;

    public XMLAbstractContainerResource() {
    }

    public XMLAbstractContainerResource(AbstractContainerResource containerResource) {
        super(containerResource);
        if (containerResource != null) {
            if (containerResource.getExpirationDate() != null) {
                setExpirationdate(DateUtils.getDateAsFormattedString(containerResource.getExpirationDate()));
            }
            if (containerResource.getArchiveExpirationDate() != null) {
                setArchiveexpirationdate(DateUtils.getDateAsFormattedString(containerResource.getArchiveExpirationDate()));
            }
            if (containerResource.getContainer() != null) {
                setContainer(new XMLContainer(containerResource.getContainer(), true));
            }
            if (containerResource.getReport() != null) {
                setReport(containerResource.getReport());
            }
            if (containerResource.getSample() != null) {
                setSample(new XMLSample(containerResource.getSample(), true));
            }
            if (containerResource.getUri() != null) {
                setUrl(containerResource.getUri());
            }
            if (containerResource.isChecked()) {
                setFullDetails(containerResource);
            }
        }
    }

    public XMLAbstractContainerResource(AbstractContainerResource entity, boolean reference) {
        super(entity, reference);
    }

    public String getArchiveexpirationdate() {
        return archiveexpirationdate;
    }

    public XMLContainer getContainer() {
        return container;
    }

    public String getExpirationdate() {
        return expirationdate;
    }

    public String getReport() {
        return report;
    }

    public XMLSample getSample() {
        return sample;
    }

    public List<String> getUri() {
        return uri;
    }

    public String getUrl() {
        return url;
    }

    public void setArchiveexpirationdate(String archiveexpirationdate) {
        this.archiveexpirationdate = archiveexpirationdate;
    }

    public void setContainer(XMLContainer container) {
        this.container = container;
    }

    public void setExpirationdate(String expirationdate) {
        this.expirationdate = expirationdate;
    }

    private void setFullDetails(@NotNull AbstractContainerResource containerResource) {
        if (containerResource.getUrisByAccessType() != null) {
            setUri(containerResource.getUrisByAccessType());
        }
    }

    public void setReport(String report) {
        this.report = report;
    }

    public void setSample(XMLSample sample) {
        this.sample = sample;
    }

    public void setUri(List<String> uri) {
        this.uri = uri;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
