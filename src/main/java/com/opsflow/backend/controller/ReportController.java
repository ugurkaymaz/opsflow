package com.opsflow.backend.controller;

import com.opsflow.backend.dto.ReportSummary;
import com.opsflow.backend.dto.UserActivityReport;
import com.opsflow.backend.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(
        name = "Reports",
        description = "Operation analytics and audit activity reports."
)
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @Operation(
            summary = "Get report summary",
            description = "Returns current operation statistics and audit activity statistics. Audit activity can optionally be filtered by date range."
    )
    @GetMapping("/summary")
    public ResponseEntity<ReportSummary> getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        validateOptionalDateRange(
                startDate,
                endDate
        );

        if (startDate == null) {
            return ResponseEntity.ok(
                    service.getSummary()
            );
        }

        return ResponseEntity.ok(
                service.getSummary(
                        startDate,
                        endDate
                )
        );
    }

    @Operation(
            summary = "Get user activity report",
            description = "Returns aggregated audit activity for each identified user. Activity can optionally be filtered by date range."
    )
    @GetMapping("/user-activity")
    public ResponseEntity<List<UserActivityReport>>
    getUserActivityReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        validateOptionalDateRange(
                startDate,
                endDate
        );

        if (startDate == null) {
            return ResponseEntity.ok(
                    service.getUserActivityReport()
            );
        }

        return ResponseEntity.ok(
                service.getUserActivityReport(
                        startDate,
                        endDate
                )
        );
    }

    private void validateOptionalDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        boolean startDateProvided =
                startDate != null;

        boolean endDateProvided =
                endDate != null;

        if (startDateProvided != endDateProvided) {
            throw new IllegalArgumentException(
                    "Start date and end date must be provided together"
            );
        }

        if (startDateProvided
                && startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "Start date must not be after end date"
            );
        }
    }
}