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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Booking;
import org.bfabric.entity.Charge;

@XmlRootElement(name = "booking")
public class XMLBooking extends XMLContainerReferencingEntity {

    @XmlElement
    private String accountid;

    @XmlElement
    private String billingaddress;

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
    private XMLUser bookingissuer;

    @XmlElement
    private Long bookingnr;

    @XmlElement
    private String bookingtypeid;

    @XmlElement
    private List<XMLCharge> charge = new ArrayList<>();

    @XmlElement
    private String costcentre;

    @XmlElement
    private String costcentreid;

    @XmlElement
    private String currency;

    @XmlElement
    private String currencyid;

    @XmlElement
    private XMLDivision division;

    @XmlElement
    private String executionperiodenddate;

    @XmlElement
    private String executionperiodstartdate;

    @XmlElement
    private String financialcenterid;

    @XmlElement
    private XMLInstitute institute;

    @XmlElement
    private Long oldserviceorderbookingid;

    @XmlElement
    private String orderdate;

    @XmlElement
    private Boolean paid;

    @XmlElement
    private String parentclassname;

    @XmlElement
    private String parentid;

    @XmlElement
    private String referencenumber;

    @XmlElement
    private BigDecimal roundingvalue;

    @XmlElement
    private Long sapnumber;

    @XmlElement
    private Long sapnumbernext;

    @XmlElement
    private BigDecimal subtotal;

    @XmlElement
    private BigDecimal tax;

    @XmlElement
    private BigDecimal total;

    @XmlElement
    private BigDecimal totalcharges;

    @XmlElement
    private String vatnumber;

    public XMLBooking() {
    }

    public XMLBooking(Booking entity, boolean reference) {
        super(entity, reference);
    }

    public XMLBooking(Booking entity) {
        super(entity);
        if (entity != null) {
            if (entity.getAccount() != null) {
                setAccountid(String.valueOf(entity.getAccount().getId()));
            }
            if (entity.getBookingDate() != null) {
                setBookingdate(entity.getBookingDate().toString());
            }
            if (entity.getBookingIssuer() != null) {
                setBookingissuer(new XMLUser(entity.getBookingIssuer(), true));
            }
            setBookingnr(entity.getBookingNr());
            if (entity.getBookingType() != null) {
                setBookingtypeid(String.valueOf(entity.getBookingType().getId()));
            }
            if (entity.getCharges() != null) {
                for (Charge charge : entity.getCharges()) {
                    getCharge().add(new XMLCharge(charge, true));
                }
            }
            if (entity.getCostCentre() != null) {
                setCostcentreid(String.valueOf(entity.getCostCentre().getId()));
                setCostcentre(String.valueOf(entity.getCostCentre().getName()));
            }
            if (entity.getCurrency() != null) {
                setCurrencyid(String.valueOf(entity.getCurrency().getIdString()));
                setCurrency(String.valueOf(entity.getCurrency().getName()));
            }
            if (entity.getDivision() != null) {
                setDivision(new XMLDivision(entity.getDivision(), true));
            }
            if (entity.getExecutionPeriodEndDate() != null) {
                setExecutionperiodenddate(entity.getExecutionPeriodEndDate().toString());
            }
            if (entity.getExecutionPeriodStartDate() != null) {
                setExecutionperiodstartdate(entity.getExecutionPeriodStartDate().toString());
            }
            if (entity.getFinancialCenter() != null) {
                setFinancialcenterid(String.valueOf(entity.getFinancialCenter().getId()));
            }
            if (entity.getInstitute() != null) {
                setInstitute(new XMLInstitute(entity.getInstitute(), true));
            }
            if (entity.getOldServiceOrderBookingId() != null) {
                setOldserviceorderbookingid(entity.getOldServiceOrderBookingId());
            }
            if (entity.getOrderDate() != null) {
                setOrderdate(entity.getOrderDate().toString());
            }
            if (entity.getPaid() != null) {
                entity.setPaid(getPaid());
            }
            if (entity.getRoundingValue() != null) {
                setRoundingvalue(entity.getRoundingValue());
            }
            if (entity.getSapNumber() != null) {
                setSapnumber(entity.getSapNumber());
            }
            if (entity.getSapNumberNext() != null) {
                setSapnumbernext(entity.getSapNumberNext());
            }
            if (entity.getSubTotal() != null) {
                setSubtotal(entity.getSubTotal());
            }
            if (entity.getTax() != null) {
                setTax(entity.getTax());
            }
            if (entity.getTotal() != null) {
                setTotal(entity.getTotal());
            }
            if (entity.getTotalCharges() != null) {
                setTotalcharges(entity.getTotalCharges());
            }
            if (entity.getParentId() != null) {
                setParentid(String.valueOf(entity.getParentId()));
            }
            if (entity.getParentClassName() != null) {
                setParentclassname(entity.getParentClassName());
            }
            if (entity.getBillingInfo() != null) {
                if (entity.getBillingInfo().getBillingAddress() != null) {
                    setBillingaddress(entity.getBillingInfo().getBillingAddress());
                }
                if (entity.getBillingInfo().getBillingAddressCity() != null) {
                    setBillingaddresscity(entity.getBillingInfo().getBillingAddressCity());
                }
                if (entity.getBillingInfo().getBillingAddressCountry() != null) {
                    setBillingaddresscountryid(String.valueOf(entity.getBillingInfo().getBillingAddressCountry().getId()));
                }
                if (entity.getBillingInfo().getBillingAddressStreet() != null) {
                    setBillingaddressstreet(entity.getBillingInfo().getBillingAddressStreet());
                }
                if (entity.getBillingInfo().getBillingAddressSupplement() != null) {
                    setBillingaddresssupplement(entity.getBillingInfo().getBillingAddressSupplement());
                }
                if (entity.getBillingInfo().getBillingAddressZip() != null) {
                    setBillingaddresszip(entity.getBillingInfo().getBillingAddressZip());
                }
                if (entity.getBillingInfo().getBillingCustomerFirstName() != null) {
                    setBillingcustomerfirstname(entity.getBillingInfo().getBillingCustomerFirstName());
                }
                if (entity.getBillingInfo().getBillingCustomerLastName() != null) {
                    setBillingcustomerlastname(entity.getBillingInfo().getBillingCustomerLastName());
                }
                if (entity.getBillingInfo().getBillingCustomerTitle() != null) {
                    setBillingcustomertitle(entity.getBillingInfo().getBillingCustomerTitle());
                }
                if (entity.getBillingInfo().getBillingEmail() != null) {
                    setBillingemail(entity.getBillingInfo().getBillingEmail());
                }
                if (entity.getBillingInfo().getReferenceNumber() != null) {
                    setReferencenumber(String.valueOf(entity.getBillingInfo().getReferenceNumber()));
                }
                if (entity.getBillingInfo().getVatNumber() != null) {
                    setVatnumber(String.valueOf(entity.getBillingInfo().getVatNumber()));
                }
            }
        }
    }

