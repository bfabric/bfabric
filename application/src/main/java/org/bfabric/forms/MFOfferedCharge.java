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

import java.math.BigDecimal;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.entity.Offer;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.entity.OrganizationType;
import org.bfabric.entity.Service;
import org.bfabric.entity.TaxType;
import org.bfabric.entity.User;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.TaxTypeService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOfferedCharge;

public class MFOfferedCharge extends AbstractMF {

    private final OfferedCharge offeredCharge;

    private final XMLRequestParameterSaveOfferedCharge xmlRequestSaveOfferedCharge;

    public MFOfferedCharge(OfferedCharge offeredCharge, XMLRequestParameterSaveOfferedCharge xmlRequestSaveOfferedCharge) {
        this.offeredCharge = offeredCharge;
        this.xmlRequestSaveOfferedCharge = xmlRequestSaveOfferedCharge;
    }

    @Override
    public void apply() throws Exception {
        getOfferedCharge().setService(getService());
        getOfferedCharge().setOrganizationType(getOrganizationType());
        getOfferedCharge().setNotAccounted(getNotAccounted());
        getOfferedCharge().setAdditionalPrice(getAdditionalPrice());
        getOfferedCharge().setBasicPrice(getBasicPrice());
        getOfferedCharge().setBillable(getBillable());
        getOfferedCharge().setCharger(getCharger());
        getOfferedCharge().setDiscount(getDiscount());
        getOfferedCharge().setDiscountedPrice(getDiscountPrice());
        getOfferedCharge().setNotes(getNotes());
        getOfferedCharge().setTaxType(getTaxType());
        getOfferedCharge().setTaxRate(getTaxRate());
        getOfferedCharge().setTotal(getTotal());
        getOfferedCharge().setOffer(getOffer());
        getOfferedCharge().setPrice();
        getOfferedCharge().setPrice(getPrice());
    }

    private BigDecimal getAdditionalPrice() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getAdditionalprice() != null) {
            MFHelper.checkNotNull("additionalprice", getXmlRequestSaveOfferedCharge().getAdditionalprice());
            return MFHelper.bigDecimalValueOf("additionalprice", getXmlRequestSaveOfferedCharge().getAdditionalprice());
        }
        return getOfferedCharge().getAdditionalPrice();
    }

    private BigDecimal getBasicPrice() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getBasicprice() != null) {
            MFHelper.checkNotNull("basicprice", getXmlRequestSaveOfferedCharge().getBasicprice());
            return MFHelper.bigDecimalValueOf("basicprice", getXmlRequestSaveOfferedCharge().getBasicprice());
        }
        return getOfferedCharge().getBasicPrice();
    }

    public Boolean getBillable() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getBillable() != null) {
            return MFHelper.booleanValueOf("billable", getXmlRequestSaveOfferedCharge().getBillable());
        }
        return getOfferedCharge().isBillable();
    }

    public User getCharger() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getChargerid() != null) {
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("chargerid", getXmlRequestSaveOfferedCharge().getChargerid()));
        }
        return getOfferedCharge().getCharger();
    }

    private BigDecimal getDiscount() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getDiscount() != null) {
            MFHelper.checkNotNull("discount", getXmlRequestSaveOfferedCharge().getDiscount());
            return MFHelper.bigDecimalValueOf("discount", getXmlRequestSaveOfferedCharge().getDiscount());
        }
        return getOfferedCharge().getDiscount();
    }

    private BigDecimal getDiscountPrice() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getDiscountedprice() != null) {
            MFHelper.checkNotNull("discountprice", getXmlRequestSaveOfferedCharge().getDiscountedprice());
            return MFHelper.bigDecimalValueOf("discountprice", getXmlRequestSaveOfferedCharge().getDiscountedprice());
        }
        return getOfferedCharge().getDiscountedPrice();
    }

    private BigDecimal getNotAccounted() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getNotaccounted() != null) {
            MFHelper.checkNotNull("notaccounted", getXmlRequestSaveOfferedCharge().getNotaccounted());
            return MFHelper.bigDecimalValueOf("notaccounted", getXmlRequestSaveOfferedCharge().getNotaccounted());
        }
        return getOfferedCharge().getNotAccounted();
    }

    public String getNotes() {
        if (getXmlRequestSaveOfferedCharge().getNotes() != null) {
            return getXmlRequestSaveOfferedCharge().getNotes();
        }
        return getOfferedCharge().getNotes();
    }

    public Offer getOffer() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getOfferid() != null) {
            return (Offer) fetch(Offer.class, MFHelper.positiveLongValueOf("offerid", getXmlRequestSaveOfferedCharge().getOfferid()));
        }
        return getOfferedCharge().getOffer();
    }

    public OfferedCharge getOfferedCharge() {
        return offeredCharge;
    }

    private OrganizationType getOrganizationType() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getOrganizationtypeid() != null) {
            MFHelper.checkNotNull("organizationtypeid", getXmlRequestSaveOfferedCharge().getOrganizationtypeid());
            return (OrganizationType) fetch(OrganizationType.class, MFHelper.positiveLongValueOf("organizationtypeid", getXmlRequestSaveOfferedCharge().getOrganizationtypeid()));
        }
        return getOfferedCharge().getOrganizationType();
    }

    private BigDecimal getPrice() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getPrice() != null) {
            MFHelper.checkNotNull("price", getXmlRequestSaveOfferedCharge().getPrice());
            return MFHelper.bigDecimalValueOf("price", getXmlRequestSaveOfferedCharge().getPrice());
        }
        return getOfferedCharge().getPrice();
    }

    public Service getService() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getServiceid() != null) {
            return (Service) fetch(Service.class, MFHelper.positiveLongValueOf("serviceid", getXmlRequestSaveOfferedCharge().getServiceid()));
        }
        return getOfferedCharge().getService();
    }

    private BigDecimal getTaxRate() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getTaxrate() != null) {
            MFHelper.checkNotNull("taxrate", getXmlRequestSaveOfferedCharge().getTaxrate());
            return MFHelper.bigDecimalValueOf("taxrate", getXmlRequestSaveOfferedCharge().getTaxrate());
        }
        if (getOfferedCharge().getTaxRate() == null && getOfferedCharge().getTaxType() != null) {
            return getOfferedCharge().getTaxType().getTax();
        }
        return getOfferedCharge().getTaxRate();
    }

    public TaxType getTaxType() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getTaxtype() != null) {
            return (TaxType) fetch(TaxType.class, MFHelper.positiveLongValueOf("taxtype", getXmlRequestSaveOfferedCharge().getTaxtype()));
        }
        if (getOfferedCharge().getTaxType() == null) {
            return CDI.current().select(TaxTypeService.class).get().getDefaultTaxType();
        }
        return getOfferedCharge().getTaxType();
    }

    private BigDecimal getTotal() throws InvalidDataException {
        if (getXmlRequestSaveOfferedCharge().getTotal() != null) {
            MFHelper.checkNotNull("total", getXmlRequestSaveOfferedCharge().getTotal());
            return MFHelper.bigDecimalValueOf("total", getXmlRequestSaveOfferedCharge().getTotal());
        }
        return getOfferedCharge().getTotal();
    }

    public XMLRequestParameterSaveOfferedCharge getXmlRequestSaveOfferedCharge() {
        return xmlRequestSaveOfferedCharge;
    }
}
