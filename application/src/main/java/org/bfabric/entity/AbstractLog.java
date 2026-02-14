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

package org.bfabric.entity;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.bfabric.Constants;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.util.StringHelper;
import org.bfabric.xml.XmlLogRow;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@MappedSuperclass
public class AbstractLog extends AbstractEntity implements NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "TEXT")
    @XmlElement
    protected String log;

    @NotNull
    @XmlElement
    private LocalDateTime created = LocalDateTime.now();

    @NotBlank
    @Size(max = 32)
    @XmlElement
    private String createdBy;

    @Transient
    private transient List<XmlLogRow> logAsTable;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private LogStatusEnum status = LogStatusEnum.NEW;

    public AbstractLog() {
    }

    public void appendLog(String logText) {
        setLog(StringHelper.concatenate(getLog(), logText, " "));
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedByFull() {
        return getUserDate(null, getCreatedBy(), getCreated(), true);
    }

    @Transient
    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        addEntityInfoItem(summary, "log", getLog());
        addEntityInfoItem(summary, "status", getStatus());
        addEntityInfoItem(summary, "createdBy", getCreatedByFull());
        return summary.toString();
    }

    public String getLog() {
        return log;
    }

    public List<XmlLogRow> getLogAsTable() {
        if (logAsTable == null) {
            logAsTable = new ArrayList<>();
            if (getLog() != null && !getLog().isEmpty()) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(false); // important!
                    factory.setValidating(false); // important!
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(getLog())));

                    // Iterate over all leaf elements. Thereby exclude root elements of embedded objects in case they are still included due to legacy.
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();

                    XPathExpression oldExpr = xpath.compile("//old//*[not(child::*)]");
                    NodeList oldNodes = (NodeList) oldExpr.evaluate(document, XPathConstants.NODESET);
                    TreeMap<String, XmlLogRow> xmlLogRows = new TreeMap<>();
                    for (int i = 0; i < oldNodes.getLength(); i++) {
                        String oldField = oldNodes.item(i).getNodeName();
                        String oldValue = StringHelper.trimBoth(oldNodes.item(i).getTextContent());
                        XmlLogRow xmlLogRow = xmlLogRows.get(oldField);
                        if (xmlLogRow != null) {
                            // There is already an entry for this field.
                            xmlLogRow.addOldValue(oldValue);
                        } else {
                            // Put new entry in hash map if the is no entry for this field.
                            xmlLogRows.put(oldField, new XmlLogRow(oldField, oldValue, null));
                        }
                    }

                    XPathExpression newExpr = xpath.compile("//new//*[not(child::*)]");
                    NodeList newNodes = (NodeList) newExpr.evaluate(document, XPathConstants.NODESET);
                    for (int i = 0; i < newNodes.getLength(); i++) {
                        String newField = newNodes.item(i).getNodeName();
                        String newValue = StringHelper.trimBoth(newNodes.item(i).getTextContent());
                        if (i < newNodes.getLength()) {
                            newValue += " ";
                        }
                        XmlLogRow xmlLogRow = xmlLogRows.get(newField);
                        if (xmlLogRow != null) {
                            // There is already an entry for this field.
                            xmlLogRow.addNewValue(newValue);
                        } else {
                            // Put new entry in hash map if the is no entry for this field.
                            xmlLogRows.put(newField, new XmlLogRow(newField, null, newValue));
                        }
                    }

                    logAsTable = new ArrayList<>(xmlLogRows.values());
                } catch (XPathExpressionException | SAXException | IOException | ParserConfigurationException e) {
                    e.printStackTrace();
                }
            }
        }
        return logAsTable;
    }

    public LogStatusEnum getStatus() {
        return status;
    }

    @Override
    protected void prePersist() {
        super.prePersist();
        setCreated(LocalDateTime.now());
        setCreatedBy(getCreatedBy() == null && getCurrentUsername() != null ? getCurrentUsername() : getCreatedBy());
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = StringHelper.isNotEmpty(createdBy) ? StringUtils.truncate(String.format(createdBy), 32) : Constants.SYSTEM;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public void setStatus(LogStatusEnum status) {
        this.status = status;
    }
}