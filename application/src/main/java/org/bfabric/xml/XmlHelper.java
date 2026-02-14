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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.bfabric.Constants;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.EntityLog;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHelper {

    public static String getReleaseLog(Configuration configuration, EntityLog lastReleaseEntityLog) {
        if (configuration != null) {
            String[] elements = {
                "revision", configuration.getDeploymentGitRevisionId(),
                "compiled", configuration.getDeploymentCompilationDateTime(),
                "branch", configuration.getDeploymentBranchName(),
                "version", configuration.getApplicationVersionShortName(),
                "deployer", configuration.getDeployerAbbreviationName(),
                "environment", configuration.getEnvironmentName()
            };
            StringBuilder newElements = new StringBuilder();
            StringBuilder emptyElements = new StringBuilder();
            boolean logDiffers = lastReleaseEntityLog == null;
            for (int i = 0; i < elements.length; i += 2) {
                String newElement = getWrappedXmlElementNew(elements[i + 1], elements[i]);
                newElements.append(newElement);
                if (!logDiffers && !lastReleaseEntityLog.getLog().contains(newElement)) {
                    logDiffers = true;
                }
                emptyElements.append(getWrappedXmlElement(Constants.EMPTY_STRING, elements[i]));
            }
            if (logDiffers) {
                return getWrappedXmlElement(getWrappedXmlElementOld(emptyElements.toString()) + getWrappedXmlElementNew(newElements.toString()), "log");
            }
        }
        return null;
    }

    public static String getWrappedXmlElement(String content, String elementName) {
        StringBuilder wrappedXmlElement = new StringBuilder();
        if (StringHelper.isNotEmpty(elementName)) {
            wrappedXmlElement.append("<").append(elementName).append(">");
            if (StringHelper.isNotEmpty(content)) {
                wrappedXmlElement.append(content);
            }
            wrappedXmlElement.append("</").append(elementName).append(">");
        }
        return wrappedXmlElement.toString();
    }

    public static String getWrappedXmlElementLog(String content) {
        return getWrappedXmlElement(content, "log");
    }

    public static String getWrappedXmlElementNew(String content) {
        return getWrappedXmlElement(content, "new");
    }

    public static String getWrappedXmlElementNew(String content, String elementName) {
        return getWrappedXmlElementNew(getWrappedXmlElement(content, elementName));
    }

    public static String getWrappedXmlElementOld(String content) {
        return getWrappedXmlElement(content, "old");
    }

    public static String getWrappedXmlElementOld(String content, String elementName) {
        return getWrappedXmlElementOld(getWrappedXmlElement(content, elementName));
    }

    public static String getXmlLog(Node node) {
        StringBuilder xmlLog = new StringBuilder();
        try {
            if (node != null && node.getFirstChild() != null) {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

                NodeList nodeList = node.getFirstChild().getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    DOMSource source = new DOMSource(nodeList.item(i));
                    StringWriter stringWriter = new StringWriter();
                    transformer.transform(source, new StreamResult(stringWriter));
                    xmlLog.append(stringWriter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlLog.toString();
    }

    public static String getXmlLogDiff(Node oldNode, Node newNode) {
        StringBuilder xmlLogDiff = new StringBuilder();
        List<String> oldEntityState = getXmlLogStringList(oldNode);
        List<String> newEntityState = getXmlLogStringList(newNode);
        List<String> diffOldEntityState = new ArrayList<>(oldEntityState);
        diffOldEntityState.removeAll(newEntityState);
        if (!diffOldEntityState.isEmpty()) {
            xmlLogDiff.append(getWrappedXmlElementOld(CollectionHelper.printBasic(diffOldEntityState, Constants.EMPTY_STRING)));
        }
        List<String> diffNewEntityState = new ArrayList<>(newEntityState);
        diffNewEntityState.removeAll(oldEntityState);
        if (!diffNewEntityState.isEmpty()) {
            xmlLogDiff.append(getWrappedXmlElementNew(CollectionHelper.printBasic(diffNewEntityState, Constants.EMPTY_STRING)));
        }
        return xmlLogDiff.toString();
    }

    public static List<String> getXmlLogStringList(Node node) {
        List<String> xmlStringList = new ArrayList<>();
        try {
            if (node != null && node.getFirstChild() != null) {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                // Iterate over all leaf elements. Thereby exclude root elements of embedded objects from the logging.
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression oldExpr = xpath.compile("//*[not(child::*)]");
                NodeList nodeList = (NodeList) oldExpr.evaluate(node.getFirstChild(), XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    DOMSource source = new DOMSource(nodeList.item(i));
                    StringWriter stringWriter = new StringWriter();
                    transformer.transform(source, new StreamResult(stringWriter));
                    xmlStringList.add(stringWriter.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlStringList;
    }
}
