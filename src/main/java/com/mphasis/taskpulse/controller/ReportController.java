// controller/ReportController.java
package com.mphasis.taskpulse.controller;

import com.mphasis.taskpulse.report.ProjectSheet;
import com.mphasis.taskpulse.report.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/manager/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Live data for the Reports page: approved updates grouped per project. */
    @GetMapping("/live")
    public List<ProjectSheet> live() {
        return reportService.liveReport();
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcel() throws IOException {
        byte[] file = reportService.buildManagerReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"approved-updates.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}