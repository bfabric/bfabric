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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.auth.message.AuthException;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Project;
import org.bfabric.exception.RollbackException;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ProjectService;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.FileHelper;
import org.bfabric.util.FileUploadHelper;
import org.bfabric.util.RepositoryHelper;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Faces;

@MeasureCalls
@Named
@ViewScoped
public class ProjectManager extends ContainerManager<Project> {

    private static final Logger logger = Logger.getLogger(ProjectManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    private FileUploadHelper fileUploadHelper;

    private boolean fullView = false;

    @Inject
    private ProjectService projectService;

    private int reportYear;

    @Param
    private Integer year;

    public ProjectManager() {
        super(Project.class);
    }

    public String announceFinish() {
        return printFacesMessagesAndRedirect(projectService.announceFinish(getProject()));
    }

    public String announcePrivate() {
        return printFacesMessagesAndRedirect(projectService.announcePrivate(getProject()));
    }

    public String approveExtensionReport(int extensionReportYear) {
        return printFacesMessagesAndRedirect(projectService.approveExtensionReport(getProject(), extensionReportYear));
    }

    @Override
    public Project createInstance() {
        Project project = super.createInstance();
        if (getConfiguration().isBudgetLimitEnabled() && getConfiguration().getDefaultBudgetLimit() > 0) {
            project.setBudgetLimit(BigDecimal.valueOf(getConfiguration().getDefaultBudgetLimit()));
        }
        return project;
    }

    public void downloadExtensionReports() throws IOException, AuthException {
        // Showcase for downloading all the extension reports as a zip file.
        File localTemporaryDirectory = new File(RepositoryHelper.getTemporaryStorage().getBasePath());
        File extensionReports = new File(localTemporaryDirectory.getAbsolutePath() + "/extensionReports.zip");
        FileOutputStream fileOutputStream = new FileOutputStream(extensionReports);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        // Check each file and add it to zip.
        if (getProject().getExtensionReport1() != null) {
            String extensionReport1absolutePathFM = getProject().getExtensionReport1().getAbsolutePathFM();
            File extensionReport1 = new File(extensionReport1absolutePathFM);
            FileHelper.checkFileAccessibility(extensionReport1);
            FileHelper.addToZipFile("report1_", extensionReport1, zipOutputStream);
        }
        if (getProject().getExtensionReport2() != null) {
            String extensionReport2absolutePathFM = getProject().getExtensionReport2().getAbsolutePathFM();
            File extensionReport2 = new File(extensionReport2absolutePathFM);
            FileHelper.checkFileAccessibility(extensionReport2);
            FileHelper.addToZipFile("report2_", extensionReport2, zipOutputStream);
        }
        if (getProject().getExtensionReport3() != null) {
            String extensionReport3absolutePathFM = getProject().getExtensionReport3().getAbsolutePathFM();
            File extensionReport3 = new File(extensionReport3absolutePathFM);
            FileHelper.checkFileAccessibility(extensionReport3);
            FileHelper.addToZipFile("report3_", extensionReport3, zipOutputStream);
        }

        zipOutputStream.close();

        // Stream the zip file.
        Faces.sendFile(extensionReports, true);

        // Delete temporary files.
        if (!extensionReports.delete()) {
            logger.warning("File '" + extensionReports.getAbsolutePath() + "' could not be deleted.");
        }
    }

    public String finish() {
        return printFacesMessagesClearMenuHeaderAndRedirect(projectService.finish(getProject()));
    }

    @Produces
    @Named("project")
    @CachedMethodResult
    public Project getProject() {
        return getInstance();
    }

    public int getReportYear() {
        return reportYear;
    }

    public String getViewSuffix() {
        return isFullView() ? Constants.VIEW_FULL : Constants.VIEW_BASIC;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (year != null) {
            setReportYear(year);
        }
    }

    public boolean isFullView() {
        return fullView;
    }

    public String privatize() {
        return printFacesMessagesClearMenuHeaderAndRedirect(projectService.privatize(getProject()));
    }

    public String publish() {
        return printFacesMessagesClearMenuHeaderAndRedirect(projectService.publish(getProject()));
    }

    public String publishGrantedDate() {
        return printFacesMessagesAndRedirect(projectService.publishGrantedDate(getProject()));
    }

    public String removeFailedWorkunits() {
        try {
            containerService.removeFailedWorkunits(getProject());
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeletedFailedWorkunits"));
            return createRedirectShowScreenURL(getProject(), "workunits", null);
        } catch (final Exception e) {
            getFacesMessagesManager().printError(Messages.get("removeFailedWorkunitsError"));
            logger.severe("Removing failed workunits throws " + e.getLocalizedMessage());
        }
        return null;
    }

    public String resetReminder() {
        return printFacesMessagesAndRedirect(projectService.resetReminder(getProject()));
    }

    @Override
    public String save() {
        if (getSelectedDiscussedWith() != null) {
            getProject().setDiscussedWith(new HashSet<>(getSelectedDiscussedWith()));
        }

        try {
            LinkedHashMap<String, String> validationErrorMsg = projectService.isValid(getProject(), fileUploadHelper.getSingleUploadedFile());
            if (validationErrorMsg.isEmpty()) {
                setCreated(!isManaged());

                if (isCreated() && getProject().getTechnologiesAsStringComputed().contains("Genomics")) {
                    getProject().setOrderDataOnly(true);
                }
                getFacesMessagesManager().bufferErrors(projectService.save(getProject(), fileUploadHelper.getSingleUploadedFile(), getCurrentUser()));

                if (!isCreated()) {
                    // Print some additional messages to inform the user about changes in the project managers.
                    getFacesMessagesManager().bufferWarning(Messages.get("successfullyUpdated"));
                    addFacesMessagesForChangedManager();
                }

                if (!isCreated()) {
                    return getRedirectURLAfterSave();
                }
                return postSave(true, false);
            }

            handleValidationErrors(validationErrorMsg);
            return null;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RollbackException();
        }
    }

    public String saveReport() {
        final BfabricUploadedFile uploadedFile = fileUploadHelper.getSingleUploadedFile();
        if (uploadedFile != null) {
            printFacesMessagesClear(projectService.saveReport(getProject(), uploadedFile, getReportYear()));
            return getRedirectURLAfterSave();
        }
        return null;
    }

    public void setDoi() {
        printFacesMessagesClear(projectService.setDoi(getProject(), getCurrentUser()));
    }

    public void setFullView(boolean fullView) {
        this.fullView = fullView;
    }

    public void setReportYear(int reportYear) {
        this.reportYear = reportYear;
    }

    public String setRunning() {
        return printFacesMessagesClearMenuHeaderAndRedirect(projectService.setRunning(getProject()));
    }

    public void switchFullView() {
        setFullView(!isFullView());
    }
}