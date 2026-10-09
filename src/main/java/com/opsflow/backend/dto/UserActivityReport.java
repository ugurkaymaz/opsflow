package com.opsflow.backend.dto;

public class UserActivityReport {

    private final Long userId;
    private final String userName;
    private final String userEmail;
    private final String userRole;

    private final long created;
    private final long updated;
    private final long statusChanged;
    private final long deleted;
    private final long total;

    public UserActivityReport(
            Long userId,
            String userName,
            String userEmail,
            String userRole,
            long created,
            long updated,
            long statusChanged,
            long deleted,
            long total) {

        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userRole = userRole;
        this.created = created;
        this.updated = updated;
        this.statusChanged = statusChanged;
        this.deleted = deleted;
        this.total = total;
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

    public long getCreated() {
        return created;
    }

    public long getUpdated() {
        return updated;
    }

    public long getStatusChanged() {
        return statusChanged;
    }

    public long getDeleted() {
        return deleted;
    }

    public long getTotal() {
        return total;
    }
}