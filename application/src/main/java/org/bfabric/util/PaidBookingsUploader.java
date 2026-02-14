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

package org.bfabric.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Booking;
import org.bfabric.forms.MFHelper;
import org.bfabric.manager.FacesMessagesManager;
import org.bfabric.service.BookingService;
import org.primefaces.event.FileUploadEvent;

@Named
@ViewScoped
public class PaidBookingsUploader implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String BOOKING = "Booking";

    private static final String PAID = "Paid";

    private static final DateTimeFormatter[] INPUT_FORMATTERS = new DateTimeFormatter[] {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("M/dd/yyyy"),
        DateTimeFormatter.ofPattern("MM/d/yyyy")
    };

    private final List<String> csvHeaders = new ArrayList<>();

    private final List<Map<String, String>> csvRows = new ArrayList<>();

    private final List<String> headers = new ArrayList<>();

    private final List<Map<String, Object>> rows = new ArrayList<>();

    private String CANCELLATION_DATE;

    private String PAYMENT_DATE;

    private String SAP_NUMBER;

    @Inject
    private BookingService bookingService;

    @Inject
    private FacesMessagesManager facesMessagesManager;

    private String payer;

    private static Reader createBomAwareReader(InputStream in) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(in, 8192);
        byte[] probe = new byte[8192];
        int n = pb.read(probe);
        if (n == -1) {
            return new InputStreamReader(pb, Charset.forName("Windows-1252"));
        }
        Charset charset;
        int unread = n;
        // BOM checks
        if (n >= 3 && (probe[0] & 0xFF) == 0xEF && (probe[1] & 0xFF) == 0xBB && (probe[2] & 0xFF) == 0xBF) {
            charset = StandardCharsets.UTF_8;
            unread = n - 3;
        } else if (n >= 4 && (probe[0] & 0xFF) == 0x00 && (probe[1] & 0xFF) == 0x00 && (probe[2] & 0xFF) == 0xFE && (probe[3] & 0xFF) == 0xFF) {
            charset = Charset.forName("UTF-32BE");
            unread = n - 4;
        } else if (n >= 4 && (probe[0] & 0xFF) == 0xFF && (probe[1] & 0xFF) == 0xFE && (probe[2] & 0xFF) == 0x00 && (probe[3] & 0xFF) == 0x00) {
            charset = Charset.forName("UTF-32LE");
            unread = n - 4;
        } else if (n >= 2 && (probe[0] & 0xFF) == 0xFE && (probe[1] & 0xFF) == 0xFF) {
            charset = StandardCharsets.UTF_16BE;
            unread = n - 2;
        } else if (n >= 2 && (probe[0] & 0xFF) == 0xFF && (probe[1] & 0xFF) == 0xFE) {
            charset = StandardCharsets.UTF_16LE;
            unread = n - 2;
        } else {
            // heuristic: count zero bytes at even versus odd positions to detect UTF-16LE/BE
            int zeroEven = 0, zeroOdd = 0;
            int sample = Math.min(n, 4096);
            for (int i = 0; i < sample; i++) {
                if (probe[i] == 0) {
                    if ((i & 1) == 0)
                        zeroEven++;
                    else
                        zeroOdd++;
                }
            }
            if (zeroOdd > zeroEven && zeroOdd > sample / 8) {
                charset = StandardCharsets.UTF_16LE;
            } else if (zeroEven > zeroOdd && zeroEven > sample / 8) {
                charset = StandardCharsets.UTF_16BE;
            } else {
                // no BOM / no UTF-16 signal — prefer UTF-8 but verify by decoding probe; if probe is not valid UTF-8 fall back to Windows-1252 (single-byte)
                try {
                    java.nio.charset.CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
                    utf8Decoder.decode(java.nio.ByteBuffer.wrap(probe, 0, n));
                    charset = StandardCharsets.UTF_8;
                } catch (java.nio.charset.CharacterCodingException e) {
                    charset = Charset.forName("Windows-1252");
                }
            }
        }
        if (unread > 0) {
            pb.unread(probe, n - unread, unread);
        }
        return new InputStreamReader(pb, charset);
    }

    private static char detectSeparatorOutsideQuotes(String line) {
        int commaCount = 0;
        int semicolonCount = 0;
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                // handle escaped "" by skipping next quote
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (!inQuotes) {
                if (c == ',')
                    commaCount++;
                else if (c == ';')
                    semicolonCount++;
            }
        }
        return (semicolonCount > commaCount) ? ';' : ',';
    }

    private static List<String> parseCsvLine(String line) {
        if (line == null)
            return new ArrayList<>();
        char sep = detectSeparatorOutsideQuotes(line);
        return parseCsvLine(line, sep);
    }

    private static List<String> parseCsvLine(String line, char separator) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '\"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                        sb.append('\"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '\"') {
                    inQuotes = true;
                } else if (c == separator) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }

    private static String parseTextDateToDdMmYyyy(String value) {
        if (value != null) {
            String trimmed = value.trim();
            if (trimmed.isEmpty())
                return null;
            for (DateTimeFormatter f : INPUT_FORMATTERS) {
                try {
                    LocalDate d = LocalDate.parse(trimmed, f);
                    return d.format(Constants.DATE_FORMATTER_EU);
                } catch (DateTimeParseException ignored) {
                }
            }
        }
        return null;
    }

    public void clear() {
        headers.clear();
        rows.clear();
        csvHeaders.clear();
        csvRows.clear();
    }

    private void createRows(List<String> columns) {
        Map<String, String> rowMap = new LinkedHashMap<>();
        for (int i = 0; i < csvHeaders.size(); i++) {
            String value = i < columns.size() ? columns.get(i) : "";
            rowMap.put(csvHeaders.get(i), value);
        }
        csvRows.add(rowMap);
    }

    private String findHeader(List<String> headers, String... candidates) {
        return Arrays.stream(candidates).filter(headers::contains).findFirst().orElse(null);
    }

    public FacesMessagesManager getFacesMessagesManager() {
        return facesMessagesManager;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void handleFileUpload(FileUploadEvent event) {
        clear();
        String fileName = event.getFile().getFileName();
        String lower = fileName == null ? "" : fileName.toLowerCase();
        try {
            if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
                try (InputStream inputStream = event.getFile().getInputStream();
                     Workbook workbook = WorkbookFactory.create(inputStream)) {
                    DataFormatter dataFormatter = new DataFormatter();
                    Sheet sheet = workbook.getSheetAt(0);
                    Iterator<Row> iterator = sheet.rowIterator();
                    if (!iterator.hasNext()) {
                        getFacesMessagesManager().printError("Empty Excel");
                        return;
                    }
                    Row headerRow = iterator.next();
                    csvHeaders.clear();
                    int columnsCount = Math.max(0, headerRow.getLastCellNum());
                    for (int c = 0; c < columnsCount; c++) {
                        Cell cell = headerRow.getCell(c);
                        csvHeaders.add(cell == null ? "" : dataFormatter.formatCellValue(cell));
                    }
                    while (iterator.hasNext()) {
                        Row r = iterator.next();
                        boolean empty = true;
                        List<String> columns = new ArrayList<>();
                        for (int c = 0; c < columnsCount; c++) {
                            Cell cell = r.getCell(c);
                            String v;
                            if (cell == null) {
                                v = "";
                            } else if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                                // convert Excel numeric date -> java.util.Date -> LocalDate -> dd.MM.yyyy
                                Date javaDate = DateUtil.getJavaDate(cell.getNumericCellValue());
                                LocalDate ld = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                v = ld.format(Constants.DATE_FORMATTER_EU);
                            } else {
                                String raw = dataFormatter.formatCellValue(cell);
                                String parsed = parseTextDateToDdMmYyyy(raw);
                                v = parsed != null ? parsed : raw;
                            }
                            if (!v.trim().isEmpty()) {
                                empty = false;
                            }
                            columns.add(v);
                        }
                        if (empty) {
                            continue;
                        }
                        createRows(columns);
                    }
                }
            } else {
                try (BufferedReader br = new BufferedReader(createBomAwareReader(event.getFile().getInputStream()))) {
                    String headerLine = br.readLine();
                    if (headerLine == null) {
                        getFacesMessagesManager().printError("Empty CSV");
                        return;
                    }
                    csvHeaders.addAll(parseCsvLine(headerLine));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) {
                            continue;
                        }
                        List<String> columns = parseCsvLine(line);
                        createRows(columns);
                    }
                }
            }
            headers.add(BOOKING);
            headers.add(PAID);
            headers.addAll(csvHeaders);
            SAP_NUMBER = findHeader(csvHeaders, "Belegnummer", "Belegnr", "DocumentNo");
            PAYMENT_DATE = findHeader(csvHeaders, "Erfassungsdatum", "Bezahlt am", "Paid on");
            CANCELLATION_DATE = findHeader(csvHeaders, "Storniert am", "Cancelled on");
            if (csvHeaders.contains("DocumentNo") || csvHeaders.contains("Belegnr")) {
                payer = "ETH";
            } else if (csvHeaders.contains("Belegnummer")) {
                payer = "UZH";
            }
            for (Map<String, String> row : csvRows) {
                Booking booking = null;
                try {
                    long sapNumber = Long.parseLong(StringHelper.trim(row.get(SAP_NUMBER)));
                    if (sapNumber > 0) {
                        booking = bookingService.findBySapNumber(sapNumber, payer);
                        if (booking == null) {
                            booking = bookingService.findBySapNumberNext(sapNumber, payer);
                        }
                        Map<String, Object> rowMap = new LinkedHashMap<>();
                        rowMap.put(BOOKING, booking);
                        rowMap.put(PAID, booking != null ? booking.getPaid() : null);
                        rowMap.putAll(row);
                        rows.add(rowMap);
                    }
                } catch (Exception ignored) {
                }
            }
            // sort rows so already paid come first
            rows.sort((r1, r2) -> {
                Booking b1 = (Booking) r1.get(BOOKING);
                Booking b2 = (Booking) r2.get(BOOKING);
                boolean p1 = b1 != null && Boolean.TRUE.equals(b1.getPaid());
                boolean p2 = b2 != null && Boolean.TRUE.equals(b2.getPaid());
                if (p1 != p2) {
                    return p1 ? -1 : 1;
                }
                if ((b1 == null) != (b2 == null)) {
                    return (b1 == null) ? 1 : -1;
                }
                return 0;
            });
            getFacesMessagesManager().printWarn(Messages.get("successfullyUploaded") + " " + event.getFile().getFileName());
        } catch (IOException e) {
            getFacesMessagesManager().printError("Upload failed: " + e.getMessage());
        }
    }

    public void saveBookingsAsPaid() {
        int saved = 0;
        int alreadyPaid = 0;
        int notFound = 0;
        int foundNotPaid = 0;
        int canceled = 0;
        Set<Booking> paidBookings = new HashSet<>();
        for (Map<String, Object> row : rows) {
            Booking booking = (Booking) row.get(BOOKING);
            if (booking != null) {
                if (!paidBookings.contains(booking)) {
                    paidBookings.add(booking);
                    if (booking.getPaid() == null || !booking.getPaid()) {
                        if (PAYMENT_DATE != null && row.containsKey(PAYMENT_DATE) && StringHelper.isNotEmpty((String) row.get(PAYMENT_DATE))) {
                            try {
                                booking.setPaymentDate(MFHelper.dateValueOf((String) row.get(PAYMENT_DATE)));
                                booking.setPaid(true);
                                saved += 1;
                            } catch (Exception ignored) {
                                foundNotPaid += 1;
                            }
                        } else {
                            foundNotPaid += 1;
                        }
                        if (CANCELLATION_DATE != null && row.containsKey(CANCELLATION_DATE) && StringHelper.isNotEmpty((String) row.get(CANCELLATION_DATE))) {
                            try {
                                booking.setCancellationDate(MFHelper.dateValueOf((String) row.get(CANCELLATION_DATE)));
                                canceled += 1;
                            } catch (Exception ignored) {
                            }
                        }
                        bookingService.save(booking);
                    }
                } else {
                    alreadyPaid += 1;
                }
            } else {
                notFound += 1;
            }
        }
        getFacesMessagesManager().printWarn("Bookings saved as paid: " + saved + ", already paid: " + alreadyPaid + ", not found: " + notFound + ", found but not paid yet: " + foundNotPaid + ", canceled: " + canceled);
    }
}