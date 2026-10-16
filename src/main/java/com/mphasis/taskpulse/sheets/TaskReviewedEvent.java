// sheets/TaskReviewedEvent.java
package com.mphasis.taskpulse.sheets;

import com.mphasis.taskpulse.report.ExcelRow;

import java.util.List;

/** Published after every manager decision. approvedRows is empty for a rejection. */
public record TaskReviewedEvent(ReviewRow review, List<ExcelRow> approvedRows) {
}