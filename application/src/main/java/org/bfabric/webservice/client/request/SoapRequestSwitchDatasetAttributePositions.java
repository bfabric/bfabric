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

package org.bfabric.webservice.client.request;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.webservice.request.parameter.XMLRequestParameterSwitchDatasetAttributePositions;

@XmlRootElement(name = "soapenv:Envelope")
@XmlAccessorType(XmlAccessType.FIELD)
public class SoapRequestSwitchDatasetAttributePositions extends AbstractSoapRequest {

    @XmlElement(name = "soapenv:Body")
    private SoapenvBody soapenvBody;

    public static SoapRequestSwitchDatasetAttributePositions instance(String login, String password) {
        SoapRequestSwitchDatasetAttributePositions soapRequestSwitchAttributePositions = new SoapRequestSwitchDatasetAttributePositions();
        soapRequestSwitchAttributePositions.soapenvBody = new SoapenvBody();
        soapRequestSwitchAttributePositions.soapenvBody.end = new End();
        soapRequestSwitchAttributePositions.soapenvBody.end.parameters = new Parameters();
        soapRequestSwitchAttributePositions.soapenvBody.end.parameters.login = login;
        soapRequestSwitchAttributePositions.soapenvBody.end.parameters.password = password;
        return soapRequestSwitchAttributePositions;
    }

    public SoapenvBody getSoapenvBody() {
        return soapenvBody;
    }

    public static class End {

        @XmlElement
        private Parameters parameters;

        public Parameters getParameters() {
            return parameters;
        }
    }

    public static class Parameters {

        @XmlElement
        private String login;

        @XmlElement
        private String password;

        @XmlElement(name = "dataset")
        private List<XMLRequestParameterSwitchDatasetAttributePositions> xmlRequestSwitchDatasetAttributePositions;

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public List<XMLRequestParameterSwitchDatasetAttributePositions> getXmlRequestSwitchDatasetAttributePositions() {
            return xmlRequestSwitchDatasetAttributePositions;
        }
    }

    public static class SoapenvBody {

        @XmlElement(name = "end:switchAttributePositions")
        private End end;

        public End getEnd() {
            return end;
        }
    }
}
