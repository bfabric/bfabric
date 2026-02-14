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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.Constants;
import org.bfabric.entity.Container;
import org.bfabric.entity.CustomStatus;
import org.bfabric.entity.Mail;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class ContainerService extends AbstractContainerService {

    private static final long serialVersionUID = 1;

    @Inject
    private PlateService plateService;

    public ContainerService() {
        super(Container.class);
    }

    public Map<String, Set<String>> changeCustomStatus(Container container, String name) {
        if (container != null) {
            final String parentStatusName = container.getStatus().getLabel().toUpperCase();
            final String type = container.getClassName().toUpperCase();
            if (container.isManaged() && createNamedQuery("CustomStatus.findByNameAndType").setParameter("parentStatusName", parentStatusName).setParameter("name", name)
                .setParameter("type", type).setMaxResults(1).getResultList().isEmpty()) {
                save(new CustomStatus(name, parentStatusName, type));
            }
            Set<Mail> mails = container.changeCustomStatus(name);
            save(container);
            Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(type + " " + name);
            facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
            if (!mails.isEmpty()) {
                setCustomContainerStatusSentMail(container);
            }
            return facesMessages;
        }
        return null;
    }

    public List<Container> getContainersByPlateId(Long plateId) {
        return createNamedQuery("Container.findByPlateId").setParameter("plateId", plateId).getResultList();
    }

    public Long getContainersByPlateIdCount(Long plateId) {
        return (Long) createNamedQuery("Container.countByPlateId").setParameter("plateId", plateId).getSingleResult();
    }

    public List<Container> getContainersByRunId(Long runId) {
        return createNamedQuery("Container.findByRunId").setParameter("runId", runId).getResultList();
    }

    public Long getContainersByRunIdCount(Long runId) {
        return (Long) createNamedQuery("Container.countByRunId").setParameter("runId", runId).getSingleResult();
    }

    public List<String> getCustomStates(String discriminator) {
        return createNamedQuery("CustomContainerStatus.getDistinctNamesByDiscriminator").setParameter("discriminator", discriminator).getResultList();
    }

    public List<String> getCustomStatesFiltered(String filterString, String discriminator, String parentStatusName, String excluded) {
        // Note: If status is NULL, this means that all filtered custom states are retrieved regardless of the status.
        String queryString = "SELECT DISTINCT(name) FROM CustomStatus WHERE LOWER(name) LIKE :filterString";
        if (StringHelper.isNotEmpty(discriminator)) {
            queryString += " AND LOWER(type) = LOWER(:type)";
        }
        if (parentStatusName != null) {
            queryString += " AND LOWER(parentStatusName) = LOWER(:parentStatusName)";
        }
        if (excluded != null) {
            queryString += " AND LOWER(name) != LOWER(:excluded)";
        }
        queryString += " ORDER BY name ASC";
        Query query = createNativeQuery(queryString);
        query.setParameter("filterString", "%" + (StringHelper.isNotEmpty(filterString) ? filterString.trim().toLowerCase() : Constants.EMPTY_STRING) + "%");
        if (StringHelper.isNotEmpty(discriminator)) {
            query.setParameter("type", discriminator);
        }
        if (parentStatusName != null) {
            query.setParameter("parentStatusName", parentStatusName.toLowerCase());
        }
        if (excluded != null) {
            query.setParameter("excluded", excluded.toLowerCase());
        }
        query.setMaxResults(25);
        return (List<String>) query.getResultList();
    }

    public List<String> getCustomStatesFiltered(String filterString, Container container) {
        if (container != null) {
            return getCustomStatesFiltered(filterString, container.getClassName(), container.getSelectedStatus() != null ? container.getSelectedStatus().getLabel() : null, null);
        }
        return new ArrayList<>();
    }

    public BfabricLazyDataModel<Container> getFinanceSourceToBeChecked() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("status not in :finalContainerStatusList and institute.department.organization.organizationType.id = 1 AND (costCentre IS NULL OR costCentre IN('00000','0000000','99999','9999999')) AND (pspElement IS NULL OR pspElement IN ('9-999999-999','0-000000-000'))");
        entityQuery.addParameter("finalContainerStatusList", StatusEnum.FINAL_CONTAINER_STATUS_LIST);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public Set<Long> getOrderIdsByContainerIds(Set<Long> containerIds) {
        return new HashSet<Long>(createNamedQuery("Container.findOrderIdsByContainerIds").setParameter("containerIds", containerIds).getResultList());
    }

    public void rollbackStatus(Container container) {
        if (container != null && container.isRollbackable()) {
            if (container.getLastState() != null && container.getLastState().isCustomContainerStatus()) {
                String name = container.getLastState().getName();
                if (createNamedQuery("CustomContainerStatus.findByNameAndDiscriminator").setParameter("name", name).setParameter("discriminator", container.getClassName()).getResultList()
                    .size() == 1) {
                    CustomStatus customStatus = (CustomStatus) createNamedQuery("CustomStatus.findByNameAndType").setParameter("parentStatusName", container.getStatus().getLabel())
                        .setParameter("name", name).setParameter("type", container.getClassName()).setMaxResults(1).getSingleResult();
                    if (customStatus != null) {
                        remove(customStatus);
                    }
                }
            }
            container.rollbackStatus();
            // Set the status of the plates to 'Pending' if the user rollbacks the order, which processes plates, from 'Submitted' to 'Pending'.
            if (container.getStatus().equals(StatusEnum.PENDING) && !container.isContainerProject() && ((Order) container).isProcessesPlates()) {
                Order order = (Order) container;
                for (OrderItem orderItem : order.getOrderItems()) {
                    if (orderItem.getPlate().isPlateTypeUserSubmitted()) {
                        plateService.changeStatus(orderItem.getPlate(), StatusEnum.PENDING, false);
                    }
                }
            }
            save(container);
        }
    }
}