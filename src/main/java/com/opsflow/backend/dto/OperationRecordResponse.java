package com.opsflow.backend.dto;

import com.opsflow.backend.entity.OperationRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(
        name = "OperationRecordResponse",
        description = "Response containing the details of an operation record."
)
public class OperationRecordResponse {

    @Schema(
            description = "Unique identifier of the operation.",
            example = "7"
    )
    private Long id;

    @Schema(
            description = "Title of the operation.",
            example = "Database Backup"
    )
    private String title;

    @Schema(
            description = "Detailed description of the operation.",
            example = "Perform scheduled backup of the production database"
    )
    private String description;

    @Schema(
            description = "Date on which the operation is scheduled.",
            example = "2026-10-05"
    )
    private LocalDate operationDate;

    @Schema(
            description = "Time at which the operation is scheduled.",
            example = "09:30:00"
    )
    private LocalTime operationTime;

    @Schema(
            description = "Current status of the operation.",
            example = "PLANNED",
            allowableValues = {
                    "PLANNED",
                    "IN_PROGRESS",
                    "COMPLETED"
            }
    )
    private String status;

    public OperationRecordResponse(OperationRecord record) {
        this.id = record.getId();
        this.title = record.getTitle();
        this.description = record.getDescription();
        this.operationDate = record.getOperationDate();
        this.operationTime = record.getOperationTime();
        this.status = record.getStatus();
    }

    public OperationRecordResponse() {
    }

    public OperationRecordResponse(
            Long id,
            String title,
            String description,
            LocalDate operationDate,
            LocalTime operationTime,
            String status) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.operationDate = operationDate;
        this.operationTime = operationTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(LocalDate operationDate) {
        this.operationDate = operationDate;
    }

    public LocalTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}