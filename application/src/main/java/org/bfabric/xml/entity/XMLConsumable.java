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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Consumable;

@XmlRootElement(name = "consumable")
public class XMLConsumable extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private String articlenumber;

    @XmlElement
    private String currency;

    @XmlElement
    private String price;

    @XmlElement
    private String supplier;

    @XmlElement
    private String unit;

    public XMLConsumable() {
    }

    public XMLConsumable(Consumable entity, boolean reference) {
        super(entity, reference);
    }

    public XMLConsumable(Consumable consumable) {
        super(consumable);
        if (consumable != null) {
            if (consumable.getArticleNumber() != null) {
                setArticlenumber(consumable.getArticleNumber());
            }
            if (consumable.getPrice() != null && consumable.getCurrency() != null) {
                setPrice(consumable.getPrice().toString());
            }
            if (consumable.getCurrency() != null) {
                setCurrency(consumable.getCurrency().getCode());
            }
            if (consumable.getSupplierName() != null) {
                setSupplier(consumable.getSupplierName());
            }
            if (consumable.getUnit() != null) {
                setUnit(consumable.getUnit());
            }
        }
    }

    public String getArticlenumber() {
        return articlenumber;
    }

    public String getCurrency() {
        return currency;
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

    public void setArticlenumber(String articlenumber) {
        this.articlenumber = articlenumber;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
