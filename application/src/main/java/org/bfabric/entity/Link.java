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

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;

@Entity
@XmlRootElement
@NamedQuery(name = "Link.findByParent", query = "SELECT a FROM Link a WHERE a.parentId = :parentId and a.parentClassName = :parentClassName ORDER BY a.name")
@NamedQuery(name = "Link.findAllParentClassNames", query = "SELECT DISTINCT a.parentClassName FROM Link a WHERE a.parentClassName IS NOT NULL ORDER BY a.parentClassName")
@NamedQuery(name = "Link.findByValidityCheckRequired", query = "SELECT a FROM Link a WHERE a.validityChecked is null or a.validityChecked <= :validityChecked order by a.id")
public class Link extends AbstractParentDependentBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    protected Boolean valid;

    protected LocalDateTime validityChecked;

    @Size(max = Constants.MAX_LENGTH_NAME)
    @XmlElement
    private String name;

    @Size(max = 512)
    @NotEmpty
    @XmlElement
    private String url;

    public Link() {
    }

    public Link(AbstractEntity parent) {
        if (parent != null) {
            setParent(parent);
        }
    }

    public void checkValidity() {
        setValidityChecked(LocalDateTime.now());
        if (isValidUrl()) {
            setValid(UriHelper.existsUrl(getUrl()));
        } else {
            setValid(false);
        }
    }

    @Override
    public Link clone() throws CloneNotSupportedException {
        Link clone = (Link) super.clone();
        ClassHelper.initializeFullObject(clone);
        return clone;
    }

    public Link clone(AbstractEntity parent) throws CloneNotSupportedException {
        Link clone = clone();
        clone.setParent(parent);
        return clone;
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        checkValidity();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getUrl())) {
            addEntityInfoItem(summary, "url", getUrl());
        }
        if (StringHelper.isNotEmpty(getName())) {
            addEntityInfoItem(summary, "name", getName());
        }
        return summary.toString();
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getValid() {
        return valid;
    }

    public LocalDateTime getValidityChecked() {
        return validityChecked;
    }

    public String getValidityCheckedAsText() {
        return getValidityChecked() != null ? Constants.DATE_FORMATTER.format(getValidityChecked()) : "";
    }

    @Override
    public boolean isCreatable() {
        return getParent() != null && getParent().isExtensible();
    }

    @Override
    public boolean isDeletable() {
        return getParent() != null && getParent().isExtensible();
    }

    @Override
    public boolean isReadable() {
        return getParent() != null && getParent().isReadable();
    }

    @Override
    public boolean isUpdatable() {
        return getParent() != null && getParent().isExtensible();
    }

    public boolean isValidUrl() {
        return UriHelper.isValidUrl(getUrl());
    }

    public void setName(String name) {
        this.name = StringHelper.format(name);
    }

    public void setUrl(String url) {
        boolean checkValidity = true;
        String newUrl = StringHelper.format(url);
        if (newUrl == null || newUrl.equals(this.url)) {
            checkValidity = false;
        }
        this.url = newUrl;
        if (checkValidity) {
            checkValidity();
        }
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public void setValidityChecked(LocalDateTime validityChecked) {
        this.validityChecked = validityChecked;
    }
}
