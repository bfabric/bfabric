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
import org.bfabric.entity.Institute;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstitute;

public class MFInstitute extends AbstractMF {

    private final Institute institute;

    private final XMLRequestParameterSaveInstitute xmlRequestSaveInstitute;

    public MFInstitute(Institute institute, XMLRequestParameterSaveInstitute xmlRequestSaveInstitute) {
        this.institute = institute;
        this.xmlRequestSaveInstitute = xmlRequestSaveInstitute;

    }

    @Override
    public void apply() throws Exception {
        getInstitute().setName(getName());
        getInstitute().setDepartment(getDepartment());
    }

    private Department getDepartment() throws InvalidDataException {
        if (getXmlRequestSaveInstitute().getDepartmentid() != null) {
            return (Department) fetch(Department.class, MFHelper.positiveLongValueOf("departmentid", getXmlRequestSaveInstitute().getDepartmentid()));
        }
        return getInstitute().getDepartment();
    }

    public Institute getInstitute() {
        return institute;
    }

    public String getName() {
        if (getXmlRequestSaveInstitute().getName() != null) {
            return getXmlRequestSaveInstitute().getName();
        }
        return getInstitute().getName();
    }

    public XMLRequestParameterSaveInstitute getXmlRequestSaveInstitute() {
        return xmlRequestSaveInstitute;
    }
}
