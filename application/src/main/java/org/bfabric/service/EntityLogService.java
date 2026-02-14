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

package org.bfabric.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.apache.commons.codec.digest.DigestUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.AccessRequest;
import org.bfabric.entity.Credit;
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.Event;
import org.bfabric.entity.User;
import org.bfabric.entity.UserBillingInfo;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.InvalidCodeException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class EntityLogService extends AbstractService {

    private static final List<String> EXCLUDED_ENTITY_CLASS_NAMES = Arrays.asList(
        AccessRequest.class.getSimpleName(),
        Credit.class.getSimpleName(),
        Event.class.getSimpleName(),
        UserBillingInfo.class.getSimpleName(),
        User.class.getSimpleName()
    );

    private static final long serialVersionUID = 1;

    public EntityLogService() {
        super(EntityLog.class);
    }

    public BfabricLazyDataModel<EntityLog> getEntityLogsLazyModel(AbstractBaseEntity entity) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("(entityId = :entityId AND entityClassName = :entityClassName) OR (parentEntityId = :entityId AND parentEntityClassName = :entityClassName)");
        entityQuery.addParameter("entityId", entity.getId());
        entityQuery.addParameter("entityClassName", entity.getTrimmedClassName());
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public String getExcludedEntityClassNames() {
        return String.join(",", EXCLUDED_ENTITY_CLASS_NAMES);
    }

    public BfabricLazyDataModel<EntityLog> getLazyModel(User user) {
        if (user == null || !user.hasRoleImplicit(RoleEnum.CONTAINERMANAGER)) {
            return null;
        }
        EntityQuery entityQuery = createEntityQuery();
        if (!user.hasRoleImplicit(RoleEnum.ADMIN)) {
            entityQuery.addWhereClause("entityClassName not in (:entityClassNames)");
            entityQuery.addParameter("entityClassNames", EXCLUDED_ENTITY_CLASS_NAMES);
        }
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public BfabricLazyDataModel<EntityLog> getLazyModelByCreatedBy(String createdBy) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("createdBy = :createdBy");
        entityQuery.addParameter("createdBy", createdBy);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public void logDownloadStatus(Long id, String code, String status, String ip, String summary) throws InvalidCodeException, CloneNotSupportedException {
        if (id != null && StringHelper.isNotEmpty(code) && StringHelper.isNotEmpty(status) && StringHelper.isNotEmpty(ip)) {
            EntityLog entityLog = find(EntityLog.class, id);
            if (entityLog != null) {
                String logCode = DigestUtils.md5Hex(String.valueOf(id) + entityLog.getCreated());
                String targetCode = logCode + status.length() + "c1a3Nt" + ip.length();
                if (entityLog.getCreated() == null && !code.equals(targetCode)) {
                    throw new InvalidCodeException(Messages.get("entityLogCodeInvalid"));
                }
                LogStatusEnum newStatus = LogStatusEnum.value(status);
                if (newStatus != null && !newStatus.equals(entityLog.getStatus())) {
                    EntityLog downloadStatusLog = entityLog.clone();
                    downloadStatusLog.setCreated(LocalDateTime.now());
                    downloadStatusLog.setStatus(newStatus);
                    String summaryLog = "<summary>";
                    if (StringHelper.isNotEmpty(summary)) {
                        summaryLog += StringHelper.html2text(summary.replaceAll("<head>.*<\\/head>", Constants.EMPTY_STRING).replaceAll("<h3>.*<\\/h3>", Constants.EMPTY_STRING));
                    }
                    summaryLog += "</summary>";
                    downloadStatusLog.setLog("<log><old><rootid></rootid><ip></ip><summary></summary></old><new><rootid>" + entityLog.getId() + "</rootid><ip>" + ip.trim() + "</ip>" + summaryLog + "</new></log>");
                    save(downloadStatusLog);
                }
            }
        }
    }
}