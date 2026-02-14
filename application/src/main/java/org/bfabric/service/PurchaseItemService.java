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

package org.bfabric.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.PurchaseItem;

@Named
@Stateless
public class PurchaseItemService extends AbstractService {

    private static final long serialVersionUID = 1;

    public void save(PurchaseItem purchaseItem) {
        save(purchaseItem, true);
    }

    public void save(PurchaseItem purchaseItem, boolean index) {
        Purchase purchase = purchaseItem.getPurchase();
        if (purchase != null && purchase.isRecalculatePriceChecked()) {
            purchase.setPriceToSumOfPurchaseItemPrices();
            purchase.setPrice(purchase.getPrice().add(purchaseItem.getTotalPrice()));
            save(purchase, index);
        }
        super.save(purchaseItem, index);
    }

    public String savePurchaseItems(Purchase purchase, List<PurchaseItem> editList, List<PurchaseItem> deleteList) {
        int deleted = 0;
        int created = 0;

        if (purchase.getCurrency() == null) {
            purchase.setCurrency(getDefaultCurrency());
        }

        if (purchase.isCurrencyChanged() || purchase.isRecalculatePriceChecked()) {
            if (purchase.isRecalculatePriceChecked()) {
                if (editList.isEmpty()) {
                    purchase.setPrice(null);
                } else {
                    purchase.setPrice(purchase.getPurchaseItemsPriceSum(editList));
                }
            }
            save(purchase);
        }

        for (PurchaseItem purchaseItem : editList) {
            if (purchaseItem.getId() == 0) {
                save(purchaseItem);
                created++;
            }
            if (purchaseItem.isChanged()) {
                save(purchaseItem);
            }
        }

        for (PurchaseItem purchaseItem : deleteList) {
            if (purchaseItem.getId() > 0) {
                remove(purchaseItem);
                deleted++;
            }
        }

        // Create and print the faces messages.
        if (created > 0 || deleted > 0) {
            return createFacesMessagesForCreatedAndDeletedItems(Messages.get("successfullyEditedPurchaseItems"), created, deleted);
        }

        return Messages.get("successfullyUpdated");
    }
}