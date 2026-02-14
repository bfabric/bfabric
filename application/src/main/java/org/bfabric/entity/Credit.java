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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CreditTypeEnum;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexMap;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.AgendaYearClosedService;
import org.bfabric.service.UserService;

@Entity
@Table(name = "AgendaCredit")
@XmlRootElement
@NamedQuery(name = "Credit.findByYear", query = "SELECT a FROM Credit a WHERE a.year = :year")
@NamedQuery(name = "Credit.findByYearAndUser", query = "SELECT a FROM Credit a WHERE a.year = :year and a.user = :user")
public class Credit extends AbstractDescriptionBaseEntity implements ShowScreen, Indexable {

    private static final long serialVersionUID = 1;

    @NotNull
    @Digits(integer = 3, fraction = 2)
    @XmlElement
    private BigDecimal days = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private CreditTypeEnum type = CreditTypeEnum.VACATION;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    @NotNull
    @XmlIDREF
    private User user;

    @NotNull
    @Min(2007)
    @Max(2037)
    @XmlElement
    private int year = LocalDate.now().getYear();

    public Credit() {
        super();
    }

    public BigDecimal getDays() {
        return days;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.AGENDAMANAGER;
    }

    @Override
    @NotBlank
    @Size(max = 256)
    public String getDescription() {
        return super.getDescription();
    }

    public List<User> getEmployeesIncludingUser(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getUser());
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getUser() != null) {
            addEntityInfoItem(summary, "user", getUser().getName());
        }
        addEntityInfoItem(summary, "year", getYear());
        if (getDays() != null) {
            addEntityInfoItem(summary, "days", getDays());
        }
        if (getType() != null) {
            addEntityInfoItem(summary, "type", getType());
        }
        return summary.toString();
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
        fields.add(IndexMapContentEnum.USERID.getField());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.YEAR.getField());
        fields.add(IndexMapContentEnum.DAYS.getField());
        fields.add(IndexMapContentEnum.USER.getField());
        fields.add(IndexMapContentEnum.TYPE.getField());
        fields.add(IndexMapContentEnum.DESCRIPTION.getField());
        return fields;
    }

    @Override
    public IndexMap getIndexMap() throws Exception {
        IndexMap indexMap = super.getIndexMap();
        indexMap.put(Constants.INDEXMAP_GROUP, RoleEnum.AGENDAUSER);
        return indexMap;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getName());
        content.add(IndexMapContentEnum.DAYS, getDays().toString());
        content.add(IndexMapContentEnum.YEAR, Integer.toString(getYear()));
        content.add(IndexMapContentEnum.TYPE, getType().getLabel());

        addUserToIndexMapContent(getUser(), content);

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.CREDIT;
    }

    public String getName() {
        String name = Constants.EMPTY_STRING;
        if (getUser() != null) {
            name = getUser().getFullName() + ": ";
        }
        name += getYear() + ": " + getDays();
        if (getType() != null) {
            name += ": " + getType().getLabel();
        }
        return name;
    }

    public CreditTypeEnum getType() {
        return type;
    }

    public User getUser() {
        return user;
    }

    public int getYear() {
        return year;
    }

    @Override
    public boolean isExtensible() {
        return hasCurrentUserRoleEnum(RoleEnum.AGENDAUSER);
    }

    public boolean isInYear(int agendaYear) {
        return year == agendaYear;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.AGENDAUSER);
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && CDI.current().select(AgendaYearClosedService.class).get().isOpen(getYear());
    }

    public void setDays(BigDecimal days) {
        this.days = days;
    }

    public void setType(CreditTypeEnum type) {
        this.type = type;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
