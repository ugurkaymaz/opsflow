package com.opsflow.backend;

import com.opsflow.backend.dto.ReportSummary;
import com.opsflow.backend.dto.UserActivityReport;
import com.opsflow.backend.repository.OperationHistoryRepository;
import com.opsflow.backend.repository.OperationRecordRepository;
import com.opsflow.backend.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private OperationRecordRepository operationRepository;

    @Mock
    private OperationHistoryRepository historyRepository;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(
                operationRepository,
                historyRepository
        );
    }

    @Test
    void getSummaryShouldReturnAllTimeReportStatistics() {

        when(operationRepository.count())
                .thenReturn(10L);

        when(operationRepository.countByStatus("PLANNED"))
                .thenReturn(3L);

        when(operationRepository.countByStatus("IN_PROGRESS"))
                .thenReturn(2L);

        when(operationRepository.countByStatus("COMPLETED"))
                .thenReturn(5L);

        when(historyRepository.count())
                .thenReturn(20L);

        when(historyRepository.countByEventType("CREATED"))
                .thenReturn(7L);

        when(historyRepository.countByEventType("UPDATED"))
                .thenReturn(3L);

        when(historyRepository.countByEventType("STATUS_CHANGED"))
                .thenReturn(6L);

        when(historyRepository.countByEventType("DELETED"))
                .thenReturn(4L);

        when(historyRepository.countByUserIdIsNotNull())
                .thenReturn(12L);

        when(historyRepository.countByUserRole("USER"))
                .thenReturn(7L);

        when(historyRepository.countByUserRole("ADMIN"))
                .thenReturn(5L);

        when(historyRepository.countByUserIdIsNull())
                .thenReturn(8L);

        ReportSummary result =
                service.getSummary();

        assertEquals(
                10L,
                result.getTotalOperations()
        );

        assertEquals(
                3L,
                result.getPlanned()
        );

        assertEquals(
                2L,
                result.getInProgress()
        );

        assertEquals(
                5L,
                result.getCompleted()
        );

        assertEquals(
                50.0,
                result.getCompletionRate(),
                0.001
        );

        assertEquals(
                20L,
                result.getTotalAuditEvents()
        );

        assertEquals(
                7L,
                result.getCreatedEvents()
        );

        assertEquals(
                3L,
                result.getUpdatedEvents()
        );

        assertEquals(
                6L,
                result.getStatusChangedEvents()
        );

        assertEquals(
                4L,
                result.getDeletedEvents()
        );

        assertEquals(
                12L,
                result.getTrackedUserActions()
        );

        assertEquals(
                7L,
                result.getUserActions()
        );

        assertEquals(
                5L,
                result.getAdminActions()
        );

        assertEquals(
                8L,
                result.getUnattributedActions()
        );
    }

    @Test
    void getSummaryShouldCalculateCompletionRate() {

        when(operationRepository.count())
                .thenReturn(8L);

        when(operationRepository.countByStatus("PLANNED"))
                .thenReturn(2L);

        when(operationRepository.countByStatus("IN_PROGRESS"))
                .thenReturn(3L);

        when(operationRepository.countByStatus("COMPLETED"))
                .thenReturn(3L);

        ReportSummary result =
                service.getSummary();

        assertEquals(
                37.5,
                result.getCompletionRate(),
                0.001
        );
    }

    @Test
    void getSummaryShouldReturnZeroCompletionRateWhenThereAreNoOperations() {

        when(operationRepository.count())
                .thenReturn(0L);

        when(operationRepository.countByStatus("PLANNED"))
                .thenReturn(0L);

        when(operationRepository.countByStatus("IN_PROGRESS"))
                .thenReturn(0L);

        when(operationRepository.countByStatus("COMPLETED"))
                .thenReturn(0L);

        ReportSummary result =
                service.getSummary();

        assertEquals(
                0.0,
                result.getCompletionRate(),
                0.001
        );
    }

    @Test
    void getSummaryWithDateRangeShouldReturnFilteredAuditStatistics() {

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        LocalDateTime startDateTime =
                LocalDateTime.of(
                        2026,
                        10,
                        4,
                        0,
                        0
                );

        LocalDateTime endDateTime =
                LocalDateTime.of(
                        2026,
                        10,
                        5,
                        0,
                        0
                );

        when(operationRepository.count())
                .thenReturn(2L);

        when(operationRepository.countByStatus("PLANNED"))
                .thenReturn(1L);

        when(operationRepository.countByStatus("IN_PROGRESS"))
                .thenReturn(1L);

        when(operationRepository.countByStatus("COMPLETED"))
                .thenReturn(0L);

        when(
                historyRepository
                        .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(10L);

        when(
                historyRepository
                        .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "CREATED",
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(4L);

        when(
                historyRepository
                        .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "UPDATED",
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(0L);

        when(
                historyRepository
                        .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "STATUS_CHANGED",
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(3L);

        when(
                historyRepository
                        .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "DELETED",
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(3L);

        when(
                historyRepository
                        .countByUserIdIsNotNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(4L);

        when(
                historyRepository
                        .countByUserRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "USER",
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(2L);

        when(
                historyRepository
                        .countByUserRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                "ADMIN",
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(2L);

        when(
                historyRepository
                        .countByUserIdIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                startDateTime,
                                endDateTime
                        )
        ).thenReturn(6L);

        ReportSummary result =
                service.getSummary(
                        startDate,
                        endDate
                );

        assertEquals(
                2L,
                result.getTotalOperations()
        );

        assertEquals(
                1L,
                result.getPlanned()
        );

        assertEquals(
                1L,
                result.getInProgress()
        );

        assertEquals(
                0L,
                result.getCompleted()
        );

        assertEquals(
                0.0,
                result.getCompletionRate(),
                0.001
        );

        assertEquals(
                10L,
                result.getTotalAuditEvents()
        );

        assertEquals(
                4L,
                result.getCreatedEvents()
        );

        assertEquals(
                0L,
                result.getUpdatedEvents()
        );

        assertEquals(
                3L,
                result.getStatusChangedEvents()
        );

        assertEquals(
                3L,
                result.getDeletedEvents()
        );

        assertEquals(
                4L,
                result.getTrackedUserActions()
        );

        assertEquals(
                2L,
                result.getUserActions()
        );

        assertEquals(
                2L,
                result.getAdminActions()
        );

        assertEquals(
                6L,
                result.getUnattributedActions()
        );
    }

    @Test
    void getSummaryWithDateRangeShouldUseExclusiveNextDayBoundary() {

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        10,
                        6
                );

        LocalDateTime expectedStart =
                LocalDateTime.of(
                        2026,
                        10,
                        4,
                        0,
                        0
                );

        LocalDateTime expectedEnd =
                LocalDateTime.of(
                        2026,
                        10,
                        7,
                        0,
                        0
                );

        service.getSummary(
                startDate,
                endDate
        );

        verify(historyRepository)
                .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        expectedStart,
                        expectedEnd
                );

        verify(historyRepository)
                .countByEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        "CREATED",
                        expectedStart,
                        expectedEnd
                );
    }

    @Test
    void getUserActivityReportShouldReturnAllTimeUserActivity() {

        UserActivityReport standardUser =
                new UserActivityReport(
                        2L,
                        "Standard User",
                        "user@opsflow.com",
                        "USER",
                        1L,
                        0L,
                        1L,
                        0L,
                        2L
                );

        UserActivityReport adminUser =
                new UserActivityReport(
                        1L,
                        "Test User",
                        "test@opsflow.com",
                        "ADMIN",
                        1L,
                        0L,
                        0L,
                        1L,
                        2L
                );

        List<UserActivityReport> expected =
                List.of(
                        standardUser,
                        adminUser
                );

        when(
                historyRepository
                        .getUserActivityReport()
        ).thenReturn(expected);

        List<UserActivityReport> result =
                service.getUserActivityReport();

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                expected,
                result
        );

        verify(historyRepository)
                .getUserActivityReport();
    }

    @Test
    void getUserActivityReportWithDateRangeShouldUseCorrectDateTimeBoundaries() {

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        LocalDateTime expectedStart =
                LocalDateTime.of(
                        2026,
                        10,
                        4,
                        0,
                        0
                );

        LocalDateTime expectedEnd =
                LocalDateTime.of(
                        2026,
                        10,
                        5,
                        0,
                        0
                );

        UserActivityReport standardUser =
                new UserActivityReport(
                        2L,
                        "Standard User",
                        "user@opsflow.com",
                        "USER",
                        1L,
                        0L,
                        1L,
                        0L,
                        2L
                );

        UserActivityReport adminUser =
                new UserActivityReport(
                        1L,
                        "Test User",
                        "test@opsflow.com",
                        "ADMIN",
                        1L,
                        0L,
                        0L,
                        1L,
                        2L
                );

        List<UserActivityReport> expected =
                List.of(
                        standardUser,
                        adminUser
                );

        when(
                historyRepository
                        .getUserActivityReportByDateRange(
                                expectedStart,
                                expectedEnd
                        )
        ).thenReturn(expected);

        List<UserActivityReport> result =
                service.getUserActivityReport(
                        startDate,
                        endDate
                );

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                expected,
                result
        );

        verify(historyRepository)
                .getUserActivityReportByDateRange(
                        expectedStart,
                        expectedEnd
                );
    }

    @Test
    void getSummaryShouldRejectStartDateAfterEndDate() {

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        10,
                        5
                );

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.getSummary(
                                        startDate,
                                        endDate
                                )
                );

        assertEquals(
                "Start date must not be after end date",
                exception.getMessage()
        );

        verify(
                operationRepository,
                never()
        ).count();

        verify(
                historyRepository,
                never()
        ).count();
    }

    @Test
    void getSummaryShouldRejectNullStartDate() {

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.getSummary(
                                        null,
                                        endDate
                                )
                );

        assertEquals(
                "Start date and end date are required",
                exception.getMessage()
        );

        verify(
                operationRepository,
                never()
        ).count();
    }

    @Test
    void getSummaryShouldRejectNullEndDate() {

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.getSummary(
                                        startDate,
                                        null
                                )
                );

        assertEquals(
                "Start date and end date are required",
                exception.getMessage()
        );

        verify(
                operationRepository,
                never()
        ).count();
    }

    @Test
    void getUserActivityReportShouldRejectInvalidDateRange() {

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        10,
                        10
                );

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        10,
                        4
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.getUserActivityReport(
                                        startDate,
                                        endDate
                                )
                );

        assertEquals(
                "Start date must not be after end date",
                exception.getMessage()
        );

        verify(
                historyRepository,
                never()
        ).getUserActivityReportByDateRange(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}