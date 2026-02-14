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

package org.bfabric.manager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.entity.Booking;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.util.DataTableHelper;
import org.bfabric.util.FileHelper;
import org.primefaces.component.datatable.DataTable;

@MeasureCalls
@Named
@ViewScoped
public class BookingExportManager extends AbstractManager {

    private static final long serialVersionUID = 1;

    private static final String TAB_DELIMITER = "########";

    @Inject
    private DataTableHelper dataTableHelper;

    private Set<Booking> selectedBookings = new HashSet<>();

    private Map<Booking, Boolean> selectedBookingsMap = new HashMap<>();

    public void checkAll(boolean check) {
        DataTable dataTable = dataTableHelper.getDataTableByTableClientId();
        if (dataTable != null) {
            List<Booking> bookings = dataTableHelper.getDataTableValues(dataTable);
            for (Booking booking : bookings) {
                if (booking.isCSVExportableForETHIS()) {
                    selectBooking(booking, check);
                }
            }
        }
    }

    private void clearSelectedBookings() {
        getSelectedBookings().clear();
        getSelectedBookingsMap().clear();
    }

    public void exportBookings() {
        StringBuilder csv = new StringBuilder();
        for (Booking booking : getSelectedBookings()) {
            csv.append(booking.generateCSVForETHIS(booking.generateExportedBookingFileName(".pdf")));
        }
        FileHelper.download("exported-bookings-" + Constants.DATETIME_FORMATTER.format(LocalDateTime.now()) + ".txt", csv.toString());
        clearSelectedBookings();
        DataTable dataTable = dataTableHelper.getDataTableByTableClientId();
        if (dataTable != null) {
            dataTableHelper.updateColumn(dataTable.getClientId(), Constants.SELECT_CHECK_BOX, false);
        }
    }

    public Set<Booking> getSelectedBookings() {
        return selectedBookings;
    }

    public Map<Booking, Boolean> getSelectedBookingsMap() {
        return selectedBookingsMap;
    }

    public String getTabs() {
        // Important: Pop-ups need to allowed in the browser settings for the target site.
        StringBuilder tabs = new StringBuilder("\"");
        for (Booking booking : getSelectedBookings()) {
            tabs.append(TAB_DELIMITER).append(booking.getExportPDFLink());
        }
        return tabs.append("\"").toString();
    }

    public void selectBooking(Booking booking, boolean isSelect) {
        if (isSelect) {
            getSelectedBookings().add(booking);
            getSelectedBookingsMap().put(booking, Boolean.TRUE);
        } else {
            getSelectedBookings().remove(booking);
            getSelectedBookingsMap().remove(booking);
        }
    }

    public void setSelectedBookings(Set<Booking> selectedBookings) {
        this.selectedBookings = selectedBookings;
    }

    public void setSelectedBookingsMap(Map<Booking, Boolean> selectedBookingsMap) {
        this.selectedBookingsMap = selectedBookingsMap;
    }
}
