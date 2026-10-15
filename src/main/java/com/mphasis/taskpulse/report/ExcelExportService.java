// report/ExcelExportService.java
package com.mphasis.taskpulse.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

    private static final String SHEET_NAME = "Approved Updates";
    private static final String[] HEADERS = {
            "Date", "Employee", "Project", "Week", "Task",
            "Update", "Hours", "% Added", "Approved By", "Approved On"};
    private static final int[] WIDTHS = {12, 20, 24, 8, 28, 80, 8, 9, 20, 18};   // in characters

    private final Path masterFile;

    public ExcelExportService(@Value("${taskpulse.excel.path:./reports/approved-updates.xlsx}") String path) {
        this.masterFile = Path.of(path).toAbsolutePath();
    }

    /** Adds rows to the end of the master Excel file (creates the file the first time). */
    public synchronized void appendToMasterFile(List<ExcelRow> rows) throws IOException {
        if (rows.isEmpty()) {
            return;
        }
        Files.createDirectories(masterFile.getParent());

        try (Workbook workbook = openOrCreate()) {
            Sheet sheet = getOrCreateSheet(workbook);
            CellStyle wrap = wrapStyle(workbook);

            int nextRow = sheet.getLastRowNum() + 1;
            for (ExcelRow row : rows) {
                writeRow(sheet, nextRow++, row, wrap);
            }

            // Write to a temp file first, then swap it in. A crash mid-write can't corrupt the real file.
            Path temp = masterFile.resolveSibling(masterFile.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(temp)) {
                workbook.write(out);
            }
            Files.move(temp, masterFile, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("Appended {} row(s) to {}", rows.size(), masterFile);
    }

    /** Builds a brand-new workbook in memory (used by the "Download report" button). */
    public byte[] buildWorkbookBytes(List<ExcelRow> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = getOrCreateSheet(workbook);
            CellStyle wrap = wrapStyle(workbook);

            int nextRow = 1;   // row 0 is the header
            for (ExcelRow row : rows) {
                writeRow(sheet, nextRow++, row, wrap);
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ---------------- helpers ----------------

    private Workbook openOrCreate() throws IOException {
        if (Files.exists(masterFile)) {
            try (InputStream in = Files.newInputStream(masterFile)) {
                return new XSSFWorkbook(in);
            }
        }
        return new XSSFWorkbook();
    }

    private Sheet getOrCreateSheet(Workbook workbook) {
        Sheet sheet = workbook.getSheet(SHEET_NAME);
        if (sheet != null) {
            return sheet;
        }
        sheet = workbook.createSheet(SHEET_NAME);

        CellStyle headerStyle = workbook.createCellStyle();
        Font bold = workbook.createFont();
        bold.setBold(true);
        headerStyle.setFont(bold);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, WIDTHS[i] * 256);
        }
        sheet.createFreezePane(0, 1);   // header stays visible while scrolling
        return sheet;
    }

    private CellStyle wrapStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private void writeRow(Sheet sheet, int rowIndex, ExcelRow r, CellStyle wrap) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(r.date());
        row.createCell(1).setCellValue(r.employee());
        row.createCell(2).setCellValue(r.project());
        row.createCell(3).setCellValue(r.week() == null ? 0 : r.week());
        row.createCell(4).setCellValue(r.task());

        Cell update = row.createCell(5);
        update.setCellValue(r.update());
        update.setCellStyle(wrap);

        row.createCell(6).setCellValue(r.hours() == null ? 0 : r.hours());
        row.createCell(7).setCellValue(r.percentAdded() == null ? 0 : r.percentAdded());
        row.createCell(8).setCellValue(r.approvedBy());
        row.createCell(9).setCellValue(r.approvedOn());
    }
}