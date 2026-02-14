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

package org.bfabric.manager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Transient;
import javax.transaction.Transactional;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.Criterion;
import org.bfabric.entity.Query;
import org.bfabric.entity.ResourceBasket;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.QueryDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.SampleAttributeEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.indexer.Indexer;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.indexer.enums.IndexMapTypeEnum;
import org.bfabric.indexer.enums.IndexerEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.ResourceList;
import org.bfabric.service.QueryService;
import org.bfabric.service.util.QueryTableModel;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.DateUtils;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Faces;

@MeasureCalls
@Named
@ViewScoped
public class QueryManager extends AbstractEntityManager<Query> {

    private static final Logger logger = Logger.getLogger(QueryManager.class.getName());

    private static final long serialVersionUID = 1;

    @Transient
    List<String> resultColumns;

    @Param
    private Boolean creator;

    private boolean excludeFeeder;

    private transient TopDocs hits;

    private transient IndexSearcher indexSearcher;

    // Used on the manage queries admin page
    private boolean loggedQueries = false;

    private transient QueryTableModel<?> model;

    private transient org.apache.lucene.search.Query permissionFilterQuery;

    @Inject
    private QueryService queryService;

    @Inject
    private ResourceBasketHelper resourceBasketHelper;

    @Inject
    private ResourceList resourceList;

    @Param
    private Boolean showAll;

    // false only if the search form is reached without running a query
    private boolean showResults = true;

    private Object tableState;

    @Param
    private String term;

    @Param
    private String type;

    public QueryManager() {
        super(Query.class);
    }

    private static org.apache.lucene.search.Query addRangeDateFilter(LocalDateTime start, LocalDateTime end) {
        return TermRangeQuery.newStringRange(IndexMapContentEnum.MODIFIED.getField(), DateUtils.getDateIndexString(start), DateUtils.getDateIndexString(end), true, true);
    }

    public void addCriterionDefault() {
        getQuery().addCriterionDefault();
        queryChanged();
    }

    private void addFilters(BooleanQuery.Builder booleanQueryBuilder) {
        switch (getQuery().getDateType()) {
        case Query.DATE_TYPE_RELATIVE:
            booleanQueryBuilder.add(addRelativeDateFilter(), BooleanClause.Occur.FILTER);
            break;
        case Query.DATE_TYPE_RANGE:
            booleanQueryBuilder.add(addRangeDateFilter(getQuery().getStart(), getQuery().getEnd()), BooleanClause.Occur.FILTER);
            break;
        default:
            break;
        }

        if (!IndexMapTypeEnum.all.equals(getQuery().getIndexMapType())) {
            booleanQueryBuilder.add(addIndexMapTypeFilter(), BooleanClause.Occur.FILTER);
        }

        if (permissionFilterQuery != null || getPermissionFilterQuery() != null) {
            booleanQueryBuilder.add(permissionFilterQuery, BooleanClause.Occur.FILTER);
        }
    }

    private org.apache.lucene.search.Query addIndexMapTypeFilter() {
        // Lucene index stores indexMapType in lowercase. Hence, we need to lower case the indexMapType.
        return new TermQuery(new Term(IndexMapContentEnum.INDEXMAPTYPE.getField(), getQuery().getIndexMapType().name().toLowerCase()));
    }

