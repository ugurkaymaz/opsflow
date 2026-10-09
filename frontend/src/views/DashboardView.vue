<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  HISTORY_API,
  OPERATIONS_API,
  REPORTS_API,
} from '../config/api'
import {
  apiFetch,
  ApiError
} from '../services/api'

const summary = ref({
  total: 0,
  planned: 0,
  inProgress: 0,
  completed: 0,
})

const reportSummary = ref({
  totalAuditEvents: 0,
  trackedUserActions: 0,
  userActions: 0,
  adminActions: 0,
  unattributedActions: 0,
})

const upcomingOperations = ref([])
const recentActivity = ref([])

const loading = ref(true)
const error = ref('')

const completionRate = computed(() => {
  if (summary.value.total === 0) {
    return 0
  }

  return Math.round(
    (summary.value.completed / summary.value.total) * 100
  )
})

const plannedPercentage = computed(() => {
  return calculatePercentage(
    summary.value.planned,
    summary.value.total
  )
})

const inProgressPercentage = computed(() => {
  return calculatePercentage(
    summary.value.inProgress,
    summary.value.total
  )
})

const completedPercentage = computed(() => {
  return calculatePercentage(
    summary.value.completed,
    summary.value.total
  )
})

const userActivityPercentage = computed(() => {
  if (reportSummary.value.trackedUserActions === 0) {
    return 0
  }

  return Math.round(
    (
      reportSummary.value.userActions /
      reportSummary.value.trackedUserActions
    ) * 100
  )
})

const adminActivityPercentage = computed(() => {
  if (reportSummary.value.trackedUserActions === 0) {
    return 0
  }

  return Math.round(
    (
      reportSummary.value.adminActions /
      reportSummary.value.trackedUserActions
    ) * 100
  )
})

function calculatePercentage(value, total) {
  if (!total) {
    return 0
  }

  return Math.round(
    (value / total) * 100
  )
}

function formatEventType(eventType) {
  const labels = {
    CREATED: 'Created',
    UPDATED: 'Updated',
    STATUS_CHANGED: 'Status Changed',
    DELETED: 'Deleted',
  }

  return labels[eventType] || eventType
}

function formatStatus(status) {
  if (!status) {
    return '-'
  }

  const labels = {
    PLANNED: 'Planned',
    IN_PROGRESS: 'In Progress',
    COMPLETED: 'Completed',
  }

  return labels[status] || status
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(date)
}

function getEventClass(eventType) {
  return {
    CREATED: 'event-created',
    UPDATED: 'event-updated',
    STATUS_CHANGED: 'event-status-changed',
    DELETED: 'event-deleted',
  }[eventType] || ''
}

function getStatusClass(status) {
  return {
    PLANNED: 'status-planned',
    IN_PROGRESS: 'status-in-progress',
    COMPLETED: 'status-completed',
  }[status] || ''
}

function getStatusChange(activity) {
  return `${formatStatus(activity.oldStatus)} → ${formatStatus(activity.newStatus)}`
}

function getActivityUser(activity) {
  if (activity.userName) {
    return activity.userName
  }

  if (activity.userEmail) {
    return activity.userEmail
  }

  return 'Legacy activity'
}

function getErrorMessage(
  err,
  fallbackMessage
) {
  if (err instanceof ApiError) {
    return (
      err.message ||
      fallbackMessage
    )
  }

  return (
    err?.message ||
    fallbackMessage
  )
}

