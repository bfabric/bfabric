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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.Query;

import org.bfabric.entity.Container;
import org.bfabric.entity.Membership;
import org.bfabric.entity.User;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class MembershipService extends AbstractService {

    private static final long serialVersionUID = 1;

    public MembershipService() {
        super(Membership.class);
    }

    public boolean checkMemberByContainerAndUserAndDiscriminatorAndRole(Container container, User user, String discriminator, String role) {
        String roleClause = (role != null) ? " and role = :role" : "";
        Query query = createQuery("select m from Membership m WHERE container = :container and user = :user and discriminator = :discriminator" + roleClause).setParameter("container", container)
            .setParameter("user", user).setParameter("discriminator", discriminator);
        if (role != null) {
            query.setParameter("role", role);
        }
        return !query.getResultList().isEmpty();
    }

    public Set<Long> getCurrentAndRunningProjectIdsByUser(User user) {
        return new HashSet<Long>(createNamedQuery("Membership.findCurrentAndRunningProjectIdsByUser").setParameter("user", user).setParameter("discriminator", Membership.DISCRIMINATOR_CURRENT)
            .setParameter("running", StatusEnum.RUNNING).getResultList());
    }

    public Set<Long> getCurrentContainerIdsByUserId(long userId) {
        return new HashSet<Long>(createNamedQuery("Membership.findContainerIdsByUserId").setParameter("userId", userId).setParameter("discriminator", Membership.DISCRIMINATOR_CURRENT)
            .getResultList());
    }

    public BfabricLazyDataModel<Membership> getCurrentLazyModel(Container container) {
        return getLazyModelByContainerAndDiscriminator(container, Membership.DISCRIMINATOR_CURRENT);
    }

    public BfabricLazyDataModel<Membership> getFormerLazyModel(Container container) {
        return getLazyModelByContainerAndDiscriminator(container, Membership.DISCRIMINATOR_FORMER);
    }

    public BfabricLazyDataModel<Membership> getLazyModelByContainerAndDiscriminator(Container container, String discriminator) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("container = :container and discriminator = :discriminator");
        entityQuery.addParameter("container", container);
        entityQuery.addParameter("discriminator", discriminator);
        entityQuery.setOrder("user.lastName, user.firstName");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public Membership getMembership(Container container, User user) {
        List<Membership> ret = getMembershipByContainerAndUser(container, user);
        return !ret.isEmpty() ? ret.get(0) : null;
    }

    public List<Membership> getMembershipByContainerAndUser(Container container, User user) {
        return createNamedQuery("Membership.findByContainerAndUser").setParameter("container", container).setParameter("user", user).setMaxResults(1).getResultList();
    }

    public List<Membership> getMembershipsByContainerAndDiscriminatorOrderByUser(Container container, String discriminator) {
        return createNamedQuery("Membership.findByContainerAndDiscriminatorOrderByUser").setParameter("container", container).setParameter("discriminator", discriminator).getResultList();
    }

    public List<Membership> getMembershipsByUserAndDiscriminator(User user, String discriminator) {
        return createNamedQuery("Membership.findByUserAndDiscriminator").setParameter("user", user).setParameter("discriminator", discriminator).getResultList();
    }

    public List<Membership> getMembershipsByUserAndDiscriminatorOrderByUser(User user, String discriminator) {
        return createNamedQuery("Membership.findByUserAndDiscriminatorOrderByUser").setParameter("user", user).setParameter("discriminator", discriminator).getResultList();
    }
}
