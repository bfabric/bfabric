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

package org.bfabric.manager;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.util.MessageHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.primefaces.component.datatable.DataTable;

@MeasureCalls
@Named
@ViewScoped
public class FacesMessagesManager implements Serializable {

    private static final long serialVersionUID = 1;

    @Param
    protected String soapResponse;

    @Inject
    private SessionManager sessionManager;

    public void buffer(Severity severity, String message) {
        sessionManager.addFacesMessage(new MessageHelper(severity, message));
    }

    public void bufferError(String message) {
        buffer(FacesMessage.SEVERITY_ERROR, message);
    }

    public void bufferErrorClear(String message) {
        clearGlobalMessages();
        bufferError(message);
    }

    public void bufferErrors(Collection<String> messages) {
        for (String message : messages) {
            if (message != null) {
                bufferError(message);
            }
        }
    }

    public void bufferWarning(String message) {
        buffer(null, message);
    }

    public void bufferWarningClear(String message) {
        clearGlobalMessages();
        bufferWarning(message);
    }

    public void bufferWarnings(Collection<String> messages) {
        for (String message : messages) {
            if (message != null) {
                bufferWarning(message);
            }
        }
    }

    public void clearGlobalMessages() {
        sessionManager.clearGlobalMessages();
    }

    public FacesMessage getLatestMessage() {
        return FacesContext.getCurrentInstance().getMessageList().isEmpty() ? null : FacesContext.getCurrentInstance().getMessageList().get(FacesContext.getCurrentInstance().getMessageList().size()
            - 1);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public String getSoapResponse() {
        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(soapResponse), StandardCharsets.UTF_8);
    }

    @PostConstruct
    public void init() {
        if (soapResponse != null) {
            String soapResponseString = new String(org.apache.commons.codec.binary.Base64.decodeBase64(soapResponse), StandardCharsets.UTF_8);
            if (soapResponseString.contains(Messages.get("error"))) {
                printError(soapResponseString);
            } else {
                printWarn(soapResponseString);
            }
        }
    }

    public void printError(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
        }
    }

    public void printErrors(Collection<String> messages) {
        for (String message : messages) {
            if (message != null) {
                printError(message);
            }
        }
    }

    public void printValidationErrors(LinkedHashMap<String, String> messages) {
        printValidationErrors(messages, Messages.get("validationErrors"));
    }

    public void printValidationErrors(LinkedHashMap<String, String> messages, Object... arguments) {
        printValidationErrors(messages, Messages.get("validationErrors"), arguments);
    }

    public void printValidationErrors(LinkedHashMap<String, String> messages, String defaultErrorMessage) {
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            if (entry.getKey() != null) {
                validationError(entry.getKey(), entry.getValue(), false);
            } else {
                printError(entry.getValue());
            }
        }
        if (!FacesContext.getCurrentInstance().isValidationFailed() && FacesContext.getCurrentInstance().getMessageList(null).isEmpty()) {
            printError(defaultErrorMessage);
        }
    }

    public void printValidationErrors(LinkedHashMap<String, String> messages, String defaultErrorMessage, Object... arguments) {
        // LinkedHashMap<row key id, LinkedHashMap<column id, display message>>
        LinkedHashMap<String, LinkedHashMap<String, String>> tableValidationErrorMsg = null;
        Collection<DataTable> tables = null;
        if (arguments.length == 1) {
            // arguments[0] = org.primefaces.component.DataTable
            tableValidationErrorMsg = new LinkedHashMap<>();
            tables = (Collection<DataTable>) arguments[0];
        }

        for (Map.Entry<String, String> entry : messages.entrySet()) {
            if (entry.getKey() != null) {
                if (!entry.getKey().contains(Constants.MESSAGE_COMPONENT_ROW_IDENTIFIER)) {
                    validationError(entry.getKey(), entry.getValue(), false);
                } else {
                    if (arguments.length == 1) {
                        // The error message is occurring in a table.
                        String rowKeyId = StringHelper.getRowKeyIdFromRowMessageComponentKey(entry.getKey());
                        if (!tableValidationErrorMsg.containsKey(rowKeyId)) {
                            LinkedHashMap<String, String> message = new LinkedHashMap<>();
                            message.put(entry.getKey(), entry.getValue());
                            tableValidationErrorMsg.put(rowKeyId, message);
                        } else {
                            tableValidationErrorMsg.get(rowKeyId).put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            } else {
                printError(entry.getValue());
            }
        }

        if (tableValidationErrorMsg != null && !tableValidationErrorMsg.isEmpty() && tables != null) {
            Set<String> rowKeyIds = tableValidationErrorMsg.keySet();
            for (DataTable dataTable : tables) {
                boolean lazy = dataTable.isLazy();
                List<AbstractEntity> dataList = lazy ? ((BfabricLazyDataModel<AbstractEntity>) dataTable.getValue()).getDataList() : (List<AbstractEntity>) dataTable.getValue();
                final int lo = dataTable.getPage() * dataTable.getRowsToRender();
                final int hi = lazy ? dataList.size() : Math.min(lo + dataTable.getRowsToRender(), dataList.size());
                for (int i = lo; i < hi; i++) {
                    AbstractEntity abstractEntity = dataList.get(i);
                    String rowKeyId = String.valueOf(abstractEntity.getRowKeyId());
                    if (rowKeyIds.contains(rowKeyId)) {
                        for (Map.Entry<String, String> entry : tableValidationErrorMsg.get(rowKeyId).entrySet()) {
                            // key = Constants.MESSAGE_COMPONENT_ROW_ID + Constants.MESSAGE_COMPONENT_SEPARATOR + rowKeyId + Constants.MESSAGE_COMPONENT_SEPARATOR + componentId
                            // value = message
                            validationError(dataTable.getClientId() + ":" + i + ":" + StringHelper.getComponentIdFromRowMessageComponent(entry.getKey()), entry.getValue());
                        }
                    }
                }
            }
        }

        if (!FacesContext.getCurrentInstance().isValidationFailed() && FacesContext.getCurrentInstance().getMessageList(null).isEmpty()) {
            printError(defaultErrorMessage);
        }
    }

    public void printWarn(String message) {
        if (message != null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
            }
        }
    }

    public void printWarning(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
        }
    }

    public void printWarnings(Collection<String> messages) {
        for (String message : messages) {
            if (message != null) {
                printWarning(message);
            }
        }
    }

    public void validationError(String clientId, String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
        }
    }

    public void validationError(String clientId, String message, boolean showValidationErrorMessage) {
        if (showValidationErrorMessage) {
            printError(Messages.get("validationErrors"));
        }
        validationError(clientId, message);
    }
}