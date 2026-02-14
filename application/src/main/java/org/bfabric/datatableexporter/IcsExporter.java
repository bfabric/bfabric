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

package org.bfabric.datatableexporter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.faces.context.FacesContext;

import net.fortuna.ical4j.model.Calendar;
import org.bfabric.entity.AbstractEvent;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.export.DataTableExporter;
import org.primefaces.component.export.ExporterOptions;

public class IcsExporter extends DataTableExporter<PrintWriter, ExporterOptions> {

    private final Calendar calendar;

    public IcsExporter(Calendar calendar) {
        super(null, Collections.emptySet(), false);
        this.calendar = calendar;
    }

    @Override
    protected PrintWriter createDocument(FacesContext context) throws IOException {
        return new PrintWriter(new OutputStreamWriter(os(), StandardCharsets.UTF_8));
    }

    @Override
    protected void exportCellValue(FacesContext context, DataTable table, UIColumn col, String text, int index) {
    }

    @Override
    protected void exportRow(FacesContext context, DataTable table, int rowIndex) {
        table.setRowIndex(rowIndex);
        ((AbstractEvent) table.getRowData()).addEventToIcsCalendar(calendar);
    }

    @Override
    public String getContentType() {
        return "text/calendar";
    }

    @Override
    public String getFileExtension() {
        return ".ics";
    }

    @Override
    protected void postExport(FacesContext context) throws IOException {
        document.write(calendar.copy().toString());
        super.postExport(context);
        if (document != null) {
            document.flush();
        }
    }
}