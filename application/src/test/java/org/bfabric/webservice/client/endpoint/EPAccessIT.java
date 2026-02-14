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

import org.bfabric.xml.entity.XMLAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPAccessIT extends AbstractIT {

    @Test
    public void accessShouldBeRead() {
        XMLAccess access1 = getSoapClient().getEpAccess().getWmRead().getEntity(Long.valueOf(3));
        XMLAccess access1Duplicate = getSoapClient().getEpAccess().getWmRead().getEntity(Long.valueOf(3));
        XMLAccess access2 = getSoapClient().getEpAccess().getWmRead().getEntity(Long.valueOf(2));
        XMLAccess nonExistingAccess = getSoapClient().getEpAccess().getWmRead().getEntity(getEntityIdNonExisting());

        Assertions.assertNull(access1.getErrorreport());
        Assertions.assertNotNull(access1);
        Assertions.assertNotNull(access1Duplicate);
        Assertions.assertNotNull(access2);
        Assertions.assertNull(nonExistingAccess);

        Assertions.assertSame(access1, access1Duplicate);
        Assertions.assertNotSame(access1, access2);
    }
}