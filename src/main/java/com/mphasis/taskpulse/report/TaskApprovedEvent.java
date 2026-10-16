// report/TaskApprovedEvent.java
package com.mphasis.taskpulse.report;

/** Published by ApprovalService after a task is approved. Tells the report which manager's file to refresh. */
public record TaskApprovedEvent(Long managerId) {
}