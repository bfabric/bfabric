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
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Container;
import org.bfabric.entity.Executable;
import org.bfabric.entity.ExternalJob;
import org.bfabric.entity.Mail;
import org.bfabric.entity.User;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.enums.ExternalJobClientClassEnum;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class ExternalJobService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(ExternalJobService.class.getName());

    private static final String BASH_ABSOLUTE_PATH_DEFAULT = "/bin/bash";

    private static final Object mutexWorkunitUpdate = new Object();

    public ExternalJobService() {
        super(ExternalJob.class);
    }

    public Map<String, Set<String>> changeStatus(ExternalJob externalJob, StatusEnum statusEnum, boolean isSendMail) {
        Set<Mail> mails = externalJob.changeStatus(statusEnum);
        super.save(externalJob);
        flush();
        Map<String, Set<String>> facesMessages = createDisplayFacesMessagesMap(Messages.get("externalJob") + " " + statusEnum.getLabel());
        if (isSendMail) {
            facesMessages.get(Constants.ERROR_MESSAGES).addAll(mailSendService.sendMails(mails));
        }
        return facesMessages;
    }

    public void createAndExecuteExternalJob(@NotNull Workunit workunit, LogActionEnum logAction, @NotNull Executable executable) {
        ExternalJob externalJob = new ExternalJob(workunit, logAction, executable);
        persistAndExecute(externalJob);
    }

    public void execute(long id) {
        // Important: fetch externalJob from the database again to avoid optimistic locking error!
        ExternalJob externalJob = find(ExternalJob.class, id);
        if (externalJob != null && externalJob.getExecutable() != null) {
            final Configuration configuration = getConfiguration();
            final Executable executable = externalJob.getExecutable();
            Executable masterExecutable = executable.getMasterExecutable();
            if (masterExecutable == null) {
                // Use the default master executable of the corresponding executable context.
                switch (executable.getExecutableContext()) {
                case MASTER:
                    masterExecutable = executable;
                    break;
                case STORAGE:
                    masterExecutable = getMasterExecutable(configuration.getDefaultMasterExecutableIdStorage());
                    break;
                case SUBMITTER:
                    masterExecutable = getMasterExecutable(configuration.getDefaultMasterExecutableIdSubmitter());
                    break;
                case WRAPPERCREATOR:
                    masterExecutable = getMasterExecutable(configuration.getDefaultMasterExecutableIdWrapperCreator());
                    break;
                case APPLICATION:
                case WORKUNIT:
                default:
                    break;
                }
            }
            if (masterExecutable != null) {
                String bashAbsolutePath = configuration.getBashAbsolutePath();
                if (bashAbsolutePath == null) {
                    bashAbsolutePath = BASH_ABSOLUTE_PATH_DEFAULT;
                }
                File file = new File(bashAbsolutePath);
                if (file.exists() && file.canExecute()) {
                    String externalCommand = masterExecutable.getAbsolutePathFM() + " -j " + externalJob.getId();
                    ProcessBuilder processBuilder = new ProcessBuilder(bashAbsolutePath, "-c", externalCommand);
                    try {
                        processBuilder.start();
                    } catch (IOException e) {
                        logger.warning("Failed to start process: " + e.getMessage());
                    }
                } else {
                    logger.warning("Cannot execute " + externalJob + " since the bash path " + bashAbsolutePath + " does not exist or cannot be executed.");
                }
            } else {
                logger.warning("Cannot execute " + externalJob + " since no master executable is associated.");
            }
        } else {
            logger.warning("Cannot execute " + externalJob + " since no executable is associated.");
        }
    }

    public List<String> getDistinctActions() {
        return createNamedQuery("ExternalJob.findDistinctActions").getResultList();
    }

    public List<ExternalJob> getExternalJobsByClientEntity(AbstractEntity clientEntity) {
        return createNamedQuery("ExternalJob.findByClient").setParameter("clientEntityClassName", clientEntity.getTrimmedClassName()).setParameter("clientEntityId", clientEntity.getId())
            .getResultList();
    }

    public List<ExternalJob> getExternalJobsByClientEntityAndExecutableContext(AbstractEntity clientEntity, ExecutableContextEnum executableContext) {
        return createNamedQuery("ExternalJob.findByClientAndExecutableContext").setParameter("clientEntityClassName", clientEntity.getTrimmedClassName()).setParameter("clientEntityId",
            clientEntity.getId()).setParameter("context", executableContext.toString()).getResultList();
    }

    @Override
    public BfabricLazyDataModel<ExternalJob> getLazyModelByContainerId(long containerId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("clientEntityClassName = :clientEntityClassName AND clientEntityId = :clientEntityId");
        entityQuery.addParameter("clientEntityClassName", ExternalJobClientClassEnum.CONTAINER.getClientClassName());
        entityQuery.addParameter("clientEntityId", containerId);
        entityQuery.setOrder("created DESC");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    @Override
    public BfabricLazyDataModel<?> getLazyModelByContainerId(long containerId, Collection<? extends Container> associatedContainers, boolean all) {
        return getLazyModelByContainerId(containerId);
    }

    public BfabricLazyDataModel<ExternalJob> getLazyModelByExecutable(Executable executable) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("executable = :executable");
        entityQuery.addParameter("executable", executable);
        return new BfabricLazyDataModel<>(entityQuery);
    }

    @Override
    public BfabricLazyDataModel<ExternalJob> getLazyModelByUserId(long userId) {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.addWhereClause("clientEntityClassName = :clientEntityClassName AND clientEntityId = :clientEntityId");
        entityQuery.addParameter("clientEntityClassName", ExternalJobClientClassEnum.USER.getClientClassName());
        entityQuery.addParameter("clientEntityId", userId);
        entityQuery.setOrder("created DESC");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    private Executable getMasterExecutable(long executableId) {
        final Executable executable = find(Executable.class, executableId);
        if (executable == null) {
            logger.warning("There is no executable with id " + executableId + ".");
            return null;
        }
        if (!ExecutableContextEnum.MASTER.equals(executable.getExecutableContext())) {
            logger.warning("Executable " + executableId + " is not a master executable.");
            return null;
        }
        if (executable.getRelativePath() == null) {
            logger.warning("Executable " + executableId + " has no path.");
            return null;
        }
        final File executableFile = new File(executable.getAbsolutePathFM());
        if (!executableFile.exists()) {
            logger.warning("There is no executable file for the executable with id " + executableId + ".");
            return null;
        }
        if (!executableFile.canExecute()) {
            logger.warning("The executable file of the executable with id " + executableId + " cannot be executed.");
            return null;
        }
        return executable;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Object merge(Object entity) {
        return getEntityManager().merge(entity);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void persist(Object entity) {
        getEntityManager().persist(entity);
    }

    public void persistAndExecute(@NotNull ExternalJob externalJob) {
        persist(externalJob);
        execute(externalJob.getId());
    }

    public void resubmit(Workunit workunit) {
        if (workunit != null) {
            workunit.setPending();
            submit(workunit);
        }
    }

    public void runPostUpdatePostCommitActions(Long id) {
        // Note: fetch externalJob from the database again to avoid optimistic locking error!
        ExternalJob externalJob = find(ExternalJob.class, id);

        // When externalJob is done, then do some internal work.
        if (StatusEnum.DONE.equals(externalJob.getStatus())) {
            if (LogActionEnum.UPDATE.name().equals(externalJob.getAction())) {
                User user = externalJob.getUser();
                // Set computer login activated if it is not already set.
                if (user != null && !user.isComputerLoginActivated()) {
                    user.setComputerLoginActivated(true);
                    merge(user);
                }
            }

            if (externalJob.getWorkunit() != null) {
                // Find an external job which has the "SUBMITTER" context and which is connected to the workunit to which the current external job is connected too.
                // If there is one, then assume that the submission for this workunit was triggered currently, there is only one case when this code fragment is passed through:
                // when the status of an external job in context of wrapper creation is set to "done".
                boolean submitted = false;
                for (ExternalJob job : externalJob.getWorkunit().getExternalJobs()) {
                    if (job.getExecutable() != null && job.getExecutable().getContext().equals(ExecutableContextEnum.SUBMITTER.name())) {
                        submitted = true;
                        break;
                    }
                }

                if (!submitted) {
                    // If the submission for the workunit connected to the current external job did not happen, then trigger it now!
                    submit(externalJob.getWorkunit());
                }
            }
        }

        // If the current external job is connected to a workunit, then check whether the status of this workunit needs to be reset (which must happen in a synchronized way).
        synchronized (mutexWorkunitUpdate) {
            if (externalJob.getWorkunit() != null && !externalJob.getWorkunit().isAvailable()) {
                externalJob.getWorkunit().setStatusExternalJobUpdate();
                merge(externalJob.getWorkunit());
            }
        }
    }

    public Set<String> save(ExternalJob externalJob) {
        return save(externalJob, true);
    }

    public Set<String> save(ExternalJob externalJob, boolean index) {
        Set<String> errorMsg = new HashSet<>();
        boolean isManaged = externalJob.isManaged();
        super.save(externalJob, index);
        if (isManaged) {
            runPostUpdatePostCommitActions(externalJob.getId());
        }
        return errorMsg;
    }

    public void submit(Workunit workunit) {
        if (workunit != null) {
            if (workunit.getSubmitterExecutable() != null) {
                createAndExecuteExternalJob(workunit, LogActionEnum.SUBMIT, workunit.getSubmitterExecutable());
            } else {
                logger.warning("External job for " + workunit + " could not be submitted since the workunit has no associated submitter executable!");
            }
        }
    }
}