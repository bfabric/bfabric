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

import org.bfabric.entity.Membership;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.exception.InvalidEnumValueException;

public class XMLRequestParameterReadContainer extends XMLRequestParameterReadStatusNamedBaseEntity {

    private final List<Long> divisionid = new ArrayList<>();

    private final List<Long> instituteid = new ArrayList<>();

    private final List<Long> budgetofficerid = new ArrayList<>();

    private final List<Long> coachid = new ArrayList<>();

    private final List<Long> contactid = new ArrayList<>();

    private final List<Long> discussedwithid = new ArrayList<>();

    private final List<Long> formermemberid = new ArrayList<>();

    private final List<Long> leaderid = new ArrayList<>();

    private final List<Long> memberid = new ArrayList<>();

    private final List<Long> requesterid = new ArrayList<>();

    private final List<Long> projectid = new ArrayList<>();

    private final List<Long> technologyid = new ArrayList<>();

    private final List<String> internal = new ArrayList<>();

    private final List<String> orderdataonly = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();

        items.addAll(getWhereClauseItemsLong(coachid, "coach.id"));
        items.addAll(getWhereClauseItemsLong(contactid, "contact.id"));
        items.addAll(getWhereClauseItemsLong(requesterid, "requester.id"));
        items.addAll(getWhereClauseItemsLong(budgetofficerid, "budgetOfficer.id"));
        items.addAll(getWhereClauseItemsLong(leaderid, "leader.id"));
        items.addAll(getWhereClauseItemsLong(projectid, "project.id"));
        items.addAll(getWhereClauseItemsLong(divisionid, "division.id"));
        items.addAll(getWhereClauseItemsLong(instituteid, "institute.id"));
        items.addAll(getWhereClauseItemsBoolean(internal, "internal"));
        items.addAll(getWhereClauseItemsBoolean(orderdataonly, "orderDataOnly"));

        items.addAll(getJoinWhereClauseItemsLong(memberid, "memberships membership", "membership.discriminator = '" + Membership.DISCRIMINATOR_CURRENT + "' and membership.user.id", "userid"));
        items.addAll(getJoinWhereClauseItemsLong(formermemberid, "memberships membership", "membership.discriminator = '" + Membership.DISCRIMINATOR_FORMER + "' and membership.user.id", "userid"));
        items.addAll(getJoinWhereClauseItemsLong(discussedwithid, "discussedWith user", "user.id", "userid"));
        items.addAll(getJoinWhereClauseItemsLong(technologyid, "technologies technology", "technology.id", "technologyid"));

        return items;
    }
}
