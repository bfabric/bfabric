# B-Fabric Documentation

## Web Services

### Overview

The B-Fabric web services use the SOAP protocol: a client sends an HTTP POST whose body is XML and receives an XML POST response. Each service publishes a WSDL at its URL that defines the required XML formats. To add a new service you typically need at least the following:

* A class which represents the structure of an XML input.

* A class which represents the structure of an XML output.

* A class which represents the endpoint, means which provides the information about the necessary XML format, which takes requests and returns responses.

  At the time of writing, the implemented web services provide the possibility to perform some basic operations on B-Fabric entities.

* Example: the web service for the entity "sample"

All classes that model SOAP XML requests are located in the `org.bfabric.webservice.request` package. These classes use annotations from `javax.xml.bind.annotation` to describe the XML structure. For example, the request class for reading samples is `org.bfabric.webservice.request.read.parameters.XMLRequestReadSample` (file `XMLRequestReadSample.java`). The XML response wrapper is `org.bfabric.webservice.response.XMLResponse` (file `XMLResponse.java`).

Endpoint classes live in the `org.bfabric.component.webservice.endpoint` package. By convention their class names start with `API`, for example `APISample` (file `APISample.java`). Each endpoint class uses two key annotations:

- `@javax.jws.WebService(serviceName = "sample")` — marks the class as a web service endpoint and sets the service name used in the URL. With a base URL of `https://your_bfabric_url/bfabric/`, a service named `sample` is reachable at `https://your_bfabric_url/bfabric/sample?wsdl`.
- `@javax.jws.soap.SOAPBinding(style = SOAPBinding.Style.RPC)` — selects RPC-style SOAP binding (the default).

Method parameters on endpoints are annotated with `@javax.jws.WebParam`; a common pattern is `@WebParam(name = "parameters")` to name the XML element that wraps the method's input data.

### Example: Java program to call a web service on an endpoint

```
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SOAPCall {

    public static void main(String[] args) throws UnsupportedOperationException, SOAPException, IOException {

        String soapString = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:end=\"http://endpoint.server.webservice.bfabric.org/\"><soapenv:Header/><soapenv:Body><end:read><parameters><login>bemployee</login><password>0f81754c2f9e0993700ad483b7ee4447</password><query><id>123456</id></query></parameters></end:read></soapenv:Body></soapenv:Envelope>";
        String endPoint = "https://your_bfabric_url/bfabric/workunit";

        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
        InputStream is = new ByteArrayInputStream(soapString.getBytes(StandardCharsets.UTF_8));
        SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);
        SOAPMessage response = soapConnection.call(request, endPoint);

        System.out.println("SOAP request");
        request.writeTo(System.out);
        System.out.println("");

        System.out.println("SOAP response");
        response.writeTo(System.out);
        System.out.println("");
    }
}
```