// report/ProjectSheet.java
package com.mphasis.taskpulse.report;

import java.util.List;

/** One project's approved rows. Becomes one Excel tab and one block on the live page. */
public record ProjectSheet(Long projectId, String projectName, List<ExcelRow> rows) {
}