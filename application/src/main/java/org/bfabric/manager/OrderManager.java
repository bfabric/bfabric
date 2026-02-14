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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Offer;
import org.bfabric.entity.Order;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Project;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePlatePosition;
import org.bfabric.entity.ServiceType;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.OfferService;
import org.bfabric.service.OrderService;
import org.bfabric.service.ProjectService;
import org.bfabric.service.SampleService;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.omnifaces.util.Ajax;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@MeasureCalls
@Named
@ViewScoped
public class OrderManager extends ContainerManager<Order> {

    private static final long serialVersionUID = 1;

    private Offer offer;

    @Inject
    private OrderService orderService;

    @Param
    private Long projectId;

    @Inject
    private ProjectService projectService;

    private List<SamplePlatePosition> samplePlatePositionsOnPlate;

    @Inject
    private SampleService sampleService;

    private List<Sample> samplesInMultiplex;

    private ServiceType serviceTypeShowServices;

    private boolean showSamplePlatePositionsOnPlate = false;

    private boolean showSamplesInMultiplex = false;

    private Boolean showUserSubmittedEditColumn;

    public OrderManager() {
        super(Order.class);
    }

    public String changeStatus(StatusEnum statusEnum) {
        initProcessPlatesAndSubmittable();
        if (getOrder().getProcessPlatesAndSubmittable() != null && !getOrder().getProcessPlatesAndSubmittable()) {
            getFacesMessagesManager().clearGlobalMessages();
            getFacesMessagesManager().printError(Messages.get("validationErrors"));
            return null;
        }
        printFacesMessagesClear(orderService.changeStatus(getOrder(), statusEnum));
        String tab = statusEnum.equals(StatusEnum.SUBMITTED) ? "instructions" : "details";
        return getShowScreenRedirectURL(tab);
    }

    @Override
    public Order createInstance() {
        Order order = null;
        if (getCurrentUser() != null) {
            order = super.createInstance();
            if (getProjectId() != null) {
                Project project = entityService.find(Project.class, getProjectId());
                if (project != null) {
                    order.setProject(project);
                    order.setOldBudgetOfficer(project.getBudgetOfficer());
                    order.setBudgetOfficerAndBillingData(project.getBudgetOfficer());
                }
            }
        }
        return order;
    }

    public List<Offer> getAssignableOffersFiltered(String filterString) {
        return CDI.current().select(OfferService.class).get().getAssignableOffersFiltered(getCurrentUser(), filterString, getOrder().getOffers());
    }

    public Offer getOffer() {
        return offer;
    }

    @Produces
    @Named("order")
    public Order getOrder() {
        return getInstance();
    }

