// report/MasterSheetService.java
package com.mphasis.taskpulse.report;

import com.mphasis.taskpulse.entity.User;
import com.mphasis.taskpulse.enums.Role;
import com.mphasis.taskpulse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MasterSheetService {

    private static final Logger log = LoggerFactory.getLogger(MasterSheetService.class);

    private final ReportService reportService;
    private final ExcelExportService excelExportService;
    private final UserRepository userRepository;

    // What was last written successfully for each manager (used to skip unchanged files)
    private final Map<Long, List<ProjectSheet>> lastWritten = new ConcurrentHashMap<>();
    // Managers whose file is currently locked (so the warning is logged only once)
    private final Set<Long> warned = new HashSet<>();

    public MasterSheetService(ReportService reportService,
                              ExcelExportService excelExportService,
                              UserRepository userRepository) {
        this.reportService = reportService;
        this.excelExportService = excelExportService;
        this.userRepository = userRepository;
    }

    /** Rebuilds one manager's master file if the database content changed. */
    public synchronized void refresh(Long managerId) {
        List<ProjectSheet> sheets = reportService.sheetsForManager(managerId);
        if (sheets.equals(lastWritten.get(managerId))) {
            return;   // nothing new
        }
        try {
            byte[] content = excelExportService.buildWorkbookBytes(sheets);
            excelExportService.writeAtomically(managerId, content);
            lastWritten.put(managerId, sheets);
            warned.remove(managerId);
            log.info("Master Excel updated: {}", excelExportService.fileFor(managerId));
        } catch (IOException e) {
            // Not stored in lastWritten, so the background job tries again automatically
            if (warned.add(managerId)) {
                log.warn("Could not update {} (is it open in Excel?). Close it and it will update automatically.",
                        excelExportService.fileFor(managerId));
            }
        }
    }

    /** Safety net and retry: checks every manager every 30 seconds (see taskpulse.excel.refresh-ms). */
    @Scheduled(initialDelay = 5000, fixedDelayString = "${taskpulse.excel.refresh-ms:30000}")
    public void syncAll() {
        for (User manager : userRepository.findByRole(Role.MANAGER)) {
            try {
                refresh(manager.getId());
            } catch (Exception e) {
                log.error("Excel sync failed for manager {}", manager.getId(), e);
            }
        }
    }
}