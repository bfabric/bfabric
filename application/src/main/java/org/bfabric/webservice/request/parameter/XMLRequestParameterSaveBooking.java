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

public class XMLRequestParameterSaveBooking extends XMLRequestParameterSaveAbstractDescriptionNamedBaseEntity {

    @XmlElement
    private String accountid;

    @XmlElement
    private String billingaddresscity;

    @XmlElement
    private String billingaddresscountryid;

    @XmlElement
    private String billingaddressstreet;

    @XmlElement
    private String billingaddresssupplement;

    @XmlElement
    private String billingaddresszip;

    @XmlElement
    private String billingcustomerfirstname;

    @XmlElement
    private String billingcustomerlastname;

    @XmlElement
    private String billingcustomertitle;

    @XmlElement
    private String billingemail;

    @XmlElement
    private String bookingdate;

    @XmlElement
    private String bookingissuerid;

    @XmlElement
    private String bookingnr;

    @XmlElement(name = "chargeid")
    private List<String> charges;

    @XmlElement(required = true)
    private String containerid;

    @XmlElement
    private String costcentreid;

    @XmlElement
    private String currencyid;

    @XmlElement
    private String divisionid;

    @XmlElement
    private String executionperiodenddate;

    @XmlElement
    private String executionperiodstartdate;

    @XmlElement
    private String financialcenterid;

    @XmlElement
    private String instituteid;

    @XmlElement
    private String oldserviceorderbookingid;

    @XmlElement
    private String orderdate;

    @XmlElement
    private String paid;

    @XmlElement
    private String referencenumber;

    @XmlElement
    private String roundingvalue;

    @XmlElement
    private String sapnumber;

    @XmlElement
    private String sapnumbernext;

    @XmlElement
    private String subtotal;

    @XmlElement
    private String tax;

    @XmlElement
    private String total;

    @XmlElement
    private String totalcharges;

    @XmlElement
    private String vatnumber;

    public String getAccountid() {
        return accountid;
    }

    public String getBillingaddresscity() {
        return billingaddresscity;
    }

    public String getBillingaddresscountryid() {
        return billingaddresscountryid;
    }

    public String getBillingaddressstreet() {
        return billingaddressstreet;
    }

    public String getBillingaddresssupplement() {
        return billingaddresssupplement;
    }

    public String getBillingaddresszip() {
        return billingaddresszip;
    }

    public String getBillingcustomerfirstname() {
        return billingcustomerfirstname;
    }

    public String getBillingcustomerlastname() {
        return billingcustomerlastname;
    }

    public String getBillingcustomertitle() {
        return billingcustomertitle;
    }

    public String getBillingemail() {
        return billingemail;
    }

    public String getBookingdate() {
        return bookingdate;
    }

    public String getBookingissuerid() {
        return bookingissuerid;
    }

    public String getBookingnr() {
        return bookingnr;
    }

    public List<String> getCharges() {
        return charges;
    }

    public String getContainerid() {
        return containerid;
    }

    public String getCostcentreid() {
        return costcentreid;
    }

    public String getCurrencyid() {
        return currencyid;
    }

    public String getDivisionid() {
        return divisionid;
    }

    public String getExecutionperiodenddate() {
        return executionperiodenddate;
    }

    public String getExecutionperiodstartdate() {
        return executionperiodstartdate;
    }

    public String getFinancialcenterid() {
        return financialcenterid;
    }

    public String getInstituteid() {
        return instituteid;
    }

    public String getOldserviceorderbookingid() {
        return oldserviceorderbookingid;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public String getPaid() {
        return paid;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public String getRoundingvalue() {
        return roundingvalue;
    }

    public String getSapnumber() {
        return sapnumber;
    }

    public String getSapnumbernext() {
        return sapnumbernext;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public String getTax() {
        return tax;
    }

    public String getTotal() {
        return total;
    }

    public String getTotalcharges() {
        return totalcharges;
    }

    public String getVatnumber() {
        return vatnumber;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public void setBillingaddresscity(String billingaddresscity) {
        this.billingaddresscity = billingaddresscity;
    }

    public void setBillingaddresscountryid(String billingaddresscountryid) {
        this.billingaddresscountryid = billingaddresscountryid;
    }

    public void setBillingaddressstreet(String billingaddressstreet) {
        this.billingaddressstreet = billingaddressstreet;
    }

    public void setBillingaddresssupplement(String billingaddresssupplement) {
        this.billingaddresssupplement = billingaddresssupplement;
    }

    public void setBillingaddresszip(String billingaddresszip) {
        this.billingaddresszip = billingaddresszip;
    }

    public void setBillingcustomerfirstname(String billingcustomerfirstname) {
        this.billingcustomerfirstname = billingcustomerfirstname;
    }

    public void setBillingcustomerlastname(String billingcustomerlastname) {
        this.billingcustomerlastname = billingcustomerlastname;
    }

    public void setBillingcustomertitle(String billingcustomertitle) {
        this.billingcustomertitle = billingcustomertitle;
    }

    public void setBillingemail(String billingemail) {
        this.billingemail = billingemail;
    }

    public void setBookingdate(String bookingdate) {
        this.bookingdate = bookingdate;
    }

    public void setBookingissuerid(String bookingissuerid) {
        this.bookingissuerid = bookingissuerid;
    }

    public void setBookingnr(String bookingnr) {
        this.bookingnr = bookingnr;
    }

    public void setCharges(List<String> charges) {
        this.charges = charges;
    }

    public void setContainerid(String containerid) {
        this.containerid = containerid;
    }

    public void setCostcentreid(String costcentreid) {
        this.costcentreid = costcentreid;
    }

    public void setCurrencyid(String currencyid) {
        this.currencyid = currencyid;
    }

    public void setDivisionid(String divisionid) {
        this.divisionid = divisionid;
    }

    public void setExecutionperiodenddate(String executionperiodenddate) {
        this.executionperiodenddate = executionperiodenddate;
    }

    public void setExecutionperiodstartdate(String executionperiodstartdate) {
        this.executionperiodstartdate = executionperiodstartdate;
    }

    public void setFinancialcenterid(String financialcenterid) {
        this.financialcenterid = financialcenterid;
    }

    public void setInstituteid(String instituteid) {
        this.instituteid = instituteid;
    }

    public void setOldserviceorderbookingid(String oldserviceorderbookingid) {
        this.oldserviceorderbookingid = oldserviceorderbookingid;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public void setRoundingvalue(String roundingvalue) {
        this.roundingvalue = roundingvalue;
    }

    public void setSapnumber(String sapnumber) {
        this.sapnumber = sapnumber;
    }

    public void setSapnumbernext(String sapnumbernext) {
        this.sapnumbernext = sapnumbernext;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setTotalcharges(String totalcharges) {
        this.totalcharges = totalcharges;
    }

    public void setVatnumber(String vatnumber) {
        this.vatnumber = vatnumber;
    }
}
