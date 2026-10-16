// sheets/SheetSyncListener.java
package com.mphasis.taskpulse.sheets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SheetSyncListener {

    private static final Logger log = LoggerFactory.getLogger(SheetSyncListener.class);

    private final SheetSyncService sheetSyncService;

    public SheetSyncListener(SheetSyncService sheetSyncService) {
        this.sheetSyncService = sheetSyncService;
    }

    // Runs only AFTER the manager's decision is safely saved in the database
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskReviewed(TaskReviewedEvent event) {
        if (!sheetSyncService.isEnabled()) {
            return;
        }
        try {
            sheetSyncService.recordReview(event.review(), event.approvedRows());
        } catch (Exception e) {
            // The decision is already saved. Never fail the manager's click because of the sheet.
            log.error("Google Sheet update failed. Use 'Sync all to sheet' to repair it.", e);
        }
    }
}