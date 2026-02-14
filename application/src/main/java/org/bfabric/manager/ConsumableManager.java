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

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.Consumable;
import org.bfabric.entity.Instrument;
import org.bfabric.entity.Supplier;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.ConsumableService;
import org.bfabric.service.InstrumentService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class ConsumableManager extends AbstractEntityManager<Consumable> {

    private static final long serialVersionUID = 1;

    @Param
    protected Long supplierId;

    protected Integer inventoryCount = 1;

    @Inject
    private InstrumentService instrumentService;

    private Supplier modalSupplier;

    public ConsumableManager() {
        super(Consumable.class);
    }

    public String checkIn() {
        getConsumable().checkIn(inventoryCount);
        entityService.save(getConsumable());
        getFacesMessagesManager().bufferWarningClear("Checked in " + inventoryCount);
        return getShowScreenRedirectURL();
    }

    public String checkOut() {
        if (isCheckOutValid() && getConsumable().checkOut(inventoryCount)) {
            entityService.save(getConsumable());
            getFacesMessagesManager().bufferWarningClear("Checked out " + inventoryCount);
        } else {
            getFacesMessagesManager().bufferWarningClear("Checked out failed!");
        }
        return getShowScreenRedirectURL();
    }

    @Override
    protected Consumable createInstance() {
        final Consumable consumable = super.createInstance();
        if (consumable != null && supplierId != null) {
            final Supplier supplier = entityService.find(Supplier.class, supplierId);
            if (supplier != null) {
                consumable.setSupplier(supplier);
            }
        }
        return consumable;
    }

    public void createNewSupplier() {
        setModalSupplier(new Supplier());
    }

    @Produces
    @Named("consumable")
    public Consumable getConsumable() {
        return getInstance();
    }

    public List<Instrument> getInstruments(String filterString) {
        return (List<Instrument>) instrumentService.getFilteredEnabledIncludingOrderBy(getConsumable().getInstrument(), filterString, null);
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public Supplier getModalSupplier() {
        return modalSupplier;
    }

    public boolean isCheckOutValid() {
        if (inventoryCount == null || inventoryCount < 1) {
            inventoryCount = 1;
        }
        return getConsumable().getInventoryCount() != null && inventoryCount <= getConsumable().getInventoryCount();
    }

    @Override
    public String save() {
        return validateAndSave(CDI.current().select(ConsumableService.class).get());
    }

    public void saveNewSupplier() {
        entityService.save(getModalSupplier());
        getConsumable().setSupplier(getModalSupplier());
    }

    public void setInventoryCount(Integer inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public void setModalSupplier(Supplier modalSupplier) {
        this.modalSupplier = modalSupplier;
    }
}
