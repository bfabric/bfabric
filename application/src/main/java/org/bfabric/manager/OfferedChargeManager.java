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

package org.bfabric.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Offer;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.entity.Service;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OfferService;
import org.bfabric.service.OfferedChargeService;
import org.bfabric.service.TaxTypeService;
import org.bfabric.util.NumberUtils;
import org.omnifaces.cdi.Param;
import org.primefaces.event.SelectEvent;

@MeasureCalls
@Named
@ViewScoped
public class OfferedChargeManager extends AbstractEntityManager<OfferedCharge> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(OfferedChargeManager.class.getName());

    @Param
    private Long offerId;

    @Inject
    private OfferService offerService;

    @Inject
    private OfferedChargeService offeredChargeService;

    private List<Service> selectedServices = new ArrayList<>();

    @Inject
    private TaxTypeService taxTypeService;

    public OfferedChargeManager() {
        super(OfferedCharge.class);
    }

    @Override
    protected OfferedCharge createInstance() {
        final OfferedCharge offeredCharge = super.createInstance();
        if (offerId != null) {
            getOfferedCharge().setOffer(entityService.find(Offer.class, offerId));
            getOfferedCharge().setOrganizationType(getOfferedCharge().getOffer().getOrganizationType());
            getOfferedCharge().setTaxType(taxTypeService.getDefaultTaxType());
            getOfferedCharge().setTaxRate(taxTypeService.getDefaultTaxType().getTax());
            getOfferedCharge().setCharger(getCurrentUser());
        }
        return offeredCharge;
    }

    @Produces
    @Named("offeredCharge")
    public OfferedCharge getOfferedCharge() {
        return getInstance();
    }

    @Override
    public String getRedirectURLAfterCancel() {
        return createRedirectShowScreenURL(getOfferedCharge().getOffer());
    }

    @Override
    public String getRedirectURLAfterRemove() {
        return createRedirectShowScreenURL(getOfferedCharge().getOffer());
    }

    @Override
    public String getRedirectURLAfterSave() {
        return createRedirectShowScreenURL(getOfferedCharge().getOffer());
    }

    public List<Service> getSelectedServices() {
        return selectedServices;
    }

    public BigDecimal getTotalPrice() {
        double price = 0;
        if (!isManaged()) {
            for (final Service service : getSelectedServices()) {
                OfferedCharge charge;
                try {
                    charge = getOfferedCharge().clone();
                    charge.setService(service);
                    charge.setPrice();
                    price += charge.getPrice().doubleValue();
                } catch (final CloneNotSupportedException e) {
                    logger.warning("Could not clone OfferedCharge: " + e.getMessage());
                }
            }
        } else {
            OfferedCharge charge;
            try {
                charge = getOfferedCharge().clone();
                charge.setService(getOfferedCharge().getService());
                charge.setPrice();
                price = charge.getPrice().doubleValue();
            } catch (final CloneNotSupportedException e) {
                logger.warning("Could not clone OfferedCharge: " + e.getMessage());
            }
        }
        return NumberUtils.getDecimalScale2(BigDecimal.valueOf(price));
    }

    public String removeOfferedCharge(OfferedCharge charge) {
        setInstance(charge);
        String ret = remove();
        offerService.updateModified(charge.getOffer());
        return ret;
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = offeredChargeService.isValid(getOfferedCharge(), getSelectedServices().size());
        if (validationErrorMsg.isEmpty()) {
            offeredChargeService.save(getOfferedCharge(), getSelectedServices());
            return postSave(true, false);
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public void serviceChanged(SelectEvent<Service> event) {
        if (!isManaged() && !getSelectedServices().contains(event.getObject())) {
            getSelectedServices().add(event.getObject());
        }
        getOfferedCharge().setService(event.getObject());
        getOfferedCharge().setPrice();
    }

    public void setSelectedServices(List<Service> selectedServices) {
        this.selectedServices = selectedServices;
    }
}