package com.opsflow.backend.service;

import com.opsflow.backend.dto.OperationSummary;
import com.opsflow.backend.entity.OperationRecord;
import com.opsflow.backend.repository.OperationRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class OperationRecordService {

    private final OperationRecordRepository repository;

    public OperationRecordService(OperationRecordRepository repository) {
        this.repository = repository;
    }

    public OperationRecord create(OperationRecord record) {
        return repository.save(record);
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

    public Optional<OperationRecord> update(
            Long id,
            OperationRecord updatedRecord) {

        return repository.findById(id)
                .map(existingRecord -> {

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

                    return repository.save(existingRecord);
                });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<OperationRecord> search(
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime,
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
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "startDate must not be after endDate"
            );
        }

        // Validate time range
        if (startTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException(
                    "startTime must not be after endTime"
            );
        }

        // Date + Time filter
        if (startDate != null
                && endDate != null
                && startTime != null
                && endTime != null) {

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
        if (startDate != null && endDate != null) {

            return repository.findByOperationDateBetween(
                    startDate,
                    endDate,
                    pageable
            );
        }

        // Time only
        if (startTime != null && endTime != null) {

            return repository.findByOperationTimeBetween(
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

        long total = repository.count();

        long planned =
                repository.countByStatus("PLANNED");

        long inProgress =
                repository.countByStatus("IN_PROGRESS");

        long completed =
                repository.countByStatus("COMPLETED");

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
        if (startDate == null && endDate == null) {
            return getSummary();
        }

        // Only one date supplied
        if (startDate == null || endDate == null) {
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
                repository.countByOperationDateBetween(
                        startDate,
                        endDate
                );

        long planned =
                repository.countByStatusAndOperationDateBetween(
                        "PLANNED",
                        startDate,
                        endDate
                );

        long inProgress =
                repository.countByStatusAndOperationDateBetween(
                        "IN_PROGRESS",
                        startDate,
                        endDate
                );

        long completed =
                repository.countByStatusAndOperationDateBetween(
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