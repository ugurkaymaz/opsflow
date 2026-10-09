package com.opsflow.backend.repository;

import com.opsflow.backend.dto.UserActivityReport;
import com.opsflow.backend.entity.OperationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationHistoryRepository
        extends JpaRepository<OperationHistory, Long> {

    // Get history for a specific operation
    List<OperationHistory> findByOperationIdOrderByCreatedAtDesc(
            Long operationId
    );

    // Get all history records ordered by newest first
    Page<OperationHistory> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    // Count audit events by event type
    long countByEventType(
            String eventType
    );

    // Count audit events by user role
    long countByUserRole(
            String userRole
    );

    // Count audit events with an identified user
    long countByUserIdIsNotNull();

    // Count legacy audit events without an identified user
    long countByUserIdIsNull();

    // Count audit events within a date-time range
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    // Count audit events by event type within a date-time range
    long countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String eventType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    // Count audit events by user role within a date-time range
    long countByUserRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String userRole,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    // Count identified user actions within a date-time range
    long countByUserIdIsNotNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    // Count unattributed audit actions within a date-time range
    long countByUserIdIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    // Aggregate all-time audit activity by identified application user
    @Query("""
            SELECT new com.opsflow.backend.dto.UserActivityReport(
                u.id,
                u.name,
                u.email,
                CAST(u.role AS string),
                SUM(CASE WHEN h.eventType = 'CREATED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN h.eventType = 'UPDATED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN h.eventType = 'STATUS_CHANGED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN h.eventType = 'DELETED' THEN 1 ELSE 0 END),
                COUNT(h)
            )
            FROM OperationHistory h, AppUser u
            WHERE h.userId = u.id
            GROUP BY
                u.id,
                u.name,
                u.email,
                u.role
            ORDER BY COUNT(h) DESC, u.name ASC
            """)
    List<UserActivityReport> getUserActivityReport();

    // Aggregate audit activity by identified application user
    // within a date-time range
    @Query("""
            SELECT new com.opsflow.backend.dto.UserActivityReport(
                u.id,
                u.name,
                u.email,
                CAST(u.role AS string),
                SUM(CASE WHEN h.eventType = 'CREATED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN h.eventType = 'UPDATED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN h.eventType = 'STATUS_CHANGED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN h.eventType = 'DELETED' THEN 1 ELSE 0 END),
                COUNT(h)
            )
            FROM OperationHistory h, AppUser u
            WHERE h.userId = u.id
              AND h.createdAt >= :startDateTime
              AND h.createdAt < :endDateTime
            GROUP BY
                u.id,
                u.name,
                u.email,
                u.role
            ORDER BY COUNT(h) DESC, u.name ASC
            """)
    List<UserActivityReport> getUserActivityReportByDateRange(
            @Param("startDateTime")
            LocalDateTime startDateTime,

            @Param("endDateTime")
            LocalDateTime endDateTime
    );
}