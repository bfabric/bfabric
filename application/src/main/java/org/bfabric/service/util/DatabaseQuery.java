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

package org.bfabric.service.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.validation.constraints.NotBlank;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Membership;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.util.StringHelper;
import org.hibernate.engine.spi.SessionImplementor;

public class DatabaseQuery implements Serializable {

    private static final Logger logger = Logger.getLogger(DatabaseQuery.class.getName());

    private static final long serialVersionUID = 1;

    private static final String containerMembershipExistsClause = "EXISTS(SELECT id FROM Membership m WHERE (m.container = entity OR m.container = entity.project) AND m.user = :user AND m.discriminator = :discriminator)";

    private List<String> columns;

    private EntityManager entityManager;

    private int firstResult;

    private String from = null;

    private String join = null;

    private String joinType;

    private int maxResult;

    private String nativeQueryString = null;

    private String order = null;

    private HashMap<String, Object> parameters = new HashMap<>();

    private String unnest = null;

    private String where = null;

    public DatabaseQuery(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    public DatabaseQuery(String className, EntityManager entityManager) {
        this(entityManager);
        setEntityQuery(className);
    }

    public DatabaseQuery(String className, String filterString, EntityManager entityManager) {
        this(className, entityManager);
        addIdOrNameWhereClause(filterString);
    }

    public DatabaseQuery(String className, String filterString, String filterNamePath, EntityManager entityManager) {
        this(className, entityManager);
        addIdOrNameWhereClause(filterString, filterNamePath);
    }

    /**
     * Add coach clause.
     *
     * @param userId the userId
     */
    public void addCoachClause(long userId) {
        if (userId > 0) {
            addWhereClause("entity.coach.id = :coachId or entity.coachBackup.id = :coachId");
            addParameter("coachId", userId);
        }
    }

    /**
     * Add container bookable clause.
     *
     * @param user the user
     */
    public void addContainerBookableClause(User user) {
        addWhereClause("status not in :nonBookableStatusList");
        addParameter("nonBookableStatusList", StatusEnum.NON_BOOKABLE_CONTAINER_STATUS_LIST);
        if (!user.hasRoleImplicit(RoleEnum.CONTAINERREADER)) {
            addWhereClause(containerMembershipExistsClause);
            addParameter("user", user);
            addParameter("discriminator", Membership.DISCRIMINATOR_CURRENT);
        }
    }

    /**
     * Add container readable clause.
     *
     * @param user the user
     */
    public void addContainerReadableClause(User user) {
        if (!user.hasRoleImplicit(RoleEnum.CONTAINERREADER)) {
            addWhereClause("status = :published OR ((requester = :user OR budgetOfficer = :user OR contact = :user OR leader = :user) AND status in :notAccepted) OR " + containerMembershipExistsClause);
            addParameter("published", StatusEnum.PUBLISHED);
            addParameter("notAccepted", Arrays.asList(StatusEnum.PENDING, StatusEnum.REVIEW, StatusEnum.REJECTED, StatusEnum.CANCELED));
            addParameter("user", user);
            addParameter("discriminator", Membership.DISCRIMINATOR_CURRENT);
        }
    }

    /**
     * Add trackable container clause for the given user.
     *
     * @param user the user
     */
    public void addContainerTrackableClause(User user) {
        if (!user.hasRoleImplicit(RoleEnum.CONTAINERREADER)) {
            addWhereClause(containerMembershipExistsClause);
            addParameter("user", user);
            addParameter("discriminator", Membership.DISCRIMINATOR_CURRENT);
        }
    }

    /**
     * Add entity filter to the entity query.
     *
     * @param entity the entity
     * @param pathToEntity The pathToEntity
     */
    public void addEntityWhereClause(AbstractEntity entity, String pathToEntity) {
        if (entity != null) {
            String entityAsString = StringHelper.firstLower(entity.getTrimmedClassName());
            if (pathToEntity != null) {
                addWhereClause(pathToEntity + " = :" + entityAsString);
            } else {
                addWhereClause(entityAsString + " = :" + entityAsString);
            }
            addParameter(entityAsString, entity);
        }
    }

    /**
     * Add IdOrNameWhereClause.
     *
     * @param filterString the filterString
     */
    public void addIdOrNameWhereClause(String filterString) {
        addIdOrNameWhereClause(filterString, null);
    }

    /**
     * Add IdOrNameWhereClause.
     *
     * @param filterString the filterString
     * @param filterNamePath the filterNamePath
     */
    public void addIdOrNameWhereClause(String filterString, String filterNamePath) {
        addIdOrNameWhereClause(filterString, filterNamePath, "AND");
    }

    /**
     * Add IdOrNameWhereClause.
     *
     * @param filterString the filterString
     * @param filterNamePath the filterNamePath
     * @param logicalOperator the logicalOperator
     */
    public void addIdOrNameWhereClause(String filterString, String filterNamePath, String logicalOperator) {
        setMaxResult(25);
        setOrder("entity.id DESC");
        if (StringHelper.isNotEmpty(filterString)) {
            // If the filterString represents a number, get the entities whose id matches the given filterString.
            String filterStringTrimmed = filterString.trim().toLowerCase();
            String filterName = "LOWER(entity.name)";
            if (Pattern.compile("\\d+").matcher(filterStringTrimmed).matches()) {
                filterName = "STR(entity.id)";
                setOrder("entity.id");
            } else if (StringHelper.isNotEmpty(filterNamePath)) {
                filterName = "LOWER(" + filterNamePath + ")";
            }
            addWhereClause(filterName + " LIKE :filterString", logicalOperator);
            addParameterFilterString("filterString", filterStringTrimmed);
        }
    }

    /**
     * Add InEntitiesClause to the entity query.
     *
     * @param entities the entities to include
     */
    public void addInEntitiesClause(Collection<? extends AbstractEntity> entities) {
        if (entities != null && !entities.isEmpty()) {
            String parameterName = "entities" + (parameters.containsKey("entities") ? parameters.size() : Constants.EMPTY_STRING);
            addWhereClause("entity IN :" + parameterName);
            addParameter(parameterName, entities);
        }
    }

    /**
     * Add NotInEntitiesClause to the entity query.
     *
     * @param entities the entities to exclude
     */
    public void addNotInEntitiesClause(Collection<? extends AbstractEntity> entities) {
        if (entities != null && !entities.isEmpty()) {
            String parameterName = "entities" + (parameters.containsKey("entities") ? parameters.size() : Constants.EMPTY_STRING);
            addWhereClause("entity NOT IN :" + parameterName);
            addParameter(parameterName, entities);
        }
    }

    /**
     * Add NotInEntitiesClause to the entity query.
     *
     * @param entities the entities to exclude
     * @param logicalOperator the logicalOperator
     */
    public void addNotInEntitiesClause(Collection<? extends AbstractEntity> entities, String logicalOperator) {
        if (entities != null && !entities.isEmpty()) {
            String parameterName = "entities" + (parameters.containsKey("entities") ? parameters.size() : Constants.EMPTY_STRING);
            addWhereClause("entity NOT IN :" + parameterName, logicalOperator);
            addParameter(parameterName, entities);
        }
    }

    /**
     * Add parameter.
     *
     * @param key The key
     * @param value The value
     */
    public void addParameter(String key, Object value) {
        parameters.put(key, value);
    }

    /**
     * Add parameter filterString.
     *
     * @param key The key
     * @param value The value
     */
    public void addParameterFilterString(String key, Object value) {
        String filterString = (String) value;
        if (StringHelper.isNotEmpty(filterString)) {
            filterString = filterString.trim().toLowerCase();
        } else {
            filterString = Constants.EMPTY_STRING;
        }
        addParameter(key, "%" + filterString + "%");
    }

    /**
     * Add parameters.
     *
     * @param params the parameters to be added
     */
    public void addParameters(HashMap<String, Object> params) {
        parameters.putAll(params);
    }

    /**
     * Add UnnestWhereClause.
     *
     * @param nestedColumn the nestedColumn
     * @param id the id
     * @param idPath the idPath
     */
    public void addUnnestWhereClause(String nestedColumn, String idPath, long id) {
        if (StringHelper.isNotEmpty(nestedColumn)) {
            addWhereClause("EXISTS(SELECT nestedColumn.id FROM entity." + nestedColumn.trim() + " nestedColumn WHERE nestedColumn." + (StringHelper.isNotEmpty(idPath) ? idPath
                .trim() + "." : Constants.EMPTY_STRING) + "id = " + id + ")", "AND");
        }
    }

    /**
     * Add IdOrNameWhereClause.
     *
     * @param filterString the filterString
     */
    public void addUserNameWhereClause(String filterString) {
        if (StringHelper.isNotEmpty(filterString)) {
            // Important: trim to avoid that leading/trailing spaces lead to wrong separation!
            filterString = filterString.trim();
            String filterStringClause = getFilterStringLikeClause("firstName", "filterString") + " or " + getFilterStringLikeClause("lastName", "filterString") + " or " +
                getFilterStringLikeClause("login", "filterString") + " or " + getFilterStringLikeClause("STR(id)", "filterString");
            if (filterString.contains(" ")) {
                String filterStringOne = filterString.substring(0, filterString.indexOf(" ") - 1);
                String filterStringTwo = filterString.substring(filterString.indexOf(" "));
                filterStringClause += " or " + getFilterStringLikeClause("firstName", "filterStringOne") + " and " + getFilterStringLikeClause("lastName", "filterStringTwo") + " or " +
                    getFilterStringLikeClause("firstName", "filterStringTwo") + " and " + getFilterStringLikeClause("lastName", "filterStringOne");
                addParameterFilterString("filterStringOne", filterStringOne);
                addParameterFilterString("filterStringTwo", filterStringTwo);
            }
            addWhereClause(filterStringClause);
            addParameterFilterString("filterString", filterString);
        }
        setOrder("lastName, firstName");
        setMaxResult(100);
    }

    /**
     * Add WhereClause.
     *
     * @param clause the clause
     */
    public void addWhereClause(String clause) {
        addWhereClause(clause, "AND");
    }

    /**
     * Add WhereClause.
     *
     * @param clause the clause
     * @param logicalOperator the logicalOperator
     */
    public void addWhereClause(String clause, String logicalOperator) {
        String operator = logicalOperator != null ? logicalOperator : "AND";
        if (StringHelper.isNotEmpty(clause)) {
            String modifiedClause = "(" + clause + ")";
            if (StringHelper.isNotEmpty(getWhere())) {
                modifiedClause = getWhere() + " " + operator + " " + modifiedClause;
            }
            setWhere(modifiedClause);
        }
    }

    /**
     * Add WhereClause.
     *
     * @param clause the clause
     */
    public void addWhereClauseDisjunctive(String clause) {
        addWhereClause(clause, "OR");
    }

    /**
     * Clear parameters.
     */
    public void clearParameter() {
        parameters.clear();
    }

    /**
     * Get the columns of the query's result table
     *
     * @return List of columns names.
     */
    public List<String> getColumns() {
        if (columns == null) {
            columns = new ArrayList<>();
            if (StringHelper.isNotEmpty(getQueryString())) {
                SessionImplementor sessionImplementor = (SessionImplementor) getEntityManager().getDelegate();
                Statement statement = null;
                ResultSet resultSet = null;
                ResultSetMetaData metadata;
                try {
                    statement = sessionImplementor.connection().createStatement();
                    resultSet = statement.executeQuery(getQueryString());
                    metadata = resultSet.getMetaData();
                    for (int i = 1; i <= metadata.getColumnCount(); i++) {
                        columns.add(metadata.getColumnName(i));
                    }
                } catch (SQLException e) {
                    logger.warning(e.getMessage());
                } finally {
                    try {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    } catch (SQLException e) {
                        logger.warning(e.getMessage());
                    }

                    try {
                        if (statement != null) {
                            statement.close();
                        }
                    } catch (SQLException e) {
                        logger.warning(e.getMessage());
                    }
                    if (sessionImplementor != null) {
                        sessionImplementor.close();
                    }
                }
            }
        }
        return columns;
    }

    /**
     * Get count of rows of the query's result table.
     *
     * @return count of rows of the query's result table.
     */
    public long getCount() {
        boolean nativeQuery = StringHelper.isNotEmpty(getNativeQueryString());
        Query query = getQuery(getQueryCountString(), nativeQuery);
        if (query != null) {
            try {
                return nativeQuery ? ((BigInteger) query.getSingleResult()).longValue() : (Long) query.getSingleResult();
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    /**
     * Get entityManager.
     *
     * @return the entityManager
     */
    public EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = Persistence.createEntityManagerFactory("bfabric").createEntityManager();
        }
        return entityManager;
    }

    /**
     * Get FilterStringLikeClause.
     *
     * @param attributeName the attributeName
     * @param filterString the filterString
     */
    public String getFilterStringLikeClause(String attributeName, String filterString) {
        return StringHelper.isNotEmpty(attributeName) && StringHelper.isNotEmpty(filterString) ? "LOWER(" + attributeName + ") LIKE '%' || :" + filterString + " || '%'" : Constants.EMPTY_STRING;
    }

    /**
     * Get firstResult.
     *
     * @return the firstResult
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * Get from-clause.
     *
     * @return the from-clause
     */
    public String getFrom() {
        return from;
    }

    /**
     * Get join.
     *
     * @return the join
     */
    public String getJoin() {
        return join;
    }

    /**
     * Get joinType.
     *
     * @return the joinType
     */
    public String getJoinType() {
        return joinType;
    }

    /**
     * Get maxResult.
     *
     * @return the maxResult
     */
    public int getMaxResult() {
        return maxResult;
    }

    /**
     * Get nativeQueryString.
     *
     * @return the nativeQueryString
     */
    public String getNativeQueryString() {
        return nativeQueryString;
    }

    /**
     * Get order.
     *
     * @return the order
     */
    public String getOrder() {
        return order;
    }

    /**
     * Get parameters.
     *
     * @return the parameters
     */
    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public Query getQuery(@NotBlank String queryString, boolean nativeQuery) {
        try {
            Query query = nativeQuery ? getEntityManager().createNativeQuery(queryString) : getEntityManager().createQuery(queryString);
            for (Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            query.setFlushMode(FlushModeType.COMMIT);
            return query;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Get queryCountString.
     *
     * @return the queryCountString
     */
    public String getQueryCountString() {
        String queryString = getNativeQueryString();
        if (StringHelper.isEmpty(queryString)) {
            if (StringHelper.isNotEmpty(getFrom())) {
                if (StringHelper.isNotEmpty(getJoin())) {
                    queryString = "select count(distinct entity.id) from " + getFrom() + " entity ";
                    queryString += (StringHelper.isNotEmpty(getJoinType()) ? " " + getJoinType() : Constants.EMPTY_STRING) + " join " + getJoin();
                } else {
                    queryString = "select count(*) from " + getFrom() + " entity ";
                    if (StringHelper.isNotEmpty(getUnnest())) {
                        queryString += (StringHelper.isNotEmpty(getJoinType()) ? " " + getJoinType() : Constants.EMPTY_STRING) + " join " + getUnnest();
                    }
                }
                if (StringHelper.isNotEmpty(getWhere())) {
                    queryString += " where " + getWhere();
                }
            } else {
                logger.warning("This should not happen. Both no full query and no from clause are given for count query!");
            }
        } else {
            queryString = queryString.replaceAll("(SELECT|Select|select)[^&]*(FROM|From|from)", "SELECT count(*) FROM");
            if (queryString.toLowerCase().contains("order by")) {
                queryString = queryString.substring(0, queryString.toLowerCase().indexOf("order by"));
            }
        }
        return queryString;
    }

    /**
     * Get queryString.
     *
     * @return the queryString
     */
    public String getQueryString() {
        String queryString = getNativeQueryString();
        if (StringHelper.isEmpty(queryString)) {
            if (StringHelper.isNotEmpty(getFrom())) {
                if (StringHelper.isNotEmpty(getJoin())) {
                    queryString = "select distinct entity from " + getFrom() + " entity";
                    queryString += (StringHelper.isNotEmpty(getJoinType()) ? " " + getJoinType() : Constants.EMPTY_STRING) + " join " + getJoin();
                } else {
                    queryString = "select entity from " + getFrom() + " entity";
                    if (StringHelper.isNotEmpty(getUnnest())) {
                        queryString += (StringHelper.isNotEmpty(getJoinType()) ? " " + getJoinType() : Constants.EMPTY_STRING) + " join " + getUnnest();
                    }
                }
                if (StringHelper.isNotEmpty(getWhere())) {
                    queryString += " where " + getWhere();
                }
                if (StringHelper.isNotEmpty(getOrder())) {
                    queryString += " order by " + getOrder();
                }
            } else {
                logger.warning("This should not happen. Both no full query and no from clause are given!");
            }
        } else {
            if (StringHelper.isNotEmpty(getOrder())) {
                if (queryString.toLowerCase().contains(" order by ")) {
                    queryString = queryString.substring(0, queryString.toLowerCase().indexOf(" order by "));
                }
                queryString += " order by " + getOrder();
            }
        }
        return queryString;
    }

    public List<?> getResultList() {
        boolean nativeQuery = StringHelper.isNotEmpty(getNativeQueryString());
        Query query = getQuery(getQueryString(), nativeQuery);
        if (query != null) {
            if (firstResult > 0) {
                query.setFirstResult(firstResult);
            }
            if (maxResult > 0) {
                query.setMaxResults(maxResult);
            }
            try {
                return query.getResultList();
            } catch (Exception ignored) {
            }
        }
        return new ArrayList<>();
    }

    /**
     * Get unnest.
     *
     * @return the unnest
     */
    public String getUnnest() {
        return unnest;
    }

    /**
     * Get where.
     *
     * @return the where
     */
    public String getWhere() {
        return where;
    }

    /**
     * Set entityManager.
     *
     * @param entityManager the entityManager to set
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Set EntityQuery.
     *
     * @param className the class name
     */
    public void setEntityQuery(String className) {
        if (className != null) {
            setFrom(className);
            setOrder("entity.id DESC");
        }
    }

    /**
     * Set firstResult.
     *
     * @param firstResult the firstResult to set
     */
    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    /**
     * Set from-clause.
     *
     * @param from the from-clause to set
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Set join.
     *
     * @param join the join to set
     */
    public void setJoin(String join) {
        this.join = join;
    }

    /**
     * Set joinType.
     *
     * @param joinType the joinType to set
     */
    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    /**
     * Set JoinTypeLeftOuter.
     */
    public void setJoinTypeLeftOuter() {
        setJoinType(Constants.LEFT_OUTER);
    }

    /**
     * Set maxResult.
     *
     * @param maxResult the maxResult to set
     */
    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    /**
     * Set nativeQueryString.
     *
     * @param nativeQueryString the nativeQueryString to set
     */
    public void setNativeQueryString(String nativeQueryString) {
        this.nativeQueryString = nativeQueryString;
    }

    /**
     * Set order.
     *
     * @param order the order to set
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * Set parameters.
     *
     * @param parameters the parameters to set
     */
    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Set parenthesis around the current where clause if non-empty so far.
     */
    public void setParenthesisAroundWhere() {
        if (StringHelper.isNotEmpty(getWhere())) {
            setWhere("(" + getWhere() + ")");
        }
    }

    /**
     * Set unnest.
     *
     * @param unnest the unnest to set
     */
    public void setUnnest(String unnest) {
        this.unnest = unnest;
    }

    /**
     * Set where.
     *
     * @param where the where to set
     */
    public void setWhere(String where) {
        this.where = where;
    }
}