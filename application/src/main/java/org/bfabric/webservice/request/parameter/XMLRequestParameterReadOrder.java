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

import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadOrder extends XMLRequestParameterReadContainer {

    private final List<String> biosafetylevel2precautionsrequired = new ArrayList<>();

    private final List<String> fastasequence = new ArrayList<>();

    private final List<String> sampletype = new ArrayList<>();

    private final List<String> samplescontaintransgenes = new ArrayList<>();

    private final List<Long> oldprojectorderid = new ArrayList<>();

    private final List<Long> oldserviceorderid = new ArrayList<>();

    private final List<Long> serviceareaid = new ArrayList<>();

    private final List<Long> servicetypeid = new ArrayList<>();

    private final List<String> processessamples = new ArrayList<>();

    private final List<String> express = new ArrayList<>();

    private final List<String> processesplates = new ArrayList<>();

    private final List<String> requiresproject = new ArrayList<>();

    private final List<String> servicecolumnenabled = new ArrayList<>();

    private final List<String> initialcustomstatus = new ArrayList<>();

    private final List<Long> platesubmissionproposallimit = new ArrayList<>();

    private final List<String> sampleretention = new ArrayList<>();

    private final List<String> mailtrackingnumber = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsLong(oldprojectorderid, "oldProjectOrderId"));
        items.addAll(getWhereClauseItemsLong(oldserviceorderid, "oldServiceOrderId"));
        items.addAll(getWhereClauseItemsLong(servicetypeid, "serviceType.id"));
        items.addAll(getWhereClauseItemsLong(serviceareaid, "serviceType.serviceArea.id"));
        items.addAll(getWhereClauseItemsLong(platesubmissionproposallimit, "plateSubmissionProposalLimit"));
        items.addAll(getWhereClauseItemsString(fastasequence, "fastaSequence"));
        items.addAll(getWhereClauseItemsString(sampletype, "sampleType"));
        items.addAll(getWhereClauseItemsString(sampleretention, "sampleRetention"));
        items.addAll(getWhereClauseItemsString(mailtrackingnumber, "mailTrackingNumber"));
        items.addAll(getWhereClauseItemsBoolean(express, "express"));
        items.addAll(getWhereClauseItemsBoolean(requiresproject, "requiresProject"));
        items.addAll(getWhereClauseItemsBoolean(processessamples, "processesSamples"));
        items.addAll(getWhereClauseItemsBoolean(servicecolumnenabled, "serviceColumnEnabled"));
        items.addAll(getWhereClauseItemsBoolean(processesplates, "processesPlates"));
        items.addAll(getWhereClauseItemsBoolean(initialcustomstatus, "initialCustomStatus"));
        items.addAll(getWhereClauseItemsBoolean(biosafetylevel2precautionsrequired, "bioSafetyLevel2PrecautionsRequired"));
        items.addAll(getWhereClauseItemsBoolean(samplescontaintransgenes, "samplesContainTransgenes"));
        return items;
    }
}
