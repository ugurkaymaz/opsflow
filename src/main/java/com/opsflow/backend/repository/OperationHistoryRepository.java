package com.opsflow.backend.repository;

import com.opsflow.backend.entity.OperationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
    long countByEventType(String eventType);
}