// report/TaskApprovedListener.java
package com.mphasis.taskpulse.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TaskApprovedListener {

    private static final Logger log = LoggerFactory.getLogger(TaskApprovedListener.class);

    private final ExcelExportService excelExportService;

    public TaskApprovedListener(ExcelExportService excelExportService) {
        this.excelExportService = excelExportService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskApproved(TaskApprovedEvent event) {
        try {
            excelExportService.appendToMasterFile(event.rows());
        } catch (Exception e) {
            // The approval is already saved. Do not fail the manager's request because of Excel.
            log.error("Excel update failed (is the file open in Excel?). "
                    + "Use 'Download Excel report' to rebuild it from the database.", e);
        }
    }
}