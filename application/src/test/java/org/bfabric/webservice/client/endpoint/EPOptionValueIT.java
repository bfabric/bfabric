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
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveOptionValue;
import org.bfabric.xml.entity.XMLOption;
import org.bfabric.xml.entity.XMLOptionValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EPOptionValueIT extends AbstractIT {

    public static XMLOptionValue createOptionValue() {

        XMLRequestParameterSaveOptionValue xmlRequestSaveOptionValue = new XMLRequestParameterSaveOptionValue();

        xmlRequestSaveOptionValue.setName(GENERATED_NAME);

        XMLOption xmlOption = EPOptionIT.createOption();

        xmlRequestSaveOptionValue.setOptionid(xmlOption.getId().toString());

        XMLOptionValue optionValue = getSoapClient().getEpOptionValue().getWmSave().save(xmlRequestSaveOptionValue);
        if (optionValue.getErrorreport() != null) {
            throw new SoapClientException("Could not create optionValue: " + optionValue.getErrorreport());
        }
        return optionValue;
    }

    public static void deleteOptionValue(Long id) {
        getSoapClient().getEpOptionValue().getWmDelete().delete(id);
    }

    @Test
    public void crudTest() {
        XMLOptionValue optionValue = createOptionValue();

        Assertions.assertNull(optionValue.getErrorreport());

        XMLRequestParameterSaveOptionValue xmlRequestSaveOptionValue = new XMLRequestParameterSaveOptionValue();

        xmlRequestSaveOptionValue.setId(optionValue.getId());
        xmlRequestSaveOptionValue.setOptionid(optionValue.getOption().getId().toString());
        xmlRequestSaveOptionValue.setName(GENERATED_NAME_NEW);

        XMLOptionValue updateOptionValue = getSoapClient().getEpOptionValue().getWmSave().save(xmlRequestSaveOptionValue);
        updateOptionValue = getSoapClient().getEpOptionValue().getWmRead().getEntity(updateOptionValue.getId());

        Assertions.assertEquals(GENERATED_NAME_NEW, updateOptionValue.getName());
        Assertions.assertEquals(optionValue.getOption().getId().toString(), updateOptionValue.getOption().getId().toString());

        deleteOptionValue(updateOptionValue.getId());
        EPOptionIT.deleteOption(updateOptionValue.getOption().getId());
    }
}
