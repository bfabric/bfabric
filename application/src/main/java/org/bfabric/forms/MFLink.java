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

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Link;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveLink;

public class MFLink extends AbstractMF {

    private final Link link;

    private final XMLRequestParameterSaveLink xmlRequestSaveLink;

    public MFLink(Link link, XMLRequestParameterSaveLink xmlRequestSaveLink) {
        this.link = link;
        this.xmlRequestSaveLink = xmlRequestSaveLink;
    }

    @Override
    public synchronized void apply() throws Exception {
        getLink().setName(getName());
        getLink().setParent(getParent());
        getLink().setUrl(getUrl());
    }

    public Link getLink() {
        return link;
    }

    public String getName() {
        if (getXmlRequestSaveLink().getName() != null) {
            return getXmlRequestSaveLink().getName();
        }
        return getLink().getName();
    }

    public AbstractEntity getParent() throws InvalidDataException {
        Long parentId = MFHelper.positiveLongValueOf("parentid", getXmlRequestSaveLink().getParentid());
        AbstractEntity ret = getLink().getParent();
        if (getParentClassName() != null && (ret == null || !getParentClassName().equals(ret.getTrimmedClassName()) || !parentId.equals(ret.getId()))) {
            ret = fetch(getParentClassName(), parentId);
        }
        return ret;
    }

    public String getParentClassName() throws InvalidDataException {
        if (getXmlRequestSaveLink().getParentclassname() != null) {
            return MFHelper.getEntityClass("parentclassname", getXmlRequestSaveLink().getParentclassname()).getSimpleName();
        }
        return getLink().getParentClassName();
    }

    public String getUrl() {
        if (getXmlRequestSaveLink().getUrl() != null) {
            return getXmlRequestSaveLink().getUrl();
        }
        return getLink().getUrl();
    }

    public XMLRequestParameterSaveLink getXmlRequestSaveLink() {
        return xmlRequestSaveLink;
    }
}