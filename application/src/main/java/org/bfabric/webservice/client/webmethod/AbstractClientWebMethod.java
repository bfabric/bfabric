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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.bfabric.util.ClassHelper;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.UriHelper;
import org.bfabric.webservice.client.endpoint.AbstractEndPoint;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.client.response.SoapResponse;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.JAXBMarshaller;
import org.bfabric.xml.entity.XMLAbstractEntity;

public abstract class AbstractClientWebMethod<XMLEntity extends XMLAbstractEntity> {

    protected final AbstractEndPoint<XMLEntity> endPoint;

    protected AbstractClientWebMethod(AbstractEndPoint<XMLEntity> endPoint) {
        this.endPoint = endPoint;
    }

    protected void checkResponse(SoapResponse elementSEnvelope) throws SoapClientException {
        if (elementSEnvelope == null) {
            throw new SoapClientException("No response from the server.");
        }
        if (elementSEnvelope.sBody == null) {
            throw new SoapClientException("No S:Body element available.");
        }
        if (elementSEnvelope.sBody.ns2Response == null) {
            throw new SoapClientException("No ns2:Response element available.");
        }
        if (elementSEnvelope.sBody.ns2Response.returnElement == null) {
            throw new SoapClientException("No return element available.");
        }
        if (elementSEnvelope.sBody.ns2Response.returnElement.getErrorreport() != null) {
            throw new SoapClientException(elementSEnvelope.sBody.ns2Response.returnElement.getErrorreport());
        }
    }

    protected String createHeader(String soapMessage) {
        return "POST " + endPoint.getWsdl() + " HTTP/1.0\r\nHost: " + endPoint.getSoapClient().getHostname() + "\r\nContent-Length: " + soapMessage.length()
            + "\r\nContent-Type: text/xml; charset=\"utf-8\"\r\n\r\n" + soapMessage;
    }

    protected List<XMLEntity> getEntityListFromReturnElement(XMLResponse elementReturn) {
        List<XMLEntity> ret = new ArrayList<>();
        for (Field field : XMLResponse.class.getDeclaredFields()) {
            // only have a look at fields of type List
            field.setAccessible(true);
            if (field.getType().equals(List.class)) {
                Type fieldType = ClassHelper.getRuntimeClass(field.getGenericType(), 0);
                if (fieldType.equals(getXMLEntityClass())) {
                    try {
                        ret = (List<XMLEntity>) field.get(elementReturn);
                        break;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to get the list from the return element: " + e.getLocalizedMessage());
                    }
                }
            }
        }
        for (XMLEntity xmlEntity : ret) {
            xmlEntity.setSoapClient(endPoint.getSoapClient());
        }
        return ret;
    }

    protected Class<XMLEntity> getXMLEntityClass() {
        return (Class<XMLEntity>) ClassHelper.getRuntimeClass(getClass(), 0);
    }

    protected String sendSoapRequest(Object soapRequest) {
        return sendSoapRequestToServer(JAXBMarshaller.getXmlAsText(soapRequest));
    }

    private String sendSoapRequestToServer(String soapMessage) {
        InetAddress serverAddress;
        // Set hostname and remove protocol (if given).
        String hostname = UriHelper.removeProtocol(endPoint.getSoapClient().getHostname());
        try {
            serverAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException unknownHostException) {
            throw new SoapClientException("Unknown host exception: " + hostname + unknownHostException.getLocalizedMessage());
        }

        StringBuilder serverResponse = new StringBuilder();
        Socket socket = null;
        try {
            // IMPORTANT: Do not remove the try-catch-block since it is necessary for JUNIT purposes!
            String defaultCharset;
            try {
                defaultCharset = ConfigurationHelper.getConfiguration().getDefaultCharset();
            } catch (Exception e) {
                defaultCharset = StandardCharsets.UTF_8.toString();
            }
            socket = new Socket(serverAddress, endPoint.getSoapClient().getPort());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), defaultCharset));
            bufferedWriter.write(createHeader(soapMessage));
            bufferedWriter.flush();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), defaultCharset));

            String line = bufferedReader.readLine();
            serverResponse.append(line);
            while (line != null) {
                serverResponse.append(line);
                line = bufferedReader.readLine();
            }
        } catch (IOException ioException) {
            throw new SoapClientException("An error occurred when sending the soap request: " + ioException.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return serverResponse.toString();
    }

    protected synchronized SoapResponse unmarshallSoapResponse(String xmlResponse, String methodName) {
        int startIndex = xmlResponse.indexOf("<S:Envelope");
        String correctedXmlResponse = xmlResponse.substring(startIndex);
        correctedXmlResponse = correctedXmlResponse.replaceAll("S:Envelope", "SEnvelope");
        correctedXmlResponse = correctedXmlResponse.replaceAll("S:Body", "SBody");
        correctedXmlResponse = correctedXmlResponse.replaceAll("ns2:" + methodName + "Response", "ns2Response");
        try {
            return (SoapResponse) JAXBMarshaller.unmarshal(correctedXmlResponse, SoapResponse.class);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
}