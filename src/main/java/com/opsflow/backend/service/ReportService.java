package com.opsflow.backend.service;

import com.opsflow.backend.dto.ReportSummary;
import com.opsflow.backend.dto.UserActivityReport;
import com.opsflow.backend.repository.OperationHistoryRepository;
import com.opsflow.backend.repository.OperationRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

        double completionRate =
                calculateCompletionRate(
                        completed,
                        totalOperations
                );

        // All-time audit statistics
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

        // All-time user activity statistics
        long trackedUserActions =
                historyRepository.countByUserIdIsNotNull();

        long userActions =
                historyRepository.countByUserRole(
                        "USER"
                );

        long adminActions =
                historyRepository.countByUserRole(
                        "ADMIN"
                );

        long unattributedActions =
                historyRepository.countByUserIdIsNull();

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
                deletedEvents,
                trackedUserActions,
                userActions,
                adminActions,
                unattributedActions
        );
    }

    public ReportSummary getSummary(
            LocalDate startDate,
            LocalDate endDate) {

        validateDateRange(
                startDate,
                endDate
        );

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate
                        .plusDays(1)
                        .atStartOfDay();

        // Current operation statistics remain current-state statistics
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

        double completionRate =
                calculateCompletionRate(
                        completed,
                        totalOperations
                );

        // Audit statistics for the selected period
        long totalAuditEvents =
                historyRepository
                        .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                startDateTime,
                                endDateTime
                        );

        long createdEvents =
                historyRepository
                        .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "CREATED",
                                startDateTime,
                                endDateTime
                        );

        long updatedEvents =
                historyRepository
                        .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "UPDATED",
                                startDateTime,
                                endDateTime
                        );

        long statusChangedEvents =
                historyRepository
                        .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "STATUS_CHANGED",
                                startDateTime,
                                endDateTime
                        );

        long deletedEvents =
                historyRepository
                        .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "DELETED",
                                startDateTime,
                                endDateTime
                        );

        // User activity statistics for the selected period
        long trackedUserActions =
                historyRepository
                        .countByUserIdIsNotNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                startDateTime,
                                endDateTime
                        );

        long userActions =
                historyRepository
                        .countByUserRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "USER",
                                startDateTime,
                                endDateTime
                        );

        long adminActions =
                historyRepository
                        .countByUserRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "ADMIN",
                                startDateTime,
                                endDateTime
                        );

        long unattributedActions =
                historyRepository
                        .countByUserIdIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                startDateTime,
                                endDateTime
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
                deletedEvents,
                trackedUserActions,
                userActions,
                adminActions,
                unattributedActions
        );
    }

    public List<UserActivityReport> getUserActivityReport() {

        return historyRepository
                .getUserActivityReport();
    }

    public List<UserActivityReport> getUserActivityReport(
            LocalDate startDate,
            LocalDate endDate) {

        validateDateRange(
                startDate,
                endDate
        );

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate
                        .plusDays(1)
                        .atStartOfDay();

        return historyRepository
                .getUserActivityReportByDateRange(
                        startDateTime,
                        endDateTime
                );
    }

    private double calculateCompletionRate(
            long completed,
            long totalOperations) {

        if (totalOperations == 0) {
            return 0.0;
        }

        return ((double) completed / totalOperations)
                * 100.0;
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required"
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date"
            );
        }
    }
}