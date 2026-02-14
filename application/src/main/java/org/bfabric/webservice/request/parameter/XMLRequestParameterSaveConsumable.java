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

public class XMLRequestParameterSaveConsumable extends XMLRequestParameterSaveAbstractDescriptionBaseEntity {

    @XmlElement
    private String articlenumber;

    @XmlElement
    private String enabled;

    @XmlElement
    private String instrumentid;

    @XmlElement
    private String name;

    @XmlElement
    private String price;

    @XmlElement
    private String supplier;

    @XmlElement
    private String unit;

    public String getArticlenumber() {
        return articlenumber;
    }

    public String getInstrumentid() {
        return instrumentid;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getUnit() {
        return unit;
    }

    public String isEnabled() {
        return enabled;
    }

    public void setArticlenumber(String articlenumber) {
        this.articlenumber = articlenumber;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public void setInstrumentid(String instrumentid) {
        this.instrumentid = instrumentid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}