package com.opsflow.backend.service;

import com.opsflow.backend.dto.ReportSummary;
import com.opsflow.backend.repository.OperationHistoryRepository;
import com.opsflow.backend.repository.OperationRecordRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final OperationRecordRepository operationRepository;
    private final OperationHistoryRepository historyRepository;

    public ReportService(
            OperationRecordRepository operationRepository,
            OperationHistoryRepository historyRepository) {

        this.operationRepository = operationRepository;
        this.historyRepository = historyRepository;
    }

    public ReportSummary getSummary() {

        // Current operation statistics
        long totalOperations =
                operationRepository.count();

        long planned =
                operationRepository.countByStatus(
                        "PLANNED"
                );

        long inProgress =
                operationRepository.countByStatus(
                        "IN_PROGRESS"
                );

        long completed =
                operationRepository.countByStatus(
                        "COMPLETED"
                );

        // Completion percentage
        double completionRate = 0.0;

        if (totalOperations > 0) {
            completionRate =
                    ((double) completed / totalOperations)
                            * 100.0;
        }

        // Audit statistics
        long totalAuditEvents =
                historyRepository.count();

        long createdEvents =
                historyRepository.countByEventType(
                        "CREATED"
                );

        long updatedEvents =
                historyRepository.countByEventType(
                        "UPDATED"
                );

        long statusChangedEvents =
                historyRepository.countByEventType(
                        "STATUS_CHANGED"
                );

        long deletedEvents =
                historyRepository.countByEventType(
                        "DELETED"
                );

        return new ReportSummary(
                totalOperations,
                planned,
                inProgress,
                completed,
                completionRate,
                totalAuditEvents,
                createdEvents,
                updatedEvents,
                statusChangedEvents,
                deletedEvents
        );
    }
}