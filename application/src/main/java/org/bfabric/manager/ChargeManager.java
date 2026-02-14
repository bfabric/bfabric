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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.InstrumentReservation;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Service;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ChargeService;
import org.bfabric.service.InstrumentReservationService;
import org.bfabric.service.TaxTypeService;
import org.omnifaces.cdi.Param;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DualListModel;

@MeasureCalls
@Named
@ViewScoped
public class ChargeManager extends AbstractEntityManager<Charge> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(ChargeManager.class.getName());

    @Inject
    private ChargeService chargeService;

    @Param
    private Long containerId;

    @Param
    private Long instrumentReservationId;

    @Inject
    private InstrumentReservationService instrumentReservationService;

    @Param
    private Long orderItemId;

    private DualListModel<OrderItem> orderItemsModel;

    private DualListModel<Sample> samplesModel;

    private List<Service> selectedServices = new ArrayList<>();

    @Inject
    private TaxTypeService taxTypeService;

    private ValueChangeEvent valueChangeEvent = null;

    public ChargeManager() {
        super(Charge.class);
    }

    public void approveContainerChanged() {
        getCharge().setContainer((Container) getValueChangeEvent().getNewValue());
        getCharge().getSamples().clear();
        getCharge().getOrderItems().clear();
        setSamplesModel(null);
        setOrderItemsModel(null);
        getCharge().getInstrumentReservations().removeIf(instrumentReservation -> !instrumentReservation.getContainers().contains(getCharge().getContainer()));
        setValueChangeEvent(null);
        getContextManager().setContextContainer(getCharge().getContainer());
    }

    public void cancelContainerChanged() {
        getCharge().setContainer((Container) getValueChangeEvent().getOldValue());
        setValueChangeEvent(null);
    }

    public void containerChanged(ValueChangeEvent event) {
        setValueChangeEvent(event);
        if (getCharge().getInstrumentReservations().isEmpty() && (getSamplesModel() == null || getSamplesModel().getTarget().isEmpty()) && (getOrderItemsModel() == null || getOrderItemsModel()
            .getTarget().isEmpty())) {
            approveContainerChanged();
            PrimeFaces.current().ajax().update(Constants.EDIT);
        } else {
            // Open confirmation modal.
            PrimeFaces.current().executeScript("PF('confirmContainerChangeDialog').show();");
        }
    }

    @Override
    protected Charge createInstance() {
        Charge charge = super.createInstance();

        if (getContainerId() != null) {
            charge.setContainer(entityService.find(Container.class, getContainerId()));
        } else if (getContextContainer() != null && getContextContainer().isExtensible()) {
            charge.setContainer(entityService.find(Container.class, getContextContainer().getId()));
        }

        if (getOrderItemId() != null) {
            OrderItem orderItem = entityService.find(OrderItem.class, getOrderItemId());
            charge.getOrderItems().add(orderItem);
        }

        Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (map.containsKey("selectedInstrumentReservations")) {
            charge.getInstrumentReservations().addAll(getSelectedInstrumentReservations(map.get("selectedInstrumentReservations")));
            charge.setTotalAndComputePrice(getCharge().computeTotal(charge.getInstrumentReservations()));
        }

        if (getInstrumentReservationId() != null) {
            InstrumentReservation instrumentReservation = entityService.find(InstrumentReservation.class, getInstrumentReservationId());
            if (instrumentReservation != null) {
                charge.getInstrumentReservations().add(instrumentReservation);
                if (getContainerId() == null) {
                    instrumentReservation.getContainers().stream().findFirst().ifPresent(charge::setContainer);
                }
                Service service = instrumentReservation.getInstrument().getService();
                if (service != null) {
                    charge.setService(service);
                    getSelectedServices().add(service);
                }
                charge.setTotalAndComputePrice(getCharge().computeTotal(charge.getInstrumentReservations()));
            }
        }

        charge.setTaxType(taxTypeService.getDefaultTaxType());
        charge.setTaxRate(charge.getTaxType().getTax());
        charge.setCharger(getCurrentUser());

        return charge;
    }

    @Produces
    @Named("charge")
    public Charge getCharge() {
        return getInstance();
    }

    public Charge getClone(Charge original) {
        Charge charge = null;
        try {
            charge = original.clone();

            // Avoid shared references to samples.
            final Set<Sample> samples = new HashSet<>(original.getSamples());
            charge.setSamples(samples);

            // Avoid shared references to order items.
            final Set<OrderItem> orderItems = new HashSet<>(original.getOrderItems());
            charge.setOrderItems(orderItems);

        } catch (final CloneNotSupportedException e) {
            logger.severe(e.getMessage());
        }

        return charge;
    }

    public Long getContainerId() {
        return containerId;
    }

    public List<InstrumentReservation> getFilteredInstrumentReservationAsList(String filter) {
        return instrumentReservationService.getInstrumentReservationByContainerQuery(filter, getCharge().getInstrumentReservations(), getCharge().getContainer()
            .getInstrumentReservations(), getCharge().getChargeTimeUnit());
    }

    public Long getInstrumentReservationId() {
        return instrumentReservationId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public DualListModel<OrderItem> getOrderItemsModel() {
        if (orderItemsModel == null && getCharge().getContainer() != null && getCharge().getContainer().getOrderItems() != null) {
            // Initialize source list and remove entities already contained in the target list to prevent duplicates.
            final ArrayList<OrderItem> sourceList = new ArrayList<>(getCharge().getContainer().getOrderItems());
            sourceList.removeAll(getCharge().getOrderItems());
            // Initialize the target list.
            final ArrayList<OrderItem> targetList = new ArrayList<>(getCharge().getOrderItems());
            // Initialize the DualListModel.
            orderItemsModel = new DualListModel<>(sourceList, targetList);
        }
        return orderItemsModel;
    }

    @Override
    public String getRedirectURLAfterRemove() {
        return getRefererURL() != null ? getRedirectURLFromRefererUrl() : getListScreenRedirectURL(getCurrentUser().hasRoleImplicit(RoleEnum.EMPLOYEE));
    }

    @Override
    public String getRedirectURLAfterSave() {
        if (isManaged()) {
            return super.getRedirectURLAfterSave();
        }
        if (getInstrumentReservationId() != null) {
            return createRedirectShowScreenURL(entityService.find(InstrumentReservation.class, getInstrumentReservationId()));
        }
        return createRedirectShowScreenURL(getCharge().getContainer(), "charges", null);
    }

    public DualListModel<Sample> getSamplesModel() {
        if (samplesModel == null && getCharge().getContainer() != null) {
            // Initialize source list and remove entities already contained in the target list to prevent duplicates.
            final ArrayList<Sample> sourceList = new ArrayList<>(getCharge().getContainer().getSamples());
            sourceList.removeAll(getCharge().getSamples());
            // Initialize the target list.
            final ArrayList<Sample> targetList = new ArrayList<>(getCharge().getSamples());
            // Initialize the DualListModel.
            samplesModel = new DualListModel<>(sourceList, targetList);
        }
        return samplesModel;
    }

    public List<InstrumentReservation> getSelectedInstrumentReservations(String selectedInstrumentReservationsParameter) {
        List<String> selectedInstrumentReservationIds = new ArrayList<>();
        if (selectedInstrumentReservationsParameter != null) {
            selectedInstrumentReservationIds = Arrays.stream(selectedInstrumentReservationsParameter.split(",")).collect(Collectors.toList());
        }
        List<InstrumentReservation> instrumentReservations = new ArrayList<>();
        for (String str : selectedInstrumentReservationIds) {
            instrumentReservations.add(entityService.find(InstrumentReservation.class, Long.valueOf(str)));
        }
        return instrumentReservations;
    }

    public List<Service> getSelectedServices() {
        if (selectedServices != null && !selectedServices.isEmpty()) {
            Set<Service> childServices = new HashSet<>();
            for (Service service : selectedServices) {
                if (!service.getChildren().isEmpty()) {
                    childServices.addAll(service.getChildren());
                }
            }
            for (Service service : childServices) {
                if (!selectedServices.contains(service)) {
                    selectedServices.add(service);
                }
            }
        }
        return selectedServices;
    }

    public BigDecimal getTotalDiscountedPriceForSelectedServices() {
        double price = 0;
        for (final Service service : getSelectedServices()) {
            Charge charge;
            try {
                charge = getCharge().clone();
                charge.setService(service);
                charge.setPrice();
                price += charge.getDiscountedPrice().doubleValue();
            } catch (final CloneNotSupportedException e) {
                logger.severe(e.getMessage());
            }
        }
        return BigDecimal.valueOf(price);
    }

    public BigDecimal getTotalPriceForSelectedServices() {
        double price = 0;
        for (final Service service : getSelectedServices()) {
            Charge charge;
            try {
                charge = getCharge().clone();
                charge.setService(service);
                charge.setPrice();
                price += charge.getPrice().doubleValue();
            } catch (final CloneNotSupportedException e) {
                logger.severe(e.getMessage());
            }
        }
        return BigDecimal.valueOf(price);
    }

    public ValueChangeEvent getValueChangeEvent() {
        return valueChangeEvent;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getCharge() != null) {
            setChargeUnitTime();
            if (getInstance().getContainer() != null) {
                getContextManager().setContextContainer(getInstance().getContainer());
            }
            if (isManaged()) {
                getSelectedServices().add(getCharge().getService());
            } else if (getContainerId() != null) {
                getContextManager().setContextContainer(entityService.find(Container.class, getContainerId()));
            }
        }
    }

    @Override
    public void initClone() throws CloneNotSupportedException {
        super.initClone();
        if (getCharge() != null) {
            getSelectedServices().add(getCharge().getService());
            if (getCharge().getContainer().isInFinalState()) {
                getCharge().setContainer(null);
            }
        }
    }

    @Override
    public String save() {
        if (getSamplesModel() != null) {
            getCharge().setSamples(new HashSet<>(getSamplesModel().getTarget()));
        }
        if (getOrderItemsModel() != null) {
            getCharge().setOrderItems(new HashSet<>(getOrderItemsModel().getTarget()));
        }
        LinkedHashMap<String, String> errorMsg = chargeService.isValid(getCharge(), getSelectedServices().size());
        if (errorMsg.isEmpty()) {
            // Cache whether the entity is created or not.
            setCreated(!isManaged());
            chargeService.save(getCharge(), getSelectedServices());
            return postSave(true, isCreated());
        }
        getFacesMessagesManager().printValidationErrors(errorMsg);
        return null;
    }

    public void serviceChanged(SelectEvent<Service> event) {
        Service service = event.getObject();
        if (service != null && !getSelectedServices().contains(service)) {
            if (isManaged() && getSelectedServices().size() == 1) {
                getSelectedServices().clear();
            }
            getSelectedServices().add(service);
            getCharge().setService(service);
            getCharge().setPrice();
        }
    }

    public void setChargeUnitTime() {
        List<InstrumentReservation> instrumentReservations = getCharge().getInstrumentReservationsAsList();
        if (!instrumentReservations.isEmpty()) {
            getCharge().setChargeTimeUnit(instrumentReservations.get(instrumentReservations.size() - 1).getInstrumentReservationSetting().getChargeTimeUnit());
        }
    }

    public void setInstrumentReservationId(Long instrumentReservationId) {
        this.instrumentReservationId = instrumentReservationId;
    }

    public void setOrderItemsModel(DualListModel<OrderItem> orderItemsModel) {
        this.orderItemsModel = orderItemsModel;
    }

    public void setSamplesModel(DualListModel<Sample> samplesModel) {
        this.samplesModel = samplesModel;
    }

    public void setSelectedServices(List<Service> selectedServices) {
        this.selectedServices = selectedServices;
    }

    public void setValueChangeEvent(ValueChangeEvent valueChangeEvent) {
        this.valueChangeEvent = valueChangeEvent;
    }
}