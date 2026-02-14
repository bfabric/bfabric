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

package org.bfabric.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.bfabric.entity.api.NotEntityLoggable;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "membership_containerid_userid_unique", columnNames = { "containerid", "userid" }) })
@NamedQuery(name = "Membership.findByContainerAndUser", query = "SELECT a FROM Membership a WHERE a.container = :container and a.user = :user")
@NamedQuery(name = "Membership.findByContainerAndDiscriminatorOrderByUser", query = "SELECT a FROM Membership a WHERE a.container = :container and a.discriminator = :discriminator ORDER BY a.user.lastName, a.user.firstName")
@NamedQuery(name = "Membership.findByUserAndDiscriminator", query = "SELECT a FROM Membership a WHERE a.user = :user and a.discriminator = :discriminator")
@NamedQuery(name = "Membership.findByUserAndDiscriminatorOrderByUser", query = "SELECT a FROM Membership a WHERE a.user = :user and a.discriminator = :discriminator ORDER BY a.container.id DESC")
@NamedQuery(name = "Membership.findContainerIdsByUserId", query = "select a.container.id from Membership a WHERE a.user.id = :userId and a.discriminator = :discriminator ORDER BY a.container.id DESC")
@NamedQuery(name = "Membership.findCurrentAndRunningOrFinishedProjectIdsByUser", query = "SELECT a.container.id FROM Membership a WHERE a.user = :user AND a.discriminator = :discriminator AND a.container.discriminator = 'Project' AND (a.container.status = :running OR a.container.status = :finished) ORDER BY a.container.id DESC")
@NamedQuery(name = "Membership.findCurrentAndRunningProjectIdsByUser", query = "SELECT a.container.id FROM Membership a WHERE a.user = :user AND a.discriminator = :discriminator AND a.container.discriminator = 'Project' AND a.container.status = :running ORDER BY a.container.id DESC")
public class Membership extends AbstractEntity implements NotEntityLoggable {

    public static final String DISCRIMINATOR_CURRENT = "C";

    public static final String DISCRIMINATOR_FORMER = "F";

    public static final String ROLE_MANAGER = "Manager";

    public static final String ROLE_MEMBER = "Member";

    private static final long serialVersionUID = 1;

    protected LocalDate created;

    protected String createdBy;

    @NotNull
    @Column(name = "discriminator")
    protected String discriminator;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "containerid")
    protected Container container;

    @NotNull
    @Column(columnDefinition = "varchar DEFAULT '" + ROLE_MEMBER + "'")
    protected String role = ROLE_MEMBER;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid")
    protected User user;

    public Membership() {
    }

    public Membership(final Container container, final User user, final boolean asManager, final String createdBy) {
        setContainer(container);
        setUser(user);
        setRole(asManager);
        setCreated(LocalDate.now());
        setCreatedBy(createdBy);
        setDiscriminator(DISCRIMINATOR_CURRENT);
    }

    public Membership copy(User user) {
        Membership membership = new Membership();
        membership.setUser(user);
        membership.setContainer(getContainer());
        membership.setCreated(getCreated());
        membership.setDiscriminator(getDiscriminator());
        membership.setRole(getRole());
        return membership;
    }

    public Container getContainer() {
        return container;
    }

    public LocalDate getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getRole() {
        return role;
    }

    public User getUser() {
        return user;
    }

    public boolean isCurrent() {
        return DISCRIMINATOR_CURRENT.equals(getDiscriminator());
    }

    public boolean isFormer() {
        return DISCRIMINATOR_FORMER.equals(getDiscriminator());
    }

    public boolean isRoleManager() {
        return ROLE_MANAGER.equals(getRole());
    }

    public boolean isRoleMember() {
        return ROLE_MEMBER.equals(getRole());
    }

    public void reactivate(boolean asManager, String reactivator) {
        setDiscriminator(DISCRIMINATOR_CURRENT);
        setRole(asManager);
        setCreated(LocalDate.now());
        setCreatedBy(reactivator);
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public void setRole(boolean asManager) {
        setRole(asManager ? ROLE_MANAGER : ROLE_MEMBER);
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void switchRole() {
        if (isRoleMember()) {
            setRole(ROLE_MANAGER);
        } else {
            setRole(ROLE_MEMBER);
        }
    }
}
