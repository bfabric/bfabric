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

package org.bfabric.webservice.request.parameter;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveOrganizationType extends XMLRequestParameterSaveAbstractNamedBaseEntity {

    @XmlElement
    private String academic;

    @XmlElement
    private String color;

    @XmlElement
    private String domestic;

    @XmlElement
    private String extensible;

    public String getAcademic() {
        return academic;
    }

    public String getColor() {
        return color;
    }

    public String getDomestic() {
        return domestic;
    }

    public String getExtensible() {
        return extensible;
    }

    public void setAcademic(String academic) {
        this.academic = academic;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setDomestic(String domestic) {
        this.domestic = domestic;
    }

    public void setExtensible(String extensible) {
        this.extensible = extensible;
    }
}
