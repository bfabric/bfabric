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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Charge;
import org.bfabric.entity.Project;
import org.bfabric.entity.Service;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ChargeService;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@ViewScoped
public class ChargesManager extends AbstractEntityManager<Charge> {

    private static final int defaultNumberOfLines = 5;

    private static final long serialVersionUID = 1;

    private boolean allRowsBillable;

    private LocalDate allRowsDate;

    private String allRowsDescription;

    private BigDecimal allRowsNotAccounted;

    private String allRowsNotes;

    private Project allRowsProject;

    private Service allRowsService;

    private BigDecimal allRowsTotal;

    @Inject
    private ChargeService chargeService;

    private List<Charge> charges;

    private List<Charge> newCharges = new ArrayList<>();

    private int numberOfNewLines = 1;

    public ChargesManager() {
        super(Charge.class);
    }

    public void addLinesToNewCharges() {
        for (int i = 0; i < getNumberOfNewLines(); i++) {
            Charge currentCharge = new Charge();
            currentCharge.setDate(LocalDate.now());
            currentCharge.setCharger(getCurrentUser());
            getNewCharges().add(currentCharge);
        }
        setNumberOfNewLines(1);
    }

    public void changeBillableForAllRows() {
        for (Charge currentCharge : getNewCharges()) {
            currentCharge.setBillable(allRowsBillable);
        }
    }

    public void changeDateForAllRows() {
        if (allRowsDate != null) {
            for (Charge currentCharge : getNewCharges()) {
                currentCharge.setDate(allRowsDate);
            }
        }
    }

    public void changeDescriptionForAllRows() {
        for (Charge currentCharge : getNewCharges()) {
            currentCharge.setDescription(allRowsDescription);
        }
    }

    public void changeNotAccountedForAllRows() {
        for (Charge currentCharge : getNewCharges()) {
            currentCharge.setNotAccountedAndComputePrice(allRowsNotAccounted);
        }
    }

    public void changeNotesForAllRows() {
        for (Charge currentCharge : getNewCharges()) {
            currentCharge.setNotes(allRowsNotes);
        }
    }

    public void changeProjectForAllRows() {
        for (Charge currentCharge : getNewCharges()) {
            currentCharge.setContainer(allRowsProject);
        }
    }

    public void changeServiceForAllRows() {
        for (Charge currentCharge : getNewCharges()) {
            currentCharge.setService(allRowsService);
        }
    }

    public void changeTotalForAllRows() {
        for (Charge currentCharge : getNewCharges()) {
            currentCharge.setTotal(allRowsTotal);
        }
    }

    public LocalDate getAllRowsDate() {
        return allRowsDate;
    }

    public String getAllRowsDescription() {
        return allRowsDescription;
    }

    public BigDecimal getAllRowsNotAccounted() {
        return allRowsNotAccounted;
    }

    public String getAllRowsNotes() {
        return allRowsNotes;
    }

    public Project getAllRowsProject() {
        return allRowsProject;
    }

    public Service getAllRowsService() {
        return allRowsService;
    }

    public BigDecimal getAllRowsTotal() {
        return allRowsTotal;
    }

    public Charge getCharge() {
        return getInstance();
    }

    public List<Charge> getCharges() {
        if (charges == null) {
            charges = chargeService.getChargesByCreatedByOrderByCreated(getCurrentUser().getLogin());
        }
        return charges;
    }

    public List<Charge> getNewCharges() {
        return newCharges;
    }

    public int getNumberOfNewLines() {
        return numberOfNewLines;
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        charges = null;
        getNewCharges().clear();
        for (int i = 0; i < defaultNumberOfLines; i++) {
            Charge currentCharge = new Charge();
            currentCharge.setDate(LocalDate.now());
            currentCharge.setCharger(getCurrentUser());
            getNewCharges().add(currentCharge);
        }

        setAllRowsService(null);
        setAllRowsProject(null);
        setAllRowsNotAccounted(BigDecimal.ZERO);
        setAllRowsTotal(BigDecimal.ZERO);
        setAllRowsDescription(null);
        setAllRowsNotes(null);
        setAllRowsBillable(false);
        setAllRowsDate(null);
    }

