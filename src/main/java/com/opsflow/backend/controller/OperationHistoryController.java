package com.opsflow.backend.controller;

import com.opsflow.backend.dto.OperationHistoryResponse;
import com.opsflow.backend.service.OperationHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operations/history")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(
        name = "Operation History",
        description = "Retrieve operation audit and history records."
)
public class OperationHistoryController {

    private final OperationHistoryService service;

    public OperationHistoryController(
            OperationHistoryService service) {

        this.service = service;
    }

    @Operation(
            summary = "Get operation history",
            description = "Returns operation history records using server-side pagination, ordered from newest to oldest."
    )
    @GetMapping
    public Page<OperationHistoryResponse> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number must not be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Pageable pageable =
                PageRequest.of(page, size);

        return service
                .getHistory(pageable)
                .map(OperationHistoryResponse::new);
    }

    @Operation(
            summary = "Get history for an operation",
            description = "Returns all history records for a specific operation, ordered from newest to oldest."
    )
    @GetMapping("/{operationId}")
    public ResponseEntity<List<OperationHistoryResponse>>
    getHistoryByOperationId(
            @PathVariable Long operationId) {

        List<OperationHistoryResponse> history =
                service
                        .getHistoryByOperationId(operationId)
                        .stream()
                        .map(OperationHistoryResponse::new)
                        .toList();

        return ResponseEntity.ok(history);
    }
}