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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.QueryDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.enums.IndexMapTypeEnum;
import org.bfabric.util.DateUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class Query extends AbstractNamedBaseEntity implements NotEntityLoggable {

    public static final String AND_BOOL = "and";

    public static final String ASCENDING = "asc";

    public static final int DATE_TYPE_NONE = -1;

    public static final int DATE_TYPE_RANGE = 1;

    public static final int DATE_TYPE_RELATIVE = 0;

    public static final String DEFAULT_COLUMN = "modified";

    public static final String DESCENDING = "desc";

    public static final String OR_BOOL = "or";

    public static final int RELATIVE_DATE_CURRENT_MONTH = 5;

    public static final int RELATIVE_DATE_FORTNIGHT = 4;

    public static final int RELATIVE_DATE_HOUR = 0;

    public static final int RELATIVE_DATE_NONE = -1;

    public static final int RELATIVE_DATE_SIX_MONTHS = 8;

    public static final int RELATIVE_DATE_THREE_MONTHS = 7;

    public static final int RELATIVE_DATE_TODAY = 1;

    public static final int RELATIVE_DATE_TWELVE_MONTHS = 9;

    public static final int RELATIVE_DATE_TWO_MONTHS = 6;

    public static final int RELATIVE_DATE_WEEK = 3;

    public static final int RELATIVE_DATE_YESTERDAY = 2;

    private static final long serialVersionUID = 1;

    @Size(max = 16)
    @XmlElement
    private String bool = AND_BOOL;

    @Transient
    private boolean creator = true;

    @OneToMany(mappedBy = "query", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlElement(name = "criterion")
    private List<Criterion> criteria = new ArrayList<>();

    @NotNull
    @XmlElement
    private int dateType = DATE_TYPE_NONE;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private QueryDiscriminator discriminator = QueryDiscriminator.NONE;

    @Column(name = "endDate")
    @XmlElement
    private LocalDateTime end;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private IndexMapTypeEnum indexMapType = IndexMapTypeEnum.all;

    @Transient
    private boolean listing = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean published = false;

    @NotNull
    @XmlElement
    private int relative = RELATIVE_DATE_NONE;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean showAll = true;

    @Size(max = 64)
    @XmlElement
    private String sortColumn = DEFAULT_COLUMN;

    @Size(max = 64)
    @XmlElement
    private String sortDirection = DESCENDING;

    @Column(name = "startDate")
    @XmlElement
    private LocalDateTime start;

    @NotNull
    @XmlElement
    private long total;

    public Query() {
        super();
        setTotal(0);
        setBool(AND_BOOL);
    }

    public Query(final long id) {
        super();
        setId(id);
        setTotal(0);
        setBool(AND_BOOL);
    }

    public static Map<Integer, String> getRelativeMap() {
        HashMap<Integer, String> relativeMap = new HashMap<>();
        relativeMap.put(RELATIVE_DATE_HOUR, Messages.get("queryDetails_RELATIVE_DATE_HOUR"));
        relativeMap.put(RELATIVE_DATE_TODAY, Messages.get("queryDetails_RELATIVE_DATE_TODAY"));
        relativeMap.put(RELATIVE_DATE_YESTERDAY, Messages.get("queryDetails_RELATIVE_DATE_YESTERDAY"));
        relativeMap.put(RELATIVE_DATE_WEEK, Messages.get("queryDetails_RELATIVE_DATE_WEEK"));
        relativeMap.put(RELATIVE_DATE_FORTNIGHT, Messages.get("queryDetails_RELATIVE_DATE_FORTNIGHT"));
        relativeMap.put(RELATIVE_DATE_CURRENT_MONTH, Messages.get("queryDetails_RELATIVE_DATE_CURRENT_MONTH"));
        relativeMap.put(RELATIVE_DATE_TWO_MONTHS, Messages.get("queryDetails_RELATIVE_DATE_TWO_MONTHS"));
        relativeMap.put(RELATIVE_DATE_THREE_MONTHS, Messages.get("queryDetails_RELATIVE_DATE_THREE_MONTHS"));
        relativeMap.put(RELATIVE_DATE_SIX_MONTHS, Messages.get("queryDetails_RELATIVE_DATE_SIX_MONTHS"));
        relativeMap.put(RELATIVE_DATE_TWELVE_MONTHS, Messages.get("queryDetails_RELATIVE_DATE_TWELVE_MONTHS"));
        return relativeMap;
    }

    public static List<String> getRelativeTimes() {
        List<String> times = new ArrayList<>();
        times.add(String.valueOf(RELATIVE_DATE_HOUR));
        times.add(String.valueOf(RELATIVE_DATE_TODAY));
        times.add(String.valueOf(RELATIVE_DATE_YESTERDAY));
        times.add(String.valueOf(RELATIVE_DATE_WEEK));
        times.add(String.valueOf(RELATIVE_DATE_FORTNIGHT));
        times.add(String.valueOf(RELATIVE_DATE_CURRENT_MONTH));
        times.add(String.valueOf(RELATIVE_DATE_TWO_MONTHS));
        times.add(String.valueOf(RELATIVE_DATE_THREE_MONTHS));
        times.add(String.valueOf(RELATIVE_DATE_SIX_MONTHS));
        times.add(String.valueOf(RELATIVE_DATE_TWELVE_MONTHS));
        return times;
    }

    private static LocalDateTime setToMidnight(LocalDateTime localDateTime) {
        return localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public void addCriterion(final Criterion criterion) {
        if (criterion != null) {
            getCriteria().add(criterion);
            criterion.setQuery(this);
        }
    }

    public void addCriterionDefault() {
        addCriterion(new Criterion(IndexMapContentEnum.ANY.getField(), "*"));
    }

    public void cleanCriteria() {
        List<Criterion> cleanedCriteria = new ArrayList<>();
        for (Criterion criterion : getCriteria()) {
            if (criterion.getTerm() != null && !criterion.getTerm().isEmpty()) {
                boolean found = false;
                Iterator<Criterion> criterionIter = cleanedCriteria.iterator();
                while (criterionIter.hasNext() && !found) {
                    if (criterion.equals(criterionIter.next())) {
                        found = true;
                    }
                }
                if (!found) {
                    // add non-empty and non-duplicate to cleaned set of criteria
                    cleanedCriteria.add(criterion);
                }
            }
        }
        setCriteria(cleanedCriteria);
        if (getCriteria().isEmpty()) {
            addCriterionDefault();
        }
    }

    @Override
    public Query clone() throws CloneNotSupportedException {
        final Query clone = (Query) super.clone();
        clone.setPublished(false); // default published false
        clone.setName(null);
        clone.setCriteria(new ArrayList<>());
        for (Criterion crit : getCriteria()) {
            clone.addCriterion(crit.clone());
        }
        return clone;
    }

    public String getBool() {
        return bool;
    }

    public List<Criterion> getCriteria() {
        return criteria;
    }

    public int getDateType() {
        return dateType;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.QUERYMANAGER;
    }

    public QueryDiscriminator getDiscriminator() {
        return discriminator;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public String getExportFileName() {
        return "Search Results " + getQueryDetails().replaceAll(":", Constants.EMPTY_STRING);
    }

    public IndexMapTypeEnum getIndexMapType() {
        return indexMapType;
    }

    public String getQueryDetails() {
        // Clean query, e.g., remove empty and duplicate criteria, before aggregating the query details.
        cleanCriteria();
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(isShowAll() ? Messages.get("all") : Messages.get("my")).append(" ");
        if (getIndexMapType().equals(IndexMapTypeEnum.all)) {
            nameBuilder.append(Messages.get("documents")).append(":");
        } else {
            nameBuilder.append(StringHelper.transformColumnName(getIndexMapType().name())).append(Constants.PLURAL_S);
            if (!isListing()) {
                nameBuilder.append(":");
            }
        }
        if (!isListing()) {
            boolean isFirst = true;
            for (Criterion criterion : getCriteria()) {
                if (!isFirst) {
                    nameBuilder.append(" ").append(getBool());
                } else {
                    isFirst = false;
                }
                nameBuilder.append(" ").append(StringHelper.transformColumnName(criterion.getField())).append(" = ").append(criterion.getTerm());
            }
            if (getStart() != null || getEnd() != null) {
                nameBuilder.append(" ").append(Messages.get("queryDetailsAnd")).append(" ").append(Messages.get("modified")).append(" [");
                if (getStart() != null) {
                    nameBuilder.append(DateUtils.getDateAsFormattedStringWithoutTime(getStart()));
                }
                nameBuilder.append(", ");
                if (getEnd() != null) {
                    nameBuilder.append(DateUtils.getDateAsFormattedStringWithoutTime(getEnd()));
                }
                nameBuilder.append("] ");
            }
            if (getRelative() != RELATIVE_DATE_NONE) {
                nameBuilder.append(" ").append(Messages.get("queryDetailsAnd")).append(" ").append(Messages.get("modified")).append(" ").append(getRelativeMap().get(getRelative()));
            }
            if (getSortColumn() != null && getSortDirection() != null) {
                nameBuilder.append(" ").append(Messages.get("queryDetailsOrderBy")).append(" ").append(StringHelper.transformColumnName(getSortColumn())).append(" ")
                    .append(Messages.get("queryDetails" + StringHelper.firstUpper(getSortDirection())));
            }
        }
        return nameBuilder.toString();
    }

    public int getRelative() {
        return relative;
    }

    public LocalDateTime getRelativeTime() {
        LocalDateTime relativeTime = LocalDateTime.now();
        switch (getRelative()) {
        case RELATIVE_DATE_HOUR:
            return relativeTime.minusDays(1);
        case RELATIVE_DATE_TODAY:
            return setToMidnight(relativeTime);
        case RELATIVE_DATE_YESTERDAY:
            return setToMidnight(relativeTime).minusDays(1);
        case RELATIVE_DATE_WEEK:
            return setToMidnight(relativeTime).plusDays(1);
        case RELATIVE_DATE_CURRENT_MONTH:
            return setToMidnight(relativeTime).withDayOfMonth(1);
        case RELATIVE_DATE_TWO_MONTHS:
            return setToMidnight(relativeTime).minusMonths(2);
        case RELATIVE_DATE_THREE_MONTHS:
            return setToMidnight(relativeTime).minusMonths(3);
        case RELATIVE_DATE_SIX_MONTHS:
            return setToMidnight(relativeTime).minusMonths(6);
        case RELATIVE_DATE_TWELVE_MONTHS:
            return setToMidnight(relativeTime).minusMonths(12);
        default:
            return relativeTime.minusHours(1);
        }
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public String getTitle() {
        return name == null ? getQueryDetails() : super.getName();
    }

    public long getTotal() {
        return total;
    }

    public boolean hasMultipleCriteria() {
        return getCriteria().size() > 1;
    }

    public boolean isCollection() {
        return getId() != 0L;
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    public boolean isCreator() {
        return creator;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    public boolean isListing() {
        return listing;
    }

    public boolean isPublishable() {
        return !isPublished() && (hasCurrentUserRoleEnum(RoleEnum.QUERYMANAGER) || hasCurrentUserRoleEnum(RoleEnum.USER));
    }

    public boolean isPublished() {
        return published;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isCreator() || isPublished();
    }

    public boolean isResourcesQuery() {
        return IndexMapTypeEnum.resource.equals(getIndexMapType()) || IndexMapTypeEnum.workunit.equals(getIndexMapType());
    }

    public boolean isResultNotEmpty() {
        return total > 0;
    }

    public boolean isSaved() {
        return QueryDiscriminator.SAVED.equals(discriminator);
    }

    public boolean isShowAll() {
        return showAll;
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isCreator();
    }

    public void removeCriterion(final Criterion criterion) {
        if (getCriteria().remove(criterion)) {
            criterion.setQuery(null);
        }
    }

    public void resetDateTypeFilter() {
        switch (getDateType()) {
        case DATE_TYPE_RELATIVE:
            setRelative(RELATIVE_DATE_HOUR);
            setStart(null);
            setEnd(null);
            break;
        case DATE_TYPE_RANGE:
            setRelative(RELATIVE_DATE_NONE);
            setStart(setToMidnight(LocalDateTime.now()).minusDays(1));
            setEnd(LocalDateTime.now());
            break;
        default:
            setRelative(RELATIVE_DATE_NONE);
            setStart(null);
            setEnd(null);
            break;
        }
    }

    public void resetName() {
        String newName = getQueryDetails();
        if (StringHelper.isNotEmpty(newName) && newName.length() > 256) {
            newName = newName.substring(0, 255);
        }
        setName(newName);
    }

    public void setBool(String bool) {
        this.bool = bool;
    }

    public void setCreator(boolean creator) {
        this.creator = creator;
    }

    public void setCriteria(final List<Criterion> criteria) {
        this.criteria = criteria;
    }

    public void setCriterion(String field, String term) {
        boolean duplicate = false;
        // Avoid duplicates
        for (Criterion crit : getCriteria()) {
            if (crit.getField().equals(field) && crit.getTerm().equalsIgnoreCase(term)) {
                duplicate = true;
                break;
            }
        }
        if (!duplicate && term != null && !term.isEmpty()) {
            // Add a new criterion
            addCriterion(new Criterion(field, term));
        }
    }

    public void setDateType(int dateType) {
        this.dateType = dateType;
    }

    public void setDiscriminator(QueryDiscriminator discriminator) {
        this.discriminator = discriminator;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void setIndexMapType(IndexMapTypeEnum indexMapType) {
        this.indexMapType = indexMapType;
    }

    public void setListing(boolean listing) {
        this.listing = listing;
    }

    public void setName() {
        if (getName() == null || StringHelper.isEmpty(StringHelper.format(getName()))) {
            resetName();
        }
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public void setRelative(int relative) {
        this.relative = relative;
    }

    public void setSearchBasics(final Boolean creator, final Boolean showAll) {
        // Set listing by default to false
        setListing(false);
        // Set creator to show items of all or my containers
        setCreator(creator != null && creator);
        // Set whether to show all or my items
        setShowAll(showAll == null || showAll);
    }

    public void setSearchByIndexMapType(final Boolean creator, final String indexMapType) {
        if (creator != null && creator) {
            addCriterion(new Criterion(IndexMapContentEnum.CREATEDBY.getField(), getCurrentUser().getLogin()));
        }
        IndexMapTypeEnum indexMapTypeEnum = IndexMapTypeEnum.value(indexMapType);
        if (indexMapTypeEnum != null) {
            setListing(true);
        } else {
            indexMapTypeEnum = IndexMapTypeEnum.all;
            setShowAll(true);
        }
        setIndexMapType(indexMapTypeEnum);
        setSortColumn(Constants.ID);
        setSortDirection(DESCENDING);
    }

    public void setSearchTermForQuickSearch(final String term) {
        setBool(OR_BOOL);
        addCriterion(new Criterion(IndexMapContentEnum.NAME.getField(), term));
        addCriterion(new Criterion(IndexMapContentEnum.ID.getField(), term));
        if (hasCurrentUserRoleEnum(RoleEnum.USERREADER)) {
            addCriterion(new Criterion(IndexMapContentEnum.LOGIN.getField(), term));
            addCriterion(new Criterion(IndexMapContentEnum.EMAIL.getField(), term));
        }
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void showAllChanged(ValueChangeEvent event) {
        setShowAll((Boolean) event.getNewValue());
    }
}