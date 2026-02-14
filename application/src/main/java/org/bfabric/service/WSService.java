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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.CollectionHelper;
import org.bfabric.webservice.request.parameter.WhereClauseItem;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadContainer;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadContainerReferencingEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadUser;

@Named
@Stateless
public class WSService extends AbstractService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(WSService.class.getName());

    @Inject
    private IdentityService identityService;

    private static String getManagedEntityTrimmedClassName(Class<?> entityClass) {
        return ClassHelper.getTrimmedClassName(entityClass);
    }

    private Query generateQuery(String querySelectClause, XMLRequestParameterReadEntity item, int page, int maxNumberOfEntitiesPerPage) throws InvalidDataException, InvalidEnumValueException {
        String queryRestriction = generateQueryRestriction(item);
        StringBuilder queryString = new StringBuilder(querySelectClause);

        List<WhereClauseItem> whereClauseItems = new ArrayList<>();
        if (item != null) {
            whereClauseItems.addAll(item.getWhereClauseItems());

            if (whereClauseItems.size() > getConfiguration().getWebServiceQueryMaxElements()) {
                throw new InvalidDataException("Query has " + whereClauseItems.size() + " elements and exceeds the maximum of " + getConfiguration().getWebServiceQueryMaxElements() + " allowed elements.");
            }

            if (!whereClauseItems.isEmpty()) {
                Set<String> joinClauseSet = new HashSet<>();

                // create a set with all attribute names
                Set<String> attributeNamesSet = new HashSet<>();
                for (WhereClauseItem whereClauseItem : whereClauseItems) {
                    attributeNamesSet.add(whereClauseItem.getAttributeName());
                    if (whereClauseItem.getJoinClause() != null) {
                        joinClauseSet.add(whereClauseItem.getJoinClause());
                    }
                }
                for (String joinClause : joinClauseSet) {
                    queryString.append("JOIN entity.").append(joinClause).append(" ");
                }

                queryString.append("WHERE ");

                if (!queryRestriction.isEmpty()) {
                    queryString.append(queryRestriction);
                    if (!attributeNamesSet.isEmpty()) {
                        queryString.append(" AND ");
                    }
                }

                // create a map with attribute names and corresponding list of where clause items
                Map<String, List<WhereClauseItem>> attributeNamesMap = new HashMap<>();
                for (String attributeName : attributeNamesSet) {
                    List<WhereClauseItem> attributeWhereClauseItemList = new ArrayList<>();
                    attributeNamesMap.put(attributeName, attributeWhereClauseItemList);
                    for (WhereClauseItem whereClauseItem : whereClauseItems) {
                        if (attributeName.equals(whereClauseItem.getAttributeName())) {
                            attributeWhereClauseItemList.add(whereClauseItem);
                        }
                    }
                }

                Iterator<String> iteratorAttributeNamesSet = attributeNamesSet.iterator();
                while (iteratorAttributeNamesSet.hasNext()) {
                    String attributeName = iteratorAttributeNamesSet.next();
                    List<WhereClauseItem> attributeNameWhereClauseList = attributeNamesMap.get(attributeName);
                    queryString.append("( ");
                    Iterator<WhereClauseItem> iteratorAttributeNameWhereClauseList = attributeNameWhereClauseList.iterator();
                    while (iteratorAttributeNameWhereClauseList.hasNext()) {
                        WhereClauseItem whereClauseItem = iteratorAttributeNameWhereClauseList.next();
                        queryString.append(whereClauseItem.getPredicate());
                        if (iteratorAttributeNameWhereClauseList.hasNext()) {
                            if (whereClauseItem.getParameterValueDateTimeBefore() != null || whereClauseItem.getParameterValueDateTimeAfter() != null || whereClauseItem
                                .getParameterValueDateBefore() != null || whereClauseItem.getParameterValueDateAfter() != null) {
                                queryString.append("AND ");
                            } else {
                                queryString.append("OR ");
                            }
                        }
                    }
                    queryString.append(") ");
                    if (iteratorAttributeNamesSet.hasNext()) {
                        queryString.append("AND ");
                    }
                }
            } else {
                if (!queryRestriction.isEmpty()) {
                    queryString.append("WHERE ").append(queryRestriction);
                }
            }
        } else {
            if (!queryRestriction.isEmpty()) {
                queryString.append("WHERE ").append(queryRestriction);
            }
        }

        Query query;
        if (page < 0) {
            query = createQuery(queryString.toString());
        } else {
            queryString.append("ORDER BY entity.id desc ");
            int currentPosition = page * maxNumberOfEntitiesPerPage;
            query = createQuery(queryString.toString()).setFirstResult(currentPosition).setMaxResults(maxNumberOfEntitiesPerPage);
        }

        for (WhereClauseItem whereClauseItem : whereClauseItems) {
            query.setParameter(whereClauseItem.getParameterName(), whereClauseItem.getParameterValue());
        }

        logger.fine("Web Service Query: " + queryString + " page=" + page);

        return query;
    }

    private String generateQueryRestriction(XMLRequestParameterReadEntity item) {
        StringBuilder queryRestriction = new StringBuilder();
        if (!identityService.hasRoleEnum(RoleEnum.WEBSERVICEUSER)) {
            User currentUser = identityService.getCurrentUser();
            if (item instanceof XMLRequestParameterReadUser) {
                queryRestriction.append("login = '").append(currentUser.getLogin()).append("'");
            } else if (item instanceof XMLRequestParameterReadContainerReferencingEntity) {
                Set<Long> containerIds = currentUser.getContainerIds();
                if (!containerIds.isEmpty()) {
                    queryRestriction.append("container.id in (").append(CollectionHelper.print(containerIds)).append(") ");
                } else {
                    queryRestriction.append("1 <> 1 ");
                }
            } else if (item instanceof XMLRequestParameterReadContainer) {
                Set<Long> containerIds = currentUser.getContainerIds();
                if (!containerIds.isEmpty()) {
                    queryRestriction.append("id in (").append(CollectionHelper.print(containerIds)).append(") ");
                } else {
                    queryRestriction.append("1 <> 1 ");
                }
            }
            if (currentUser.getLastContainer() != null) {
                entityManager.detach(currentUser.getLastContainer());
            }
        }
        return queryRestriction.toString();
    }

    public List<?> getEntities(XMLRequestParameterReadEntity item, int page, int maxNumberOfEntitiesPerPage, Class<?> entityClass) throws InvalidDataException, InvalidEnumValueException {
        String querySelectClause = "SELECT distinct(entity) FROM " + getManagedEntityTrimmedClassName(entityClass) + " entity ";
        return generateQuery(querySelectClause, item, page, maxNumberOfEntitiesPerPage).getResultList();
    }

    public Integer getNumberOfPages(XMLRequestParameterReadEntity query, int maxNumberOfEntitiesPerPage, Class<?> entityClass) throws InvalidDataException, InvalidEnumValueException {
        String querySelectClause = "SELECT count(distinct entity) FROM " + getManagedEntityTrimmedClassName(entityClass) + " entity ";
        Long numberOfEntities = (Long) generateQuery(querySelectClause, query, -1, maxNumberOfEntitiesPerPage).getSingleResult();
        long numberOfPages = numberOfEntities / maxNumberOfEntitiesPerPage;
        if (!Long.valueOf(numberOfPages * maxNumberOfEntitiesPerPage).equals(numberOfEntities)) {
            numberOfPages = numberOfPages + 1;
        }
        return (int) numberOfPages;
    }

    public void indexEntities(Set<Indexable> indexableEntities) {
        if (indexableEntities != null && !indexableEntities.isEmpty()) {
            IndexHelper.indexEntities(indexableEntities);
            indexableEntities.clear();
        }
    }
}