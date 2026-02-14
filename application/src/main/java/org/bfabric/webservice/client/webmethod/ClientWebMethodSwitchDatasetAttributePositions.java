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

package org.bfabric.webservice.client.webmethod;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.bfabric.util.ClassHelper;
import org.bfabric.webservice.client.endpoint.AbstractEndPoint;
import org.bfabric.webservice.client.request.SoapRequestSwitchDatasetAttributePositions;
import org.bfabric.webservice.client.response.SoapResponse;
import org.bfabric.xml.entity.XMLAbstractBaseEntity;

public class ClientWebMethodSwitchDatasetAttributePositions<XMLDataset extends XMLAbstractBaseEntity, XMLRequestSwitchDatasetAttributePositions> extends AbstractClientWebMethod<XMLDataset> {

    protected ClientWebMethodSwitchDatasetAttributePositions(AbstractEndPoint<XMLDataset> endPoint) {
        super(endPoint);
    }

    protected void setRequestSwitchDatasetAttributePositionsList(SoapRequestSwitchDatasetAttributePositions soapRequestSwitchDataseAttributePositions,
        List<XMLRequestSwitchDatasetAttributePositions> xmlRequestSwitchDatasetAttributePositionsList) {
        for (Field field : SoapRequestSwitchDatasetAttributePositions.Parameters.class.getDeclaredFields()) {
            field.setAccessible(true);
            // only have a look at fields of type List
            if (field.getType().equals(List.class)) {
                Type fieldType = ClassHelper.getRuntimeClass(field.getGenericType(), 0);
                if (fieldType.equals(ClassHelper.getRuntimeClass(getClass(), 1))) {
                    try {
                        field.set(soapRequestSwitchDataseAttributePositions.getSoapenvBody().getEnd().getParameters(), xmlRequestSwitchDatasetAttributePositionsList);
                        break;
                    } catch (Exception exception) {
                        throw new RuntimeException("Failed to set query " + xmlRequestSwitchDatasetAttributePositionsList.getClass().getName() + ": " + exception.getLocalizedMessage());
                    }
                }
            }
        }
    }

    public XMLDataset switchAttributePositions(XMLRequestSwitchDatasetAttributePositions xmlRequestSwitchDatasetAttributePositions) {
        List<XMLRequestSwitchDatasetAttributePositions> list = new ArrayList<>();
        list.add(xmlRequestSwitchDatasetAttributePositions);
        return switchAttributePositions(list).get(0);
    }

    public synchronized List<XMLDataset> switchAttributePositions(List<XMLRequestSwitchDatasetAttributePositions> xmlRequestSwitchDatasetAttributePositionsList) {
        SoapRequestSwitchDatasetAttributePositions soapRequestSwitchDataseAttributePositions = SoapRequestSwitchDatasetAttributePositions.instance(endPoint.getSoapClient().getLogin(),
            endPoint.getSoapClient().getPassword());
        setRequestSwitchDatasetAttributePositionsList(soapRequestSwitchDataseAttributePositions, xmlRequestSwitchDatasetAttributePositionsList);

        String soapResponseXML = sendSoapRequest(soapRequestSwitchDataseAttributePositions);
        SoapResponse soapResponse = unmarshallSoapResponse(soapResponseXML, "switchAttributePositions");
        checkResponse(soapResponse);
        List<XMLDataset> ret = getEntityListFromReturnElement(soapResponse.sBody.ns2Response.returnElement);
        for (XMLDataset xmlEntity : ret) {
            endPoint.addToCache(xmlEntity);
        }
        return ret;
    }

}
