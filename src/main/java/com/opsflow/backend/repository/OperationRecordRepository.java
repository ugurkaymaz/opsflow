package com.opsflow.backend.repository;

import com.opsflow.backend.entity.OperationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface OperationRecordRepository
        extends JpaRepository<OperationRecord, Long> {

    // Count by status
    long countByStatus(String status);

    // Count all operations within a date range
    long countByOperationDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    // Count operations by status within a date range
    long countByStatusAndOperationDateBetween(
            String status,
            LocalDate startDate,
            LocalDate endDate
    );

    // Date + Time filter
    Page<OperationRecord> findByOperationDateBetweenAndOperationTimeBetween(
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
            Pageable pageable
    );

    // Date filter
    Page<OperationRecord> findByOperationDateBetween(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    // Time filter
    Page<OperationRecord> findByOperationTimeBetween(
            LocalTime startTime,
            LocalTime endTime,
            Pageable pageable
    );
    // Status filter
    Page<OperationRecord> findByStatus(
            String status,
            Pageable pageable
    );

    // Status + Date filter
    Page<OperationRecord> findByStatusAndOperationDateBetween(
            String status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    // Status + Time filter
    Page<OperationRecord> findByStatusAndOperationTimeBetween(
            String status,
            LocalTime startTime,
            LocalTime endTime,
            Pageable pageable
    );

    // Status + Date + Time filter
    Page<OperationRecord> findByStatusAndOperationDateBetweenAndOperationTimeBetween(
            String status,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
            Pageable pageable
    );

}