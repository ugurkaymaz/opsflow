package com.opsflow.backend;

import com.opsflow.backend.entity.OperationRecord;
import com.opsflow.backend.repository.OperationRecordRepository;
import com.opsflow.backend.service.OperationRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

        assertEquals(
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

        assertTrue(result.isPresent());

        assertEquals(
                1L,
                result.get().getId()
        );

        assertEquals(
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

        assertTrue(result.isEmpty());
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
                LocalDate.of(2026, 9, 25)
        );
        updatedRecord.setOperationTime(
                LocalTime.of(14, 30)
        );
        updatedRecord.setStatus("COMPLETED");

        when(repository.findById(1L))
                .thenReturn(java.util.Optional.of(existingRecord));

        when(repository.save(existingRecord))
                .thenReturn(existingRecord);

        java.util.Optional<OperationRecord> result =
                service.update(1L, updatedRecord);

        assertTrue(result.isPresent());

        assertEquals(
                "Updated Title",
                result.get().getTitle()
        );

        assertEquals(
                "Updated Description",
                result.get().getDescription()
        );

        assertEquals(
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
                LocalDate.of(2026, 9, 25)
        );
        updatedRecord.setOperationTime(
                LocalTime.of(14, 30)
        );
        updatedRecord.setStatus("COMPLETED");

        when(repository.findById(9999L))
                .thenReturn(java.util.Optional.empty());

        java.util.Optional<OperationRecord> result =
                service.update(9999L, updatedRecord);

        assertTrue(result.isEmpty());

        verify(repository).findById(9999L);

        Mockito.verify(
                repository,
                Mockito.never()
        ).save(Mockito.any(OperationRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenEndDateIsMissing() {

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        Pageable pageable =
                PageRequest.of(0, 10);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                startDate,
                                null,
                                null,
                                null,
                                null,
                                pageable
                        )
                );

        assertEquals(
                "startDate and endDate must be provided together",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsMissing() {

        LocalTime startTime =
                LocalTime.of(10, 0);

        Pageable pageable =
                PageRequest.of(0, 10);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                null,
                                null,
                                startTime,
                                null,
                                null,
                                pageable
                        )
                );

        assertEquals(
                "startTime and endTime must be provided together",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {

        LocalDate startDate =
                LocalDate.of(2026, 9, 25);

        LocalDate endDate =
                LocalDate.of(2026, 9, 20);

        Pageable pageable =
                PageRequest.of(0, 10);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                startDate,
                                endDate,
                                null,
                                null,
                                null,
                                pageable
                        )
                );

        assertEquals(
                "startDate must not be after endDate",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsAfterEndTime() {

        LocalTime startTime =
                LocalTime.of(18, 0);

        LocalTime endTime =
                LocalTime.of(10, 0);

        Pageable pageable =
                PageRequest.of(0, 10);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                null,
                                null,
                                startTime,
                                endTime,
                                null,
                                pageable
                        )
                );

        assertEquals(
                "startTime must not be after endTime",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldSearchByDateRange() {

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        LocalDate endDate =
                LocalDate.of(2026, 9, 25);

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository.findByOperationDateBetween(
                startDate,
                endDate,
                pageable
        )).thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        startDate,
                        endDate,
                        null,
                        null,
                        null,
                        pageable
                );

        assertSame(
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

        LocalTime startTime =
                LocalTime.of(9, 0);

        LocalTime endTime =
                LocalTime.of(17, 0);

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository.findByOperationTimeBetween(
                startTime,
                endTime,
                pageable
        )).thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        null,
                        null,
                        startTime,
                        endTime,
                        null,
                        pageable
                );

        assertSame(
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

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        LocalDate endDate =
                LocalDate.of(2026, 9, 25);

        LocalTime startTime =
                LocalTime.of(9, 0);

        LocalTime endTime =
                LocalTime.of(17, 0);

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository.findByOperationDateBetweenAndOperationTimeBetween(
                startDate,
                endDate,
                startTime,
                endTime,
                pageable
        )).thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        startDate,
                        endDate,
                        startTime,
                        endTime,
                        null,
                        pageable
                );

        assertSame(
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

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository.findAll(pageable))
                .thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        null,
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        assertSame(
                expectedPage,
                result
        );

        verify(repository).findAll(pageable);
    }

    @Test
    void shouldSearchByStatus() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository.findByStatus(
                "COMPLETED",
                pageable
        )).thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        null,
                        null,
                        null,
                        null,
                        "COMPLETED",
                        pageable
                );

        assertSame(expectedPage, result);

        verify(repository).findByStatus(
                "COMPLETED",
                pageable
        );
    }

    @Test
    void shouldNormalizeStatusToUpperCase() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository.findByStatus(
                "COMPLETED",
                pageable
        )).thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        null,
                        null,
                        null,
                        null,
                        "completed",
                        pageable
                );

        assertSame(expectedPage, result);

        verify(repository).findByStatus(
                "COMPLETED",
                pageable
        );
    }

    @Test
    void shouldThrowExceptionWhenStatusIsInvalid() {

        Pageable pageable =
                PageRequest.of(0, 10);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.search(
                                null,
                                null,
                                null,
                                null,
                                "INVALID",
                                pageable
                        )
                );

        assertEquals(
                "Invalid status: INVALID",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldSearchByStatusAndDateRange() {

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        LocalDate endDate =
                LocalDate.of(2026, 9, 25);

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository.findByStatusAndOperationDateBetween(
                "PLANNED",
                startDate,
                endDate,
                pageable
        )).thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        startDate,
                        endDate,
                        null,
                        null,
                        "PLANNED",
                        pageable
                );

        assertSame(expectedPage, result);

        verify(repository)
                .findByStatusAndOperationDateBetween(
                        "PLANNED",
                        startDate,
                        endDate,
                        pageable
                );
    }

    @Test
    void shouldSearchByStatusAndTimeRange() {

        LocalTime startTime =
                LocalTime.of(9, 0);

        LocalTime endTime =
                LocalTime.of(17, 0);

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository.findByStatusAndOperationTimeBetween(
                "IN_PROGRESS",
                startTime,
                endTime,
                pageable
        )).thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        null,
                        null,
                        startTime,
                        endTime,
                        "IN_PROGRESS",
                        pageable
                );

        assertSame(expectedPage, result);

        verify(repository)
                .findByStatusAndOperationTimeBetween(
                        "IN_PROGRESS",
                        startTime,
                        endTime,
                        pageable
                );
    }

    @Test
    void shouldSearchByStatusDateAndTimeRange() {

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        LocalDate endDate =
                LocalDate.of(2026, 9, 25);

        LocalTime startTime =
                LocalTime.of(9, 0);

        LocalTime endTime =
                LocalTime.of(17, 0);

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<OperationRecord> expectedPage =
                new PageImpl<>(List.of());

        when(repository
                .findByStatusAndOperationDateBetweenAndOperationTimeBetween(
                        "COMPLETED",
                        startDate,
                        endDate,
                        startTime,
                        endTime,
                        pageable
                )).thenReturn(expectedPage);

        Page<OperationRecord> result =
                service.search(
                        startDate,
                        endDate,
                        startTime,
                        endTime,
                        "COMPLETED",
                        pageable
                );

        assertSame(expectedPage, result);

        verify(repository)
                .findByStatusAndOperationDateBetweenAndOperationTimeBetween(
                        "COMPLETED",
                        startDate,
                        endDate,
                        startTime,
                        endTime,
                        pageable
                );
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

        assertEquals(
                10L,
                result.getTotal()
        );

        assertEquals(
                4L,
                result.getPlanned()
        );

        assertEquals(
                2L,
                result.getInProgress()
        );

        assertEquals(
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

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        LocalDate endDate =
                LocalDate.of(2026, 9, 25);

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

        assertEquals(
                6L,
                result.getTotal()
        );

        assertEquals(
                3L,
                result.getPlanned()
        );

        assertEquals(
                1L,
                result.getInProgress()
        );

        assertEquals(
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

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getSummary(
                                startDate,
                                null
                        )
                );

        assertEquals(
                "startDate and endDate must be provided together",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenSummaryStartDateIsAfterEndDate() {

        LocalDate startDate =
                LocalDate.of(2026, 9, 25);

        LocalDate endDate =
                LocalDate.of(2026, 9, 20);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getSummary(
                                startDate,
                                endDate
                        )
                );

        assertEquals(
                "startDate must not be after endDate",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }
}