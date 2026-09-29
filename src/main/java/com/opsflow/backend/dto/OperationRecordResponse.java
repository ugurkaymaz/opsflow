package com.opsflow.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import com.opsflow.backend.entity.OperationRecord;


public class OperationRecordResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate operationDate;
    private LocalTime operationTime;
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