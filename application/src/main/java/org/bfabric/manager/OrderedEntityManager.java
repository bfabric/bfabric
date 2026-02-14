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

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractOrderedEnabledNamedBaseEntity;
import org.bfabric.entity.Account;
import org.bfabric.entity.CostCentre;
import org.bfabric.entity.RunUnitType;
import org.bfabric.entity.SampleType;
import org.bfabric.entity.SequencingApplication;
import org.bfabric.entity.Service;
import org.bfabric.entity.ServiceArea;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.Technology;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OrderedEntityService;
import org.bfabric.util.CollectionHelper;

@MeasureCalls
@Named
@ViewScoped
public class OrderedEntityManager extends AbstractManager {

    private static final long serialVersionUID = 1;

    private String entityClassName;

    @Inject
    private OrderedEntityService orderedEntityService;

    public OrderedEntityManager() {
        super();
    }

    public void entityClassNameChanged(ValueChangeEvent event) {
        entityClassName = (String) event.getNewValue();
    }

    public List<AbstractOrderedEnabledNamedBaseEntity> getEntities() {
        return orderedEntityService.getOrderedEntitiesByClassNameOrderByPosition(getEntityClassName());
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public List<String> getEntityClassNames() {
        // Note that the classes WorkflowTemplateStep and WorkflowStep are not included because there is no general order on the entities of these classes.
        // Rather, the step entities are relatively ordered in the context of their parent entity.
        final List<String> entityClassNames = new ArrayList<>();

        if (identityManager.hasCurrentUserRoleEnum(RoleEnum.ADMIN)) {
            entityClassNames.add(SampleType.class.getSimpleName());
        }

        if (identityManager.hasCurrentUserRoleEnum(RoleEnum.BOOKINGMANAGER)) {
            entityClassNames.add(Account.class.getSimpleName());
            entityClassNames.add(CostCentre.class.getSimpleName());
        }

        if (identityManager.hasCurrentUserRoleEnum(RoleEnum.SERVICEMANAGER)) {
            entityClassNames.add(RunUnitType.class.getSimpleName());
            entityClassNames.add(SequencingApplication.class.getSimpleName());
            entityClassNames.add(Service.class.getSimpleName());
            entityClassNames.add(ServiceArea.class.getSimpleName());
            entityClassNames.add(ServiceType.class.getSimpleName());
        }

        if (identityManager.hasCurrentUserRoleEnum(RoleEnum.TECHNOLOGYMANAGER)) {
            entityClassNames.add(Technology.class.getSimpleName());
        }

        return CollectionHelper.sortObjects(entityClassNames);
    }

    private boolean isBottom(AbstractOrderedEnabledNamedBaseEntity entity) {
        return !getEntities().isEmpty() && getEntities().get(getEntities().size() - 1).equals(entity);
    }

    private boolean isTop(AbstractOrderedEnabledNamedBaseEntity entity) {
        return !getEntities().isEmpty() && getEntities().get(0).equals(entity);
    }

    public void moveOrderPositionDown(AbstractOrderedEnabledNamedBaseEntity entity) {
        if (!isBottom(entity)) {
            orderedEntityService.moveOrderPositionDown(entity, getEntities());
        }
        getFacesMessagesManager().printWarn(Messages.get("movedOrderPositionDownHint").replace("{0}", entity.getClass().getSimpleName() + " " + entity.getDisplayName()));
    }

    public void moveOrderPositionEnd(AbstractOrderedEnabledNamedBaseEntity entity) {
        if (!isBottom(entity)) {
            orderedEntityService.moveOrderPositionEnd(entity, getEntities());
        }
        getFacesMessagesManager().printWarn(Messages.get("movedOrderPositionEndHint").replace("{0}", entity.getClass().getSimpleName() + " " + entity.getDisplayName()));
    }

    public void moveOrderPositionStart(AbstractOrderedEnabledNamedBaseEntity entity) {
        if (!isTop(entity)) {
            orderedEntityService.moveOrderPositionStart(entity, getEntities());
        }
        getFacesMessagesManager().printWarn(Messages.get("movedOrderPositionStartHint").replace("{0}", entity.getClass().getSimpleName() + " " + entity.getDisplayName()));
    }

    public void moveOrderPositionUp(AbstractOrderedEnabledNamedBaseEntity entity) {
        if (!isTop(entity)) {
            orderedEntityService.moveOrderPositionUp(entity, getEntities());
        }
        getFacesMessagesManager().printWarn(Messages.get("movedOrderPositionUpHint").replace("{0}", entity.getClass().getSimpleName() + " " + entity.getDisplayName()));
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }
}
