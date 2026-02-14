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
import java.time.format.DateTimeParseException;
import java.util.Objects;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SystemPropertyDiscriminator;
import org.bfabric.util.StringHelper;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "systemproperty_name_unique", columnNames = { "deployer", "environment", "instance", "name" }) })
@NamedQuery(name = "SystemProperty.findByRoles", query = "SELECT a FROM SystemProperty a WHERE a.requiredRole in (:roles) ORDER BY a.name")
@XmlRootElement
public class SystemProperty extends AbstractNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @XmlElement
    protected String value;

    @XmlElement
    private String comment;

    @XmlElement
    private String deployer;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private SystemPropertyDiscriminator discriminator = SystemPropertyDiscriminator.S;

    @XmlElement
    private String environment;

    @XmlElement
    private String instance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requiredRoleId")
    @XmlIDREF
    private Role requiredRole;

    @Transient
    @NotNull
    private LocalDate valueDate;

    public SystemProperty() {
    }

    public SystemProperty(@NotNull String name, SystemPropertyDiscriminator discriminator, @NotNull String value, String comment, String environment, String deployer, String instance, Role requiredRole) {
        setName(name);
        setDiscriminator(discriminator);
        setValue(value);
        setComment(comment);
        setEnvironment(environment);
        setDeployer(deployer);
        setInstance(instance);
        setRequiredRole(requiredRole);
    }

    public boolean configurationContextMatches(String pEnvironment, String pDeployer, String pInstance) {
        return Objects.equals(environment, pEnvironment) && Objects.equals(deployer, pDeployer) && Objects.equals(instance, pInstance);
    }

    @Override
    public boolean equals(Object entity) {
        if (this == entity) {
            return true;
        }
        if (entity == null) {
            return false;
        }
        if (entity instanceof SystemProperty) {
            SystemProperty other = (SystemProperty) entity;
            return name.equals(other.getName()) && configurationContextMatches(other.getEnvironment(), other.getDeployer(), other.getInstance());
        }
        return false;
    }

    public String getComment() {
        return comment;
    }

    @SuppressWarnings("unused")
    public String getCommentTrimmed() {
        return StringHelper.removeDoubleEmptyLines(getComment());
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        RoleEnum requiredRoleEnum = getRequiredRole() != null ? RoleEnum.value(getRequiredRole().getName()) : null;
        return requiredRoleEnum != null ? requiredRoleEnum : RoleEnum.CONFIGURATIONMANAGER;
    }

    public String getDeployer() {
        return deployer;
    }

    public SystemPropertyDiscriminator getDiscriminator() {
        return discriminator;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getDiscriminator() != null) {
            addEntityInfoItem(summary, "discriminator", getDiscriminator().getLabel());
        }
        if (getEnvironment() != null) {
            addEntityInfoItem(summary, "environment", getEnvironment());
        }
        if (getDeployer() != null) {
            addEntityInfoItem(summary, "deployer", getDeployer());
        }
        if (getInstance() != null) {
            addEntityInfoItem(summary, "instance", getInstance());
        }
        if (getComment() != null) {
            addEntityInfoItem(summary, "comment", getComment());
        }
        return summary.toString();
    }

    public String getEnvironment() {
        return environment;
    }

    public String getInstance() {
        return instance;
    }

    public Role getRequiredRole() {
        return requiredRole;
    }

    public String getValue() {
        return value;
    }

    public LocalDate getValueDate() {
        if (value != null) {
            valueDate = LocalDate.parse(value, Constants.DATE_FORMATTER);
        }
        return valueDate;
    }

    public Object getValueTyped() {
        Object ret = null;
        if (getValue() != null) {
            String trimmedValue = getValue().trim();
            String errMsg = null;
            if (trimmedValue.isEmpty()) {
                errMsg = "errorNullOrEmptyValue";
            } else {
                switch (getDiscriminator()) {
                case B:
                    if (trimmedValue.equalsIgnoreCase("true") || trimmedValue.equalsIgnoreCase("false")) {
                        ret = Boolean.parseBoolean(trimmedValue);
                    } else {
                        errMsg = "errorNotBooleanValue";
                    }
                    break;
                case I:
                    try {
                        ret = Integer.parseInt(trimmedValue);
                    } catch (final NumberFormatException e) {
                        errMsg = "errorNotIntegerValue";
                    }
                    break;
                case L:
                    try {
                        ret = Long.parseLong(trimmedValue);
                    } catch (final NumberFormatException e) {
                        errMsg = "errorNotLongValue";
                    }
                    break;
                case N:
                    try {
                        ret = Double.parseDouble(trimmedValue);
                    } catch (final NumberFormatException e) {
                        errMsg = "errorNotNumericValue";
                    }
                    break;
                case D:
                    try {
                        ret = LocalDate.parse(trimmedValue);
                    } catch (final DateTimeParseException e) {
                        errMsg = "errorNotDateValue";
                    }
                    break;
                default: // String
                    ret = trimmedValue;
                    break;
                }
            }

            if (errMsg != null) {
                System.out.println("Error system property name=" + getName() + " value=" + trimmedValue + " disc=" + getDiscriminator().name());
                // throw new BfabricValidatorException(errMsg);
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + name.hashCode();
        result = PRIME * result + (environment == null ? 0 : environment.hashCode());
        result = PRIME * result + (deployer == null ? 0 : deployer.hashCode());
        result = PRIME * result + (comment == null ? 0 : comment.hashCode());
        result = PRIME * result + (instance == null ? 0 : instance.hashCode());
        return result;
    }

    @Override
    public boolean isDeletable() {
        return getDeployer() != null || getEnvironment() != null || getInstance() != null;
    }

    public boolean isDiscriminatorEditable() {
        return getId() == 0;
    }

    public void setComment(String comment) {
        this.comment = StringHelper.format(comment);
    }

    public void setDeployer(String deployer) {
        this.deployer = StringHelper.format(deployer);
    }

    public void setDiscriminator(SystemPropertyDiscriminator discriminator) {
        this.discriminator = discriminator;
    }

    public void setEnvironment(String environment) {
        this.environment = StringHelper.format(environment);
    }

    public void setInstance(String instance) {
        this.instance = StringHelper.format(instance);
    }

    public void setRequiredRole(Role requiredRole) {
        this.requiredRole = requiredRole;
    }

    public void setValue(String value) {
        this.value = value != null && SystemPropertyDiscriminator.B.equals(getDiscriminator()) ? StringHelper.format(value.toLowerCase()) : value;
    }

    public void setValueDate(LocalDate dateValue) {
        this.valueDate = dateValue;
        if (dateValue != null) {
            setValue(Constants.DATE_FORMATTER.format(dateValue));
        } else {
            setValue(null);
        }
    }

    public void systemPropertyDiscriminatorChanged(ValueChangeEvent event) {
        setDiscriminator((SystemPropertyDiscriminator) event.getNewValue());
        setValue(null);
    }
}