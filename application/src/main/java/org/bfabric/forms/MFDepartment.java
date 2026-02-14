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

import org.bfabric.entity.Department;
import org.bfabric.entity.Organization;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveDepartment;

public class MFDepartment extends AbstractMF {

    private final Department department;

    private final XMLRequestParameterSaveDepartment xmlRequestSaveDepartment;

    public MFDepartment(Department department, XMLRequestParameterSaveDepartment xmlRequestSaveDepartment) {
        this.department = department;
        this.xmlRequestSaveDepartment = xmlRequestSaveDepartment;
    }

    @Override
    public void apply() throws Exception {
        getDepartment().setName(getName());
        getDepartment().setOrganization(getOrganization());
    }

    public Department getDepartment() {
        return department;
    }

    public String getName() {
        if (getXmlRequestSaveDepartment().getName() != null) {
            return getXmlRequestSaveDepartment().getName();
        }
        return getDepartment().getName();
    }

    private Organization getOrganization() throws InvalidDataException {
        if (getXmlRequestSaveDepartment().getOrganizationid() != null) {
            MFHelper.checkNotNull("organizationid", getXmlRequestSaveDepartment().getOrganizationid());
            return (Organization) fetch(Organization.class, MFHelper.positiveLongValueOf("organizationid", getXmlRequestSaveDepartment().getOrganizationid()));
        }
        return getDepartment().getOrganization();
    }

    public XMLRequestParameterSaveDepartment getXmlRequestSaveDepartment() {
        return xmlRequestSaveDepartment;
    }

}
