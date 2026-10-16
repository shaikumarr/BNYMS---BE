// report/ExcelExportService.java
package com.mphasis.taskpulse.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ExcelExportService {

    private static final String[] HEADERS = {
            "Date", "Employee", "Week", "Task", "Update",
            "Hours", "% Added", "Approved By", "Approved On"};
    private static final int[] WIDTHS = {12, 20, 8, 28, 80, 8, 9, 20, 18};   // in characters

    private final Path directory;

    public ExcelExportService(@Value("${taskpulse.excel.dir:./reports}") String dir) {
        this.directory = Path.of(dir).toAbsolutePath();
    }

    /** Each manager has their own master file, so managers never see each other's data. */
    public Path fileFor(Long managerId) {
        return directory.resolve("approved-updates-manager-" + managerId + ".xlsx");
    }

    /** Builds a complete workbook in memory: one tab per project. */
    public byte[] buildWorkbookBytes(List<ProjectSheet> projectSheets) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle header = headerStyle(workbook);
            CellStyle wrap = wrapStyle(workbook);
            Set<String> usedNames = new HashSet<>();

            for (ProjectSheet ps : projectSheets) {
                Sheet sheet = workbook.createSheet(uniqueSheetName(ps, usedNames));
                writeHeader(sheet, header);
                int rowIndex = 1;
                for (ExcelRow row : ps.rows()) {
                    writeRow(sheet, rowIndex++, row, wrap);
                }
                sheet.setAutoFilter(new CellRangeAddress(0, ps.rows().size(), 0, HEADERS.length - 1));
            }

            // An Excel file needs at least one tab
            if (projectSheets.isEmpty()) {
                writeHeader(workbook.createSheet("No projects yet"), header);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /** Writes to a temp file first, then swaps it in, so a crash can never leave a half-written file. */
    public synchronized void writeAtomically(Long managerId, byte[] content) throws IOException {
        Files.createDirectories(directory);
        Path target = fileFor(managerId);
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(temp, content);
        try {
            // On Windows this fails while the file is open in Excel
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw e;
        }
    }

    // ---------------- helpers ----------------

    /** Excel tab names: max 31 characters, no : \ / ? * [ ], and must be unique. */
    private String uniqueSheetName(ProjectSheet ps, Set<String> usedNames) {
        String base = WorkbookUtil.createSafeSheetName(ps.projectName());
        String name = base;
        if (usedNames.contains(name.toLowerCase())) {
            String suffix = " (" + ps.projectId() + ")";
            int keep = Math.min(base.length(), 31 - suffix.length());
            name = base.substring(0, keep) + suffix;
        }
        usedNames.add(name.toLowerCase());
        return name;
    }

    private void writeHeader(Sheet sheet, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        header.setHeightInPoints(22);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, WIDTHS[i] * 256);
        }
        sheet.createFreezePane(0, 1);   // header stays visible while scrolling
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        // TaskPulse green (#10B981)
        style.setFillForegroundColor(new XSSFColor(new byte[] {(byte) 0x10, (byte) 0xB9, (byte) 0x81}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle wrapStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private void writeRow(Sheet sheet, int rowIndex, ExcelRow r, CellStyle wrap) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(r.date());
        row.createCell(1).setCellValue(r.employee());
        row.createCell(2).setCellValue(r.week() == null ? 0 : r.week());
        row.createCell(3).setCellValue(r.task());

        Cell update = row.createCell(4);
        update.setCellValue(r.update());
        update.setCellStyle(wrap);

        row.createCell(5).setCellValue(r.hours() == null ? 0 : r.hours());
        row.createCell(6).setCellValue(r.percentAdded() == null ? 0 : r.percentAdded());
        row.createCell(7).setCellValue(r.approvedBy());
        row.createCell(8).setCellValue(r.approvedOn());
    }
}