    private void addPermGroupTerm(BooleanQuery.Builder accessibilityQueryBuilder) {
        for (final String group : getCurrentUser().getCurrentUserRoleNames()) {
            accessibilityQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.PERM_GROUP.getField(), group.toLowerCase())), BooleanClause.Occur.SHOULD);
        }
        for (final Container container : getCurrentUser().getContainers()) {
            accessibilityQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.PERM_GROUP.getField(), container.getMemberRoleName().toLowerCase())), BooleanClause.Occur.SHOULD);
        }
    }

    private org.apache.lucene.search.Query addRelativeDateFilter() {
        return addRangeDateFilter(getQuery().getRelativeTime(), LocalDateTime.now());
    }

    public void addResourcesToBasket(ResourceBasket resourceBasket) {
        if (getQuery() != null && getQuery().isResourcesQuery()) {
            final int resourceBasketLimit = resourceBasketHelper.getResourceBasketLimit();
            final Set<Long> resourceIds = new HashSet<>();
            final Set<Long> workunitIds = new HashSet<>();

            if (IndexMapTypeEnum.resource.equals(getQuery().getIndexMapType())) {
                if (getModel().getSize() <= resourceBasketLimit) {
                    for (final HashMap<String, Object> item : getModel().getWrapperData(getModel().getSize())) {
                        resourceIds.add(Long.valueOf(item.get("id").toString()));
                    }
                    resourceBasketHelper.addResourcesToBasket(resourceBasket, resourceList.getResourcesByIds(resourceIds));
                } else {
                    getFacesMessagesManager().clearGlobalMessages();
                    getFacesMessagesManager().printWarn(Messages.get("resourcesLimitExceeds").replace("{0}", Integer.toString(resourceBasketHelper.getResourceBasketLimit())));
                }
            } else if (IndexMapTypeEnum.workunit.equals(getQuery().getIndexMapType())) {
                long resourcesCount = 0;
                for (final HashMap<String, Object> item : getModel().getWrapperData(getModel().getSize())) {
                    resourcesCount += Long.parseLong(item.get(IndexMapContentEnum.RESOURCES.getField()).toString());
                    if (resourcesCount <= resourceBasketLimit) {
                        workunitIds.add(Long.valueOf(item.get("id").toString()));
                    } else {
                        break;
                    }
                }
                resourceBasketHelper.addResourcesToBasket(resourceBasket, resourceList.getResourcesByWorkunitIds(workunitIds));
            }
        }
    }

    public void clearCriteria() {
        getQuery().getCriteria().clear();
        queryChanged();
    }

    public void cloneInstance() {
        if (getQuery() != null) {
            try {
                setInstance(getQuery().clone());
                setIdLong(0L);
            } catch (final CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    public void dateTypeChanged(ValueChangeEvent event) {
        getQuery().setDateType((Integer) event.getNewValue());
        resetDateTypeFilter();
    }

    public void doQuery() {
        // clean query: remove empty and duplicate criteria
        getQuery().cleanCriteria();

        // do not log queries that are triggered by the listings or just search for anything.
        if (QueryDiscriminator.NONE.equals(getQuery().getDiscriminator()) && !getQuery().isListing()) {
            if (getQuery().getCriteria().size() == 1) {
                final Criterion criterion = getQuery().getCriteria().iterator().next();
                final Criterion any = new Criterion(IndexMapContentEnum.ANY.getField(), "*");
                final Criterion currentUser = new Criterion("createdBy", getCurrentUser().getLogin());
                if (!(any.equals(criterion) || criterion.equals(currentUser))) {
                    getQuery().setDiscriminator(QueryDiscriminator.LOGGED);
                }
            } else {
                getQuery().setDiscriminator(QueryDiscriminator.LOGGED);
            }
        }

        model = new QueryTableModel<>(this);

        search(getSessionManager().getListingRows());

        // since the query is executed, the results should be shown.
        setShowResults(true);

        if (!QueryDiscriminator.NONE.equals(getQuery().getDiscriminator())) {
            getQuery().setName();
            getQuery().setDiscriminator(QueryDiscriminator.LOGGED);
            save();
            getFacesMessagesManager().clearGlobalMessages();
        }
    }

    public Map<Integer, Map<String, Object>> findLuceneResults(int firstRow, int numberOfRows) {
        final Map<Integer, Map<String, Object>> res = Collections.synchronizedMap(new LinkedHashMap<>());
        try {
            final ScoreDoc[] resultHits = getHits(firstRow + numberOfRows).scoreDocs;
            final long endIndex = firstRow + numberOfRows > getQuery().getTotal() ? getQuery().getTotal() : firstRow + numberOfRows;

            for (int i = firstRow; i < endIndex; i++) {
                final Map<String, Object> hit = new LinkedHashMap<>();
                final int docId = resultHits[i].doc;
                final Document doc = getIndexSearcher().doc(docId);
                // Put the values into the hit map.
                for (final IndexableField field : doc.getFields()) {
                    hit.put(field.name(), field.stringValue());
                }
                // Add the hit to the result.
                res.put(docId, hit);
            }
        } catch (final Exception e) {
            logger.severe(Messages.get("failedToCreateSearchResult") + ": " + e);
        }
        return res;
    }

    public String getExcludeFeeder() {
        return isExcludeFeeder() ? Messages.get("includeFeeder") : Messages.get("excludeFeeder");
    }

    public String getExcludeFeederHint() {
        return isExcludeFeeder() ? Messages.get("includeFeederHint") : Messages.get("excludeFeederHint");
    }

    public TopDocs getHits(int maxSearchResults) {
        if (hits == null || hits.scoreDocs.length < maxSearchResults) {
            search(maxSearchResults);
        }
        return hits;
    }

    @CachedMethodResult
    public Collection<String> getIndexMapTypeFields(String indexMapType) {
        return IndexMapEnum.getIndexMapTypeFields(indexMapType);
    }

    public Collection<String> getIndexMapTypes() {
        return IndexMapEnum.getIndexMapTypes(false);
    }

    public IndexSearcher getIndexSearcher() {
        if (indexSearcher == null) {
            try {
                final Indexer indexer = IndexerEnum.ALL.getIndexer();
                indexSearcher = new IndexSearcher(DirectoryReader.open(indexer.getIndexWriter(), true, true));
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return indexSearcher;
    }

    public Object getKey(HashMap<String, Object> map) {
        return map.get(IndexMapContentEnum.ID.getField());
    }

    public QueryTableModel<?> getModel() {
        return model;
    }

    public String getPageTitle() {
        if (!getQuery().isListing()) {
            return Messages.get("search");
        }
        return (getQuery().isShowAll() ? Messages.get("all") : Messages.get("my")) + " " + Messages.get(getQuery().getIndexMapType().name() + Constants.PLURAL_S);
    }

    private org.apache.lucene.search.Query getPermissionFilterQuery() {
        if (!getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.ADMIN)) {
            final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            boolean isBooleanQuerySet = false;
            // Add accessibility filter
            final BooleanQuery.Builder accessibilityQueryBuilder = new BooleanQuery.Builder();
            boolean isAccessibilityQuerySet = false;

            if (getQuery() != null && getQuery().isShowAll()) {
                // Show all available documents
                if (IndexMapEnum.isContainerDependent(getQuery().getIndexMapType().name()) || IndexMapTypeEnum.all.equals(getQuery().getIndexMapType())) {
                    if (!getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER)) {
                        // Place restriction on published documents if the user is no container reader who searches for all containerDependent documents.
                        accessibilityQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.PERM_STATUS.getField(), StatusEnum.PUBLISHED.getLabel())), BooleanClause.Occur.SHOULD);
                        addPermGroupTerm(accessibilityQueryBuilder);
                        isAccessibilityQuerySet = true;

                        // Place restriction to containers with doi if the user has not the 'user' role.
                        if (!getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.USER)) {
                            accessibilityQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.PERM_DOI.getField(), "true")), BooleanClause.Occur.MUST);
                        }
                    }

                    // Filter document types the user has access for.
                    final BooleanQuery.Builder roleBoundQueryBuilder = new BooleanQuery.Builder();
                    for (final String indexMapType : getIndexMapTypes()) {
                        roleBoundQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.INDEXMAPTYPE.getField(), indexMapType.toLowerCase())), BooleanClause.Occur.SHOULD);
                    }
                    booleanQueryBuilder.add(roleBoundQueryBuilder.build(), BooleanClause.Occur.MUST);
                    isBooleanQuerySet = true;
                }
            } else {
                // Filter documents for which the user owns the required role.
                addPermGroupTerm(accessibilityQueryBuilder);
                isAccessibilityQuerySet = true;

                // Filter containerDependent document types.
                final BooleanQuery.Builder containerDependentQueryBuilder = new BooleanQuery.Builder();
                for (final String containerDependentIndexMapType : IndexMapEnum.getContainerDependentIndexMapTypes()) {
                    containerDependentQueryBuilder.add(new TermQuery(new Term(IndexMapContentEnum.INDEXMAPTYPE.getField(), containerDependentIndexMapType.toLowerCase())), BooleanClause.Occur.SHOULD);
                }
                booleanQueryBuilder.add(containerDependentQueryBuilder.build(), BooleanClause.Occur.MUST);
                isBooleanQuerySet = true;
            }

            if (isAccessibilityQuerySet) {
                booleanQueryBuilder.add(accessibilityQueryBuilder.build(), BooleanClause.Occur.MUST);
            }
            if (isBooleanQuerySet || isAccessibilityQuerySet) {
                permissionFilterQuery = booleanQueryBuilder.build();
            }
        }
        return permissionFilterQuery;
    }

    @Produces
    @Named("query")
    public Query getQuery() {
        return getInstance();
    }

    public List<String> getResultColumns() {
        if (resultColumns == null) {
            resultColumns = new ArrayList<>();
            resultColumns.add(IndexMapContentEnum.ID.getField());
            resultColumns.add(IndexMapContentEnum.NAME.getField());
            final Query query = getQuery();
            if (query != null && !query.getIndexMapType().equals(IndexMapTypeEnum.all)) {
                try {
                    // Determine the document specific columns
                    resultColumns.addAll(((Indexable) ClassHelper.getNewObject(query.getIndexMapType().name())).getIndexListingFields());
                } catch (final Exception e) {
                    logger.severe("Class not found for name " + getQuery().getIndexMapType() + ": " + e);
                }
            } else {
                resultColumns.add(IndexMapContentEnum.INDEXMAPTYPE.getField());
            }
            resultColumns.add(IndexMapContentEnum.CREATEDBY.getField());
            resultColumns.add(IndexMapContentEnum.CREATED.getField());
            resultColumns.add(IndexMapContentEnum.MODIFIEDBY.getField());
            resultColumns.add(IndexMapContentEnum.MODIFIED.getField());
        }
        return resultColumns;
    }

    public int getRowCount() {
        return getModel().getRowCount();
    }

    public int getRows() {
        return getSessionManager().getListingRows();
    }

    public Object getTableState() {
        return tableState;
    }

    public long getTotal() {
        return getQuery().getTotal();
    }

    public int getTotalHits() {
        return getTotalHits(hits);
    }

    public int getTotalHits(TopDocs topDocsHits) {
        return topDocsHits == null ? 0 : (int) topDocsHits.totalHits.value;
    }

    public void indexMapTypeChanged(ValueChangeEvent event) {
        final IndexMapTypeEnum indexMapType = (IndexMapTypeEnum) event.getNewValue();
        if (!getQuery().getIndexMapType().equals(indexMapType)) {
            getQuery().setIndexMapType(indexMapType);
            queryChanged();
            resetCriteria();
        }
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        // reset local cache
        resetPermissionFilter();
        if (getQuery() != null) {
            getQuery().setSearchBasics(creator, showAll);
            creator = getQuery().isCreator();
            if (StringHelper.isNotEmpty(term) && !term.equals(Messages.get("searchTerm"))) {
                logger.fine("Execute Quick Search: " + term);
                getQuery().setSearchTermForQuickSearch(term);
                doQuery();
            } else if (type != null) {
                logger.fine("Execute Search with Type: " + type);
                clearCriteria();
                getQuery().setSearchByIndexMapType(creator, type);
                doQuery();
            } else if (id != null) {
                logger.fine("Execute Saved Query: " + getQuery().getId());
                cloneInstance();
                doQuery();
            } else {
                logger.fine("Show Empty Search Form");
                resetCriteria();
            }
        }
        excludeFeeder = getSessionManager().isExcludeFeeder();
    }

    public void initInstance() {
        if (getId() == null) {
            setInstance(createInstance());
        } else {
            if (getIdLong() != null) {
                setInstance(loadInstance());
                if (getInstance() == null) {
                    redirectToEntityNotFoundErrorPage();
                }
            } else {
                redirectToEntityIdInvalidErrorPage();
            }
        }
    }

    public void initShowQuery() {
        cloneInstance();
    }

    @CachedMethodResult
    public boolean isCustomField(String fieldName) {
        return IndexMapContentEnum.getIndexMapFieldEnum(fieldName) == null && SampleAttributeEnum.getAttributeByName(fieldName) == null;
    }

    public boolean isExcludeFeeder() {
        return excludeFeeder;
    }

    public boolean isLoggedQueries() {
        return loggedQueries;
    }

    public boolean isRenderedAddResourcesToBasketButton() {
        return getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.USER) && getQuery() != null && getQuery().isResourcesQuery();
    }

    public boolean isShowResults() {
        return showResults;
    }

    public String publishQuery(Query query) {
        if (query != null) {
            queryService.publishQuery(query);
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyPublished"));
            final String viewId = Faces.getViewId();
            return createRedirectURL(viewId != null && viewId.contains("query/list-all") ? "query/list-all" : "query/list-my");
        }
        return null;
    }

    public void queryChanged() {
        resetPermissionFilter();
        getQuery().setListing(false);
        setShowResults(false);
    }

    public void rangeEndChanged() {
        if (getQuery().getStart() != null && getQuery().getEnd() != null && getQuery().getStart().isAfter(getQuery().getEnd())) {
            getQuery().setStart(getQuery().getEnd());
        }
        queryChanged();
    }

    public void rangeStartChanged() {
        if (getQuery().getStart() != null && getQuery().getEnd() != null && getQuery().getStart().isAfter(getQuery().getEnd())) {
            getQuery().setEnd(getQuery().getStart());
        }
        queryChanged();
    }

    public void removeCriterion(Criterion criterion) {
        if (criterion != null) {
            for (final Criterion crit : getQuery().getCriteria()) {
                if (crit.getField().equalsIgnoreCase(criterion.getField())) {
                    if (crit.getTerm() == null) {
                        if (criterion.getTerm() == null) {
                            getQuery().removeCriterion(crit);
                            break;
                        }
                    } else {
                        if (crit.getTerm().equalsIgnoreCase(criterion.getTerm())) {
                            getQuery().removeCriterion(crit);
                            break;
                        }
                    }
                }
            }
            queryChanged();
        }
    }

    public String removeQuery(Query query) {
        // Remember the last query context
        final Query curQuery = getInstance();

        // Set query to be published
        setInstance(query);

        // Remove query
        String ret = super.remove();

        // Reset to the last query context
        setInstance(curQuery);

        if (ret != null) {
            final String viewId = Faces.getViewId();
            if (viewId != null && viewId.contains("query/list-all")) {
                ret = createRedirectURL("query/list-all");
            } else {
                ret = createRedirectURL("query/list-my");
            }
        }
        return ret;
    }

    public void resetCriteria() {
        clearCriteria();
        addCriterionDefault();
        resultColumns = null;
    }

    public void resetDateTypeFilter() {
        getQuery().resetDateTypeFilter();
        queryChanged();
    }

    public void resetPermissionFilter() {
        permissionFilterQuery = null;
    }

    @Override
    @Transactional
    public String save() {
        // Clean query: remove empty and duplicate criteria
        getQuery().cleanCriteria();

        // Save the entity
        String ret = super.save(true, true, false);

        // create a clone for the next query
        cloneInstance();

        if (ret != null) {
            ret = createRedirectURL("query/list-my");
        }
        return ret;
    }

    public void saveNew() {
        // set default name and correct type before showing modal panel
        cloneInstance();
        getQuery().resetName();
        getQuery().setDiscriminator(QueryDiscriminator.SAVED);
    }

    public void search(int maxSearchResults) {
        setHits(search(getQuery(), maxSearchResults));
    }

    public TopDocs search(Query query, int maxSearchResults) {
        Collection<Criterion> criteriaList;
        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);

        // Add criteria
        final boolean required = Query.AND_BOOL.equals(query.getBool());

        // append session criteria
        criteriaList = query.getCriteria();

        if (query.getIndexMapType() != null && query.getIndexMapType().equals(IndexMapTypeEnum.workunit) && getSessionManager() != null && getSessionManager().isExcludeFeeder()) {
            criteriaList.add(new Criterion(IndexMapContentEnum.CREATEDBY.getField(), "* NOT feeder"));
        }

        for (final Criterion criterion : criteriaList) {
            if (criterion.getField().equalsIgnoreCase(IndexMapContentEnum.ID.getField())) {
                criterion.setField(IndexMapContentEnum.ID.getField());
            }

            final org.apache.lucene.search.Query subquery = criterion.generateQuery(new StandardAnalyzer());

            if (subquery != null) {
                if (required) {
                    booleanQueryBuilder.add(subquery, BooleanClause.Occur.MUST);
                } else {
                    booleanQueryBuilder.add(subquery, BooleanClause.Occur.SHOULD);
                    // The default value is not 1 anymore
                    booleanQueryBuilder.setMinimumNumberShouldMatch(1);
                }
            }
        }

        // Add sorting
        Sort sort = null;
        if (!query.getSortColumn().equalsIgnoreCase("score")) {
            // determine sort direction
            boolean sortDescending = !query.getSortDirection().equalsIgnoreCase(Query.ASCENDING);

            final SortField sortField = new SortField(query.getSortColumn(), SortField.Type.STRING, sortDescending);
            final SortField sortFieldId = new SortField("id", SortField.Type.STRING, true);
            sort = new Sort(sortField, sortFieldId);
        }

        // Add filters to the query
        addFilters(booleanQueryBuilder);

        TopDocs topDocHits = null;

        // Perform search
        try {
            logger.fine("Query details: " + query.getQueryDetails() + " indexMapType=" + query.getIndexMapType() + " booleanQueryBuilder=" + booleanQueryBuilder);
            if (sort != null) {
                topDocHits = getIndexSearcher().search(booleanQueryBuilder.build(), maxSearchResults, sort);
            } else {
                topDocHits = getIndexSearcher().search(booleanQueryBuilder.build(), maxSearchResults);
            }

            query.setTotal(getTotalHits(topDocHits));

            logger.fine("Query Hits: " + query.getTotal());

        } catch (final Exception e) {
            logger.severe("Failed to search: " + e);
        } finally {
            // Reset the id field to id
            for (final Criterion criterion : query.getCriteria()) {
                if (criterion.getField().equalsIgnoreCase(IndexMapContentEnum.ID.getField())) {
                    criterion.setField(IndexMapContentEnum.ID.getField());
                }
            }
        }

        return topDocHits;
    }

    public void setExcludeFeeder(boolean excludeFeeder) {
        this.excludeFeeder = excludeFeeder;
    }

    public void setHits(TopDocs hits) {
        this.hits = hits;
    }

    public void setLoggedQueries(boolean loggedQueries) {
        this.loggedQueries = loggedQueries;
    }

    public void setShowResults(boolean showResults) {
        this.showResults = showResults;
    }

    public void setTableState(Object tableState) {
        this.tableState = tableState;
    }

    public void toggleExcludeFeeder() {
        if (getQuery() != null) {
            setExcludeFeeder(!isExcludeFeeder());
            getSessionManager().setExcludeFeeder(isExcludeFeeder());
            getSessionManager().redirectRelative("/search/show.html?indexMapType=" + getQuery().getIndexMapType() + "&showAll=" + getQuery().isShowAll() + "&creator=" + getQuery().isCreator() + "&term=" + Messages.get("searchTerm")
                .replaceAll(" ", "+"));
        }
    }
}