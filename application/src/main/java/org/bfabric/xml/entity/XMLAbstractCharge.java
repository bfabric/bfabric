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

import org.bfabric.entity.AbstractCharge;

public class XMLAbstractCharge extends XMLAbstractDescriptionBaseEntity {

    @XmlElement
    private String additionalprice;

    @XmlElement
    private String basicprice;

    @XmlElement
    private String billable;

    @XmlElement
    private XMLUser charger;

    @XmlElement
    private String date;

    @XmlElement
    private String discount;

    @XmlElement
    private String discountedprice;

    @XmlElement
    private String notacccounted;

    @XmlElement
    private String notes;

    @XmlElement
    private XMLOrganizationType organizationtype;

    @XmlElement
    private String price;

    @XmlElement
    private String serviceareaname;

    @XmlElement
    private String serviceid;

    @XmlElement
    private String servicename;

    @XmlElement
    private String servicetypename;

    @XmlElement
    private String taxrate;

    @XmlElement
    private String taxtype;

    @XmlElement
    private String taxtypeid;

    @XmlElement
    private String total;

    public XMLAbstractCharge() {

    }

    public XMLAbstractCharge(AbstractCharge entity, boolean reference) {
        super(entity, reference);
    }

    public XMLAbstractCharge(AbstractCharge entity) {
        super(entity);
        if (entity != null) {
            if (entity.getCharger() != null) {
                setCharger(new XMLUser(entity.getCharger(), true));
            }
            if (entity.getAdditionalPrice() != null) {
                setAdditionalprice(entity.getAdditionalPrice().toString());
            }
            if (entity.getBasicPrice() != null) {
                setBasicprice(entity.getBasicPrice().toString());
            }
            setBillable(Boolean.toString(entity.isBillable()));
            if (entity.getDate() != null) {
                setDate(entity.getDate().toString());
            }
            if (entity.getDiscount() != null) {
                setDiscount(entity.getDiscount().toString());
            }
            if (entity.getDiscountedPrice() != null) {
                setDiscountedprice(entity.getDiscountedPrice().toString());
            }
            if (entity.getNotAccounted() != null) {
                setNotacccounted(entity.getNotAccounted().toString());
            }
            if (entity.getNotes() != null) {
                setNotes(entity.getNotes());
            }
            if (entity.getOrganizationType() != null) {
                setOrganizationtype(new XMLOrganizationType(entity.getOrganizationType(), true));
            }
            if (entity.getPrice() != null) {
                setPrice(entity.getPrice().toString());
            }
            if (entity.getService() != null) {
                setServiceid(String.valueOf(entity.getService().getId()));
                setServicename(entity.getServiceName());
                setServicetypename(entity.getServiceName());
                setServiceareaname(entity.getServiceName());
            }
            if (entity.getTaxRate() != null) {
                setTaxrate(entity.getTaxRate().toString());
            }
            if (entity.getTaxType() != null) {
                setTaxtypeid(entity.getTaxType().getIdString());
                setTaxtype(entity.getTaxType().getName());
            }
            if (entity.getTotal() != null) {
                setTotal(entity.getTotal().toString());
            }
        }
    }

    public String getAdditionalprice() {
        return additionalprice;
    }

    public String getBasicprice() {
        return basicprice;
    }

    public String getBillable() {
        return billable;
    }

    public XMLUser getCharger() {
        return charger;
    }

    public String getDate() {
        return date;
    }

    public String getDiscount() {
        return discount;
    }

    public String getDiscountedprice() {
        return discountedprice;
    }

    public String getNotacccounted() {
        return notacccounted;
    }

    public String getNotes() {
        return notes;
    }

    public XMLOrganizationType getOrganizationtype() {
        return organizationtype;
    }

    public String getPrice() {
        return price;
    }

    public String getServiceareaname() {
        return serviceareaname;
    }

    public String getServiceid() {
        return serviceid;
    }

    public String getServicename() {
        return servicename;
    }

    public String getServicetypename() {
        return servicetypename;
    }

    public String getTaxrate() {
        return taxrate;
    }

    public String getTaxtype() {
        return taxtype;
    }

    public String getTaxtypeid() {
        return taxtypeid;
    }

    public String getTotal() {
        return total;
    }

    public void setAdditionalprice(String additionalprice) {
        this.additionalprice = additionalprice;
    }

    public void setBasicprice(String basicprice) {
        this.basicprice = basicprice;
    }

    public void setBillable(String billable) {
        this.billable = billable;
    }

    public void setCharger(XMLUser charger) {
        this.charger = charger;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public void setDiscountedprice(String discountedprice) {
        this.discountedprice = discountedprice;
    }

    public void setNotacccounted(String notacccounted) {
        this.notacccounted = notacccounted;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setOrganizationtype(XMLOrganizationType organizationtype) {
        this.organizationtype = organizationtype;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setServiceareaname(String serviceareaname) {
        this.serviceareaname = serviceareaname;
    }

    public void setServiceid(String serviceid) {
        this.serviceid = serviceid;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public void setServicetypename(String servicetypename) {
        this.servicetypename = servicetypename;
    }

    public void setTaxrate(String taxrate) {
        this.taxrate = taxrate;
    }

    public void setTaxtype(String taxtype) {
        this.taxtype = taxtype;
    }

    public void setTaxtypeid(String taxtypeid) {
        this.taxtypeid = taxtypeid;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}