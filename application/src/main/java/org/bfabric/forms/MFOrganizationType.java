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

import org.bfabric.entity.OrganizationType;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOrganizationType;

public class MFOrganizationType extends AbstractMF {

    private final OrganizationType organizationType;

    private final XMLRequestParameterSaveOrganizationType xmlRequestSaveOrganizationType;

    public MFOrganizationType(OrganizationType organizationType, XMLRequestParameterSaveOrganizationType xmlRequestSaveOrganizationType) {
        this.organizationType = organizationType;
        this.xmlRequestSaveOrganizationType = xmlRequestSaveOrganizationType;
    }

    @Override
    public void apply() throws Exception {
        getOrganizationType().setName(getName());
        getOrganizationType().setAcademic(getAcademic());
        getOrganizationType().setDomestic(getDomestic());
        getOrganizationType().setExtensible(getExtensible());
        getOrganizationType().setColor(getColor());
    }

    public Boolean getAcademic() throws InvalidDataException {
        if (getXmlRequestSaveOrganizationType().getAcademic() != null) {
            return MFHelper.booleanValueOf("academic", getXmlRequestSaveOrganizationType().getAcademic());
        }
        return getOrganizationType().isAcademic();
    }

    public String getColor() {
        if (getXmlRequestSaveOrganizationType().getColor() != null) {
            return getXmlRequestSaveOrganizationType().getColor();
        }
        return getOrganizationType().getColor();
    }

    public Boolean getDomestic() throws InvalidDataException {
        if (getXmlRequestSaveOrganizationType().getDomestic() != null) {
            return MFHelper.booleanValueOf("domestic", getXmlRequestSaveOrganizationType().getDomestic());
        }
        return getOrganizationType().isDomestic();
    }

    public Boolean getExtensible() throws InvalidDataException {
        if (getXmlRequestSaveOrganizationType().getExtensible() != null) {
            return MFHelper.booleanValueOf("extensible", getXmlRequestSaveOrganizationType().getExtensible());
        }
        return getOrganizationType().isExtensible();
    }

    public String getName() {
        if (getXmlRequestSaveOrganizationType().getName() != null) {
            return getXmlRequestSaveOrganizationType().getName();
        }
        return getOrganizationType().getName();
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    public XMLRequestParameterSaveOrganizationType getXmlRequestSaveOrganizationType() {
        return xmlRequestSaveOrganizationType;
    }
}
