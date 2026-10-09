package com.opsflow.backend.dto;

import com.opsflow.backend.entity.OperationHistory;

import java.time.LocalDateTime;

public class OperationHistoryResponse {

    private final Long id;
    private final Long operationId;
    private final String eventType;
    private final String oldStatus;
    private final String newStatus;
    private final String operationTitle;

    private final Long userId;
    private final String userName;
    private final String userEmail;
    private final String userRole;

    private final LocalDateTime createdAt;

    public OperationHistoryResponse(OperationHistory history) {
        this.id = history.getId();
        this.operationId = history.getOperationId();
        this.eventType = history.getEventType();
        this.oldStatus = history.getOldStatus();
        this.newStatus = history.getNewStatus();
        this.operationTitle = history.getOperationTitle();

        this.userId = history.getUserId();
        this.userName = history.getUserName();
        this.userEmail = history.getUserEmail();
        this.userRole = history.getUserRole();

        this.createdAt = history.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getOperationTitle() {
        return operationTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserRole() {
        return userRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}