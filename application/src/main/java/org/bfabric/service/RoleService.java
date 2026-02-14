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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Role;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class RoleService extends AbstractService {

    private static final long serialVersionUID = 1;

    public RoleService() {
        super(Role.class);
    }

    public int assignRoles(List<Role> roles, List<User> users) {
        int assignments = 0;
        if (!roles.isEmpty() && !users.isEmpty()) {
            for (User user : users) {
                // Compute the new roles for the given user.
                Set<Role> newRoles = new HashSet<>(roles);
                newRoles.removeAll(user.getRoles());
                if (!newRoles.isEmpty()) {
                    // Assign roles to the user and create an entity log entry.
                    user.getRoles().addAll(newRoles);
                    user.checkAndSetMassMailEnabled();
                    merge(user);
                    assignments += newRoles.size();
                }
            }
        }
        return assignments;
    }

    @Override
    public BfabricLazyDataModel<Role> getLazyModelByUserId(long userId) {
        return (BfabricLazyDataModel<Role>) getLazyModelUnnestById("users", null, userId, "entity.name");
    }

    public List<String> getRoleNamesImplicitByUserLogin(@NotNull String login) {
        return createNativeQuery("SELECT name FROM impliedrolenames(:login)").setParameter("login", login.toLowerCase()).getResultList();
    }

    public List<Role> getRolesFiltered(String filterString, Collection<Role> exclude) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("name != :name");
        entityQuery.addParameter("name", RoleEnum.USER.getName());
        entityQuery.addNotInEntitiesClause(exclude);
        entityQuery.setOrder("name");
        entityQuery.setMaxResult(100);
        return (List<Role>) entityQuery.getResultList();
    }

    public List<Role> getRolesImplicitByUserLogin(@NotNull String login) {
        // NOTE: mapping from native query does not work! Therefore, use workaround with nested query!
        // Query query = createNativeQuery("SELECT cast(id as bigint), name, created, createdby, modified, modifiedby, cast(oplockversion as int) FROM impliedroles(:login) ORDER BY name", Role.class);
        // query.setParameter("login", login.toLowerCase());
        List<String> roleNames = getRoleNamesImplicitByUserLogin(login);
        if (!roleNames.isEmpty()) {
            return createQuery("FROM Role WHERE name in :names ORDER BY name").setParameter("names", roleNames).getResultList();
        }
        return new ArrayList<>();
    }

    public List<Role> getRolesSpecific() {
        return createNamedQuery("Role.findAllSpecific").getResultList();
    }

    public boolean hasRole(long userId, @NotNull RoleEnum roleEnum) {
        return !createQuery("SELECT user.id FROM User user join user.roles role where user.id = :userId and role.name = :roleName").setParameter("userId", userId)
            .setParameter("roleName", roleEnum.getName()).getResultList().isEmpty();
    }

    @Override
    public LinkedHashMap<String, String> isValid(AbstractEntity entity) {
        return isValidName((Role) entity);
    }
}