async function loadDashboard() {
  loading.value = true
  error.value = ''

  try {
    const today = new Date()

    const todayString =
      today.getFullYear() +
      '-' +
      String(
        today.getMonth() + 1
      ).padStart(2, '0') +
      '-' +
      String(
        today.getDate()
      ).padStart(2, '0')

    const params =
      new URLSearchParams()

    params.append(
      'startDate',
      todayString
    )

    params.append(
      'endDate',
      '2099-12-31'
    )

    params.append(
      'page',
      '0'
    )

    params.append(
      'size',
      '5'
    )

    params.append(
      'sortBy',
      'operationDate'
    )

    params.append(
      'direction',
      'asc'
    )

    const [
      summaryResponse,
      upcomingResponse,
      activityResponse,
      reportResponse,
    ] = await Promise.all([
      apiFetch(
        `${OPERATIONS_API}/summary`
      ),

      apiFetch(
        `${OPERATIONS_API}/search?${params.toString()}`
      ),

      apiFetch(
        `${HISTORY_API}?page=0&size=5`
      ),

      apiFetch(
        `${REPORTS_API}/summary`
      ),
    ])

    summary.value =
      await summaryResponse.json()

    reportSummary.value =
      await reportResponse.json()

    const upcomingData =
      await upcomingResponse.json()

    const activityData =
      await activityResponse.json()

    upcomingOperations.value =
      upcomingData.content || []

    recentActivity.value =
      activityData.content || []
  } catch (err) {
    error.value =
      getErrorMessage(
        err,
        'Failed to load dashboard'
      )
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <main class="dashboard-page">
    <div class="page-header">
      <div>
        <h1>Dashboard</h1>

        <p>
          Overview of your operation workflow and recent system activity.
        </p>
      </div>

      <button
        class="refresh-button"
        :disabled="loading"
        @click="loadDashboard"
      >
        {{ loading ? 'Loading...' : 'Refresh' }}
      </button>
    </div>

    <div
      v-if="error"
      class="error-state"
    >
      <strong>
        Unable to load dashboard
      </strong>

      <p>
        {{ error }}
      </p>

      <button
        class="retry-button"
        :disabled="loading"
        @click="loadDashboard"
      >
        {{ loading ? 'Retrying...' : 'Try Again' }}
      </button>
    </div>

    <div
      v-if="loading"
      class="loading-state"
    >
      Loading dashboard...
    </div>

    <template v-else-if="!error">
      <!-- Operation Overview -->
      <section class="section-block">
        <div class="section-header">
          <div>
            <h2>Operation Overview</h2>

            <p>
              Current operation status across the workflow.
            </p>
          </div>

          <span class="section-label">
            Current data
          </span>
        </div>

        <div class="summary-grid">
          <div class="summary-card">
            <span class="card-label">
              Total Operations
            </span>

            <strong class="card-value">
              {{ summary.total }}
            </strong>
          </div>

          <div class="summary-card">
            <span class="card-label">
              Planned
            </span>

            <strong class="card-value">
              {{ summary.planned }}
            </strong>
          </div>

          <div class="summary-card">
            <span class="card-label">
              In Progress
            </span>

            <strong class="card-value">
              {{ summary.inProgress }}
            </strong>
          </div>

          <div class="summary-card">
            <span class="card-label">
              Completed
            </span>

            <strong class="card-value">
              {{ summary.completed }}
            </strong>
          </div>
        </div>
      </section>

      <!-- Workflow and Recent Activity -->
      <section class="dashboard-grid">
        <!-- Workflow Progress -->
        <div class="panel workflow-panel">
          <div class="panel-header">
            <div>
              <h2>Workflow Progress</h2>

              <p>
                Operation completion and current status distribution.
              </p>
            </div>

            <strong class="rate">
              {{ completionRate }}%
            </strong>
          </div>

          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{
                width: completionRate + '%'
              }"
            ></div>
          </div>

          <div class="progress-details">
            <span>
              {{ summary.completed }} completed
            </span>

            <span>
              {{ summary.total }} total
            </span>
          </div>

          <!-- Status Distribution -->
          <div class="workflow-section">
            <h3>Status Distribution</h3>

            <div class="distribution-list">
              <div class="distribution-item">
                <div class="distribution-header">
                  <span>Planned</span>

                  <strong>
                    {{ summary.planned }}
                    ({{ plannedPercentage }}%)
                  </strong>
                </div>

                <div class="progress-track compact">
                  <div
                    class="progress-fill"
                    :style="{
                      width: plannedPercentage + '%'
                    }"
                  ></div>
                </div>
              </div>

              <div class="distribution-item">
                <div class="distribution-header">
                  <span>In Progress</span>

                  <strong>
                    {{ summary.inProgress }}
                    ({{ inProgressPercentage }}%)
                  </strong>
                </div>

                <div class="progress-track compact">
                  <div
                    class="progress-fill"
                    :style="{
                      width: inProgressPercentage + '%'
                    }"
                  ></div>
                </div>
              </div>

              <div class="distribution-item">
                <div class="distribution-header">
                  <span>Completed</span>

                  <strong>
                    {{ summary.completed }}
                    ({{ completedPercentage }}%)
                  </strong>
                </div>

                <div class="progress-track compact">
                  <div
                    class="progress-fill"
                    :style="{
                      width: completedPercentage + '%'
                    }"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Activity Snapshot -->
          <div class="workflow-section snapshot-section">
            <div class="subsection-header">
              <div>
                <h3>Activity Snapshot</h3>

                <p>
                  All-time audit activity recorded by the system.
                </p>
              </div>

              <span class="section-label">
                All-time
              </span>
            </div>

            <div class="snapshot-grid">
              <div class="snapshot-card">
                <span>
                  Audit Events
                </span>

                <strong>
                  {{ reportSummary.totalAuditEvents }}
                </strong>
              </div>

              <div class="snapshot-card">
                <span>
                  Tracked Actions
                </span>

                <strong>
                  {{ reportSummary.trackedUserActions }}
                </strong>
              </div>

              <div class="snapshot-card">
                <span>
                  USER Actions
                </span>

                <strong>
                  {{ reportSummary.userActions }}
                </strong>
              </div>

              <div class="snapshot-card">
                <span>
                  ADMIN Actions
                </span>

                <strong>
                  {{ reportSummary.adminActions }}
                </strong>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Activity -->
        <div class="panel recent-activity-panel">
          <div class="panel-header">
            <div>
              <h2>Recent Activity</h2>

              <p>
                Latest audit events recorded by the system.
              </p>
            </div>

            <span class="section-label">
              Latest 5
            </span>
          </div>

          <div
            v-if="recentActivity.length"
            class="activity-list"
          >
            <div
              v-for="activity in recentActivity"
              :key="activity.id"
              class="activity-item"
            >
              <div class="activity-top">
                <span
                  class="event-badge"
                  :class="getEventClass(activity.eventType)"
                >
                  {{ formatEventType(activity.eventType) }}
                </span>

                <span class="activity-operation">
                  #{{ activity.operationId }}
                </span>
              </div>

              <strong class="activity-title">
                {{
                  activity.operationTitle ||
                  'Untitled operation'
                }}
              </strong>

              <div class="activity-user">
                <span>
                  {{ getActivityUser(activity) }}
                </span>

                <span
                  v-if="activity.userRole"
                  class="role-badge"
                  :class="{
                    'role-admin':
                      activity.userRole === 'ADMIN',

                    'role-user':
                      activity.userRole === 'USER'
                  }"
                >
                  {{ activity.userRole }}
                </span>
              </div>

              <div
                v-if="
                  activity.eventType ===
                  'STATUS_CHANGED'
                "
                class="activity-change"
              >
                {{ getStatusChange(activity) }}
              </div>

              <span class="activity-time">
                {{ formatDateTime(activity.createdAt) }}
              </span>
            </div>
          </div>

          <div
            v-else
            class="small-empty-state"
          >
            No audit activity recorded.
          </div>
        </div>
      </section>

      <!-- Activity Overview -->


      <!-- Upcoming Operations -->
      <section class="panel upcoming-panel">
        <div class="panel-header">
          <div>
            <h2>Upcoming Operations</h2>

            <p>
              Next scheduled operations from today onwards.
            </p>
          </div>

          <span class="section-label">
            Next 5
          </span>
        </div>

        <div
          v-if="upcomingOperations.length"
          class="table-wrapper"
        >
          <table>
            <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Date</th>
              <th>Time</th>
              <th>Status</th>
            </tr>
            </thead>

            <tbody>
            <tr
              v-for="operation in upcomingOperations"
              :key="operation.id"
            >
              <td>
                #{{ operation.id }}
              </td>

              <td>
                <strong>
                  {{ operation.title }}
                </strong>
              </td>

              <td>
                {{ operation.operationDate }}
              </td>

              <td>
                {{ operation.operationTime }}
              </td>

              <td>
                  <span
                    class="status-badge"
                    :class="getStatusClass(operation.status)"
                  >
                    {{ formatStatus(operation.status) }}
                  </span>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <div
          v-else
          class="empty-state"
        >
          <strong>
            No upcoming operations
          </strong>

          <p>
            There are currently no future operations scheduled.
          </p>
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.page-header h1 {
  margin: 0 0 8px;
}

