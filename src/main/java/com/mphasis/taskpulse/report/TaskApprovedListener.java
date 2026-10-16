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

    private final MasterSheetService masterSheetService;

    public TaskApprovedListener(MasterSheetService masterSheetService) {
        this.masterSheetService = masterSheetService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskApproved(TaskApprovedEvent event) {
        try {
            masterSheetService.refresh(event.managerId());
        } catch (Exception e) {
            // The approval is already saved. Never fail the manager's request because of Excel.
            // The background job will try again within 30 seconds.
            log.error("Excel refresh after approval failed", e);
        }
    }
}