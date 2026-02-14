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

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.Offer;
import org.bfabric.entity.User;
import org.bfabric.entity.api.HasSupervisor;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ContainerService;
import org.bfabric.service.InstrumentService;
import org.bfabric.service.OfferService;
import org.bfabric.service.UserService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class DutyManager extends AbstractEntityManager<User> {

    private static final long serialVersionUID = 1;

    @Inject
    private ContainerService containerService;

    private List<Container> containers;

    @Inject
    private InstrumentService instrumentService;

    private List<Instrument> instruments;

    @Inject
    private OfferService offerService;

    private List<Offer> offers;

    private User selectedUser;

    private List<HasSupervisor> supervisorDuties;

    @Param
    private Long userId;

    @Inject
    private UserService userService;

    public String cancel() {
        getFacesMessagesManager().bufferWarningClear(Messages.get("canceled"));
        return redirect();
    }

    public List<Container> getContainers() {
        if (containers == null) {
            setContainers();
        }
        return containers;
    }

    public List<User> getEmployees(String filterString) {
        return userService.getEmployeesFilteredIncludingUser(filterString, getInstance());
    }

    public List<Instrument> getInstruments() {
        if (instruments == null) {
            setInstruments();
        }
        return instruments;
    }

    public List<Offer> getOffers() {
        if (offers == null) {
            setOffers();
        }
        return offers;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public List<HasSupervisor> getSupervisorDuties() {
        if (supervisorDuties == null) {
            setSupervisorDuties();
        }
        return supervisorDuties;
    }

    public User getUser() {
        return getInstance();
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (userId != null) {
            setInstance(entityService.find(User.class, userId));
        }
    }

    public boolean isRenderedDuties() {
        return getUser() != null && getUser().isRenderedDuties() && (!getSupervisorDuties().isEmpty() || !getContainers().isEmpty() || !getInstruments().isEmpty() || !getOffers().isEmpty());
    }

    public String reassign() {
        int changed = 0;
        for (Container container : getContainers()) {
            if (container.isCoachChanged() || container.isCoachBackupChanged() || container.isBioinformaticianChanged()) {
                containerService.save(container);
                changed++;
            }
        }
        for (Instrument instrument : getInstruments()) {
            if (instrument.hasAdminChanged()) {
                for (HasSupervisor duty : getSupervisorDuties()) {
                    if (duty instanceof Instrument && instrument.equals(duty) && duty.isSupervisorChanged()) {
                        instrument.setSupervisor(duty.getSupervisor());
                    }
                }
                instrumentService.save(instrument);
                changed++;
            }
        }
        for (Offer offer : getOffers()) {
            if (offer.isCoachChanged() || offer.isCoachBackupChanged()) {
                offerService.save(offer);
                changed++;

            }
        }
        for (HasSupervisor supervisorDuties : getSupervisorDuties()) {
            if (supervisorDuties.isSupervisorChanged() && (!(supervisorDuties instanceof Instrument) || !getInstruments().contains(supervisorDuties))) {
                entityService.save((AbstractEntity) supervisorDuties);
                changed++;
            }
        }
        getFacesMessagesManager().bufferWarningClear((changed > 0 ? Messages.get("successfullySaved") : Messages.get("nothingToSave")) + " " + changed + " " + Messages.get("reassignments"));
        return redirect();
    }

    public void reassignAdminForAllListener(ValueChangeEvent event) {
        User selectedUser = (User) event.getNewValue();
        if (selectedUser != null) {
            for (Instrument duty : getInstruments()) {
                duty.setAdmin(selectedUser);
            }
        }
        setSelectedUser(null);
    }

    public void reassignBioinformaticianForAllListener(ValueChangeEvent event) {
        User selectedUser = (User) event.getNewValue();
        if (selectedUser != null) {
            for (Container container : getContainers()) {
                container.setBioinformatician((User) event.getNewValue());
            }
            setSelectedUser(null);
        }
    }

    public void reassignCoachBackupForAllListener(ValueChangeEvent event) {
        User selectedUser = (User) event.getNewValue();
        if (selectedUser != null) {
            for (Container container : getContainers()) {
                container.setCoachBackup(selectedUser);
            }
            setSelectedUser(null);
        }
    }

    public void reassignCoachForAllListener(ValueChangeEvent event) {
        User selectedUser = (User) event.getNewValue();
        if (selectedUser != null) {
            for (Container container : getContainers()) {
                container.setCoach(selectedUser);
            }
        }
        setSelectedUser(null);
    }

    public void reassignOfferCoachForAllListener(ValueChangeEvent event) {
        User selectedUser = (User) event.getNewValue();
        if (selectedUser != null) {
            for (Offer duty : getOffers()) {
                duty.setCoach(selectedUser);
            }
        }
        setSelectedUser(null);
    }

    public void reassignSupervisorForAllListener(ValueChangeEvent event) {
        User selectedUser = (User) event.getNewValue();
        if (selectedUser != null) {
            for (HasSupervisor duty : getSupervisorDuties()) {
                duty.setSupervisor(selectedUser);
            }
        }
        setSelectedUser(null);
    }

    public String redirect() {
        return createRedirectURL("user/duties", null, null, Collections.singletonMap("userId", getUser().getIdString()));
    }

    public void setContainers() {
        containers = containerService.getReassignableContainersByUserId(userId);
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    public void setInstruments() {
        instruments = instrumentService.getReassignableAdminInstrumentsByUserId(userId);
        for (Instrument instrument : instruments) {
            instrument.setOldValues();
        }
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public void setOffers() {
        offers = offerService.getReassignableOffersByUserId(userId);
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public void setSupervisorDuties() {
        supervisorDuties = entityService.getSupervisorDutiesLazyModelByUserId(userId);
    }

    public void setSupervisorDuties(List<HasSupervisor> supervisorDuties) {
        this.supervisorDuties = supervisorDuties;
    }
}