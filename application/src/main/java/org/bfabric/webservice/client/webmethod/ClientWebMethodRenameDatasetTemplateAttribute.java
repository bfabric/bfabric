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
import org.bfabric.webservice.client.request.SoapRequestRenameDatasetTemplateAttribute;
import org.bfabric.webservice.client.response.SoapResponse;
import org.bfabric.xml.entity.XMLAbstractBaseEntity;

public class ClientWebMethodRenameDatasetTemplateAttribute<XMLEntity extends XMLAbstractBaseEntity, XMLRequestRenameDatasetTemplateAttribute> extends AbstractClientWebMethod<XMLEntity> {

    protected ClientWebMethodRenameDatasetTemplateAttribute(AbstractEndPoint<XMLEntity> endPoint) {
        super(endPoint);
    }

    protected Class<XMLRequestRenameDatasetTemplateAttribute> getXMLRequestRenameDatasetTemplateAttributeQuery() {
        return (Class<XMLRequestRenameDatasetTemplateAttribute>) ClassHelper.getRuntimeClass(getClass(), 1);
    }

    public synchronized List<XMLEntity> rename(List<XMLRequestRenameDatasetTemplateAttribute> xmlRequestRenameDatasetTemplateAttributeEntityList) {
        SoapRequestRenameDatasetTemplateAttribute soapRequestRenameDatasetTemplateAttribute = SoapRequestRenameDatasetTemplateAttribute.instance(endPoint.getSoapClient()
            .getLogin(), endPoint.getSoapClient().getPassword());
        setRequestRenameDatasetTemplateAttributeList(soapRequestRenameDatasetTemplateAttribute, xmlRequestRenameDatasetTemplateAttributeEntityList);
        String soapResponseXML = sendSoapRequest(soapRequestRenameDatasetTemplateAttribute);
        SoapResponse soapResponse = unmarshallSoapResponse(soapResponseXML, "renameDatasetTemplateAttribute");
        checkResponse(soapResponse);
        List<XMLEntity> ret = getEntityListFromReturnElement(soapResponse.sBody.ns2Response.returnElement);
        for (XMLEntity xmlEntity : ret) {
            endPoint.addToCache(xmlEntity);
        }
        return ret;
    }

    public XMLEntity rename(XMLRequestRenameDatasetTemplateAttribute xmlRequestRenameDatasetTemplateAttribute) {
        List<XMLRequestRenameDatasetTemplateAttribute> list = new ArrayList<>();
        list.add(xmlRequestRenameDatasetTemplateAttribute);
        return rename(list).get(0);
    }

    protected void setRequestRenameDatasetTemplateAttributeList(SoapRequestRenameDatasetTemplateAttribute soapRequestRenameDatasetTemplateAttribute, List<XMLRequestRenameDatasetTemplateAttribute> xmlRequestRenameDatasetAttributeEntityList) {
        for (Field field : SoapRequestRenameDatasetTemplateAttribute.Parameters.class.getDeclaredFields()) {
            field.setAccessible(true);
            // only have a look at fields of type List
            if (field.getType().equals(List.class)) {
                Type fieldType = ClassHelper.getRuntimeClass(field.getGenericType(), 0);
                if (fieldType.equals(getXMLRequestRenameDatasetTemplateAttributeQuery())) {
                    try {
                        field.set(soapRequestRenameDatasetTemplateAttribute.getSoapenvBody().getEnd().getParameters(), xmlRequestRenameDatasetAttributeEntityList);
                        break;
                    } catch (Exception exception) {
                        throw new RuntimeException("Failed to set query " + xmlRequestRenameDatasetAttributeEntityList.getClass().getName() + ": " + exception.getLocalizedMessage());
                    }
                }
            }
        }
    }

}
