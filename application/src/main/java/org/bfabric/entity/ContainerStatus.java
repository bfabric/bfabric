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

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.hibernate.annotations.DiscriminatorOptions;

@Entity
@Table(name = "ContainerStatus")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorOptions(force = true)
public class ContainerStatus extends AbstractStatus {

    private static final long serialVersionUID = 1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "containerid")
    @XmlIDREF
    protected Container container;

    @Column(insertable = false, updatable = false)
    private String discriminator;

    private boolean sentMail;

    public ContainerStatus() {
    }

    public ContainerStatus(Container container, StatusEnum statusEnum) {
        setContainer(container);
        setStatusEnum(statusEnum);
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public boolean isSentMail() {
        return sentMail;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void setSentMail(boolean sentMail) {
        this.sentMail = sentMail;
    }
}