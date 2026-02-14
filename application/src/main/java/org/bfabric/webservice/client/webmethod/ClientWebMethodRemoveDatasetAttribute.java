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
import org.bfabric.webservice.client.request.SoapRequestRemoveDatasetAttribute;
import org.bfabric.webservice.client.response.SoapResponse;
import org.bfabric.xml.entity.XMLAbstractBaseEntity;

public class ClientWebMethodRemoveDatasetAttribute<XMLEntity extends XMLAbstractBaseEntity, XMLRequestRemoveDatasetAttribute> extends AbstractClientWebMethod<XMLEntity> {

    protected ClientWebMethodRemoveDatasetAttribute(AbstractEndPoint<XMLEntity> endPoint) {
        super(endPoint);
    }

    protected Class<XMLRequestRemoveDatasetAttribute> getXMLRequestRemoveDatasetAttributeQuery() {
        return (Class<XMLRequestRemoveDatasetAttribute>) ClassHelper.getRuntimeClass(getClass(), 1);
    }

    public synchronized List<XMLEntity> remove(List<XMLRequestRemoveDatasetAttribute> xmlRequestRemoveDatasetAttributeEntityList) {
        SoapRequestRemoveDatasetAttribute soapRequestRemoveDatasetAttribute = SoapRequestRemoveDatasetAttribute.instance(endPoint.getSoapClient().getLogin(), endPoint.getSoapClient().getPassword());
        setRequestRemoveDatasetAttributeList(soapRequestRemoveDatasetAttribute, xmlRequestRemoveDatasetAttributeEntityList);

        String soapResponseXML = sendSoapRequest(soapRequestRemoveDatasetAttribute);
        SoapResponse soapResponse = unmarshallSoapResponse(soapResponseXML, "removeAttribute");
        checkResponse(soapResponse);
        List<XMLEntity> ret = getEntityListFromReturnElement(soapResponse.sBody.ns2Response.returnElement);
        for (XMLEntity xmlEntity : ret) {
            endPoint.addToCache(xmlEntity);
        }
        return ret;
    }

    public XMLEntity remove(XMLRequestRemoveDatasetAttribute xmlRequestRemoveDatasetAttribute) {
        List<XMLRequestRemoveDatasetAttribute> list = new ArrayList<>();
        list.add(xmlRequestRemoveDatasetAttribute);
        return remove(list).get(0);
    }

    protected void setRequestRemoveDatasetAttributeList(SoapRequestRemoveDatasetAttribute soapRequestRemoveDatasetAttribute, List<XMLRequestRemoveDatasetAttribute> xmlRequestRemoveDatasetAttributeEntityList) {
        for (Field field : SoapRequestRemoveDatasetAttribute.Parameters.class.getDeclaredFields()) {
            field.setAccessible(true);
            // only have a look at fields of type List
            if (field.getType().equals(List.class)) {
                Type fieldType = ClassHelper.getRuntimeClass(field.getGenericType(), 0);
                if (fieldType.equals(getXMLRequestRemoveDatasetAttributeQuery())) {
                    try {
                        field.set(soapRequestRemoveDatasetAttribute.getSoapenvBody().getEnd().getParameters(), xmlRequestRemoveDatasetAttributeEntityList);
                        break;
                    } catch (Exception exception) {
                        throw new RuntimeException("Failed to set query " + xmlRequestRemoveDatasetAttributeEntityList.getClass().getName() + ": " + exception.getLocalizedMessage());
                    }
                }
            }
        }
    }

}
