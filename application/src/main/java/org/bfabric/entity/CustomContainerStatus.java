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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DiscriminatorValue(value = "C")
@DynamicUpdate
@XmlRootElement
@NamedQuery(name = "CustomContainerStatus.getDistinctNamesByDiscriminator", query = "SELECT DISTINCT(a.name) FROM CustomContainerStatus a WHERE lower(a.container.discriminator) = lower(:discriminator) ORDER BY a.name ASC")
@NamedQuery(name = "CustomContainerStatus.findByNameAndDiscriminator", query = "SELECT a.id FROM CustomContainerStatus a WHERE a.name = :name AND lower(a.container.discriminator) = lower(:discriminator)")
public class CustomContainerStatus extends ContainerStatus {

    private static final long serialVersionUID = 1;

    public CustomContainerStatus() {
    }

    public CustomContainerStatus(Container container, String customStatus) {
        setContainer(container);
        if (customStatus != null) {
            setName(customStatus);
        }
    }

    @Override
    public boolean isCreatable() {
        // A custom container status is creatable iff its container allows the creation thereof.
        return getContainer().isCustomStatusCreatable();
    }

    @Override
    public boolean isCreatableWS() {
        return isCreatable();
    }

    @Override
    public boolean isDeletable() {
        // A custom container status is deletable iff its container allows the deletion thereof and the last state of the container is this custom container status.
        return getContainer().isCustomStatusDeletable() && equals(getContainer().getLastState());
    }

    @Override
    public boolean isDeletableWS() {
        return isDeletable();
    }

    @Override
    public boolean isReadable() {
        // A custom container status is readable iff the current user has either the feeder or containerManager role and its container is readable.
        return (hasCurrentUserRoleEnum(RoleEnum.FEEDER) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER)) && getContainer().isReadable();
    }

    @Override
    public boolean isUpdatable() {
        // A custom container status is never updatable as it is only creatable, i.e., adding a custom status, or deletable, i.e., rollback a custom status.
        return false;
    }

    @Override
    public boolean isUpdatableWS() {
        return isUpdatable();
    }
}