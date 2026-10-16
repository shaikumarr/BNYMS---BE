// controller/SheetController.java
package com.mphasis.taskpulse.controller;

import com.mphasis.taskpulse.exception.BadRequestException;
import com.mphasis.taskpulse.sheets.SheetReportService;
import com.mphasis.taskpulse.sheets.SheetSyncService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/manager/sheet")
public class SheetController {

    private final SheetSyncService sheetSyncService;
    private final SheetReportService sheetReportService;

    public SheetController(SheetSyncService sheetSyncService, SheetReportService sheetReportService) {
        this.sheetSyncService = sheetSyncService;
        this.sheetReportService = sheetReportService;
    }

    // Is the live sheet connected, and where is it?
    @GetMapping
    public Map<String, Object> info() {
        boolean enabled = sheetSyncService.isEnabled();
        return Map.of("enabled", enabled, "url", enabled ? sheetSyncService.getSheetUrl() : "");
    }

    // Rewrite both tabs from the database
    @PostMapping("/sync")
    public Map<String, Object> sync() {
        if (!sheetSyncService.isEnabled()) {
            throw new BadRequestException("The live Google Sheet is not configured on the server");
        }
        return Map.of("rowsWritten", sheetReportService.rebuildAll());
    }
}