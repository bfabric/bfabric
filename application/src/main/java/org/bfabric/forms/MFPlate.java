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

package org.bfabric.forms;

import org.bfabric.entity.Plate;
import org.bfabric.entity.PlateLayout;
import org.bfabric.entity.PlateType;
import org.bfabric.entity.User;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSavePlate;

public class MFPlate extends AbstractMF {

    private final Plate plate;

    private final XMLRequestParameterSavePlate xmlRequestSavePlate;

    public MFPlate(Plate plate, XMLRequestParameterSavePlate xmlPlateRequestSave) {
        this.plate = plate;
        this.xmlRequestSavePlate = xmlPlateRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getPlate().setName(getName());
        getPlate().setDescription(getDescription());
        getPlate().setPlateType(getPlateType());
        getPlate().setSupervisor(getSupervisor());
        getPlate().setPlateLayout(getPlateLayout());
        getPlate().changeStatus(getStatus());
        getPlate().setCustomAttributes(getXmlRequestSavePlate().getCustomattribute());
    }

    public String getDescription() {
        if (getXmlRequestSavePlate().getDescription() != null) {
            return getXmlRequestSavePlate().getDescription();
        }
        return getPlate().getDescription();
    }

    public String getName() {
        if (getXmlRequestSavePlate().getName() != null) {
            return getXmlRequestSavePlate().getName();
        }
        return getPlate().getName();
    }

    public Plate getPlate() {
        return plate;
    }

    public PlateLayout getPlateLayout() throws InvalidDataException {
        if (getPlate().getId() == 0 || getXmlRequestSavePlate().getPlatelayoutid() != null) {
            MFHelper.checkNotNull("platelayoutid", getXmlRequestSavePlate().getPlatelayoutid());
            if (getPlate().isNotEmpty()) {
                throw new InvalidDataException("NON-EMPTY PLATE: Cannot change the layout of the plate with id " + getPlate().getId() + " as there are already samples assigned to it");
            }
            return (PlateLayout) fetch(PlateLayout.class, MFHelper.positiveLongValueOf("platelayoutid", getXmlRequestSavePlate().getPlatelayoutid()));
        }
        return getPlate().getPlateLayout();
    }

    public PlateType getPlateType() throws InvalidDataException {
        if (getPlate().getId() == 0 || getXmlRequestSavePlate().getPlatetypeid() != null) {
            MFHelper.checkNotNull("platetypeid", getXmlRequestSavePlate().getPlatetypeid());
            if (getPlate().isNotEmpty()) {
                throw new InvalidDataException("NON-EMPTY PLATE: Cannot change the type of the plate with id " + getPlate().getId() + " as there are already samples assigned to it");
            }
            return (PlateType) fetch(PlateType.class, MFHelper.positiveLongValueOf("platetypeid", getXmlRequestSavePlate().getPlatetypeid()));
        }
        return getPlate().getPlateType();
    }

    public StatusEnum getStatus() throws InvalidEnumValueException {
        if (getXmlRequestSavePlate().getStatus() != null) {
            return StatusEnum.value(getXmlRequestSavePlate().getStatus());
        }
        return getPlate().getStatus();
    }

    public User getSupervisor() throws InvalidDataException {
        if (getPlate().getId() == 0 || getXmlRequestSavePlate().getSupervisorid() != null) {
            MFHelper.checkNotNull("supervisorid", getXmlRequestSavePlate().getSupervisorid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("supervisorid", getXmlRequestSavePlate().getSupervisorid()));
        }
        return getPlate().getSupervisor();
    }

    public XMLRequestParameterSavePlate getXmlRequestSavePlate() {
        return xmlRequestSavePlate;
    }
}