.page-header p {
  margin: 0;
  color: #64748b;
}

.refresh-button {
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
  background: #1f2937;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.refresh-button:hover {
  background: #374151;
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-state {
  padding: 30px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  color: #64748b;
  text-align: center;
}

.error-state {
  padding: 24px;
  border: 1px solid #fecaca;
  border-radius: 12px;
  background: white;
  text-align: center;
}

.error-state strong {
  color: #b91c1c;
}

.error-state p {
  margin: 8px 0 0;
  color: #6b7280;
}

.retry-button {
  margin-top: 16px;
  padding: 9px 16px;
  border: none;
  border-radius: 8px;
  background: #111827;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.retry-button:hover {
  background: #374151;
}

.retry-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.section-block,
.panel {
  padding: 22px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: white;
}

.section-header,
.panel-header,
.subsection-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.section-header {
  margin-bottom: 20px;
}

.section-header h2,
.panel-header h2 {
  margin: 0 0 6px;
}

.section-header p,
.panel-header p,
.subsection-header p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.section-label {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475569;
  white-space: nowrap;
  font-size: 12px;
  font-weight: 700;
}

.summary-grid,
.activity-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  min-height: 110px;
  padding: 22px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
}

.card-label {
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
}

.card-value {
  margin-top: 10px;
  color: #0f172a;
  font-size: 32px;
}

