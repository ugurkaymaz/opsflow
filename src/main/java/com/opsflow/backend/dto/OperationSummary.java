package com.opsflow.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "OperationSummary",
        description = "Summary statistics for operation records."
)
public class OperationSummary {

    @Schema(
            description = "Total number of operations.",
            example = "4"
    )
    private long total;

    @Schema(
            description = "Number of planned operations.",
            example = "3"
    )
    private long planned;

    @Schema(
            description = "Number of operations currently in progress.",
            example = "0"
    )
    private long inProgress;

    @Schema(
            description = "Number of completed operations.",
            example = "1"
    )
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