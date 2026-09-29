package com.opsflow.backend.dto;

public class OperationSummary {

    private long total;
    private long planned;
    private long inProgress;
    private long completed;

    public OperationSummary(
            long total,
            long planned,
            long inProgress,
            long completed
    ) {
        this.total = total;
        this.planned = planned;
        this.inProgress = inProgress;
        this.completed = completed;
    }

    public long getTotal() {
        return total;
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
}