    public boolean isAllRowsBillable() {
        return allRowsBillable;
    }

    private boolean isValid() {
        int counter = 0;
        boolean valid = true;
        for (Charge currentCharge : getNewCharges()) {
            if (!currentCharge.isEmpty()) {
                if (currentCharge.getService() == null) {
                    valid = false;
                    getFacesMessagesManager().validationError("edit-charges-form:edit-charges-table:" + counter + ":service-select", Constants.REQUIRED);
                }
                if (currentCharge.getContainer() == null) {
                    valid = false;
                    getFacesMessagesManager().validationError("edit-charges-form:edit-charges-table:" + counter + ":project-select", Constants.REQUIRED);
                }
                if (currentCharge.getDate() == null) {
                    valid = false;
                    getFacesMessagesManager().validationError("edit-charges-form:edit-charges-table:" + counter + ":date-calendar", Constants.REQUIRED);
                }
                if (currentCharge.getTotal().doubleValue() <= 0) {
                    valid = false;
                    getFacesMessagesManager().validationError("edit-charges-form:edit-charges-table:" + counter + ":total-input", "Total <= 0!");
                }
                if (currentCharge.getTotal().doubleValue() < currentCharge.getNotAccounted().doubleValue()) {
                    valid = false;
                    getFacesMessagesManager().validationError("edit-charges-form:edit-charges-table:" + counter + ":not-accounted-input", "Value > Total!");
                }
            }
            counter++;
        }
        return valid;
    }

    public void removeNewCharge(Charge newCharge) {
        getNewCharges().remove(newCharge);
    }

    public String saveNewCharges() {
        String ret = null;
        if (isValid()) {
            try {
                List<Charge> notEmptyCharges = new ArrayList<>();
                for (Charge charge : getNewCharges()) {
                    if (!charge.isEmpty()) {
                        notEmptyCharges.add(charge);
                    }
                }
                int counter = chargeService.saveCharges(notEmptyCharges);
                if (counter > 0) {
                    getFacesMessagesManager().bufferWarningClear(counter + " " + Messages.get("chargeSaved").replace("{0}", StringHelper.getEnding(counter)));
                    ret = createRedirectURL("charge/edit-batch-with-create");
                } else {
                    getFacesMessagesManager().printError(Messages.get("noChargesToSaved"));
                }
            } catch (final Exception e) {
                // When there are charges to be saved but the save failed.
                getFacesMessagesManager().printError(Messages.get("saveFailed"));
            }
        } else {
            getFacesMessagesManager().printError(Messages.get("saveFailedRequiredFieldMissingOrInvalidValues"));
        }
        return ret;
    }

    public void setAllRowsBillable(boolean allRowsBillable) {
        this.allRowsBillable = allRowsBillable;
    }

    public void setAllRowsDate(LocalDate allRowsDate) {
        this.allRowsDate = allRowsDate;
    }

    public void setAllRowsDescription(String allRowsDescription) {
        this.allRowsDescription = allRowsDescription;
    }

    public void setAllRowsNotAccounted(BigDecimal allRowsNotAccounted) {
        this.allRowsNotAccounted = allRowsNotAccounted;
    }

    public void setAllRowsNotes(String allRowsNotes) {
        this.allRowsNotes = allRowsNotes;
    }

    public void setAllRowsProject(Project allRowsProject) {
        this.allRowsProject = allRowsProject;
    }

    public void setAllRowsService(Service allRowsService) {
        this.allRowsService = allRowsService;
    }

    public void setAllRowsTotal(BigDecimal allRowsTotal) {
        this.allRowsTotal = allRowsTotal;
    }

    public void setNewCharges(List<Charge> newCharges) {
        this.newCharges = newCharges;
    }

    public void setNumberOfNewLines(int numberOfNewLines) {
        this.numberOfNewLines = numberOfNewLines;
    }
}
