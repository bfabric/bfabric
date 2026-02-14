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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class XMLRequestParameterSaveCharge extends XMLRequestParameterSaveAbstractDescriptionBaseEntity {

    @XmlElement
    protected String notaccounted;

    @XmlElement
    private String additionalprice;

    @XmlElement
    private String basicprice;

    @XmlElement
    private String billable;

    @XmlElement
    private String bookingid;

    @XmlElement
    private String chargerid;

    @XmlElement
    private String containerid;

    @XmlElement
    private String date;

    @XmlElement
    private String discount;

    @XmlElement
    private String discountedprice;

    @XmlElement
    private List<String> instrumentreservationid;

    @XmlElement
    private String notes;

    @XmlElement
    private String offeredchargeid;

    @XmlElement
    private List<String> orderitemid;

    @XmlElement
    private String organizationtypeid;

    @XmlElement
    private String price;

    @XmlElement
    private List<String> sampleid;

    @XmlElement
    private String servicecodeid;

    @XmlElement
    private String serviceid;

    @XmlElement
    private String taxrate;

    @XmlElement
    private String taxtypeid;

    @XmlElement
    private String total;

    @XmlElement
    private String usecurrentservicepricesforofferedcharge;

    public String getAdditionalprice() {
        return additionalprice;
    }

    public String getBasicprice() {
        return basicprice;
    }

    public String getBillable() {
        return billable;
    }

    public String getBookingid() {
        return bookingid;
    }

    public String getChargerid() {
        return chargerid;
    }

    public String getContainerid() {
        return containerid;
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

    public List<String> getInstrumentreservationid() {
        return instrumentreservationid;
    }

    public String getNotaccounted() {
        return notaccounted;
    }

    public String getNotes() {
        return notes;
    }

    public String getOfferedchargeid() {
        return offeredchargeid;
    }

    public List<String> getOrderitemid() {
        return orderitemid;
    }

    public String getOrganizationtypeid() {
        return organizationtypeid;
    }

    public String getPrice() {
        return price;
    }

    public List<String> getSampleid() {
        return sampleid;
    }

    public String getServicecodeid() {
        return servicecodeid;
    }

    public String getServiceid() {
        return serviceid;
    }

    public String getTaxrate() {
        return taxrate;
    }

    public String getTaxtypeid() {
        return taxtypeid;
    }

    public String getTotal() {
        return total;
    }

    public String getUsecurrentservicepricesforofferedcharge() {
        return usecurrentservicepricesforofferedcharge;
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

    public void setBookingid(String bookingid) {
        this.bookingid = bookingid;
    }

    public void setChargerid(String chargerid) {
        this.chargerid = chargerid;
    }

    public void setContainerid(String containerid) {
        this.containerid = containerid;
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

    public void setInstrumentreservationid(List<String> instrumentreservationid) {
        this.instrumentreservationid = instrumentreservationid;
    }

    public void setNotaccounted(String notaccounted) {
        this.notaccounted = notaccounted;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setOfferedchargeid(String offeredchargeid) {
        this.offeredchargeid = offeredchargeid;
    }

    public void setOrderitemid(List<String> orderitemid) {
        this.orderitemid = orderitemid;
    }

    public void setOrganizationtypeid(String organizationtypeid) {
        this.organizationtypeid = organizationtypeid;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setSampleid(List<String> sampleid) {
        this.sampleid = sampleid;
    }

    public void setServicecodeid(String servicecodeid) {
        this.servicecodeid = servicecodeid;
    }

    public void setServiceid(String serviceid) {
        this.serviceid = serviceid;
    }

    public void setTaxrate(String taxrate) {
        this.taxrate = taxrate;
    }

    public void setTaxtypeid(String taxtypeid) {
        this.taxtypeid = taxtypeid;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setUsecurrentservicepricesforofferedcharge(String usecurrentservicepricesforofferedcharge) {
        this.usecurrentservicepricesforofferedcharge = usecurrentservicepricesforofferedcharge;
    }
}