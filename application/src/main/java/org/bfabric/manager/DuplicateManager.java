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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractBaseEntity;
import org.bfabric.entity.User;
import org.bfabric.entity.api.Mergeable;
import org.bfabric.service.DuplicateService;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.ClassHelper;
import org.hibernate.internal.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.primefaces.component.datatable.DataTable;

@Named
@ViewScoped
public class DuplicateManager extends AbstractManager {

    private static final Logger logger = Logger.getLogger(DuplicateManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    private DuplicateService duplicateService;

    private List<Object[]> duplicates = null;

    private List<Object[]> duplicatesIgnored = null;

    private BfabricLazyDataModel<AbstractBaseEntity> entities = null;

    @Param
    private String merge;

    private String mergeableClass = null;

    private boolean proposalsOn = true;

    private List<AbstractBaseEntity> selectedEntities = new ArrayList<>();

    public void deselectEntity(AbstractBaseEntity abstractBaseEntity) {
        getSelectedEntities().remove(abstractBaseEntity);
    }

    public void duplicateReset(boolean all) {
        duplicateService.duplicateReset(all, getMergeableClass(), getCurrentUser());
        resetLocallyCachedLists();
        getFacesMessagesManager().printWarn(Messages.get("resetSyncCompleteHint"));
    }

    public void duplicateSync(boolean all) {
        String msg = duplicateService.duplicateSync(all, getMergeableClass(), getCurrentUser());
        resetLocallyCachedLists();
        getFacesMessagesManager().bufferWarningClear(msg);
    }

    public List<Object[]> getDuplicates() {
        if (duplicates == null && mergeableClass != null) {
            if (User.class.getSimpleName().equals(getMergeableClass())) {
                duplicates = duplicateService.getPotentialUserDuplicates();
            } else {
                duplicates = duplicateService.getPotentialDuplicatesByMergeableClass(getMergeableClass());
            }
        }
        return duplicates;
    }

    public List<Object[]> getDuplicatesIgnored() {
        if (duplicatesIgnored == null && mergeableClass != null) {
            if (User.class.getSimpleName().equals(getMergeableClass())) {
                duplicatesIgnored = duplicateService.getUserDuplicatesIgnored();
            } else {
                duplicatesIgnored = duplicateService.getDuplicatesIgnoredByMergeableClass(getMergeableClass());
            }
        }
        return duplicatesIgnored;
    }

    public BfabricLazyDataModel<AbstractBaseEntity> getEntities() {
        if (entities == null && mergeableClass != null) {
            for (final Class<Mergeable> clazz : ClassHelper.getMergeableEntityClasses()) {
                if (clazz.getSimpleName().equals(mergeableClass)) {
                    entities = duplicateService.getEntitiesByClazz(clazz);
                    break;
                }
            }
        }
        return entities;
    }

    public String getLastDuplicateSync() {
        String ret;
        try {
            if (StringHelper.isNotEmpty(mergeableClass)) {
                ret = duplicateService.getLastSyncedByClassName(mergeableClass);
            } else {
                ret = duplicateService.getMinLastSynced();
            }
        } catch (final NoResultException e) {
            ret = null;
        }
        return ret;
    }

    public String getMerge() {
        return merge;
    }

    public String getMergeableClass() {
        if (merge != null) {
            mergeableClass = merge;
        }
        return mergeableClass;
    }

    public List<AbstractBaseEntity> getSelectedEntities() {
        return selectedEntities;
    }

    public void ignore(String type, long dupId1, long dupId2, String dupName1, String dupName2) {
        try {
            String msg = duplicateService.ignore(type, dupId1, dupId2, dupName1, dupName2, getCurrentUser());
            getFacesMessagesManager().printWarn(msg);
        } catch (final Exception e) {
            getFacesMessagesManager().printError(e.getLocalizedMessage());
            logger.severe("Ignore duplicate throws " + e);
        } finally {
            resetLocallyCachedLists();
        }
    }

    public boolean isProposalsOn() {
        return proposalsOn;
    }

    public void mergeRequest(String type, Long mid1, Long mid2) {
        try {
            printFacesMessages(duplicateService.mergeRequest(type, mid1, mid2, getCurrentUser()));
        } catch (final Exception e) {
            getFacesMessagesManager().printError(e.getLocalizedMessage());
            logger.severe("Merge user request throws " + e);
        } finally {
            resetLocallyCachedLists();
        }
    }

    public void resetLocallyCachedLists() {
        duplicates = null;
        duplicatesIgnored = null;
        entities = null;
        selectedEntities = new ArrayList<>();
        merge = null;
        final DataTable entitiesTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("duplicate-list-form:entitiestable");
        if (entitiesTable != null) {
            entitiesTable.reset();
        }
        final DataTable duplicateTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("duplicate-list-form:duplicatetable");
        if (duplicateTable != null) {
            duplicateTable.reset();
        }
    }

    public void selectEntity(AbstractBaseEntity abstractBaseEntity) {
        if (!getSelectedEntities().contains(abstractBaseEntity)) {
            getSelectedEntities().add(abstractBaseEntity);
        }
    }

    public void setMergeableClass(String mergeableClass) {
        this.mergeableClass = mergeableClass;
    }

    public void setProposalsOn(boolean proposalsOn) {
        this.proposalsOn = proposalsOn;
    }

    public void setSelectedEntities(List<AbstractBaseEntity> selectedEntities) {
        this.selectedEntities = selectedEntities;
    }
}