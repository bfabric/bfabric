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
import org.bfabric.webservice.client.request.SoapRequestAddDatasetAttribute;
import org.bfabric.webservice.client.response.SoapResponse;
import org.bfabric.xml.entity.XMLAbstractBaseEntity;

public class ClientWebMethodAddDatasetAttribute<XMLEntity extends XMLAbstractBaseEntity, XMLRequestAddDatasetAttribute> extends AbstractClientWebMethod<XMLEntity> {

    protected ClientWebMethodAddDatasetAttribute(AbstractEndPoint<XMLEntity> endPoint) {
        super(endPoint);
    }

    public synchronized List<XMLEntity> add(List<XMLRequestAddDatasetAttribute> xmlRequestAddDatasetAttributeEntityList) {
        SoapRequestAddDatasetAttribute soapRequestAddDatasetAttribute = SoapRequestAddDatasetAttribute.instance(endPoint.getSoapClient().getLogin(), endPoint.getSoapClient().getPassword());
        setRequestAddDatasetAttributeList(soapRequestAddDatasetAttribute, xmlRequestAddDatasetAttributeEntityList);

        String soapResponseXML = sendSoapRequest(soapRequestAddDatasetAttribute);
        SoapResponse soapResponse = unmarshallSoapResponse(soapResponseXML, "addAttribute");
        checkResponse(soapResponse);
        List<XMLEntity> ret = getEntityListFromReturnElement(soapResponse.sBody.ns2Response.returnElement);
        for (XMLEntity xmlEntity : ret) {
            endPoint.addToCache(xmlEntity);
        }
        return ret;
    }

    public XMLEntity add(XMLRequestAddDatasetAttribute xmlRequestAddDatasetAttribute) {
        List<XMLRequestAddDatasetAttribute> list = new ArrayList<>();
        list.add(xmlRequestAddDatasetAttribute);
        return add(list).get(0);
    }

    protected Class<XMLRequestAddDatasetAttribute> getXMLRequestAddDatasetAttributeQuery() {
        return (Class<XMLRequestAddDatasetAttribute>) ClassHelper.getRuntimeClass(getClass(), 1);
    }

    protected void setRequestAddDatasetAttributeList(SoapRequestAddDatasetAttribute soapRequestAddDatasetAttribute, List<XMLRequestAddDatasetAttribute> xmlRequestAddDatasetAttributeEntityList) {
        for (Field field : SoapRequestAddDatasetAttribute.Parameters.class.getDeclaredFields()) {
            field.setAccessible(true);
            // only have a look at fields of type List
            if (field.getType().equals(List.class)) {
                Type fieldType = ClassHelper.getRuntimeClass(field.getGenericType(), 0);
                if (fieldType.equals(getXMLRequestAddDatasetAttributeQuery())) {
                    try {
                        field.set(soapRequestAddDatasetAttribute.getSoapenvBody().getEnd().getParameters(), xmlRequestAddDatasetAttributeEntityList);
                        break;
                    } catch (Exception exception) {
                        throw new RuntimeException("Failed to set query " + xmlRequestAddDatasetAttributeEntityList.getClass().getName() + ": " + exception.getLocalizedMessage());
                    }
                }
            }
        }
    }

}
