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

package org.bfabric.webservice.request.parameter;

import java.util.ArrayList;
import java.util.List;

import org.bfabric.enums.SamplePreparationProtocolDiscriminator;
import org.bfabric.enums.SamplePreparationProtocolType;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadSamplePreparationProtocol extends XMLRequestParameterReadContainerReferencingEntity {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> discriminator = new ArrayList<>();

    private final List<Long> instrumentid = new ArrayList<>();

    private final List<Long> predecessorid = new ArrayList<>();

    private final List<Long> sampletypeid = new ArrayList<>();

    private final List<Long> sequencingapplicationid = new ArrayList<>();

    private final List<String> strandedness = new ArrayList<>();

    private final List<String> adapter1 = new ArrayList<>();

    private final List<String> adapter2 = new ArrayList<>();

    private final List<Long> successorid = new ArrayList<>();

    private final List<Long> technologyid = new ArrayList<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<String> type = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsString(strandedness, "strandedness.name"));
        items.addAll(getWhereClauseItemsString(adapter1, "adapter1"));
        items.addAll(getWhereClauseItemsString(adapter2, "adapter2"));
        items.addAll(getWhereClauseItemsLong(predecessorid, "predecessor.id"));
        items.addAll(getJoinWhereClauseItemsLong(sampletypeid, "sampleTypes sampleType", "sampleType.id", "sampletypeid"));
        items.addAll(getJoinWhereClauseItemsLong(sequencingapplicationid, "sequencingApplications sequencingApplication", "sequencingApplication.id", "sequencingapplicationid"));
        items.addAll(getJoinWhereClauseItemsLong(instrumentid, "instruments instrument", "instrument.id", "instrumentid"));
        items.addAll(getJoinWhereClauseItemsLong(successorid, "successors successor", "successor.id", "successorid"));
        items.addAll(getJoinWhereClauseItemsLong(technologyid, "technologies technology", "technology.id", "technologyid"));

        for (int index = 0; index < type.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setAttributeName("type");
            whereClauseItem.setParameterName("type" + index);
            whereClauseItem.setParameterValueEnum(SamplePreparationProtocolType.value(type.get(index)));
            items.add(whereClauseItem);
        }

        for (int index = 0; index < discriminator.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setAttributeName("discriminator");
            whereClauseItem.setParameterName("discriminator" + index);
            whereClauseItem.setParameterValueEnum(SamplePreparationProtocolDiscriminator.value(discriminator.get(index)));
            items.add(whereClauseItem);
        }

        return items;
    }
}
