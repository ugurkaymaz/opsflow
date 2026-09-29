package com.opsflow.backend.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import com.opsflow.backend.entity.OperationRecord;

public class OperationRecordRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Operation date is required")
    private LocalDate operationDate;

    @NotNull(message = "Operation time is required")
    private LocalTime operationTime;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;


    public OperationRecordRequest() {
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

    public OperationRecord toEntity() {
        OperationRecord record = new OperationRecord();

        record.setTitle(this.title);
        record.setDescription(this.description);
        record.setOperationDate(this.operationDate);
        record.setOperationTime(this.operationTime);
        record.setStatus(this.status);




        return record;
    }

}