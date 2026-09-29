package com.opsflow.backend;

import com.opsflow.backend.entity.OperationRecord;
import com.opsflow.backend.repository.OperationRecordRepository;
import com.opsflow.backend.service.OperationRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationRecordServiceTest {

    private OperationRecordRepository repository;
    private OperationRecordService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(OperationRecordRepository.class);
        service = new OperationRecordService(repository);
    }

    @Test
    void shouldCreateOperation() {

        OperationRecord record = new OperationRecord();
        record.setTitle("System Maintenance");

        when(repository.save(record)).thenReturn(record);

        OperationRecord result = service.create(record);

        verify(repository).save(record);

        org.junit.jupiter.api.Assertions.assertEquals(
                "System Maintenance",
                result.getTitle()
        );
    }

    @Test
    void shouldGetOperationById() {

        OperationRecord record = new OperationRecord();
        record.setId(1L);
        record.setTitle("System Maintenance");

        when(repository.findById(1L))
                .thenReturn(java.util.Optional.of(record));

        java.util.Optional<OperationRecord> result =
                service.getById(1L);

        verify(repository).findById(1L);

        org.junit.jupiter.api.Assertions.assertTrue(
                result.isPresent()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                1L,
                result.get().getId()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "System Maintenance",
                result.get().getTitle()
        );
    }

    @Test
    void shouldReturnEmptyWhenOperationNotFound() {

        when(repository.findById(9999L))
                .thenReturn(java.util.Optional.empty());

        java.util.Optional<OperationRecord> result =
                service.getById(9999L);

        verify(repository).findById(9999L);

        org.junit.jupiter.api.Assertions.assertTrue(
                result.isEmpty()
        );
    }

    @Test
    void shouldUpdateOperation() {

        OperationRecord existingRecord = new OperationRecord();
        existingRecord.setId(1L);
        existingRecord.setTitle("Old Title");
        existingRecord.setDescription("Old Description");
        existingRecord.setStatus("PLANNED");

        OperationRecord updatedRecord = new OperationRecord();
        updatedRecord.setTitle("Updated Title");
        updatedRecord.setDescription("Updated Description");
        updatedRecord.setOperationDate(
                java.time.LocalDate.of(2026, 9, 25)
        );
        updatedRecord.setOperationTime(
                java.time.LocalTime.of(14, 30)
        );
        updatedRecord.setStatus("COMPLETED");

        when(repository.findById(1L))
                .thenReturn(java.util.Optional.of(existingRecord));

        when(repository.save(existingRecord))
                .thenReturn(existingRecord);

        java.util.Optional<OperationRecord> result =
                service.update(1L, updatedRecord);

        org.junit.jupiter.api.Assertions.assertTrue(
                result.isPresent()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Updated Title",
                result.get().getTitle()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "Updated Description",
                result.get().getDescription()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "COMPLETED",
                result.get().getStatus()
        );

        verify(repository).findById(1L);
        verify(repository).save(existingRecord);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingNonExistingOperation() {

        OperationRecord updatedRecord = new OperationRecord();
        updatedRecord.setTitle("Updated Title");
        updatedRecord.setDescription("Updated Description");
        updatedRecord.setOperationDate(
                java.time.LocalDate.of(2026, 9, 25)
        );
        updatedRecord.setOperationTime(
                java.time.LocalTime.of(14, 30)
        );
        updatedRecord.setStatus("COMPLETED");

        when(repository.findById(9999L))
                .thenReturn(java.util.Optional.empty());

        java.util.Optional<OperationRecord> result =
                service.update(9999L, updatedRecord);

        org.junit.jupiter.api.Assertions.assertTrue(
                result.isEmpty()
        );

        verify(repository).findById(9999L);

        org.mockito.Mockito.verify(
                repository,
                org.mockito.Mockito.never()
        ).save(org.mockito.ArgumentMatchers.any(OperationRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenEndDateIsMissing() {

        java.time.LocalDate startDate =
                java.time.LocalDate.of(2026, 9, 20);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                startDate,
                                null,
                                null,
                                null,
                                pageable
                        )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "startDate and endDate must be provided together",
                exception.getMessage()
        );

        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsMissing() {

        java.time.LocalTime startTime =
                java.time.LocalTime.of(10, 0);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                null,
                                null,
                                startTime,
                                null,
                                pageable
                        )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "startTime and endTime must be provided together",
                exception.getMessage()
        );

        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {

        java.time.LocalDate startDate =
                java.time.LocalDate.of(2026, 9, 25);

        java.time.LocalDate endDate =
                java.time.LocalDate.of(2026, 9, 20);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                startDate,
                                endDate,
                                null,
                                null,
                                pageable
                        )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "startDate must not be after endDate",
                exception.getMessage()
        );

        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsAfterEndTime() {

        java.time.LocalTime startTime =
                java.time.LocalTime.of(18, 0);

        java.time.LocalTime endTime =
                java.time.LocalTime.of(10, 0);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                null,
                                null,
                                startTime,
                                endTime,
                                pageable
                        )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "startTime must not be after endTime",
                exception.getMessage()
        );

        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    @Test
    void shouldSearchByDateRange() {

        java.time.LocalDate startDate =
                java.time.LocalDate.of(2026, 9, 20);

        java.time.LocalDate endDate =
                java.time.LocalDate.of(2026, 9, 25);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<OperationRecord> expectedPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of()
                );

        when(repository.findByOperationDateBetween(
                startDate,
                endDate,
                pageable
        )).thenReturn(expectedPage);

        org.springframework.data.domain.Page<OperationRecord> result =
                service.search(
                        startDate,
                        endDate,
                        null,
                        null,
                        pageable
                );

        org.junit.jupiter.api.Assertions.assertSame(
                expectedPage,
                result
        );

        verify(repository).findByOperationDateBetween(
                startDate,
                endDate,
                pageable
        );
    }

    @Test
    void shouldSearchByTimeRange() {

        java.time.LocalTime startTime =
                java.time.LocalTime.of(9, 0);

        java.time.LocalTime endTime =
                java.time.LocalTime.of(17, 0);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<OperationRecord> expectedPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of()
                );

        when(repository.findByOperationTimeBetween(
                startTime,
                endTime,
                pageable
        )).thenReturn(expectedPage);

        org.springframework.data.domain.Page<OperationRecord> result =
                service.search(
                        null,
                        null,
                        startTime,
                        endTime,
                        pageable
                );

        org.junit.jupiter.api.Assertions.assertSame(
                expectedPage,
                result
        );

        verify(repository).findByOperationTimeBetween(
                startTime,
                endTime,
                pageable
        );
    }
    @Test
    void shouldSearchByDateAndTimeRange() {

        java.time.LocalDate startDate =
                java.time.LocalDate.of(2026, 9, 20);

        java.time.LocalDate endDate =
                java.time.LocalDate.of(2026, 9, 25);

        java.time.LocalTime startTime =
                java.time.LocalTime.of(9, 0);

        java.time.LocalTime endTime =
                java.time.LocalTime.of(17, 0);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<OperationRecord> expectedPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of()
                );

        when(repository.findByOperationDateBetweenAndOperationTimeBetween(
                startDate,
                endDate,
                startTime,
                endTime,
                pageable
        )).thenReturn(expectedPage);

        org.springframework.data.domain.Page<OperationRecord> result =
                service.search(
                        startDate,
                        endDate,
                        startTime,
                        endTime,
                        pageable
                );

        org.junit.jupiter.api.Assertions.assertSame(
                expectedPage,
                result
        );

        verify(repository)
                .findByOperationDateBetweenAndOperationTimeBetween(
                        startDate,
                        endDate,
                        startTime,
                        endTime,
                        pageable
                );
    }
    @Test
    void shouldSearchAllWhenNoFiltersProvided() {

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.Page<OperationRecord> expectedPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of()
                );

        when(repository.findAll(pageable))
                .thenReturn(expectedPage);

        org.springframework.data.domain.Page<OperationRecord> result =
                service.search(
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        org.junit.jupiter.api.Assertions.assertSame(
                expectedPage,
                result
        );

        verify(repository).findAll(pageable);
    }
    @Test
    void shouldReturnGeneralSummary() {

        when(repository.count())
                .thenReturn(10L);

        when(repository.countByStatus("PLANNED"))
                .thenReturn(4L);

        when(repository.countByStatus("IN_PROGRESS"))
                .thenReturn(2L);

        when(repository.countByStatus("COMPLETED"))
                .thenReturn(4L);

        com.opsflow.backend.dto.OperationSummary result =
                service.getSummary();

        org.junit.jupiter.api.Assertions.assertEquals(
                10L,
                result.getTotal()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                4L,
                result.getPlanned()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                2L,
                result.getInProgress()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                4L,
                result.getCompleted()
        );

        verify(repository).count();
        verify(repository).countByStatus("PLANNED");
        verify(repository).countByStatus("IN_PROGRESS");
        verify(repository).countByStatus("COMPLETED");
    }

    @Test
    void shouldReturnSummaryForDateRange() {

        java.time.LocalDate startDate =
                java.time.LocalDate.of(2026, 9, 20);

        java.time.LocalDate endDate =
                java.time.LocalDate.of(2026, 9, 25);

        when(repository.countByOperationDateBetween(
                startDate,
                endDate
        )).thenReturn(6L);

        when(repository.countByStatusAndOperationDateBetween(
                "PLANNED",
                startDate,
                endDate
        )).thenReturn(3L);

        when(repository.countByStatusAndOperationDateBetween(
                "IN_PROGRESS",
                startDate,
                endDate
        )).thenReturn(1L);

        when(repository.countByStatusAndOperationDateBetween(
                "COMPLETED",
                startDate,
                endDate
        )).thenReturn(2L);

        com.opsflow.backend.dto.OperationSummary result =
                service.getSummary(
                        startDate,
                        endDate
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                6L,
                result.getTotal()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                3L,
                result.getPlanned()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                1L,
                result.getInProgress()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                2L,
                result.getCompleted()
        );

        verify(repository).countByOperationDateBetween(
                startDate,
                endDate
        );

        verify(repository)
                .countByStatusAndOperationDateBetween(
                        "PLANNED",
                        startDate,
                        endDate
                );

        verify(repository)
                .countByStatusAndOperationDateBetween(
                        "IN_PROGRESS",
                        startDate,
                        endDate
                );

        verify(repository)
                .countByStatusAndOperationDateBetween(
                        "COMPLETED",
                        startDate,
                        endDate
                );
    }

    @Test
    void shouldThrowExceptionWhenSummaryDateIsIncomplete() {

        java.time.LocalDate startDate =
                java.time.LocalDate.of(2026, 9, 20);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getSummary(
                                startDate,
                                null
                        )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "startDate and endDate must be provided together",
                exception.getMessage()
        );

        org.mockito.Mockito.verifyNoInteractions(
                repository
        );
    }

    @Test
    void shouldThrowExceptionWhenSummaryStartDateIsAfterEndDate() {

        java.time.LocalDate startDate =
                java.time.LocalDate.of(2026, 9, 25);

        java.time.LocalDate endDate =
                java.time.LocalDate.of(2026, 9, 20);

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getSummary(
                                startDate,
                                endDate
                        )
                );

        org.junit.jupiter.api.Assertions.assertEquals(
                "startDate must not be after endDate",
                exception.getMessage()
        );

        org.mockito.Mockito.verifyNoInteractions(
                repository
        );
    }
}