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

public class XMLRequestParameterReadUser extends XMLRequestParameterReadBaseEntity {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Long> containerid = new ArrayList<>();

    private final List<Long> backupid = new ArrayList<>();

    private final List<String> email = new ArrayList<>();

    private final List<String> title = new ArrayList<>();

    private final List<String> salutation = new ArrayList<>();

    private final List<String> lastname = new ArrayList<>();

    private final List<String> firstname = new ArrayList<>();

    private final List<String> login = new ArrayList<>();

    private final List<Integer> phonecountrycode = new ArrayList<>();

    private final List<Integer> phoneareacode = new ArrayList<>();

    private final List<Integer> phonelocalnumber = new ArrayList<>();

    private final List<Integer> homephonecountrycode = new ArrayList<>();

    private final List<Integer> homephoneareacode = new ArrayList<>();

    private final List<Integer> homephonelocalnumber = new ArrayList<>();

    private final List<String> privateemail = new ArrayList<>();

    private final List<Long> roleid = new ArrayList<>();

    private final List<Long> divisionid = new ArrayList<>();

    private final List<Long> instituteid = new ArrayList<>();

    private final List<Long> companyid = new ArrayList<>();

    private final List<Long> departmentid = new ArrayList<>();

    private final List<Long> organizationid = new ArrayList<>();

    private final List<Long> organizationtypeid = new ArrayList<>();

    private final List<String> division = new ArrayList<>();

    private final List<String> institute = new ArrayList<>();

    private final List<String> company = new ArrayList<>();

    private final List<String> department = new ArrayList<>();

    private final List<String> organization = new ArrayList<>();

    private final List<String> organizationtype = new ArrayList<>();

    private final List<Boolean> active = new ArrayList<>();

    private final List<Boolean> emailverified = new ArrayList<>();

    private final List<String> street = new ArrayList<>();

    private final List<String> zip = new ArrayList<>();

    private final List<String> city = new ArrayList<>();

    private final List<String> country = new ArrayList<>();

    private final List<String> room = new ArrayList<>();

    private final List<String> addresssupplement = new ArrayList<>();

    private final List<String> homeaddresssupplement = new ArrayList<>();

    private final List<String> homestreet = new ArrayList<>();

    private final List<String> homezip = new ArrayList<>();

    private final List<String> homecity = new ArrayList<>();

    private final List<String> homecountry = new ArrayList<>();

    private final List<String> accesscardcode = new ArrayList<>();

    private final List<String> accesscardnumber = new ArrayList<>();

    private final List<String> accesscardexpirydate = new ArrayList<>();

    private final List<String> accesscardexpirydatebefore = new ArrayList<>();

    private final List<String> technology = new ArrayList<>();

    private final List<Long> technologyid = new ArrayList<>();

    private final List<String> accesscardexpirydateafter = new ArrayList<>();

    private final List<Boolean> massmailenabled = new ArrayList<>();

    private final List<Boolean> accountenabled = new ArrayList<>();

    private final List<Boolean> dataaccessenabled = new ArrayList<>();

    @Override
    public List<WhereClauseItem> getWhereClauseItems() throws InvalidDataException, InvalidEnumValueException {
        List<WhereClauseItem> items = super.getWhereClauseItems();
        items.addAll(getWhereClauseItemsDate(accesscardexpirydate, "accessCardExpiryDate"));
        items.addAll(getWhereClauseItemsDateAfter(accesscardexpirydatebefore, "accessCardExpiryDate"));
        items.addAll(getWhereClauseItemsDateBefore(accesscardexpirydateafter, "accessCardExpiryDate"));

        items.addAll(getWhereClauseItemsStringCaseInsensitive(technology, "technology.name"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(email, "email"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(privateemail, "privateEmail"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(login, "login"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(salutation, "salutation"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(title, "title"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(lastname, "lastName"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(firstname, "firstName"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(street, "address.street"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(city, "address.city"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(zip, "address.zip"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(room, "address.room"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(country, "address.country.id"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(addresssupplement, "address.supplement"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(homestreet, "homeAddress.street"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(homecity, "homeAddress.city"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(homezip, "homeAddress.zip"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(homecountry, "homeAddress.country.id"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(homeaddresssupplement, "homeAddress.supplement"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(division, "division.name"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(company, "division.company.name"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(institute, "institute.name"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(department, "institute.department.name"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(organization, "institute.department.organization.name"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(organizationtype, "institute.department.organization.organizationType.name"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(accesscardcode, "accessCardCode"));
        items.addAll(getWhereClauseItemsStringCaseInsensitive(accesscardnumber, "accessCardNumber"));

        items.addAll(getWhereClauseItemsInteger(phonecountrycode, "phoneNumber.countryCode"));
        items.addAll(getWhereClauseItemsInteger(phoneareacode, "phoneNumber.areaCode"));
        items.addAll(getWhereClauseItemsInteger(phonelocalnumber, "phoneNumber.localNumber"));
        items.addAll(getWhereClauseItemsInteger(homephonecountrycode, "homePhoneNumber.countryCode"));
        items.addAll(getWhereClauseItemsInteger(homephoneareacode, "homePhoneNumber.areaCode"));
        items.addAll(getWhereClauseItemsInteger(homephonelocalnumber, "homePhoneNumber.localNumber"));

        items.addAll(getWhereClauseItemsBooleanList(active, "active"));
        items.addAll(getWhereClauseItemsBooleanList(emailverified, "emailVerified"));
        items.addAll(getWhereClauseItemsBooleanList(massmailenabled, "massMailEnabled"));
        items.addAll(getWhereClauseItemsBooleanList(accountenabled, "accountEnabled"));
        items.addAll(getWhereClauseItemsBooleanList(dataaccessenabled, "dataAccessEnabled"));

        items.addAll(getWhereClauseItemsLong(technologyid, "technology.id"));
        items.addAll(getWhereClauseItemsLong(backupid, "backup.id"));
        items.addAll(getWhereClauseItemsLong(divisionid, "division.id"));
        items.addAll(getWhereClauseItemsLong(companyid, "division.company.id"));
        items.addAll(getWhereClauseItemsLong(instituteid, "institute.id"));
        items.addAll(getWhereClauseItemsLong(departmentid, "institute.department.id"));
        items.addAll(getWhereClauseItemsLong(organizationid, "institute.department.organization.id"));
        items.addAll(getWhereClauseItemsLong(organizationtypeid, "institute.department.organization.organizationType.id"));

        // Note: special case -> therefore not generalized!
        for (int index = 0; index < containerid.size(); index++) {
            WhereClauseItem whereClauseItem = new WhereClauseItem();
            whereClauseItem.setJoinClause("memberships membership");
            // Note: This "extended" query is needed to transitively compute the container memberships!
            whereClauseItem
                .setAttributeName("membership.discriminator = '" + Membership.DISCRIMINATOR_CURRENT + "' and membership.container.id = (select project.id from Container where id=" + containerid
                    .get(index) + ") or membership.discriminator = '" + Membership.DISCRIMINATOR_CURRENT + "' and membership.container.id");
            whereClauseItem.setParameterName("containerid" + index);
            whereClauseItem.setParameterValueLong(containerid.get(index));
            items.add(whereClauseItem);
        }

        items.addAll(getJoinWhereClauseItemsLong(roleid, "roles role", "role.id", "roleid"));
        return items;
    }
}