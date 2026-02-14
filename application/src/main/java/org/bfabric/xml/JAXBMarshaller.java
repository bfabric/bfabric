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

package org.bfabric.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class JAXBMarshaller {

    private static final Map<Class<?>, JAXBContext> classContextMap;

    private static DocumentBuilder documentBuilder;

    static {
        classContextMap = new HashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        try {
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public JAXBMarshaller() {
    }

    private static JAXBContext getJAXBContext(Class<?> entityClass) throws JAXBException {
        if (!classContextMap.containsKey(entityClass)) {
            classContextMap.put(entityClass, JAXBContext.newInstance(entityClass));
        }
        return classContextMap.get(entityClass);
    }

    private static Marshaller getMarshaller(Class<?> entityClass) throws JAXBException {
        Marshaller marshaller = getJAXBContext(entityClass).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        return marshaller;
    }

    private static Marshaller getMarshaller(@NotNull Object entity) throws JAXBException {
        return getMarshaller(entity.getClass());
    }

    private static Unmarshaller getUnmarshaller(Class<?> entityClass) throws JAXBException {
        return getJAXBContext(entityClass).createUnmarshaller();
    }

    public static Document getXml(Object entity) {
        // Create empty XML document.
        Document document = documentBuilder.newDocument();
        if (entity != null) {
            try {
                getMarshaller(entity).marshal(entity, document);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        return document;
    }

    public static String getXmlAsText(Object entity) {
        String xmlAsText = null;
        try {
            if (entity != null) {
                StringWriter stringWriter = new StringWriter();
                getMarshaller(entity).marshal(entity, stringWriter);
                xmlAsText = stringWriter.toString();
                stringWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlAsText;
    }

    private static Object unmarshal(Source source, Class<?> entityClass) throws JAXBException {
        return getUnmarshaller(entityClass).unmarshal(source);
    }

    public static Object unmarshal(String xmlValue, Class<?> entityClass) throws JAXBException {
        return unmarshal(new StreamSource(new StringReader(xmlValue)), entityClass);
    }
}