    public String getAccountid() {
        return accountid;
    }

    public String getBillingaddress() {
        return billingaddress;
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

    public XMLUser getBookingissuer() {
        return bookingissuer;
    }

    public Long getBookingnr() {
        return bookingnr;
    }

    public String getBookingtypeid() {
        return bookingtypeid;
    }

    public List<XMLCharge> getCharge() {
        return charge;
    }

    public String getCostcentre() {
        return costcentre;
    }

    public String getCostcentreid() {
        return costcentreid;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCurrencyid() {
        return currencyid;
    }

    public XMLDivision getDivision() {
        return division;
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

    public XMLInstitute getInstitute() {
        return institute;
    }

    public Long getOldserviceorderbookingid() {
        return oldserviceorderbookingid;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public Boolean getPaid() {
        return paid;
    }

    public String getParentclassname() {
        return parentclassname;
    }

    public String getParentid() {
        return parentid;
    }

    public String getReferencenumber() {
        return referencenumber;
    }

    public BigDecimal getRoundingvalue() {
        return roundingvalue;
    }

    public Long getSapnumber() {
        return sapnumber;
    }

    public Long getSapnumbernext() {
        return sapnumbernext;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getTotalcharges() {
        return totalcharges;
    }

    public String getVatnumber() {
        return vatnumber;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public void setBillingaddress(String billingaddress) {
        this.billingaddress = billingaddress;
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

    public void setBookingissuer(XMLUser bookingissuer) {
        this.bookingissuer = bookingissuer;
    }

    public void setBookingnr(Long bookingnr) {
        this.bookingnr = bookingnr;
    }

    public void setBookingtypeid(String bookingtypeid) {
        this.bookingtypeid = bookingtypeid;
    }

    public void setCharge(List<XMLCharge> charge) {
        this.charge = charge;
    }

    public void setCostcentre(String costcentre) {
        this.costcentre = costcentre;
    }

    public void setCostcentreid(String costcentreid) {
        this.costcentreid = costcentreid;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCurrencyid(String currencyid) {
        this.currencyid = currencyid;
    }

    public void setDivision(XMLDivision division) {
        this.division = division;
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

    public void setInstitute(XMLInstitute institute) {
        this.institute = institute;
    }

    public void setOldserviceorderbookingid(Long oldserviceorderbookingid) {
        this.oldserviceorderbookingid = oldserviceorderbookingid;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public void setParentclassname(String parentclassname) {
        this.parentclassname = parentclassname;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public void setReferencenumber(String referencenumber) {
        this.referencenumber = referencenumber;
    }

    public void setRoundingvalue(BigDecimal roundingvalue) {
        this.roundingvalue = roundingvalue;
    }

    public void setSapnumber(Long sapnumber) {
        this.sapnumber = sapnumber;
    }

    public void setSapnumbernext(Long sapnumbernext) {
        this.sapnumbernext = sapnumbernext;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setTotalcharges(BigDecimal totalcharges) {
        this.totalcharges = totalcharges;
    }

    public void setVatnumber(String vatnumber) {
        this.vatnumber = vatnumber;
    }
}
