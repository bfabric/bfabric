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

import org.bfabric.entity.Query;
import org.bfabric.entity.User;
import org.bfabric.enums.QueryDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class QueryService extends AbstractService {

    private static final long serialVersionUID = 1;

    public QueryService() {
        super(Query.class);
    }

    @Override
    public BfabricLazyDataModel<Query> getLazyModel() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setOrder("created DESC");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Query> getSavedQueriesCreatedByUser(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("createdBy = :createdBy and discriminator = :discriminator");
        entityQuery.addParameter("createdBy", user.getLogin());
        entityQuery.addParameter("discriminator", QueryDiscriminator.SAVED);
        entityQuery.setOrder("created DESC");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<Query> getSavedQueriesReadableByUser(User user) {
        EntityQuery entityQuery = createEntityQuery();
        if (user.hasCurrentUserRoleEnum(RoleEnum.ADMIN)) {
            entityQuery.addWhereClause("discriminator = :discriminator");
        } else {
            entityQuery.addWhereClause("(createdBy = :createdBy or published = TRUE) and discriminator = :discriminator");
            entityQuery.addParameter("createdBy", user.getLogin());
        }
        entityQuery.addParameter("discriminator", QueryDiscriminator.SAVED);
        entityQuery.setOrder("created DESC");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public void publishQuery(Query query) {
        if (query != null) {
            query.setPublished(true);
            save(query);
        }
    }
}