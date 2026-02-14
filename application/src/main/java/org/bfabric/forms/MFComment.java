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

package org.bfabric.forms;

import java.util.HashSet;
import java.util.Set;

import org.bfabric.Constants;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Comment;
import org.bfabric.entity.UserGroup;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;
import org.bfabric.util.MailRecipientHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveComment;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveUploadedFile;

public class MFComment extends AbstractMF {

    private final Comment comment;

    private final XMLRequestParameterSaveComment xmlRequestSaveComment;

    public MFComment(Comment comment, XMLRequestParameterSaveComment xmlRequestSaveComment) {
        this.comment = comment;
        this.xmlRequestSaveComment = xmlRequestSaveComment;
    }

    @Override
    public synchronized void apply() throws Exception {
        getComment().setComment(getText());
        getComment().setSubject(getSubject());
        getComment().setParent(getParent());
        getComment().setDiscriminator(getDiscriminator());
        getComment().setInternal(isInternal());
        boolean sendMail = isSendMail();
        getComment().setSendMailInternals(sendMail);
        getComment().setSendMailExternals(sendMail);
        if (sendMail) {
            getComment().getMailRecipientHelper().init();
            if (getSendMailUserGroups() != null) {
                for (UserGroup userGroup : getSendMailUserGroups()) {
                    getComment().getMailRecipientHelper().addUserFunctions(userGroup.getUsers(), MailRecipientHelper.CUSTOM, true);
                }
            }
        }
        getUploadedFiles();
    }

    public Comment getComment() {
        return comment;
    }

    public CommentDiscriminator getDiscriminator() throws InvalidEnumValueException, InvalidDataException {
        if (getComment().getId() == 0) {
            MFHelper.checkNotNull("discriminator", getXmlRequestSaveComment().getDiscriminator());
            return CommentDiscriminator.value(getXmlRequestSaveComment().getDiscriminator());
        }
        return getComment().getDiscriminator();
    }

    public AbstractEntity getParent() throws InvalidDataException {
        if (getComment().getId() == 0) {
            MFHelper.checkNotNull("parentid", getXmlRequestSaveComment().getParentid());
        }
        MFHelper.checkNotNull("parentclassname", getXmlRequestSaveComment().getParentclassname());
        if (getXmlRequestSaveComment().getParentid() != null) {
            return fetch(getXmlRequestSaveComment().getParentclassname(), MFHelper.positiveLongValueOf("parentid", getXmlRequestSaveComment().getParentid()));
        }
        return getComment().getParent();
    }

    public Set<UserGroup> getSendMailUserGroups() throws Exception {
        if (getXmlRequestSaveComment().getSendmailusergroupid() != null) {
            Set<UserGroup> userGroups = new HashSet<>();
            for (String sendMailUserGroupId : getXmlRequestSaveComment().getSendmailusergroupid()) {
                if (!sendMailUserGroupId.isEmpty()) {
                    userGroups.add((UserGroup) fetch(UserGroup.class, MFHelper.positiveLongValueOf("sendmailusergroupid", sendMailUserGroupId)));
                }
            }
            return userGroups;
        }
        return null;
    }

    public String getSubject() {
        if (getXmlRequestSaveComment().getSubject() != null) {
            return getXmlRequestSaveComment().getSubject();
        }
        return getComment().getSubject();
    }

    public String getText() {
        if (getXmlRequestSaveComment().getText() != null) {
            return getXmlRequestSaveComment().getText();
        }
        return getComment().getComment();
    }

    public void getUploadedFiles() throws Exception {
        if (getXmlRequestSaveComment().getAttachment() != null) {
            for (XMLRequestParameterSaveUploadedFile xmlRequestSaveUploadedFile : getXmlRequestSaveComment().getAttachment()) {
                // No Bean Validation available
                MFHelper.checkNotNull("base64", xmlRequestSaveUploadedFile.getBase64());
                MFHelper.checkNotNull("name", xmlRequestSaveUploadedFile.getName());
                MFHelper.checkLength("name", xmlRequestSaveUploadedFile.getName(), Constants.MAX_LENGTH_NAME);
            }
        }
    }

    public XMLRequestParameterSaveComment getXmlRequestSaveComment() {
        return xmlRequestSaveComment;
    }

    public Boolean isInternal() throws InvalidDataException {
        MFHelper.checkNotNull("internal", getXmlRequestSaveComment().getInternal());
        return MFHelper.booleanValueOf("internal", getXmlRequestSaveComment().getInternal());
    }

    public Boolean isSendMail() throws InvalidDataException {
        MFHelper.checkNotNull("sendmail", getXmlRequestSaveComment().getSendmail());
        return MFHelper.booleanValueOf("sendmail", getXmlRequestSaveComment().getSendmail());
    }
}
