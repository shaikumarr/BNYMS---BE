// sheets/GoogleSheetsService.java
package com.mphasis.taskpulse.sheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.TextFormat;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.mphasis.taskpulse.exception.ExternalServiceException;
import com.mphasis.taskpulse.report.ExcelRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GoogleSheetsService implements SheetSyncService {

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsService.class);

    private static final String UPDATES_TAB = "Approved Updates";
    private static final String REVIEWS_TAB = "Manager Reviews";

    private static final List<Object> UPDATE_HEADERS = List.of(
            "Date", "Employee", "Project", "Week", "Task",
            "Update", "Hours", "% Added", "Approved By", "Approved On");

    private static final List<Object> REVIEW_HEADERS = List.of(
            "Reviewed On", "Project", "Week", "Task",
            "Employee", "Decision", "Manager", "Manager Comment");

    private final boolean enabled;
    private final String spreadsheetId;
    private final String credentialsPath;

    private Sheets sheets;                                  // created on first use
    private final Set<String> readyTabs = new HashSet<>();  // tabs we already checked/created

    public GoogleSheetsService(@Value("${sheets.enabled:false}") boolean enabled,
                               @Value("${sheets.spreadsheet-id:}") String spreadsheetId,
                               @Value("${sheets.credentials-path:}") String credentialsPath) {
        this.enabled = enabled;
        this.spreadsheetId = spreadsheetId == null ? "" : spreadsheetId.trim();
        this.credentialsPath = credentialsPath == null ? "" : credentialsPath.trim();
    }

    @Override
    public boolean isEnabled() {
        return enabled && !spreadsheetId.isBlank() && !credentialsPath.isBlank();
    }

    @Override
    public String getSheetUrl() {
        return "https://docs.google.com/spreadsheets/d/" + spreadsheetId;
    }

    // ---------------- public operations ----------------

    @Override
    public synchronized void recordReview(ReviewRow review, List<ExcelRow> approvedRows) {
        try {
            ensureTab(REVIEWS_TAB, REVIEW_HEADERS);
            append(REVIEWS_TAB, List.of(toRow(review)));

            if (!approvedRows.isEmpty()) {
                ensureTab(UPDATES_TAB, UPDATE_HEADERS);
                append(UPDATES_TAB, approvedRows.stream().map(this::toRow).toList());
            }
            log.info("Google Sheet updated: {} decision for '{}' ({} update row(s))",
                    review.decision(), review.task(), approvedRows.size());
        } catch (IOException e) {
            readyTabs.clear();   // forget cached tabs in case someone renamed or deleted one
            throw new ExternalServiceException("Could not update the Google Sheet: " + e.getMessage());
        }
    }

    @Override
    public synchronized void rebuild(List<ExcelRow> approvedRows, List<ReviewRow> reviews) {
        try {
            ensureTab(REVIEWS_TAB, REVIEW_HEADERS);
            ensureTab(UPDATES_TAB, UPDATE_HEADERS);

            clearDataRows(REVIEWS_TAB);
            clearDataRows(UPDATES_TAB);

            append(REVIEWS_TAB, reviews.stream().map(this::toRow).toList());
            append(UPDATES_TAB, approvedRows.stream().map(this::toRow).toList());
            log.info("Google Sheet rebuilt: {} review row(s), {} update row(s)",
                    reviews.size(), approvedRows.size());
        } catch (IOException e) {
            readyTabs.clear();
            throw new ExternalServiceException("Could not rebuild the Google Sheet: " + e.getMessage());
        }
    }

    // ---------------- Google client ----------------

    private Sheets client() throws IOException {
        if (sheets == null) {
            try (InputStream in = new FileInputStream(credentialsPath)) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                        .createScoped(List.of(SheetsScopes.SPREADSHEETS));
                HttpRequestInitializer adapter = new HttpCredentialsAdapter(credentials);

                // add timeouts so a slow network cannot hang the request forever
                HttpRequestInitializer withTimeouts = request -> {
                    adapter.initialize(request);
                    request.setConnectTimeout(5000);
                    request.setReadTimeout(20000);
                };

                sheets = new Sheets.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        withTimeouts)
                        .setApplicationName("TaskPulse")
                        .build();
            } catch (GeneralSecurityException e) {
                throw new IOException("Could not start the secure connection to Google", e);
            }
        }
        return sheets;
    }

    // ---------------- tabs ----------------

    /** Makes sure the tab exists (creates it with a bold, frozen header row if not). */
    private void ensureTab(String tab, List<Object> headers) throws IOException {
        if (readyTabs.contains(tab)) {
            return;
        }

        Spreadsheet spreadsheet = client().spreadsheets().get(spreadsheetId)
                .setFields("sheets.properties.title")
                .execute();

        boolean exists = spreadsheet.getSheets() != null && spreadsheet.getSheets().stream()
                .anyMatch(s -> tab.equals(s.getProperties().getTitle()));

        if (!exists) {
            BatchUpdateSpreadsheetResponse created = client().spreadsheets()
                    .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                            new Request().setAddSheet(new AddSheetRequest()
                                    .setProperties(new SheetProperties().setTitle(tab))))))
                    .execute();
            Integer sheetId = created.getReplies().get(0).getAddSheet().getProperties().getSheetId();

            // header row
            client().spreadsheets().values()
                    .update(spreadsheetId, range(tab), new ValueRange().setValues(List.of(headers)))
                    .setValueInputOption("RAW")
                    .execute();

            // bold header + freeze it
            client().spreadsheets()
                    .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                            new Request().setRepeatCell(new RepeatCellRequest()
                                    .setRange(new GridRange().setSheetId(sheetId)
                                            .setStartRowIndex(0).setEndRowIndex(1))
                                    .setCell(new CellData().setUserEnteredFormat(
                                            new CellFormat().setTextFormat(new TextFormat().setBold(true))))
                                    .setFields("userEnteredFormat.textFormat.bold")),
                            new Request().setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                                    .setProperties(new SheetProperties().setSheetId(sheetId)
                                            .setGridProperties(new GridProperties().setFrozenRowCount(1)))
                                    .setFields("gridProperties.frozenRowCount")))))
                    .execute();
        }
        readyTabs.add(tab);
    }

    private void clearDataRows(String tab) throws IOException {
        client().spreadsheets().values()
                .clear(spreadsheetId, "'" + tab + "'!A2:Z", new ClearValuesRequest())
                .execute();
    }

    private void append(String tab, List<List<Object>> rows) throws IOException {
        if (rows.isEmpty()) {
            return;
        }
        client().spreadsheets().values()
                .append(spreadsheetId, range(tab), new ValueRange().setValues(rows))
                .setValueInputOption("RAW")          // RAW = stored as typed, never run as a formula
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }

    private String range(String tab) {
        return "'" + tab + "'!A1";
    }

    // ---------------- row conversion ----------------

    private List<Object> toRow(ExcelRow r) {
        return Arrays.<Object>asList(
                v(r.date()), v(r.employee()), v(r.project()), v(r.week()), v(r.task()),
                v(r.update()), v(r.hours()), v(r.percentAdded()), v(r.approvedBy()), v(r.approvedOn()));
    }

    private List<Object> toRow(ReviewRow r) {
        return Arrays.<Object>asList(
                v(r.reviewedOn()), v(r.project()), v(r.week()), v(r.task()),
                v(r.employee()), v(r.decision()), v(r.manager()), v(r.comment()));
    }

    private Object v(Object value) {
        return value == null ? "" : value;
    }
}