.card-detail {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 24px;
  align-items: stretch;
}

.workflow-panel,
.recent-activity-panel {
  height: 100%;
  box-sizing: border-box;
}

.rate {
  color: #0f172a;
  font-size: 30px;
}

.progress-track {
  width: 100%;
  height: 12px;
  margin-top: 24px;
  overflow: hidden;
  border-radius: 999px;
  background: #e5e7eb;
}

.progress-track.compact {
  height: 9px;
  margin-top: 8px;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: #334155;
  transition: width 0.3s ease;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  color: #64748b;
  font-size: 14px;
}

.workflow-section {
  margin-top: 30px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.workflow-section h3 {
  margin: 0 0 18px;
  color: #111827;
  font-size: 16px;
}

.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.distribution-item {
  display: flex;
  flex-direction: column;
}

.distribution-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #374151;
  font-size: 14px;
}

.snapshot-section .subsection-header {
  margin-bottom: 16px;
}

.snapshot-section .subsection-header h3 {
  margin-bottom: 5px;
}

.snapshot-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.snapshot-card {
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
}

.snapshot-card span {
  color: #64748b;
  font-size: 12px;
}

.snapshot-card strong {
  color: #0f172a;
  font-size: 22px;
}

.activity-list {
  margin-top: 14px;
}

.activity-item {
  padding: 14px 0;
  border-bottom: 1px solid #e5e7eb;
}

.activity-item:first-child {
  padding-top: 4px;
}

.activity-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.activity-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 7px;
}

.activity-operation {
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
}

.activity-title {
  display: block;
  color: #1f2937;
  font-size: 14px;
}

.activity-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 7px;
  color: #64748b;
  font-size: 12px;
}

.activity-change {
  margin-top: 7px;
  color: #374151;
  font-size: 13px;
  font-weight: 600;
}

.activity-time {
  display: block;
  margin-top: 7px;
  color: #94a3b8;
  font-size: 12px;
}

.event-badge,
.role-badge,
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.event-created {
  background: #dcfce7;
  color: #166534;
}

.event-updated {
  background: #dbeafe;
  color: #1d4ed8;
}

.event-status-changed {
  background: #fef3c7;
  color: #92400e;
}

.event-deleted {
  background: #fee2e2;
  color: #b91c1c;
}

.role-user {
  background: #dbeafe;
  color: #1d4ed8;
}

.role-admin {
  background: #f3e8ff;
  color: #7e22ce;
}

.activity-bars {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-top: 24px;
}

.activity-bar-item {
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.activity-bar-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.legacy-note {
  margin-top: 20px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 13px;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 700px;
  margin-top: 20px;
  border-collapse: collapse;
}

th,
td {
  padding: 13px 14px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

th {
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

td {
  color: #334155;
  font-size: 14px;
}

tbody tr:last-child td {
  border-bottom: none;
}

.status-planned {
  background: #f1f5f9;
  color: #475569;
}

.status-in-progress {
  background: #fef3c7;
  color: #92400e;
}

.status-completed {
  background: #dcfce7;
  color: #166534;
}

.empty-state {
  padding: 32px;
  color: #64748b;
  text-align: center;
}

.empty-state strong {
  display: block;
  margin-bottom: 6px;
  color: #1f2937;
}

.empty-state p {
  margin: 0;
}

.small-empty-state {
  padding: 24px 0;
  color: #64748b;
  text-align: center;
}

@media (max-width: 1100px) {
  .summary-grid,
  .activity-summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .dashboard-page {
    gap: 18px;
  }

  .page-header,
  .section-header,
  .panel-header,
  .subsection-header {
    flex-direction: column;
  }

  .summary-grid,
  .activity-summary-grid,
  .activity-bars,
  .snapshot-grid {
    grid-template-columns: 1fr;
  }

  .section-block,
  .panel {
    padding: 18px;
  }
}
</style>
