package org.bfabric.webservice.client.endpoint;

import org.bfabric.entity.Instrument;
import org.bfabric.webservice.client.SoapClient;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodDelete;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodRead;
import org.bfabric.webservice.client.webmethod.AbstractClientWebMethodSave;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadInstrument;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveInstrument;
import org.bfabric.xml.entity.XMLInstrument;

public class EPInstrument extends AbstractEndPoint<XMLInstrument> {

    private AbstractClientWebMethodDelete<XMLInstrument> wmDelete;

    private AbstractClientWebMethodRead<XMLInstrument, XMLRequestParameterReadInstrument> wmRead;

    private AbstractClientWebMethodSave<XMLInstrument, XMLRequestParameterSaveInstrument> wmSave;

    public EPInstrument(SoapClient soapClient) {
        super(soapClient);
    }

    public AbstractClientWebMethodDelete<XMLInstrument> getWmDelete() {
        if (wmDelete == null) {
            wmDelete = new AbstractClientWebMethodDelete<XMLInstrument>(this) {
            };
        }
        return wmDelete;
    }

    public AbstractClientWebMethodRead<XMLInstrument, XMLRequestParameterReadInstrument> getWmRead() {
        if (wmRead == null) {
            wmRead = new AbstractClientWebMethodRead<XMLInstrument, XMLRequestParameterReadInstrument>(this) {
            };
        }
        return wmRead;
    }

    public AbstractClientWebMethodSave<XMLInstrument, XMLRequestParameterSaveInstrument> getWmSave() {
        if (wmSave == null) {
            wmSave = new AbstractClientWebMethodSave<XMLInstrument, XMLRequestParameterSaveInstrument>(this) {
            };
        }
        return wmSave;
    }

    @Override
    public String getWsdl() {
        return getWsdl(Instrument.class);
    }
}