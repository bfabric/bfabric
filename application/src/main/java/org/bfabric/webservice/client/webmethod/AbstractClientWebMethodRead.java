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
import java.util.ArrayList;
import java.util.List;

import org.bfabric.util.ClassHelper;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.webservice.client.endpoint.AbstractEndPoint;
import org.bfabric.webservice.client.request.SoapRequestRead;
import org.bfabric.webservice.client.request.SoapRequestRead.Parameters;
import org.bfabric.webservice.client.response.SoapResponse;
import org.bfabric.webservice.request.parameter.XMLRequestParameterReadEntity;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLAbstractEntity;

public abstract class AbstractClientWebMethodRead<XMLEntity extends XMLAbstractEntity, XMLRequestParameterRead extends XMLRequestParameterReadEntity> extends AbstractClientWebMethod<XMLEntity> {

    protected AbstractClientWebMethodRead(AbstractEndPoint<XMLEntity> endPoint) {
        super(endPoint);
    }

    public XMLRequestParameterRead createNewXMLRequestParameterRead() {
        XMLRequestParameterRead xmlRequestParameterRead;
        try {
            xmlRequestParameterRead = getXMLRequestParameterRead().getDeclaredConstructor().newInstance();
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException();
        }
        return xmlRequestParameterRead;
    }

    public synchronized XMLEntity getEntity(long entityId) {
        XMLEntity xmlEntity = null;
        List<Long> entityIdList = new ArrayList<>();
        entityIdList.add(entityId);
        List<XMLEntity> list = getEntityList(entityIdList);
        if (list.size() == 1) {
            xmlEntity = list.get(0);
        }
        return xmlEntity;
    }

    public synchronized XMLEntity getEntity(Long entityId) {
        return entityId != null ? getEntity(entityId.longValue()) : null;
    }

    public synchronized XMLEntity getEntity(XMLEntity xmlEntity) {
        return xmlEntity != null ? getEntity(xmlEntity.getId()) : null;
    }

    public synchronized List<XMLEntity> getEntityList(List<Long> entityIdList) {
        List<XMLEntity> ret = new ArrayList<>();
        List<Long> entityIdListToFetch = new ArrayList<>();

        for (Long entityId : entityIdList) {
            XMLEntity xmlEntity = endPoint.getFromCache(entityId);
            // If the entity cannot be found in the cache, then try to fetch it from the application server.
            if (xmlEntity == null) {
                entityIdListToFetch.add(entityId);
            } else {
                ret.add(xmlEntity);
            }
        }

        if (!entityIdListToFetch.isEmpty()) {
            // IMPORTANT: Do not remove the try-catch-block since it is necessary for JUNIT purposes!
            int webServiceQueryMaxElements;
            try {
                webServiceQueryMaxElements = ConfigurationHelper.getConfiguration().getWebServiceQueryMaxElements();
            } catch (Exception e) {
                webServiceQueryMaxElements = 100;
            }

            int indexLastBlock = entityIdListToFetch.size() / webServiceQueryMaxElements;

            int remainder = entityIdListToFetch.size() % webServiceQueryMaxElements;
            // if the remainder equals 0, then there is one less iteration
            if (remainder == 0) {
                indexLastBlock = indexLastBlock - 1;
            }

            for (int indexBlock = 0; indexBlock <= indexLastBlock; indexBlock++) {
                // partition the list of id that is to be fetched since not more than webServiceQueryMaxElements query elements are allowed
                int listIndexBegin = indexBlock * webServiceQueryMaxElements;
                int listIndexEnd = listIndexBegin + webServiceQueryMaxElements;
                if (listIndexEnd > entityIdListToFetch.size()) {
                    listIndexEnd = entityIdListToFetch.size();
                }

                XMLRequestParameterRead query = createNewXMLRequestParameterRead();
                query.addIdList(entityIdListToFetch.subList(listIndexBegin, listIndexEnd));
                ret.addAll(getEntityList(query));
            }
        }
        return ret;
    }

    public synchronized List<XMLEntity> getEntityList(XMLRequestParameterRead xmlRequestParameterRead) {
        XMLResponse elementReturn = getEntityList(xmlRequestParameterRead, 1);
        List<XMLEntity> ret = new ArrayList<>(getEntityListFromReturnElement(elementReturn));

        for (int page = 2; page <= elementReturn.getNumberofpages(); page++) {
            elementReturn = getEntityList(xmlRequestParameterRead, page);
            ret.addAll(getEntityListFromReturnElement(elementReturn));
        }

        return ret;
    }

    protected synchronized XMLResponse getEntityList(XMLRequestParameterRead xmlRequestParameterRead, int page) {
        XMLResponse elementReturn = read(xmlRequestParameterRead, page);
        if (endPoint.isCachingEnabled()) {
            for (XMLEntity xmlEntity : getEntityListFromReturnElement(elementReturn)) {
                endPoint.addToCache(xmlEntity);
            }
        }
        return elementReturn;
    }

    protected Class<XMLRequestParameterRead> getXMLRequestParameterRead() {
        return (Class<XMLRequestParameterRead>) ClassHelper.getRuntimeClass(getClass(), 1);
    }

    protected synchronized XMLResponse read(XMLRequestParameterRead query, int page) {
        SoapRequestRead soapRequestRead = SoapRequestRead.instance(endPoint.getSoapClient().getLogin(), endPoint.getSoapClient().getPassword(), page);
        setRequestReadQuery(soapRequestRead, query);
        String soapResponseXML = sendSoapRequest(soapRequestRead);
        SoapResponse soapResponse = unmarshallSoapResponse(soapResponseXML, "read");
        checkResponse(soapResponse);
        return soapResponse.sBody.ns2Response.returnElement;
    }

    protected void setRequestReadQuery(SoapRequestRead soapRequestRead, XMLRequestParameterRead xmlReadRequestQuery) {
        for (Field field : Parameters.class.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType().equals(getXMLRequestParameterRead())) {
                try {
                    field.set(soapRequestRead.getSoapenvBody().getEnd().getParameters(), xmlReadRequestQuery);
                } catch (Exception exception) {
                    throw new RuntimeException("Failed to set query " + xmlReadRequestQuery.getClass().getName() + ": " + exception.getLocalizedMessage());
                }
                break;
            }
        }
    }
}