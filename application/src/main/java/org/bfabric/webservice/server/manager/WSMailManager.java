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

package org.bfabric.webservice.server.manager;

import java.util.List;

import javax.inject.Inject;

import org.bfabric.Messages;
import org.bfabric.entity.Mail;
import org.bfabric.forms.AbstractMF;
import org.bfabric.forms.MFMail;
import org.bfabric.service.MailSendService;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveAbstractEntity;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveMail;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.entity.XMLMail;

public class WSMailManager extends AbstractWSEntityManager<Mail, XMLMail> {

    @Inject
    private MailSendService mailSendService;

    @Override
    protected AbstractMF getModificationFormPersist(Object aXmlRequestSaveEntity) {
        return new MFMail(getInstance(), (XMLRequestParameterSaveMail) aXmlRequestSaveEntity);
    }

    @Override
    protected AbstractMF getModificationFormUpdate(Object aXmlRequestSaveEntity) {
        return new MFMail(getInstance(), (XMLRequestParameterSaveMail) aXmlRequestSaveEntity);
    }

    @Override
    public <T> void isValid(T entity) throws Exception {
        getAdditionalFieldsToExcludeFromValidation().add("recipientsAddressList");
        super.isValid(entity);
    }

    @Override
    public void save() {
        // Do nothing - Mail will be saved when sent successfully.
    }

    @Override
    public synchronized <XMLRequestSaveEntity extends XMLRequestParameterSaveAbstractEntity> XMLResponse save(List<XMLRequestSaveEntity> xmlRequestSaveList, boolean idOnly) {
        XMLResponse xmlResponse = new XMLResponse();
        for (XMLRequestParameterSaveAbstractEntity xmlRequestSaveBaseEntity : xmlRequestSaveList) {
            XMLMail xmlEntity;
            try {
                setXmlRequestSaveEntity(xmlRequestSaveBaseEntity);
                applyModificationForm();
                mailSendService.send(getInstance());
                performEntityCheckAndSetInstance(getInstance().getId());
                xmlEntity = createNewXmlEntity(getInstance(), idOnly);
            } catch (Exception e) {
                xmlEntity = createNewXmlEntity();
                xmlEntity.setErrorreport(StringHelper.isNotEmpty(e.getMessage()) ? e.getMessage() : Messages.get("exceptionUnexpectedFailure"));
            }
            xmlResponse.add(xmlEntity);
        }
        return xmlResponse;
    }
}
