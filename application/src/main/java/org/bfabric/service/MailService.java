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

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.Mail;
import org.bfabric.entity.User;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class MailService extends AbstractParentDependentNamedEntityService {

    private static final long serialVersionUID = 1;

    public MailService() {
        super(Mail.class);
    }

    public BfabricLazyDataModel<Mail> getMailsLazyModelByParent(AbstractBaseEntity parent, User currentUser) {
        EntityQuery entityQuery = createEntityQuery();
        if (parent != null) {
            entityQuery.addWhereClause("parentId = " + parent.getId() + " and parentClassName = '" + parent.getClass().getSimpleName() + "'");
            String mailTypes = MailTypeEnum.getMailTypes(parent.getClass(), currentUser.hasRoleEmployeeImplicit());
            if (mailTypes != null && !mailTypes.isEmpty()) {
                entityQuery.addWhereClause("type IN (" + mailTypes + ")");
            } else if (!currentUser.hasRoleEmployeeImplicit()) {
                return new BfabricLazyDataModel<>();
            }
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }
}