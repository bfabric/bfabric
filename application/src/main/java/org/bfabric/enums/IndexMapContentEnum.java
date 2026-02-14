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

package org.bfabric.enums;

import org.bfabric.Constants;

public enum IndexMapContentEnum {

    ACCOUNTEDDAYS(
        "accountedDays",
        true),
    ACCOUNTEDDURATION(
        "accountedDuration",
        true),
    ACTIVE(
        "active",
        false),
    ADDRESS(
        "address",
        false),
    ADMIN(
        "admin",
        false),
    ANNOTATIONID(
        "annotationId",
        true),
    ANY(
        "any",
        false),
    APPLICATION(
        "application",
        false),
    APPROVALDATE(
        "approvalDate",
        false),
    APPROVALNOTE(
        "approvalNote",
        false),
    APPROVEDBY(
        "approvedBy",
        false),
    ARCHIVING(
        "archiving",
        false),
    AVAILABLE(
        "available",
        false),
    AVAILABLERESOURCES(
        "availableResources",
        true),
    BILLINGADDRESS(
        "billingAddress",
        false),
    BILLINGCUSTOMER(
        "billingCustomer",
        false),
    BILLINGEMAIL(
        "billingEmail",
        false),
    BIOINFORMATICIAN(
        "bioinformatician",
        false),
    BODY(
        "body",
        false),
    BOOKABLE(
        "bookable",
        false),
    BOOKER(
        "booker",
        false),
    BOOKINGNUMBER(
        "bookingNr",
        false),
    BOOKINGS(
        "bookings",
        true),
    BOOKINGTOTAL(
        "total",
        false),
    BUDGETOFFICER(
        "budgetOfficer",
        false),
    CHARGEABLE(
        "chargeable",
        true),
    CHARGEABLEITEMS(
        "chargeableItems",
        true),
    CHARGED(
        "charged",
        true),
    CHARGEDITEMS(
        "chargedItems",
        true),
    CHARGES(
        "charges",
        true),
    CHARGESBILLABLE(
        "chargesBillable",
        false),
    CHARGESBOOKED(
        "chargesBooked",
        false),
    CHARGESSUM(
        "chargesSum",
        true),
    CHILDREN(
        "children",
        true),
    COACH(
        "coach",
        false),
    COACHBACKUP(
        "coachBackup",
        false),
    COMMENT(
        "comment",
        false),
    COMPANY(
        "company",
        false),
    COMPUTER(
        "computer",
        false),
    CONTACT(
        "contact",
        false),
    CONTAINERID(
        "containerId",
        false),
    CREATED(
        "created",
        false),
    CREATEDBY(
        "createdBy",
        false),
    CURRENCY(
        "currency",
        false),
    DAYS(
        "days",
        true),
    DEPARTMENT(
        "department",
        false),
    DESCRIPTION(
        "description",
        false),
    DISCRIMINATOR(
        "discriminator",
        false),
    DIVISION(
        "division",
        false),
    DURATION(
        "duration",
        true),
    EMAIL(
        "email",
        false),
    EMAILACTIVE(
        "emailActive",
        false),
    EMAILVERIFIED(
        "emailVerified",
        false),
    ENABLED(
        "enabled",
        false),
    ENDDATE(
        "endDate",
        false),
    EVENTTYPE(
        "eventType",
        false),
    EXPIRYDATE(
        "expiryDate",
        false),
    FILENAME(
        "fileName",
        false),
    FILECHECKSUM(
        "fileChecksum",
        false),
    FIRSTNAME(
        "firstName",
        false),
    HOURS(
        "hours",
        true),
    ID(
        "id",
        true),
    IMPORTRESOUCESSIZE(
        "importResourcesSize",
        true),
    INDEXMAPTYPE(
        "indexMapType",
        false),
    INSTALLATIONDATE(
        "installationDate",
        false),
    INSTITUTE(
        "institute",
        false),
    INSTRUMENT(
        "instrument",
        false),
    INSTRUMENTS(
        "instruments",
        false),
    INVENTORYNUMBER(
        "inventoryNumber",
        false),
    INVOICEDCURRENCY(
        "invoicedCurrency",
        false),
    INVOICEDPRICE(
        "invoicedPrice",
        false),
    INVOICENUMBER(
        "invoiceNumber",
        false),
    INVOICERECEIVEDDATE(
        "invoiceReceivedDate",
        false),
    ITEMS(
        "items",
        true),
    LASTLOGINDATE(
        "lastLoginDate",
        false),
    LASTNAME(
        "lastName",
        false),
    LEADER(
        "leader",
        false),
    LOGIN(
        "login",
        false),
    MODIFIED(
        "modified",
        false),
    MODIFIEDBY(
        "modifiedBy",
        false),
    NAME(
        "name",
        false),
    ORDERDATE(
        "orderDate",
        false),
    ORDEREDBY(
        "orderedBy",
        false),
    ORDERID(
        "orderId",
        true),
    ORDERITEM(
        "orderItem",
        false),
    ORDERITEMRECEIVEDBY(
        "orderItemReceivedBy",
        false),
    ORDERITEMRECEIVEDDATE(
        "orderItemReceivedDate",
        false),
    ORGANIZATION(
        "organization",
        false),
    P4UNUMBER(
        "p4uNumber",
        false),
    PAID(
        "paid",
        false),
    PARENTID(
        "parentId",
        true),
    PARENTCLASSNAME(
        "parentClassName",
        false),
    PAYER(
        "payer",
        false),
    PAYERSREFERENCENUMBER(
        "payersReferenceNumber",
        false),
    PARENT(
        "parent",
        false),
    PARENTS(
        "parents",
        false),
    PERM_DOI(
        "permDoi",
        false),
    PERM_GROUP(
        "permGroup",
        false),
    PERM_STATUS(
        "permStatus",
        false),
    PREDECESSOR(
        "predecessor",
        false),
    PRICE(
        "price",
        true),
    PROJECTID(
        "projectId",
        true),
    OFFERID(
        "offerId",
        true),
    OPERATOR(
        "operator",
        false),
    PURCHASEDDATE(
        "purchasedDate",
        false),
    PURCHASEDPRICE(
        "purchasedPrice",
        false),
    PROGRESS(
        "progress",
        false),
    REFERENCENUMBER(
        "referenceNumber",
        false),
    RELATIVEPATH(
        "relativePath",
        false),
    RELEASED(
        "released",
        false),
    REPEATERID(
        "repeaterId",
        false),
    REQUESTER(
        "requester",
        false),
    RESOURCES(
        "resources",
        true),
    RUNENABLED(
        "runEnabled",
        false),
    SAMPLE(
        "sample",
        false),
    SAMPLEID(
        "sampleId",
        true),
    SAMPLETYPE(
        "sampleType",
        false),
    SELLER(
        "seller",
        false),
    SELLERCONTACT(
        "sellerContact",
        false),
    SERIALNUMBER(
        "serialNumber",
        false),
    SERVICEAREA(
        "serviceArea",
        false),
    SERVICETYPE(
        "serviceType",
        false),
    SIZE(
        "size",
        true),
    STARTDATE(
        "startDate",
        false),
    STATUS(
        "status",
        false),
    STORAGE(
        "storage",
        false),
    STORAGEID(
        "storageId",
        true),
    STORAGEMODEL(
        "storageModel",
        false),
    SUMMARY(
        "summary",
        false),
    SUPERVISOR(
        "supervisor",
        false),
    SUPPLIER(
        "supplier",
        false),
    TECHNOLOGY(
        "technology",
        false),
    TRAINEDUSER(
        "trainedUser",
        false),
    TYPE(
        "type",
        false),
    UP(
        "up",
        false),
    URL(
        "url",
        false),
    USER(
        "user",
        false),
    USERBOOKABLE(
        "userBookable",
        false),
    USERID(
        "userId",
        true),
    USERVISIBLE(
        "userVisible",
        false),
    VATNUMBER(
        "vatNumber",
        false),
    WORKFLOWSTEPID(
        "workflowStepId",
        true),
    WORKUNIT(
        "workunit",
        false),
    WORKUNITID(
        "workunitId",
        true),
    WORKUNITPARAMETER(
        "workunitParameter",
        false),
    YEAR(
        "year",
        true);

    private final String field;

    private final boolean numeric;

    IndexMapContentEnum(String field, boolean numeric) {
        this.field = field;
        this.numeric = numeric;
    }

    public static IndexMapContentEnum getIndexMapFieldEnum(String field) {
        IndexMapContentEnum ret = null;
        for (IndexMapContentEnum indexMapFilterEnum : values()) {
            if (indexMapFilterEnum.getField().equals(field)) {
                ret = indexMapFilterEnum;
            }
        }
        return ret;
    }

    public String getField() {
        return field;
    }

    public String getFieldColSuffix() {
        return field + Constants.INDEXER_COL_SUFFIX;
    }

    public boolean isNumeric() {
        return numeric;
    }
}
