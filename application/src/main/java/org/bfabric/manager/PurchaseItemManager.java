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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Consumable;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.PurchaseItem;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ConsumableService;
import org.bfabric.service.PurchaseItemService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class PurchaseItemManager extends AbstractEntityManager<PurchaseItem> {

    private static final long serialVersionUID = 1;

    @Param
    protected Long purchaseId;

    @Inject
    PurchaseItemService purchaseItemService;

    @Inject
    private ConsumableService consumableService;

    public PurchaseItemManager() {
        super(PurchaseItem.class);
    }

    @Override
    public String cancel() {
        return getPurchaseItem().getPurchase() != null ? createRedirectShowScreenURL(getPurchaseItem().getPurchase()) : super.getRedirectURLAfterCancel();
    }

    @Override
    protected PurchaseItem createInstance() {
        PurchaseItem purchaseItem = super.createInstance();
        if (purchaseId != null && purchaseItem != null) {
            purchaseItem.setPurchase(entityService.find(Purchase.class, purchaseId));
        }
        return purchaseItem;
    }

    public List<Consumable> getConsumablesFiltered(String filterString) {
        Set<Consumable> exclude = null;
        if (getPurchaseItem().getConsumable() != null) {
            exclude = new HashSet<>();
            exclude.add(getPurchaseItem().getConsumable());
        }
        return consumableService.getConsumableFiltered(filterString, exclude);
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    @Produces
    @Named("purchaseItem")
    public PurchaseItem getPurchaseItem() {
        return super.getInstance();
    }

    @Override
    public String getRedirectURLAfterRemove() {
        return createRedirectShowScreenURL(getPurchaseItem().getPurchase());
    }

    public String remove(PurchaseItem purchaseItem) {
        setInstance(purchaseItem);
        purchaseItem.getPurchase().getItems().remove(purchaseItem);
        return remove();
    }

    @Override
    public String save() {
        purchaseItemService.save(getPurchaseItem());
        return postSave(true, false);
    }
}
