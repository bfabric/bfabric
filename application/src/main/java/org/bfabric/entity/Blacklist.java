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

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "blacklist_name_context_unique", columnNames = { "name", "context" }) })
@XmlRootElement
@NamedQuery(name = "Blacklist.checkUniqueName", query = "SELECT a.id FROM Blacklist a WHERE lower(a.name) = lower(:name) and a.id <> :id and a.context = :context")
@NamedQuery(name = "Blacklist.checkExists", query = "SELECT a.id FROM Blacklist a WHERE lower(a.name) = lower(:name) and lower(a.context) = lower(:context)")
public class Blacklist extends AbstractNamedBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @NotBlank
    @Size(max = 16)
    @XmlElement
    private String context;

    public Blacklist() {
        super();
    }

    public String getContext() {
        return context;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.ADMIN;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getContext())) {
            addEntityInfoItem(summary, "context", getContext());
        }
        return summary.toString();
    }

    public void setContext(String context) {
        this.context = StringHelper.formatLowerCase(context);
    }

    @Override
    public void setName(final String name) {
        super.setName(StringHelper.formatLowerCase(name));
    }
}