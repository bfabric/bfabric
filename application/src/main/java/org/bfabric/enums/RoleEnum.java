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

public enum RoleEnum {

    ACCESSREQUESTMANAGER(
        "accessRequestManager"),
    ACCESSREQUESTREADER(
        "accessRequestReader"),
    ADMIN(
        "admin"),
    AFFILIATIONMANAGER(
        "affiliationManager"),
    AFFILIATIONREADER(
        "affiliationReader"),
    AGENDAMANAGER(
        "agendaManager"),
    AGENDAREADER(
        "agendaReader"),
    AGENDAUSER(
        "agendaUser"),
    ALUMNI(
        "alumni"),
    ANNOTATIONMANAGER(
        "annotationManager"),
    ANNOTATIONREADER(
        "annotationReader"),
    APPLICATIONMANAGER(
        "applicationManager"),
    APPLICATIONREADER(
        "applicationReader"),
    ARCHIVEMANAGER(
        "archiveManager"),
    ARCHIVEREADER(
        "archiveReader"),
    BIOINFORMATICIAN(
        "bioinformatician"),
    BOOKINGISSUER(
        "bookingIssuer"),
    BOOKINGMANAGER(
        "bookingManager"),
    BOOKINGREADER(
        "bookingReader"),
    CHARGEMANAGER(
        "chargeManager"),
    CHARGEREADER(
        "chargeReader"),
    COMMENTMANAGER(
        "commentManager"),
    COMMENTREADER(
        "commentReader"),
    CONFIGURATIONMANAGER(
        "configurationManager"),
    CONFIGURATIONREADER(
        "configurationReader"),
    CONTAINERMANAGER(
        "containerManager"),
    CONTAINERREADER(
        "containerReader"),
    CONTAINERMEMBER(
        "containerMember"),
    CONTRACTMANAGER(
        "contractManager"),
    CONTRACTREADER(
        "contractReader"),
    DATACLEANER(
        "dataCleaner"),
    EMPLOYEE(
        "employee"),
    EMPLOYEEMANAGER(
        "employeeManager"),
    EMPLOYEEREADER(
        "employeeReader"),
    EXECUTABLEMANAGER(
        "executableManager"),
    EXTERNALJOBREADER(
        "externalJobReader"),
    FEEDBACKMANAGER(
        "feedbackManager"),
    FEEDBACKREADER(
        "feedbackReader"),
    FEEDER(
        "feeder"),
    INSTRUMENTMANAGER(
        "instrumentManager"),
    INSTRUMENTREADER(
        "instrumentReader"),
    INTERNAL(
        "internal"),
    ITMANAGER(
        "itManager"),
    ITREADER(
        "itReader"),
    LABMANAGER(
        "labManager"),
    LABMEMBER(
        "labMember"),
    LINKMANAGER(
        "linkManager"),
    LINKREADER(
        "linkReader"),
    MAILMANAGER(
        "mailManager"),
    MAILREADER(
        "mailReader"),
    MAILSENDER(
        "mailSender"),
    MASTEREXECUTABLEMANAGER(
        "masterExecutableManager"),
    PLATEMANAGER(
        "plateManager"),
    PLATEREADER(
        "plateReader"),
    PROGRAMMEMANAGER(
        "programmeManager"),
    PUBLIC(
        "public"),
    PURCHASEADMIN(
        "purchaseAdmin"),
    PURCHASEMANAGER(
        "purchaseManager"),
    PURCHASEREADER(
        "purchaseReader"),
    QUERYMANAGER(
        "queryManager"),
    REVIEWER(
        "reviewer"),
    REVIEWMANAGER(
        "reviewManager"),
    ROLEMANAGER(
        "roleManager"),
    ROLEREADER(
        "roleReader"),
    RUNMANAGER(
        "runManager"),
    RUNREADER(
        "runReader"),
    SAMPLEPREPARATIONPROTOCOLMANAGER(
        "samplePreparationProtocolManager"),
    SAMPLEPREPARATIONPROTOCOLREADER(
        "samplePreparationProtocolReader"),
    SECRETARY(
        "secretary"),
    SERVICEMANAGER(
        "serviceManager"),
    SERVICEREADER(
        "serviceReader"),
    STATISTICSREADER(
        "statisticsReader"),
    STEERINGCOMMITTEE(
        "steeringCommittee"),
    STORAGEMANAGER(
        "storageManager"),
    STORAGEREADER(
        "storageReader"),
    SUBMITTERMANAGER(
        "submitterManager"),
    SUBMITTERREADER(
        "submitterReader"),
    TECHNOLOGYMANAGER(
        "technologyManager"),
    TECHNOLOGYREADER(
        "technologyReader"),
    UNITHEAD(
        "unitHead"),
    USER(
        "user"),
    USERGROUPMANAGER(
        "userGroupManager"),
    USERGROUPREADER(
        "userGroupReader"),
    USERMANAGER(
        "userManager"),
    USERREADER(
        "userReader"),
    WEBSERVICEUSER(
        "webServiceUser"),
    WRAPPERCREATORMANAGER(
        "wrapperCreatorManager"),
    WRAPPERCREATORREADER(
        "wrapperCreatorReader");

    private final String name;

    RoleEnum(String name) {
        this.name = name;
    }

    public static RoleEnum value(String name) {
        try {
            return name != null ? valueOf(name.toUpperCase()) : null;
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public String getName() {
        return name;
    }
}
