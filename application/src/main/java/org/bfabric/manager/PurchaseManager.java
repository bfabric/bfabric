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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Consumable;
import org.bfabric.entity.Contract;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.PurchaseItem;
import org.bfabric.entity.Supplier;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.PurchaseService;
import org.bfabric.service.TechnologyService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class PurchaseManager extends AbstractEntityManager<Purchase> {

    private static final long serialVersionUID = 1;

    @Param
    protected Long supplierId;

    @Param
    protected Long contractId;

    private Supplier modalSupplier;

    @Inject
    private PurchaseService purchaseService;

    @Inject
    private TechnologyService technologyService;

    public PurchaseManager() {
        super(Purchase.class);
    }

    @Override
    protected Purchase createInstance() {
        final Purchase purchase = super.createInstance();
        if (purchase != null) {
            if (supplierId != null) {
                purchase.setSupplier(entityService.find(Supplier.class, supplierId));
            }
            if (contractId != null) {
                purchase.getContracts().add(entityService.find(Contract.class, contractId));
            }
            purchase.setOrderDate(LocalDate.now());
            purchase.setOrderedBy(getCurrentUser());
            if (getCurrentUser().getDefaultTechnology() != null) {
                purchase.getTechnologies().add(getCurrentUser().getDefaultTechnology());
            }
        }
        return purchase;
    }

    public void createNewSupplier() {
        setModalSupplier(new Supplier());
    }

    public Supplier getModalSupplier() {
        return modalSupplier;
    }

    @Produces
    @Named("purchase")
    public Purchase getPurchase() {
        return getInstance();
    }

    @SuppressWarnings("unused")
    public boolean isOrderItemsReceivedButtonEnabled() {
        return getPurchase().getOrderItemReceivedDate() == null && getCurrentUser().hasRoleImplicit(RoleEnum.PURCHASEMANAGER);
    }

    @SuppressWarnings("unused")
    public String orderItemsReceived() {
        getPurchase().setOrderItemReceivedDate(LocalDate.now());
        getPurchase().setOrderItemReceivedBy(getCurrentUser());
        entityService.save(getPurchase());
        for (PurchaseItem purchaseItem : getPurchase().getItems()) {
            Consumable consumable = purchaseItem.getConsumable();
            if (consumable != null) {
                consumable.checkIn(purchaseItem.getQuantity().intValue());
                entityService.save(consumable);
            }
        }
        getFacesMessagesManager().bufferWarningClear(Messages.get("orderItemsReceivedMessage"));
        return getShowScreenRedirectURL();
    }

    public String recalculatePrice() {
        getPurchase().setPriceToSumOfPurchaseItemPrices();
        entityService.save(getPurchase());
        getFacesMessagesManager().bufferWarningClear(Messages.get("priceUpdatedMessage"));
        return postSave(false, false);
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = purchaseService.isValid(getPurchase());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            purchaseService.save(getPurchase());
            if (getPurchase().isCloned()) {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCloned"));
            }
            return postSave(!getPurchase().isCloned(), false);
        }

        for (Map.Entry<String, String> entry : validationErrorMsg.entrySet()) {
            if (entry.getKey() != null) {
                getFacesMessagesManager().validationError(entry.getKey(), entry.getValue(), false);
            }
        }
        getFacesMessagesManager().printWarn(Messages.get("noteAdaptedDate"));

        return null;
    }

    public void saveNewSupplier() {
        entityService.save(getModalSupplier());
        getPurchase().setSupplier(getModalSupplier());
    }

    public void setModalSupplier(Supplier modalSupplier) {
        this.modalSupplier = modalSupplier;
    }

}
