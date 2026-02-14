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
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;

@Entity
@XmlRootElement
@NamedQuery(name = "CustomStatus.findByNameAndType", query = "SELECT a FROM CustomStatus a WHERE lower(a.name) = lower(:name) and lower(a.parentStatusName) = lower(:parentStatusName) AND lower(a.type) = lower(:type)")
public class CustomStatus extends AbstractNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotBlank
    @Size(max = Constants.MAX_LENGTH_NAME)
    @XmlElement
    protected String parentStatusName;

    @NotBlank
    @Size(max = 64)
    @XmlElement
    protected String type;

    @NotBlank
    @Column(length = 9)
    @Size(max = 9)
    @XmlElement
    private String color = "#000000";

    public CustomStatus() {
    }

    public CustomStatus(String name, String parentStatusName, String type) {
        setName(name);
        setParentStatusName(parentStatusName);
        setType(type);
    }

    public String getColor() {
        return color;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    public String getParentStatusName() {
        return parentStatusName;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isDeletable() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public void setColor(String color) {
        this.color = StringHelper.format(color);
    }

    public void setParentStatusName(String parentStatusName) {
        this.parentStatusName = StringHelper.format(parentStatusName);
    }

    public void setType(String type) {
        this.type = type;
    }
}