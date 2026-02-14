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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.util.ClassHelper;
import org.bfabric.util.StringHelper;

/**
 * The Country class is responsible for the storage of countries. It contains data according to the ISO3166 standard. The whole list can be fetched from
 * <a href="http://www.iso.org/iso/en/prods-services/iso3166ma/index.html">...</a>
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "country_name_unique", columnNames = { "name" }) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@NamedQuery(name = "Country.findById", query = "SELECT a FROM Country a WHERE a.id = :id")
@NamedQuery(name = "Country.findByIdOrName", query = "SELECT a FROM Country a WHERE lower(a.id) = lower(:name) or lower(a.name) = lower(:name)")
@NamedQuery(name = "Country.filterByIdOrName", query = "SELECT a FROM Country a WHERE lower(a.id) LIKE lower(:name) or lower(a.name) LIKE lower(:name)")
@NamedQuery(name = "Country.findByDialingCode", query = "SELECT a FROM Country a WHERE a.dialingCode = :dialingCode")
@NamedQuery(name = "Country.dialingCodes", query = "SELECT DISTINCT a.dialingCode FROM Country a WHERE a.dialingCode IS NOT NULL ORDER BY a.dialingCode")
public class Country implements Serializable, Comparable {

    private static final long serialVersionUID = 1;

    @NotNull
    @Min(0)
    @Max(999)
    @XmlElement
    private Integer dialingCode;

    @Id
    @Size(max = 2)
    private String id;

    @NotBlank
    @Size(max = 128)
    @XmlElement
    private String name;

    public Country() {
    }

    @Override
    public int compareTo(Object object) throws ClassCastException {
        if (object != null) {
            // Important: use trimmed class name because of hibernate proxy issues.
            String objectClassName = ClassHelper.getTrimmedClassName(object.getClass().getName());
            if (objectClassName != null && objectClassName.equals(ClassHelper.getTrimmedClassName(getClass().getName()))) {
                Country entity = (Country) object;
                return getId().compareTo(entity.getId());
            }
            throw new ClassCastException("Cannot compare this " + getClass().getName() + " with " + object.getClass().getName());
        }
        throw new ClassCastException("Cannot compare this " + getClass().getName() + " with null");
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Country && hashCode() == object.hashCode();
    }

    public Integer getDialingCode() {
        return dialingCode;
    }

    public String getDisplayName() {
        return getId() + " - " + getName();
    }

    @XmlID
    @XmlElement
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        if (StringHelper.isEmpty(getId())) {
            return super.hashCode();
        }
        // Important: use trimmed class name because of hibernate proxy issues.
        return ClassHelper.getTrimmedClassName(getClass().getName()).concat(getId()).hashCode();
    }

    public void setDialingCode(Integer dialingCode) {
        this.dialingCode = dialingCode;
    }

    public void setId(String id) {
        this.id = StringHelper.format(id);
    }

    public void setName(String name) {
        this.name = StringHelper.format(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + getId();
    }
}
