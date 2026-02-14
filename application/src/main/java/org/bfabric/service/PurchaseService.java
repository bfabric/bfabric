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

import java.util.LinkedHashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Purchase;
import org.bfabric.entity.PurchaseItem;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class PurchaseService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    @Inject
    protected UserService userService;

    public PurchaseService() {
        super(Purchase.class);
    }

    @Override
    public List<Purchase> getFiltered(String filterString) {
        final EntityQuery entityQuery = createEntityQuery();
        entityQuery.addIdOrNameWhereClause(filterString, "entity.supplier.name");
        entityQuery.addIdOrNameWhereClause(filterString, "entity.orderedBy.name", "OR");
        entityQuery.setMaxResult(100);
        return (List<Purchase>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<Purchase> getLazyModelByInternal(boolean internal) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setWhere("internal = :internal");
        entityQuery.addParameter("internal", internal);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Purchase> getLazyModelByUserId(Long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.users user");
        entityQuery.setJoinTypeLeftOuter();
        entityQuery.setWhere("entity.orderedBy.id = :userId OR entity.orderItemReceivedBy.id = :userId or user.id = :userId");
        entityQuery.addParameter("userId", userId);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public LinkedHashMap<String, String> isValid(Purchase purchase) {
        LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();
        if (purchase.getOrderItemReceivedDate() != null && purchase.getOrderItemReceivedDate().isBefore(purchase.getOrderDate())) {
            purchase.setOrderItemReceivedDate(purchase.getOrderDate());
            validationErrorMsg.put(Constants.EDIT + ":orderItemReceivedDate", Messages.get("adaptedToOrderDate"));
        }
        if (purchase.getInvoiceReceivedDate() != null && purchase.getInvoiceReceivedDate().isBefore(purchase.getOrderDate())) {
            purchase.setInvoiceReceivedDate(purchase.getOrderDate());
            validationErrorMsg.put(Constants.EDIT + ":invoiceReceivedDate", Messages.get("adaptedToOrderDate"));
        }
        return validationErrorMsg;
    }

    public void save(Purchase purchase) {
        save(purchase, true);
    }

    public void save(Purchase purchase, boolean index) {
        try {
            super.save(purchase, index);
            if (purchase.isCloned()) {
                for (PurchaseItem purchaseItem : purchase.getItems()) {
                    super.save(purchaseItem, index);
                }
            }
            if (purchase.isSendMail()) {
                // Send mail.
                Mail mail = new Mail();
                mail.setParent(purchase);
                mail.addRecipients(userService.getUsersByRoleEnum(RoleEnum.PURCHASEADMIN));
                mail.setType(MailTypeEnum.PURCHASE_CHANGE, purchase.toString());
                mail.setInput("purchase", purchase);
                mailSendService.send(mail);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
    }
}