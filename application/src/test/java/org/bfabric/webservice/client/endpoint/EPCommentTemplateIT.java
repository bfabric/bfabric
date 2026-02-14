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

package org.bfabric.webservice.client.endpoint;

import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveCommentTemplate;
import org.bfabric.xml.entity.XMLCommentTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPCommentTemplateIT extends AbstractIT {

    public static XMLCommentTemplate createCommentTemplate() {
        XMLRequestParameterSaveCommentTemplate xmlRequestSaveCommentTemplate = new XMLRequestParameterSaveCommentTemplate();

        xmlRequestSaveCommentTemplate.setName(GENERATED_NAME);
        xmlRequestSaveCommentTemplate.setContent(S5);
        xmlRequestSaveCommentTemplate.setDescription(S5);

        XMLCommentTemplate commentTemplate = getSoapClient().getEpCommentTemplate().getWmSave().save(xmlRequestSaveCommentTemplate);

        if (commentTemplate.getErrorreport() != null) {
            throw new SoapClientException("Could not create commentTemplate: " + commentTemplate.getErrorreport());
        }
        return commentTemplate;
    }

    public static void deleteCommentTemplate(Long id) {
        XMLCommentTemplate deleteCommentTemplate = getSoapClient().getEpCommentTemplate().getWmDelete().delete(id);

        Assertions.assertNull(deleteCommentTemplate.getErrorreport());
        Assertions.assertNull(deleteCommentTemplate.getId());
    }

    @Test
    public void crudTest() {
        XMLCommentTemplate CommentTemplate = createCommentTemplate();

        XMLRequestParameterSaveCommentTemplate xmlRequestSaveCommentTemplate = new XMLRequestParameterSaveCommentTemplate();

        xmlRequestSaveCommentTemplate.setId(CommentTemplate.getId());
        xmlRequestSaveCommentTemplate.setName(GENERATED_NAME_NEW);
        xmlRequestSaveCommentTemplate.setContent(S3);
        xmlRequestSaveCommentTemplate.setDescription(S3);

        XMLCommentTemplate updateCommentTemplate = getSoapClient().getEpCommentTemplate().getWmSave().save(xmlRequestSaveCommentTemplate);
        updateCommentTemplate = getSoapClient().getEpCommentTemplate().getWmRead().getEntity(updateCommentTemplate.getId());

        Assertions.assertEquals(GENERATED_NAME_NEW, updateCommentTemplate.getName());
        Assertions.assertEquals(S3, updateCommentTemplate.getContent());
        Assertions.assertEquals(S3, updateCommentTemplate.getDescription());

        deleteCommentTemplate(updateCommentTemplate.getId());
    }
}
