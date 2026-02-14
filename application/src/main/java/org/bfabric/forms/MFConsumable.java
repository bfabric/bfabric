package org.bfabric.forms;

import java.math.BigDecimal;

import org.bfabric.entity.Consumable;
import org.bfabric.entity.Instrument;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveConsumable;

public class MFConsumable extends AbstractMF {

    private final Consumable consumable;

    private final XMLRequestParameterSaveConsumable xmlRequestSaveConsumable;

    public MFConsumable(Consumable consumable, XMLRequestParameterSaveConsumable xmlConsumableRequestSave) {
        this.consumable = consumable;
        this.xmlRequestSaveConsumable = xmlConsumableRequestSave;
    }

    @Override
    public synchronized void apply() throws Exception {
        getConsumable().setInstrument(getInstrument());
        getConsumable().setSupplierName(getSupplier());
        getConsumable().setArticleNumber(getArticlenumber());
        getConsumable().setPrice(getPrice());
        getConsumable().setUnit(getUnit());
        getConsumable().setEnabled(isEnabled());
        getConsumable().setDescription(getDescription());
        getConsumable().setName(getName());
    }

    private String getArticlenumber() {
        if (getXmlRequestSaveConsumable().getArticlenumber() != null) {
            return getXmlRequestSaveConsumable().getArticlenumber();
        }
        return getConsumable().getArticleNumber();
    }

    public Consumable getConsumable() {
        return consumable;
    }

    public String getDescription() {
        if (getXmlRequestSaveConsumable().getDescription() != null) {
            return getXmlRequestSaveConsumable().getDescription();
        }
        return getConsumable().getDescription();
    }

    private Instrument getInstrument() throws InvalidDataException {
        if (getConsumable().getId() == 0 || getXmlRequestSaveConsumable().getInstrumentid() != null) {
            MFHelper.checkNotNull("instrumentid", getXmlRequestSaveConsumable().getInstrumentid());
            return (Instrument) fetch(Instrument.class, MFHelper.positiveLongValueOf("instrumentid", getXmlRequestSaveConsumable().getInstrumentid()));
        }
        return getConsumable().getInstrument();
    }

    public String getName() {
        if (getXmlRequestSaveConsumable().getName() != null) {
            return getXmlRequestSaveConsumable().getName();
        }
        return getConsumable().getName();
    }

    private BigDecimal getPrice() throws InvalidDataException {
        if (getXmlRequestSaveConsumable().getPrice() != null) {
            MFHelper.checkNotNull("price", getXmlRequestSaveConsumable().getPrice());
            return MFHelper.bigDecimalValueOf("price", getXmlRequestSaveConsumable().getPrice());
        }
        return consumable.getPrice();
    }

    private String getSupplier() {
        if (getXmlRequestSaveConsumable().getSupplier() != null) {
            return getXmlRequestSaveConsumable().getSupplier();
        }
        return getConsumable().getSupplierName();
    }

    private String getUnit() {
        if (getXmlRequestSaveConsumable().getUnit() != null) {
            return getXmlRequestSaveConsumable().getUnit();
        }
        return consumable.getUnit();
    }

    public XMLRequestParameterSaveConsumable getXmlRequestSaveConsumable() {
        return xmlRequestSaveConsumable;
    }

    private Boolean isEnabled() throws InvalidDataException {
        return MFHelper.booleanValueOf("enabled", getXmlRequestSaveConsumable().isEnabled());
    }
}


