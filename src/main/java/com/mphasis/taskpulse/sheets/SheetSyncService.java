// sheets/SheetSyncService.java
package com.mphasis.taskpulse.sheets;

import com.mphasis.taskpulse.report.ExcelRow;

import java.util.List;

/**
 * The "plug" for a live sheet. GoogleSheetsService is one implementation;
 * a Microsoft Excel Online / SharePoint version can replace it without touching anything else.
 */
public interface SheetSyncService {

    boolean isEnabled();

    String getSheetUrl();

    /** Adds one review row and, if the task was approved, its daily-update rows. */
    void recordReview(ReviewRow review, List<ExcelRow> approvedRows);

    /** Clears both tabs and writes everything again (repairs missing rows). */
    void rebuild(List<ExcelRow> approvedRows, List<ReviewRow> reviews);
}