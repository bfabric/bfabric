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

import org.bfabric.xml.entity.XMLProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPProjectIT extends AbstractIT {

    @Test
    public void projectShouldBeRead() {
        XMLProject project403 = getSoapClient().getEpProject().getWmRead().getEntity(Long.valueOf(403));
        XMLProject project403Duplicate = getSoapClient().getEpProject().getWmRead().getEntity(Long.valueOf(403));
        XMLProject project404 = getSoapClient().getEpProject().getWmRead().getEntity(Long.valueOf(404));
        XMLProject nonExistingProject = getSoapClient().getEpProject().getWmRead().getEntity(Long.valueOf(getEntityIdNonExistingAsString()));

        Assertions.assertNull(project403.getErrorreport());
        Assertions.assertNotNull(project403);
        Assertions.assertNotNull(project403Duplicate);
        Assertions.assertNotNull(project404);
        Assertions.assertNull(nonExistingProject);

        Assertions.assertSame(project403, project403Duplicate);
        Assertions.assertNotSame(project403, project404);
    }
}
