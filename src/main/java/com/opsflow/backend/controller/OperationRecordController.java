package com.opsflow.backend.controller;

import com.opsflow.backend.dto.OperationSummary;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.opsflow.backend.entity.OperationRecord;
import com.opsflow.backend.service.OperationRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.opsflow.backend.dto.OperationRecordRequest;
import com.opsflow.backend.dto.OperationRecordResponse;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/operations")
@CrossOrigin(origins = "http://localhost:5173")

public class OperationRecordController {

    private final OperationRecordService service;

    public OperationRecordController(OperationRecordService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OperationRecordResponse> create(
            @Valid @RequestBody OperationRecordRequest request) {

        OperationRecord saved = service.create(request.toEntity());

        return ResponseEntity.ok(
                new OperationRecordResponse(saved)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperationRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OperationRecordRequest request) {

        return service.update(id, request.toEntity())
                .map(OperationRecordResponse::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping
    public List<OperationRecordResponse> getAll() {
        return service.getAll()
                .stream()
                .map(OperationRecordResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationRecordResponse> getById(
            @PathVariable Long id) {

        return service.getById(id)
                .map(OperationRecordResponse::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/page")
    public Page<OperationRecordResponse> getPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        if (size > 100) {
            throw new IllegalArgumentException(
                    "Page size must not exceed 100"
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return service.getAll(pageable)
                .map(OperationRecordResponse::new);
    }

    @GetMapping("/search")
    public Page<OperationRecordResponse> search(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime endTime,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "operationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        if (size > 100) {
            throw new IllegalArgumentException(
                    "Page size must not exceed 100"
            );
        }

        List<String> allowedSortFields = List.of(
                "id",
                "title",
                "operationDate",
                "operationTime",
                "status"
        );

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy
            );
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException(
                    "Sort direction must be 'asc' or 'desc'"
            );
        }

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );
        return service.search(
                startDate,
                endDate,
                startTime,
                endTime,
                pageable
        ).map(OperationRecordResponse::new);

    }



    @GetMapping("/summary")
    public ResponseEntity<OperationSummary> getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                service.getSummary(startDate, endDate)
        );
    }
}