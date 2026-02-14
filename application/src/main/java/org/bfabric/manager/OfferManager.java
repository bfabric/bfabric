/*
 *
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Container;
import org.bfabric.entity.Offer;
import org.bfabric.entity.OfferedCharge;
import org.bfabric.enums.StatusEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ChargeService;
import org.bfabric.service.MailSendService;
import org.bfabric.service.OfferService;
import org.bfabric.service.OfferedChargeService;
import org.omnifaces.util.Ajax;
import org.primefaces.event.UnselectEvent;

@MeasureCalls
@Named
@ViewScoped
public class OfferManager extends AbstractEntityManager<Offer> {

    private static final long serialVersionUID = 1;

    @Inject
    private ChargeService chargeService;

    private Container container;

    // Should the prices for all charges of this offer be recomputed?
    private boolean discountRateChanged = false;

    private Set<Container> initialContainers = new HashSet<>();

    @Inject
    private MailSendService mailSendService;

    @Inject
    private OfferService offerService;

    @Inject
    private OfferedChargeService offeredChargeService;

    private boolean registeredUser = false;

    private List<Charge> selectedChargesFrom = new ArrayList<>();

    private List<OfferedCharge> selectedChargesTo = new ArrayList<>();

    // Should the current service prices be used while copying a charge into a project?
    private boolean useCurrentServicePrices = false;

    public OfferManager() {
        super(Offer.class);
    }

    public String changeStatus(StatusEnum statusEnum) {
        printFacesMessagesClear(offerService.changeStatus(getOffer(), statusEnum));
        return getShowScreenRedirectURL();
    }

    public void containerChanged(ValueChangeEvent event) {
        setContainerAndCharges((Container) event.getNewValue());
        Ajax.update("copy-charges-from-container:chargesPanel", "copy-charges-to-container:chargesPanel");
    }

    public void containersUnselect(UnselectEvent<Container> event) {
        getOffer().getContainers().remove(event.getObject());
    }

    public String copyFromCharges() {
        if (!getSelectedChargesFrom().isEmpty()) {
            offeredChargeService.copyFromCharges(getOffer(), getSelectedChargesFrom());
            getFacesMessagesManager().bufferWarningClear("Successfully copied from charges");
            return createRedirectShowScreenURL(getOffer());
        }
        return null;
    }

    public String copyToCharges() {
        if (!getSelectedChargesTo().isEmpty()) {
            offeredChargeService.copyToCharges(getContainer(), getSelectedChargesTo(), isUseCurrentServicePrices());
            if (getOffer().isExpired()) {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCreatedChargesBasedOnCurrentPrices"));
            } else {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCreatedChargesFromOffer").replace("{0}", Long.toString(getOffer().getId())).replace("{1}", Integer.toString(
                    getSelectedChargesTo().size())));
            }
            return createRedirectShowScreenURL(getContainer(), "charges", null);
        }
        return null;
    }

    @Override
    protected Offer createInstance() {
        final Offer offer = super.createInstance();
        if (offer != null) {
            offer.setCoach(getCurrentUser());
        }
        return offer;
    }

    public void discountChanged(ValueChangeEvent event) {
        getOffer().setDiscount(new BigDecimal(String.valueOf(event.getNewValue())));
        setDiscountRateChanged(true);
    }

    public List<Container> getAssignableContainers(String filterString) {
        Set<Container> include = new HashSet<>(getInitialContainers());
        include.removeAll(getOffer().getContainers());
        return containerService.getOfferAssignableContainersIncludingAndExcluding(filterString, include, getOffer().getContainers());
    }

    public List<OfferedCharge> getChargesToList(String filterString) {
        if (selectedChargesTo == null) {
            selectedChargesTo = new ArrayList<>();
        }
        return offeredChargeService.getFilteredOfferedChargesByOfferIdAndSelectedChargesTo(filterString, getOffer().getId(), getSelectedChargesTo());
    }

    public Container getContainer() {
        return container;
    }

    public List<Charge> getFilteredCharges(String filterString) {
        if (selectedChargesFrom == null) {
            selectedChargesFrom = new ArrayList<>();
        }
        return chargeService.getChargesByContainerAndSelectedChargesFrom(filterString, getContainer(), getSelectedChargesFrom());
    }

    public Set<Container> getInitialContainers() {
        return initialContainers;
    }

    @Produces
    @Named("offer")
    public Offer getOffer() {
        return getInstance();
    }

    @Override
    public String getRedirectURLAfterRemove() {
        return getRefererURL() != null && !getRefererURL().contains("offeredcharge/show") ? super.getRedirectURLAfterRemove() : getListScreenRedirectURL();
    }

    public List<Charge> getSelectedChargesFrom() {
        return selectedChargesFrom;
    }

    public List<OfferedCharge> getSelectedChargesTo() {
        return selectedChargesTo;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getOffer() != null) {
            setInitialContainers(getOffer().getContainers());
            if (!getInitialContainers().isEmpty()) {
                setContainerAndCharges(getInitialContainers().iterator().next());
            }
            if (getOffer().getRequester() != null) {
                setRegisteredUser(true);
            }
        }
    }

    public boolean isCopyFromChargesAutocompleteRendered() {
        return !getContainer().getCharges().isEmpty();
    }

    public boolean isDiscountRateChanged() {
        return discountRateChanged;
    }

    public boolean isRegisteredUser() {
        return registeredUser;
    }

    public boolean isUseCurrentServicePrices() {
        return useCurrentServicePrices;
    }

    public String lock() {
        return changeStatus(StatusEnum.LOCKED);
    }

    public void registeredUserChanged(ValueChangeEvent event) {
        setRegisteredUser((Boolean) event.getNewValue());
        getOffer().setOrganizationType(null);
        if (isRegisteredUser()) {
            getOffer().setRequesterName(null);
            getOffer().setRequesterAddress(null);
            getOffer().setRequesterEmail(null);
        } else {
            getOffer().setRequester(null);
        }
    }

    public String rollbackStatus() {
        offerService.rollbackStatus(getOffer());
        getFacesMessagesManager().bufferWarningClear(Messages.get("statusRolledBack"));
        return getShowScreenRedirectURL();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = offerService.isValid(getOffer(), isRegisteredUser());

        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            offerService.save(getOffer());
            if (getOffer().isCloned()) {
                getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCloned"));
            }
            return postSave(!getOffer().isCloned(), false);
        }

        handleValidationErrors(validationErrorMsg);
        return null;
    }

    public String sendOffer() {
        String failed = mailSendService.send(getOffer().createMail());
        if (failed == null) {
            lock();
            getFacesMessagesManager().bufferWarningClear(Messages.get("sendOfferDone"));
        } else {
            getFacesMessagesManager().bufferWarningClear(Messages.get("sendOfferFailed"));
        }
        return getShowScreenRedirectURL();
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void setContainerAndCharges(Container container) {
        setContainer(container);
        setSelectedChargesFrom(new ArrayList<>());
        setSelectedChargesTo(getChargesToList(null));
    }

    public void setDiscountRateChanged(boolean discountRateChanged) {
        this.discountRateChanged = discountRateChanged;
    }

    public void setInitialContainers(Set<Container> initialContainers) {
        this.initialContainers = initialContainers;
    }

    public void setRegisteredUser(boolean registeredUser) {
        this.registeredUser = registeredUser;
    }

    public void setSelectedChargesFrom(List<Charge> selectedChargesFrom) {
        this.selectedChargesFrom = selectedChargesFrom;
    }

    public void setSelectedChargesTo(List<OfferedCharge> selectedChargesTo) {
        this.selectedChargesTo = selectedChargesTo;
    }

    public void setUseCurrentServicePrices(boolean useCurrentServicePrices) {
        this.useCurrentServicePrices = useCurrentServicePrices;
    }
}