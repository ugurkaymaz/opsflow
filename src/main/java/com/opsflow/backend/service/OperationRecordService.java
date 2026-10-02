package com.opsflow.backend.service;

import com.opsflow.backend.dto.OperationSummary;
import com.opsflow.backend.entity.OperationHistory;
import com.opsflow.backend.entity.OperationRecord;
import com.opsflow.backend.repository.OperationHistoryRepository;
import com.opsflow.backend.repository.OperationRecordRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OperationRecordService {

    private final OperationRecordRepository repository;
    private final OperationHistoryRepository historyRepository;

    public OperationRecordService(
            OperationRecordRepository repository,
            OperationHistoryRepository historyRepository) {

        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public OperationRecord create(OperationRecord record) {

        OperationRecord savedRecord =
                repository.save(record);

        saveHistory(
                savedRecord.getId(),
                "CREATED",
                null,
                savedRecord.getStatus(),
                savedRecord.getTitle()
        );

        return savedRecord;
    }

    public List<OperationRecord> getAll() {
        return repository.findAll();
    }

    public Page<OperationRecord> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<OperationRecord> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Optional<OperationRecord> update(
            Long id,
            OperationRecord updatedRecord) {

        return repository.findById(id)
                .map(existingRecord -> {

                    String oldStatus =
                            existingRecord.getStatus();

                    existingRecord.setTitle(
                            updatedRecord.getTitle()
                    );

                    existingRecord.setDescription(
                            updatedRecord.getDescription()
                    );

                    existingRecord.setOperationDate(
                            updatedRecord.getOperationDate()
                    );

                    existingRecord.setOperationTime(
                            updatedRecord.getOperationTime()
                    );

                    existingRecord.setStatus(
                            updatedRecord.getStatus()
                    );

                    OperationRecord savedRecord =
                            repository.save(existingRecord);

                    String eventType;

                    if (!Objects.equals(
                            oldStatus,
                            savedRecord.getStatus())) {

                        eventType = "STATUS_CHANGED";

                    } else {

                        eventType = "UPDATED";
                    }

                    saveHistory(
                            savedRecord.getId(),
                            eventType,
                            oldStatus,
                            savedRecord.getStatus(),
                            savedRecord.getTitle()
                    );

                    return savedRecord;
                });
    }

    @Transactional
    public void delete(Long id) {

        repository.findById(id)
                .ifPresent(record -> {

                    saveHistory(
                            record.getId(),
                            "DELETED",
                            record.getStatus(),
                            null,
                            record.getTitle()
                    );

                    repository.delete(record);
                });
    }

    private void saveHistory(
            Long operationId,
            String eventType,
            String oldStatus,
            String newStatus,
            String operationTitle) {

        OperationHistory history =
                new OperationHistory(
                        operationId,
                        eventType,
                        oldStatus,
                        newStatus,
                        operationTitle,
                        LocalDateTime.now()
                );

        historyRepository.save(history);
    }

    public Page<OperationRecord> search(
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
            String status,
            Pageable pageable) {

        // Both dates must be provided together
        if ((startDate == null) != (endDate == null)) {

            throw new IllegalArgumentException(
                    "startDate and endDate must be provided together"
            );
        }

        // Both times must be provided together
        if ((startTime == null) != (endTime == null)) {

            throw new IllegalArgumentException(
                    "startTime and endTime must be provided together"
            );
        }

        // Validate date range
        if (startDate != null
                && startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "startDate must not be after endDate"
            );
        }

        // Validate time range
        if (startTime != null
                && startTime.isAfter(endTime)) {

            throw new IllegalArgumentException(
                    "startTime must not be after endTime"
            );
        }

        // Normalize and validate status
        if (status != null && !status.isBlank()) {

            status = status.toUpperCase();

            List<String> allowedStatuses =
                    List.of(
                            "PLANNED",
                            "IN_PROGRESS",
                            "COMPLETED"
                    );

            if (!allowedStatuses.contains(status)) {

                throw new IllegalArgumentException(
                        "Invalid status: " + status
                );
            }

        } else {

            status = null;
        }

        // Status + Date + Time
        if (status != null
                && startDate != null
                && startTime != null) {

            return repository
                    .findByStatusAndOperationDateBetweenAndOperationTimeBetween(
                            status,
                            startDate,
                            endDate,
                            startTime,
                            endTime,
                            pageable
                    );
        }

        // Status + Date
        if (status != null
                && startDate != null) {

            return repository
                    .findByStatusAndOperationDateBetween(
                            status,
                            startDate,
                            endDate,
                            pageable
                    );
        }

        // Status + Time
        if (status != null
                && startTime != null) {

            return repository
                    .findByStatusAndOperationTimeBetween(
                            status,
                            startTime,
                            endTime,
                            pageable
                    );
        }

        // Status only
        if (status != null) {

            return repository.findByStatus(
                    status,
                    pageable
            );
        }

        // Date + Time
        if (startDate != null
                && startTime != null) {

            return repository
                    .findByOperationDateBetweenAndOperationTimeBetween(
                            startDate,
                            endDate,
                            startTime,
                            endTime,
                            pageable
                    );
        }

        // Date only
        if (startDate != null) {

            return repository
                    .findByOperationDateBetween(
                            startDate,
                            endDate,
                            pageable
                    );
        }

        // Time only
        if (startTime != null) {

            return repository
                    .findByOperationTimeBetween(
                            startTime,
                            endTime,
                            pageable
                    );
        }

        // No filters
        return repository.findAll(pageable);
    }

    // General summary
    public OperationSummary getSummary() {

        long total =
                repository.count();

        long planned =
                repository.countByStatus(
                        "PLANNED"
                );

        long inProgress =
                repository.countByStatus(
                        "IN_PROGRESS"
                );

        long completed =
                repository.countByStatus(
                        "COMPLETED"
                );

        return new OperationSummary(
                total,
                planned,
                inProgress,
                completed
        );
    }

    // Date filtered summary
    public OperationSummary getSummary(
            LocalDate startDate,
            LocalDate endDate) {

        // No dates -> return general summary
        if (startDate == null
                && endDate == null) {

            return getSummary();
        }

        // Only one date supplied
        if (startDate == null
                || endDate == null) {

            throw new IllegalArgumentException(
                    "startDate and endDate must be provided together"
            );
        }

        // Invalid date order
        if (startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "startDate must not be after endDate"
            );
        }

        long total =
                repository
                        .countByOperationDateBetween(
                                startDate,
                                endDate
                        );

        long planned =
                repository
                        .countByStatusAndOperationDateBetween(
                                "PLANNED",
                                startDate,
                                endDate
                        );

        long inProgress =
                repository
                        .countByStatusAndOperationDateBetween(
                                "IN_PROGRESS",
                                startDate,
                                endDate
                        );

        long completed =
                repository
                        .countByStatusAndOperationDateBetween(
                                "COMPLETED",
                                startDate,
                                endDate
                        );

        return new OperationSummary(
                total,
                planned,
                inProgress,
                completed
        );
    }
}