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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOption;
import org.bfabric.xml.entity.XMLOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPOptionIT extends AbstractIT {

    public static XMLOption createOption() {

        XMLRequestParameterSaveOption xmlRequestSaveOption = new XMLRequestParameterSaveOption();

        xmlRequestSaveOption.setName(S5);
        xmlRequestSaveOption.setEnabled(Boolean.TRUE.toString());
        xmlRequestSaveOption.setRequired(Boolean.TRUE.toString());
        xmlRequestSaveOption.setMultiple(Boolean.TRUE.toString());

        XMLOption option = getSoapClient().getEpOption().getWmSave().save(xmlRequestSaveOption);

        if (option.getErrorreport() != null) {
            throw new SoapClientException("Could not create option: " + option.getErrorreport());
        }
        return option;
    }

    @Test
    public void crudTest() {
        XMLOption option = createOption();

        Assertions.assertNull(option.getErrorreport());

        XMLRequestParameterSaveOption xmlRequestSaveOption = new XMLRequestParameterSaveOption();

        xmlRequestSaveOption.setId(option.getId());
        xmlRequestSaveOption.setName(GENERATED_NAME_NEW);
        xmlRequestSaveOption.setEnabled(Boolean.FALSE.toString());
        xmlRequestSaveOption.setMultiple(Boolean.FALSE.toString());
        xmlRequestSaveOption.setRequired(Boolean.FALSE.toString());

        XMLOption updateOption = getSoapClient().getEpOption().getWmSave().save(xmlRequestSaveOption);
        updateOption = getSoapClient().getEpOption().getWmRead().getEntity(updateOption.getId());

        Assertions.assertEquals(GENERATED_NAME_NEW, updateOption.getName());
        Assertions.assertEquals(Boolean.FALSE.toString(), updateOption.getEnabled().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), updateOption.getMultiple().toString());
        Assertions.assertEquals(Boolean.FALSE.toString(), updateOption.getRequired().toString());

        deleteOption(updateOption.getId());
    }

    public static void deleteOption(Long id) {
        XMLOption deleteOption = getSoapClient().getEpOption().getWmDelete().delete(id);

        Assertions.assertNull(deleteOption.getErrorreport());
        Assertions.assertNull(deleteOption.getId());
    }
}
