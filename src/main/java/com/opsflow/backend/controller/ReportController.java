package com.opsflow.backend.controller;

import com.opsflow.backend.dto.ReportSummary;
import com.opsflow.backend.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            description = "Returns current operation statistics and historical audit activity statistics."
    )
    @GetMapping("/summary")
    public ResponseEntity<ReportSummary> getSummary() {

        return ResponseEntity.ok(
                service.getSummary()
        );
    }
}