    public List<Project> getOrderAssignableProjects(String filterString) {
        Set<Project> currentProjects = new HashSet<>();
        if (getOrder().getProject() != null) {
            currentProjects.add(getOrder().getProject());
        }
        User filterUser = identityManager.hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) ? null : getOrder().getRequester();
        return projectService.getOrderAssignableProjects(filterString, filterUser, currentProjects);
    }

    public Long getProjectId() {
        return projectId;
    }

    @Override
    public String getRedirectURLAfterRemove() {
        return getRefererURL() != null && getRefererURL().matches(".*(/list).*") ? getRedirectURLFromRefererUrl() : getListScreenRedirectURL(getCurrentUser().hasRoleImplicit(RoleEnum.EMPLOYEE));
    }

    @Override
    public String getRedirectURLAfterSave() {
        boolean isRedirectToEditBatchScreen = getOrder().isProcessesSamples() && (isCreated() || getOrder().getOrderItems().isEmpty());
        HashMap<String, String> fParams = null;
        if (isRedirectToEditBatchScreen) {
            if (!getOrder().isProcessesPlates()) {
                if (getOrder().getNumberOfSamples() != null) {
                    fParams = new HashMap<>();
                    fParams.put("initializeItems", "true");
                }
            } else {
                if (getOrder().getNumberOfPlates() != null) {
                    fParams = new HashMap<>();
                    fParams.put("initializeItems", "true");
                }
            }
        }
        return isRedirectToEditBatchScreen ? !getOrder().isProcessesPlates() ? createRedirectURL("orderitem/edit-batch-with-create", getOrder()
            .getId(), null, fParams) : createRedirectURL("orderitem/edit-batch-with-create-plate", getOrder().getId(), null, fParams) : getShowScreenRedirectURL();
    }

    public List<SamplePlatePosition> getSamplePlatePositionsOnPlate() {
        if (samplePlatePositionsOnPlate == null) {
            samplePlatePositionsOnPlate = new ArrayList<>();
            for (OrderItem orderItem : getOrder().getOrderItems()) {
                samplePlatePositionsOnPlate.addAll(orderItem.getPlate().getCurrentSamplePlatePositionsOrderedByAssignmentOrder());
            }
        }
        return samplePlatePositionsOnPlate;
    }

    public List<Sample> getSamplesInMultiplex() {
        if (samplesInMultiplex == null) {
            samplesInMultiplex = new ArrayList<>();
            for (OrderItem orderItem : getOrder().getOrderItems()) {
                Sample sample = orderItem.getSample();
                if (sample != null) {
                    // If the multiplexed sample itself should also be shown, uncomment the commented out lines.
                    //if(sample.getLibraryPooled() != null && sample.getLibraryPooled()) {
                    sample.initializeInitialParentSamplesOfUserMultiplex();
                    //    samplesInMultiplex.add(sample);
                    samplesInMultiplex.addAll(sample.getParentSamplesOfUserMultiplex());
                    //}
                }
            }
        }
        return samplesInMultiplex;
    }

    public List<Offer> getSelectableOffersFiltered(String filterString) {
        return CDI.current().select(OfferService.class).get().getAssignableOffersFiltered(getCurrentUser(), filterString, null);
    }

    public ServiceType getServiceTypeShowServices() {
        return serviceTypeShowServices;
    }

    public Boolean getShowUserSubmittedEditColumn() {
        return showUserSubmittedEditColumn;
    }

    public List<User> getUsersFiltered(String filterString) {
        final Project project = getOrder().getProject();
        if (project != null) {
            // The requester can only be changed to a member of the project the order is associated with.
            final List<User> filteredMembers = new ArrayList<>();
            for (User user : project.getMembers()) {
                if (StringHelper.isEmpty(filterString) || user.getFirstName().toLowerCase().contains(filterString.toLowerCase()) || user.getLastName().toLowerCase()
                    .contains(filterString.toLowerCase()) || user.getLogin().toLowerCase().contains(filterString.toLowerCase())) {
                    filteredMembers.add(user);
                }
            }
            return filteredMembers;
        }
        return userService.getUsersFiltered(filterString);
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getOrder() != null) {
            getOrder().getInitialOffers().addAll(getOrder().getOffers());
            if (getOrder().isManaged() && getOrder().isProcessesPlates()) {
                if (!getOrder().isUpdatable()) {
                    setShowUserSubmittedEditColumn(false);
                } else {
                    for (OrderItem orderItem : getOrder().getOrderItems()) {
                        if (orderItem.getPlate() != null && orderItem.getPlate().isUpdatableOrUserUpdatable()) {
                            setShowUserSubmittedEditColumn(true);
                            break;
                        }
                    }
                }
            }
            if (getOrder().isManaged() && getOrder().getServiceType() != null && getOrder().getServiceType().isOrderAttribute("numberOfCellsNuclei")) {
                getOrder().setNumberOfCellsNucleiOld(getOrder().getNumberOfCellsNuclei());
            }
            getOrder().initializeRowStyleClassAndRowTitleCoupled();
        }
    }

    private void initProcessPlatesAndSubmittable() {
        if (getOrder() != null) {
            getOrder().setProcessPlatesAndSubmittable(null);
            getOrder().getInvalidPlates().clear();
            boolean processPlatesAndSubmittable;
            if (getOrder().isPending() && getOrder().isProcessesPlates()) {
                processPlatesAndSubmittable = true;
                getOrder().getInvalidPlates().clear();
                for (OrderItem orderItem : getOrder().getOrderItems()) {
                    Set<Sample> samples = orderItem.getPlate().getSamples();
                    if (samples.isEmpty()) {
                        processPlatesAndSubmittable = false;
                        getOrder().getInvalidPlates().add(orderItem.getPlate().getId());
                        break;
                    }
                    for (Sample sample : samples) {
                        if (!sampleService.isValid(sample).isEmpty()) {
                            processPlatesAndSubmittable = false;
                            getOrder().getInvalidPlates().add(orderItem.getPlate().getId());
                            break;
                        }
                    }
                }
                getOrder().setProcessPlatesAndSubmittable(processPlatesAndSubmittable);
            }
        }
    }

    public boolean isOffersAssignable() {
        return getOrder() != null && !Arrays.asList(StatusEnum.PENDING, StatusEnum.CANCELED, StatusEnum.FINISHED, StatusEnum.CLOSED, StatusEnum.INVALID)
            .contains(getOrder().getStatus()) && !getAssignableOffersFiltered(Constants.EMPTY_STRING).isEmpty();
    }

    public boolean isShowSamplePlatePositionsOnPlate() {
        return showSamplePlatePositionsOnPlate;
    }

    public boolean isShowSamplesInMultiplex() {
        return showSamplesInMultiplex;
    }

    public void offersUnselect(UnselectEvent<Offer> event) {
        getOrder().getOffers().remove(event.getObject());
    }

    public void projectClearListener() {
        if (!isManaged() && getOrder().getRequester() != null && getOrder().getBudgetOfficer() == null) {
            getOrder().setBudgetOfficerAndBillingData(getOrder().getRequester());
            Ajax.update("edit:billinginfo");
        }
        Ajax.update("edit:budgetofficergrid");
    }

    public void projectSelectListener(SelectEvent<Project> event) {
        final Project selectedProject = event.getObject();
        if (!isManaged() && selectedProject != null) {
            getOrder().setBudgetOfficerAndBillingData(selectedProject.getBudgetOfficer());
            Ajax.update("@this", "edit:budgetofficergrid", "edit:billinginfo");
        }
    }

    @Override
    public String remove() {
        final String entityName = getOrder().toString();
        orderService.remove(getOrder());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyDeleted") + " " + entityName);
        return getRedirectURLAfterRemove();
    }

    @Override
    public String save() {
        getOrder().resetOrderAttributes();

        if (getSelectedDiscussedWith() != null) {
            getOrder().setDiscussedWith(new HashSet<>(getSelectedDiscussedWith()));
        }

        LinkedHashMap<String, String> validationErrorMsg = orderService.isValid(getOrder());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            getFacesMessagesManager().bufferErrors(orderService.save(getOrder(), getCurrentUser(), getOrder().updateOffers()));
            if (!isCreated()) {
                // Print some additional messages to inform the user about changes in the order managers.
                getFacesMessagesManager().bufferWarning(Messages.get("successfullyUpdated"));
                addFacesMessagesForChangedManager();
            }

            if (!isCreated() && getOrder().getProject() == null) {
                return getRedirectURLAfterSave();
            }
            return postSave(true, false);
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public String sendMailCustomStateTransition() {
        printFacesMessagesClear(containerService.sendMailCustomStateTransition(getContainer()));
        return getShowScreenRedirectURL();
    }

    public void serviceTypeSelected(SelectEvent<ServiceType> event) {
        ServiceType serviceType = event.getObject();
        if (serviceType != null) {
            if (!serviceType.getSequencingApplications().contains(getOrder().getSequencingApplication())) {
                getOrder().setSequencingApplication(null);
            }
            if (!serviceType.getInstruments().contains(getOrder().getInstrument())) {
                getOrder().setInstrument(null);
            }
            getOrder().getTechnologies().clear();
            getOrder().addTechnologies(serviceType.getTechnologies());
            if (!getOrder().isManaged() && serviceType.isOrderAttributeAndEnabled("nuclei")) {
                getOrder().setNuclei(Boolean.FALSE);
            }
            if (getOrder().getCustomOption() == null && serviceType.isOrderAttributeAndEnabled("customOption")) {
                getOrder().setCustomOption(Boolean.FALSE);
            }
        }
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public void setServiceTypeShowServices(ServiceType serviceTypeShowServices) {
        this.serviceTypeShowServices = serviceTypeShowServices;
    }

    public void setShowSamplePlatePositionsOnPlate(boolean showSamplePlatePositionsOnPlate) {
        this.showSamplePlatePositionsOnPlate = showSamplePlatePositionsOnPlate;
    }

    public void setShowSamplesInMultiplex(boolean showSamplesInMultiplex) {
        this.showSamplesInMultiplex = showSamplesInMultiplex;
    }

    public void setShowUserSubmittedEditColumn(Boolean showUserSubmittedEditColumn) {
        this.showUserSubmittedEditColumn = showUserSubmittedEditColumn;
    }

    public String submitReplacements() {
        printFacesMessagesClear(containerService.changeCustomStatus(getContainer(), Order.WAITING_FOR_REPLACEMENT_SAMPLES));
        return getShowScreenRedirectURL();
    }
}
