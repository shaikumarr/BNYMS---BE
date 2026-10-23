// sheets/SheetSyncListener.java
package com.mphasis.taskpulse.sheets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

    // Runs only AFTER the manager's decision is safely saved in the database.
    // HIGHEST_PRECEDENCE = run before the other after-commit listeners, so an error in one of
    // them can never stop the live sheet from being updated.
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskReviewed(TaskReviewedEvent event) {
        log.info("Live sheet: review received ({} for task '{}')",
                event.review().decision(), event.review().task());

        if (!sheetSyncService.isEnabled()) {
            log.warn("Live sheet is DISABLED, so nothing was written. Check SHEETS_ENABLED, "
                    + "SHEETS_SPREADSHEET_ID and SHEETS_CREDENTIALS_PATH in the Run Configuration "
                    + "you started the app with.");
            return;
        }

        try {
            sheetSyncService.recordReview(event.review(), event.approvedRows());
        } catch (Exception e) {
            // The decision is already saved. Never fail the manager's click because of the sheet.
            log.error("Live sheet update FAILED: {}", e.getMessage(), e);
        }
    }
}