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

public class XMLRequestParameterReadWorkflowStep extends XMLRequestParameterReadNamedBaseEntity {

    private final List<String> expectedduration = new ArrayList<>();

    private final List<String> enddate = new ArrayList<>();

    private final List<String> enddateafter = new ArrayList<>();

    private final List<String> enddatebefore = new ArrayList<>();

    private final List<String> startdate = new ArrayList<>();

    private final List<String> startdateafter = new ArrayList<>();

    private final List<String> startdatebefore = new ArrayList<>();

    private final List<Long> datasetid = new ArrayList<>();

    private final List<Long> sampleid = new ArrayList<>();

    private final List<Long> supervisorid = new ArrayList<>();

    private final List<Long> workflowid = new ArrayList<>();

    private final List<Long> workunitid = new ArrayList<>();

    private final List<Long> workflowtemplatestepid = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();

        items.addAll(getWhereClauseItemsString(expectedduration, "expectedduration"));
        items.addAll(getWhereClauseItemsLong(supervisorid, "supervisor.id"));
        items.addAll(getWhereClauseItemsLong(workflowid, "workflow.id"));
        items.addAll(getWhereClauseItemsLong(workflowtemplatestepid, "workflowTemplateStep.id"));

        items.addAll(getJoinWhereClauseItemsLong(sampleid, "samples sample", "sample.id", "sampleid"));
        items.addAll(getJoinWhereClauseItemsLong(datasetid, "datasets dataset", "dataset.id", "datasetid"));
        items.addAll(getJoinWhereClauseItemsLong(workunitid, "workunits workunit", "workunit.id", "workunitid"));

        items.addAll(getWhereClauseItemsDateTime(startdate, "startDate"));
        items.addAll(getWhereClauseItemsDateTimeAfter(startdateafter, "startDate"));
        items.addAll(getWhereClauseItemsDateTimeBefore(startdatebefore, "startDate"));
        items.addAll(getWhereClauseItemsDateTime(enddate, "endDate"));
        items.addAll(getWhereClauseItemsDateTimeAfter(enddateafter, "endDate"));
        items.addAll(getWhereClauseItemsDateTimeBefore(enddatebefore, "endDate"));

        return items;
    }
}
