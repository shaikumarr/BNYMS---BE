// report/TaskApprovedEvent.java
package com.mphasis.taskpulse.report;

import java.util.List;

/** Published by ApprovalService when a task is approved. Carries the rows to write. */
public record TaskApprovedEvent(List<ExcelRow> rows) {
}