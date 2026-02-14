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

import java.util.ArrayList;
import java.util.List;

public enum JobEnum {
    CancelPendingOrders(true),
    CheckComputerLoginValidity(true),
    CheckLinkValidity(true),
    CheckOfferValidityDuration(true),
    CheckWebUrlValidity(true),
    CloseAgendaYear(false),
    DeleteEmptyWorkunits(true),
    DeleteUnassignedObjects(true),
    DeleteDeletableOffers(true),
    DeleteDeletableUsers(true),
    DeleteExpiredShibbolethMappings(true),
    DeleteExpiredMetadataFiles(true),
    DeleteLocalImportResources(true),
    RefreshMaterializedViews(true),
    Reindex(false),
    RemindAccessCardExpiry(true),
    RemindContractExpiry(true),
    RemindExtensionReport(true),
    RemindPendingOrders(true),
    RemindInstrumentReservation(true),
    ResetArchiveExpirationDatePassed(true),
    ResetUserAvailable(false),
    SendMail(false);

    final boolean anytime;

    JobEnum(boolean anytime) {
        this.anytime = anytime;
    }

    public static List<JobEnum> getJobEnumsInvokableAnytime() {
        List<JobEnum> jobEnums = new ArrayList<>();
        for (JobEnum jobEnum : values()) {
            if (jobEnum.anytime) {
                jobEnums.add(jobEnum);
            }
        }
        return jobEnums;
    }
}
