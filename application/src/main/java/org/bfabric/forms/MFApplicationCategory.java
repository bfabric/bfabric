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

import org.bfabric.entity.ApplicationCategory;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveApplicationCategory;

public class MFApplicationCategory extends AbstractMF {

    private final ApplicationCategory applicationCategory;

    private final XMLRequestParameterSaveApplicationCategory xmlRequestSaveApplicationCategory;

    public MFApplicationCategory(ApplicationCategory applicationCategory, XMLRequestParameterSaveApplicationCategory xmlRequestSaveApplicationCategory) {
        this.applicationCategory = applicationCategory;
        this.xmlRequestSaveApplicationCategory = xmlRequestSaveApplicationCategory;
    }

    @Override
    public synchronized void apply() throws Exception {
        getApplicationCategory().setName(getName());
        getApplicationCategory().setDescription(getDescription());
    }

    public ApplicationCategory getApplicationCategory() {
        return applicationCategory;
    }

    public String getDescription() {
        if (getXmlRequestSaveApplicationCategory().getDescription() != null) {
            return getXmlRequestSaveApplicationCategory().getDescription();
        }
        return getApplicationCategory().getDescription();
    }

    public String getName() {
        if (getApplicationCategory().getId() == 0) {
            return getXmlRequestSaveApplicationCategory().getName();
        }
        return getApplicationCategory().getName();
    }

    public XMLRequestParameterSaveApplicationCategory getXmlRequestSaveApplicationCategory() {
        return xmlRequestSaveApplicationCategory;
    }
}