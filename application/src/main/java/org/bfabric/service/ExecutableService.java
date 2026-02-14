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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Executable;
import org.bfabric.entity.Mail;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.exception.RollbackException;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;
import org.bfabric.util.RepositoryHelper;

@Named
@Stateless
public class ExecutableService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(ExecutableService.class.getName());

    public ExecutableService() {
        super(Executable.class);
    }

    public Map<String, Set<String>> changeStatus(Executable executable, ResourceStatusEnum statusEnum) {
        Set<Mail> mails = executable.changeStatus(statusEnum);
        super.save(executable);
        Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("executable") + " " + statusEnum.getLabel());
        facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
        return facesMessages;
    }

    public List<Executable> getEnabledValidMasterExecutablesFiltered(String filterString, Executable included) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        entityQuery.addWhereClause("enabled = true and valid = true and context = :context OR entity = :included");
        entityQuery.addParameter("context", ExecutableContextEnum.MASTER.name());
        entityQuery.addParameter("included", included);
        entityQuery.setOrder("name");
        return (List<Executable>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<Executable> getNonWorkunitExecutables() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("context <> 'WORKUNIT'");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Executable> getPotentialPredecessorExecutablesFiltered(String filterString, Executable excluded) {
        EntityQuery entityQuery = createEntityQueryFiltered(filterString);
        if (excluded != null) {
            entityQuery.addWhereClause("context = :context");
            entityQuery.addParameter("context", excluded.getContext());
            if (excluded.getId() > 0) {
                entityQuery.addWhereClause("id != :id");
                entityQuery.addParameter("id", excluded.getId());
            }
        }
        entityQuery.setOrder("name");
        entityQuery.setMaxResult(100);
        return (List<Executable>) entityQuery.getResultList();
    }

    public BfabricLazyDataModel<Executable> getReassignExecutableSupervisorTasks() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("context <> 'WORKUNIT' AND valid = TRUE and supervisor.empDegree IS NULL and supervisor.login <> 'admin'");
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public List<Executable> getResultList(ExecutableContextEnum executableContext) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("context = :context");
        entityQuery.addParameter("context", executableContext.name());
        return (List<Executable>) entityQuery.getResultList();
    }

    public void rollbackStatus(Executable executable) {
        if (executable != null) {
            executable.rollbackStatus();
            super.save(executable);
        }
    }

    public void save(Executable executable) {
        save(executable, true);
    }

    public void save(Executable executable, boolean index) {
        try {
            super.save(executable, index);
            if (executable.getUploadedFile() != null) {
                executable.setOldStatus(executable.getStatus());
                executable.setRelativePath(executable.getRelativeRepositoryPath() + File.separator + executable.computeDefaultExecutableFileName());
                executable.setStorage(RepositoryHelper.getLocalStorage(true));
                RepositoryHelper.createImport(executable);
                executable.changeStatus(ResourceStatusEnum.AVAILABLE);
                merge(executable);
            }
        } catch (final Exception e) {
            logger.severe(e.toString());
            throw new RollbackException();
        }
    }
}