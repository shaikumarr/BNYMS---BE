// report/ExcelRow.java
package com.mphasis.taskpulse.report;

public record ExcelRow(
        String date,
        String employee,
        String project,
        Integer week,
        String task,
        String update,
        Double hours,
        Integer percentAdded,
        String approvedBy,
        String approvedOn) {
}