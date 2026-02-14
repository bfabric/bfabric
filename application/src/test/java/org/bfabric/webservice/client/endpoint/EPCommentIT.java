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

import java.util.Arrays;

import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.util.CollectionHelper;
import org.bfabric.webservice.client.exception.SoapClientException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveComment;
import org.bfabric.xml.entity.XMLComment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPCommentIT extends AbstractIT {

    private final CommentDiscriminator TESTTYPE = CommentDiscriminator.PROJECT_COMMENT;

    @Test
    public void commentShouldBeCreated() {
        XMLComment comment = createComment();

        Assertions.assertNull(comment.getErrorreport());

        Assertions.assertFalse(Boolean.parseBoolean(comment.getInternal()));
        Assertions.assertEquals(CONTAINER_ID, comment.getParent().getIdString());
        Assertions.assertEquals(S5, comment.getText());
        Assertions.assertEquals(TESTTYPE.toString(), comment.getDiscriminator());

        deleteComment(comment.getId());
    }

    @Test
    public void commentShouldBeDeleted() {
        XMLComment comment = createComment();
        XMLComment deletedComment = getSoapClient().getEpComment().getWmDelete().delete(comment.getId());
        Assertions.assertNull(deletedComment.getErrorreport());
    }

    @Test
    public void commentShouldBeRead() {
        XMLComment comment = createComment();

        XMLComment readComment = getSoapClient().getEpComment().getWmRead().getEntity(comment.getId());
        XMLComment readCommentDuplicate = getSoapClient().getEpComment().getWmRead().getEntity(comment.getId());

        Assertions.assertSame(readComment, readCommentDuplicate);
        Assertions.assertNull(readComment.getErrorreport());
        Assertions.assertFalse(Boolean.parseBoolean(readComment.getInternal()));
        Assertions.assertEquals(CONTAINER_ID, comment.getParent().getIdString());
        Assertions.assertEquals(S5, comment.getText());
        Assertions.assertEquals(TESTTYPE.toString(), comment.getDiscriminator());

        deleteComment(comment.getId());
    }

    @Test
    public void commentShouldBeUpdated() {
        XMLComment comment = createComment();

        XMLRequestParameterSaveComment xmlRequestSaveComment = new XMLRequestParameterSaveComment();

        xmlRequestSaveComment.setId(comment.getId());

        xmlRequestSaveComment.setInternal("false");
        xmlRequestSaveComment.setParentclassname("Project");
        xmlRequestSaveComment.setParentid(CONTAINER_ID);
        xmlRequestSaveComment.setSendmail("false");
        xmlRequestSaveComment.setText(S3);
        xmlRequestSaveComment.setDiscriminator(TESTTYPE.toString());

        XMLComment updatedComment = getSoapClient().getEpComment().getWmSave().save(xmlRequestSaveComment);

        Assertions.assertNull(updatedComment.getErrorreport());

        Assertions.assertFalse(Boolean.parseBoolean(updatedComment.getInternal()));
        Assertions.assertEquals(CONTAINER_ID, comment.getParent().getIdString());
        Assertions.assertEquals(S3, updatedComment.getText());
        Assertions.assertEquals(TESTTYPE.toString(), comment.getDiscriminator());

        deleteComment(comment.getId());
    }

    @Test
    public void commentShouldNotBeCreatedDueToInvalidDiscriminator() {
        XMLRequestParameterSaveComment xmlRequestSaveComment = new XMLRequestParameterSaveComment();

        String INVALID = "INVALID";
        xmlRequestSaveComment.setDiscriminator(INVALID);

        XMLComment comment = getSoapClient().getEpComment().getWmSave().save(xmlRequestSaveComment);

        Assertions.assertNotNull(comment.getErrorreport());
        Assertions.assertEquals(comment.getErrorreport(), "Invalid type: " + INVALID + ". Valid values: " + CollectionHelper.print(Arrays.asList(CommentDiscriminator.values())) + "!");
    }

    @Test
    public void commentShouldNotBeCreatedDueToNonSpecifiedDiscriminator() {
        XMLRequestParameterSaveComment xmlRequestSaveComment = new XMLRequestParameterSaveComment();

        XMLComment comment = getSoapClient().getEpComment().getWmSave().save(xmlRequestSaveComment);

        Assertions.assertNotNull(comment.getErrorreport());
        Assertions.assertEquals("No value specified for type!", comment.getErrorreport());
    }

    @Test
    public void commentShouldNotBeCreatedDueToNonSpecifiedIsInternal() {
        XMLRequestParameterSaveComment xmlRequestSaveComment = new XMLRequestParameterSaveComment();

        xmlRequestSaveComment.setSendmail("false");
        xmlRequestSaveComment.setDiscriminator(TESTTYPE.toString());
        xmlRequestSaveComment.setParentclassname("Project");
        xmlRequestSaveComment.setParentid(CONTAINER_ID);

        XMLComment comment = getSoapClient().getEpComment().getWmSave().save(xmlRequestSaveComment);

        Assertions.assertNotNull(comment.getErrorreport());
        Assertions.assertEquals("No value specified for internal!", comment.getErrorreport());
    }

    @Test
    public void commentShouldNotBeCreatedDueToNonSpecifiedParentId() {
        XMLRequestParameterSaveComment xmlRequestSaveComment = new XMLRequestParameterSaveComment();

        xmlRequestSaveComment.setInternal("false");
        xmlRequestSaveComment.setSendmail("false");
        xmlRequestSaveComment.setDiscriminator(TESTTYPE.toString());

        XMLComment comment = getSoapClient().getEpComment().getWmSave().save(xmlRequestSaveComment);

        Assertions.assertNotNull(comment.getErrorreport());
        Assertions.assertEquals("No value specified for parentid!", comment.getErrorreport());
    }

    @Test
    public void commentShouldNotBeCreatedDueToWrongFormattedIsInternal() {
        XMLRequestParameterSaveComment xmlRequestSaveComment = new XMLRequestParameterSaveComment();

        xmlRequestSaveComment.setInternal(S5);
        xmlRequestSaveComment.setSendmail("false");
        xmlRequestSaveComment.setParentclassname("Project");
        xmlRequestSaveComment.setParentid(CONTAINER_ID);
        xmlRequestSaveComment.setDiscriminator(TESTTYPE.toString());

        XMLComment comment = getSoapClient().getEpComment().getWmSave().save(xmlRequestSaveComment);

        Assertions.assertNotNull(comment.getErrorreport());
        Assertions.assertEquals("internal " + S5 + " is not boolean value (true or false)!", comment.getErrorreport());
    }

    public XMLComment createComment() {
        XMLRequestParameterSaveComment xmlRequestSaveComment = new XMLRequestParameterSaveComment();

        xmlRequestSaveComment.setInternal("false");
        xmlRequestSaveComment.setSendmail("false");
        xmlRequestSaveComment.setParentclassname("Project");
        xmlRequestSaveComment.setParentid(CONTAINER_ID);
        xmlRequestSaveComment.setText(S5);
        xmlRequestSaveComment.setDiscriminator(TESTTYPE.toString());

        XMLComment comment = getSoapClient().getEpComment().getWmSave().save(xmlRequestSaveComment);

        if (comment.getErrorreport() != null) {
            throw new SoapClientException("Could not create comment: " + comment.getErrorreport());
        }

        return comment;
    }

    public void deleteComment(Long id) {
        getSoapClient().getEpComment().getWmDelete().delete(id);
    }
}
