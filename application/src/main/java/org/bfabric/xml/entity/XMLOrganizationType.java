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

package org.bfabric.xml.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.OrganizationType;

@XmlRootElement(name = "organizationtype")
public class XMLOrganizationType extends XMLAbstractNamedBaseEntity {

    @XmlElement
    protected Boolean academic;

    @XmlElement
    protected Boolean domestic;

    @XmlElement
    protected Boolean extensible;

    @XmlElement
    private String color;

    @XmlElement
    private Integer companies;

    @XmlElement
    private Integer organizations;

    public XMLOrganizationType() {
    }

    public XMLOrganizationType(OrganizationType entity, boolean reference) {
        super(entity, reference);
    }

    public XMLOrganizationType(OrganizationType entity) {
        super(entity);
        if (entity != null) {
            setAcademic(entity.isAcademic());
            setExtensible(entity.isExtensible());
            setDomestic(entity.isDomestic());
            if (entity.getColor() != null) {
                setColor(entity.getColor());
            }
            if (!entity.getCompanies().isEmpty()) {
                setCompanies(entity.getCompanies().size());
            }
            if (!entity.getOrganizations().isEmpty()) {
                setOrganizations(entity.getOrganizations().size());
            }
        }
    }

    public Boolean getAcademic() {
        return academic;
    }

    public String getColor() {
        return color;
    }

    public Integer getCompanies() {
        return companies;
    }

    public Boolean getDomestic() {
        return domestic;
    }

    public Boolean getExtensible() {
        return extensible;
    }

    public Integer getOrganizations() {
        return organizations;
    }

    public void setAcademic(Boolean academic) {
        this.academic = academic;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setCompanies(Integer companies) {
        this.companies = companies;
    }

    public void setDomestic(Boolean domestic) {
        this.domestic = domestic;
    }

    public void setExtensible(Boolean extensible) {
        this.extensible = extensible;
    }

    public void setOrganizations(Integer organizations) {
        this.organizations = organizations;
    }
}
