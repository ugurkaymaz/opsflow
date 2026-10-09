package com.opsflow.backend.dto;

public class ReportSummary {

    private final long totalOperations;
    private final long planned;
    private final long inProgress;
    private final long completed;
    private final double completionRate;

    private final long totalAuditEvents;
    private final long createdEvents;
    private final long updatedEvents;
    private final long statusChangedEvents;
    private final long deletedEvents;

    private final long trackedUserActions;
    private final long userActions;
    private final long adminActions;
    private final long unattributedActions;

    public ReportSummary(
            long totalOperations,
            long planned,
            long inProgress,
            long completed,
            double completionRate,
            long totalAuditEvents,
            long createdEvents,
            long updatedEvents,
            long statusChangedEvents,
            long deletedEvents,
            long trackedUserActions,
            long userActions,
            long adminActions,
            long unattributedActions) {

        this.totalOperations = totalOperations;
        this.planned = planned;
        this.inProgress = inProgress;
        this.completed = completed;
        this.completionRate = completionRate;

        this.totalAuditEvents = totalAuditEvents;
        this.createdEvents = createdEvents;
        this.updatedEvents = updatedEvents;
        this.statusChangedEvents = statusChangedEvents;
        this.deletedEvents = deletedEvents;

        this.trackedUserActions = trackedUserActions;
        this.userActions = userActions;
        this.adminActions = adminActions;
        this.unattributedActions = unattributedActions;
    }

    public long getTotalOperations() {
        return totalOperations;
    }

    public long getPlanned() {
        return planned;
    }

    public long getInProgress() {
        return inProgress;
    }

    public long getCompleted() {
        return completed;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public long getTotalAuditEvents() {
        return totalAuditEvents;
    }

    public long getCreatedEvents() {
        return createdEvents;
    }

    public long getUpdatedEvents() {
        return updatedEvents;
    }

    public long getStatusChangedEvents() {
        return statusChangedEvents;
    }

    public long getDeletedEvents() {
        return deletedEvents;
    }

    public long getTrackedUserActions() {
        return trackedUserActions;
    }

    public long getUserActions() {
        return userActions;
    }

    public long getAdminActions() {
        return adminActions;
    }

    public long getUnattributedActions() {
        return unattributedActions;
    }
}