package org.bfabric.service;

import java.util.LinkedHashMap;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Division;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class UserGroupService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    @Inject
    private AffiliationHelperService affiliationHelperService;

    public UserGroupService() {
        super(UserGroup.class);
    }

    public BfabricLazyDataModel<UserGroup> getMembersUserGroups(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setJoin("entity.users user");
        entityQuery.addWhereClause("user.id = :userId");
        entityQuery.addParameter("userId", user.getId());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<UserGroup> getReassignUserGroupSupervisorTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("forEmployeesOnly = true AND NOT EXISTS (SELECT user FROM User user JOIN user.roles role WHERE user = entity.supervisor AND role.name = 'employee')");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<UserGroup> getSupervisedUserGroups(User user) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("supervisor.id = :userId");
        entityQuery.addParameter("userId", user.getId());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        final UserGroup userGroup = (UserGroup) entity;
        LinkedHashMap<String, String> validationErrorMsg = isValidName(userGroup);

        if (userGroup.getUsers().isEmpty()) {
            validationErrorMsg.put(Constants.EDIT + ":userGroupautocomplete", Constants.REQUIRED);
        }

        return validationErrorMsg;
    }

    public void save(UserGroup userGroup) {
        save(userGroup, true);
    }

    public void save(UserGroup userGroup, boolean index) {
        try {
            if (userGroup.getOrganizationType() != null && userGroup.getOrganizationType().isCompany()) {
                final Division division = affiliationHelperService.saveDivisionIfNotExists(userGroup.getOrganizationType(), userGroup.getCompanyName(), userGroup.getDivisionName());
                userGroup.setDivision(division);
            }
            super.save(userGroup, index);
        } catch (final Exception e) {
            throw new RollbackException(e.getMessage());
        }
    }
}