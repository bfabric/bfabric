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

import org.bfabric.entity.Company;
import org.bfabric.entity.Division;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDivision;

public class MFDivision extends AbstractMF {

    private final Division division;

    private final XMLRequestParameterSaveDivision xmlRequestSaveDivision;

    public MFDivision(Division division, XMLRequestParameterSaveDivision xmlRequestSaveDivision) {
        this.division = division;
        this.xmlRequestSaveDivision = xmlRequestSaveDivision;
    }

    @Override
    public void apply() throws Exception {
        getDivision().setName(getName());
        getDivision().setCompany(getCompany());
    }

    private Company getCompany() throws InvalidDataException {
        if (getXmlRequestSaveDivision().getCompanyid() != null) {
            return (Company) fetch(Company.class, MFHelper.positiveLongValueOf("companyid", getXmlRequestSaveDivision().getCompanyid()));
        }
        return getDivision().getCompany();
    }

    public Division getDivision() {
        return division;
    }

    public String getName() {
        if (getXmlRequestSaveDivision().getName() != null) {
            return getXmlRequestSaveDivision().getName();
        }
        return getDivision().getName();
    }

    public XMLRequestParameterSaveDivision getXmlRequestSaveDivision() {
        return xmlRequestSaveDivision;
    }
}
