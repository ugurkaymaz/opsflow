<template>
  <div class="reports-page">
    <div class="page-header">
      <div>
        <h1>Reports</h1>
        <p class="page-description">
          Operation analytics and audit activity overview.
        </p>
      </div>

      <button
        class="refresh-button"
        :disabled="loading"
        @click="loadReport"
      >
        {{ loading ? 'Loading...' : 'Refresh' }}
      </button>
    </div>

    <div
      v-if="loading"
      class="state-message"
    >
      Loading reports...
    </div>

    <div
      v-else-if="error"
      class="error-state"
    >
      <strong>
        Unable to load reports
      </strong>

      <p>
        {{ error }}
      </p>

      <button
        class="retry-button"
        @click="retryReport"
      >
        Try Again
      </button>
    </div>

    <!-- Date Filter -->
    <section class="report-section">
      <div class="section-header">
        <div>
          <h2>Date Filter</h2>
          <p>
            Filter operation data and audit activity by a selected date range.
          </p>
        </div>

        <span
          v-if="filtersActive"
          class="active-filter-badge"
        >
          Filter Active
        </span>
      </div>

      <div class="filter-row">
        <div class="filter-field">
          <label for="startDate">
            Start Date
          </label>

          <input
            id="startDate"
            v-model="startDate"
            type="date"
          />
        </div>

        <div class="filter-field">
          <label for="endDate">
            End Date
          </label>

          <input
            id="endDate"
            v-model="endDate"
            type="date"
          />
        </div>

        <div class="filter-actions">
          <button
            class="primary-button"
            :disabled="loading"
            @click="applyDateFilter"
          >
            Apply Filter
          </button>

          <button
            class="secondary-button"
            :disabled="loading"
            @click="clearFilter"
          >
            Clear
          </button>
        </div>
      </div>

      <div
        v-if="filterError"
        class="filter-error"
      >
        {{ filterError }}
      </div>

      <div
        v-if="filtersActive"
        class="filter-summary"
      >
        Showing activity from
        <strong>{{ startDate }}</strong>
        to
        <strong>{{ endDate }}</strong>.
      </div>
    </section>

    <!-- Current Operations -->
    <section class="report-section">
      <div class="section-header">
        <div>
          <h2>Current Operations</h2>
          <p>
            Current operation totals for the selected reporting scope.
          </p>
        </div>

        <span class="section-label">
          {{ filtersActive ? 'Selected period' : 'Current data' }}
        </span>
      </div>

      <div class="metric-grid">
        <div class="metric-card">
          <span class="metric-label">
            Total Operations
          </span>

          <strong class="metric-value">
            {{ displayedSummary.total }}
          </strong>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            Planned
          </span>

          <strong class="metric-value">
            {{ displayedSummary.planned }}
          </strong>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            In Progress
          </span>

          <strong class="metric-value">
            {{ displayedSummary.inProgress }}
          </strong>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            Completed
          </span>

          <strong class="metric-value">
            {{ displayedSummary.completed }}
          </strong>
        </div>
      </div>
    </section>

    <!-- Completion Rate -->
    <section class="report-section">
      <div class="section-header">
        <div>
          <h2>Completion Rate</h2>
          <p>
            Percentage of operations currently marked as completed.
          </p>
        </div>

        <strong class="percentage-value">
          {{ completionRate }}%
        </strong>
      </div>

      <div class="progress-track">
        <div
          class="progress-fill"
          :style="{ width: `${completionRate}%` }"
        ></div>
      </div>
    </section>

    <!-- Status Distribution -->
    <section class="report-section">
      <div class="section-header">
        <div>
          <h2>Status Distribution</h2>
          <p>
            Distribution of operations by their current status.
          </p>
        </div>
      </div>

      <div class="distribution-list">
        <div class="distribution-item">
          <div class="distribution-heading">
            <span>Planned</span>

            <strong>
              {{ displayedSummary.planned }}
              ({{ percentage(
              displayedSummary.planned,
              displayedSummary.total
            ) }}%)
            </strong>
          </div>

          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{
                width: `${percentage(
                  displayedSummary.planned,
                  displayedSummary.total
                )}%`
              }"
            ></div>
          </div>
        </div>

        <div class="distribution-item">
          <div class="distribution-heading">
            <span>In Progress</span>

            <strong>
              {{ displayedSummary.inProgress }}
              ({{ percentage(
              displayedSummary.inProgress,
              displayedSummary.total
            ) }}%)
            </strong>
          </div>

          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{
                width: `${percentage(
                  displayedSummary.inProgress,
                  displayedSummary.total
                )}%`
              }"
            ></div>
          </div>
        </div>

        <div class="distribution-item">
          <div class="distribution-heading">
            <span>Completed</span>

            <strong>
              {{ displayedSummary.completed }}
              ({{ percentage(
              displayedSummary.completed,
              displayedSummary.total
            ) }}%)
            </strong>
          </div>

          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{
                width: `${percentage(
                  displayedSummary.completed,
                  displayedSummary.total
                )}%`
              }"
            ></div>
          </div>
        </div>
      </div>
    </section>

    <!-- Audit Activity -->
    <section class="report-section">
      <div class="section-header">
        <div>
          <h2>Audit Activity</h2>
          <p>
            Recorded operation activity from the audit history.
          </p>
        </div>

        <span class="section-label">
          {{
            filtersActive
              ? 'Selected period activity'
              : 'All-time activity'
          }}
        </span>
      </div>

      <div class="metric-grid">
        <div class="metric-card">
          <span class="metric-label">
            Total Audit Events
          </span>

          <strong class="metric-value">
            {{ report.totalAuditEvents }}
          </strong>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            Created
          </span>

          <strong class="metric-value">
            {{ report.createdEvents }}
          </strong>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            Updated
          </span>

          <strong class="metric-value">
            {{ report.updatedEvents }}
          </strong>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            Status Changes
          </span>

          <strong class="metric-value">
            {{ report.statusChangedEvents }}
          </strong>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            Deleted
          </span>

          <strong class="metric-value">
            {{ report.deletedEvents }}
          </strong>
        </div>
      </div>
    </section>

    <!-- User Activity -->
    <section class="report-section">
      <div class="section-header">
        <div>
          <h2>User Activity</h2>
          <p>
            Audit actions grouped by authenticated user role.
          </p>
        </div>

        <span class="section-label">
          {{
            filtersActive
              ? 'Selected period activity'
              : 'All-time activity'
          }}
        </span>
      </div>

      <div class="metric-grid">
        <div class="metric-card">
          <span class="metric-label">
            Tracked Actions
          </span>

          <strong class="metric-value">
            {{ report.trackedUserActions }}
          </strong>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            USER Actions
          </span>

          <strong class="metric-value">
            {{ report.userActions }}
          </strong>

          <span class="metric-detail">
            {{ userActivityPercentage }}% of tracked activity
          </span>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            ADMIN Actions
          </span>

          <strong class="metric-value">
            {{ report.adminActions }}
          </strong>

          <span class="metric-detail">
            {{ adminActivityPercentage }}% of tracked activity
          </span>
        </div>

        <div class="metric-card">
          <span class="metric-label">
            Unattributed
          </span>

          <strong class="metric-value">
            {{ report.unattributedActions }}
          </strong>
        </div>
      </div>

      <div class="activity-distribution">
        <h3>Tracked Activity Distribution</h3>

        <div class="distribution-list">
          <div class="distribution-item">
            <div class="distribution-heading">
              <span>USER</span>

              <strong>
                {{ report.userActions }}
                ({{ userActivityPercentage }}%)
              </strong>
            </div>

            <div class="progress-track">
              <div
                class="progress-fill"
                :style="{
                  width: `${userActivityPercentage}%`
                }"
              ></div>
            </div>
          </div>

          <div class="distribution-item">
            <div class="distribution-heading">
              <span>ADMIN</span>

              <strong>
                {{ report.adminActions }}
                ({{ adminActivityPercentage }}%)
              </strong>
            </div>

            <div class="progress-track">
              <div
                class="progress-fill"
                :style="{
                  width: `${adminActivityPercentage}%`
                }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="report.unattributedActions > 0"
        class="legacy-note"
      >
        {{ report.unattributedActions }}
        audit event{{ report.unattributedActions === 1 ? '' : 's' }}
        do not contain user attribution data.
      </div>
    </section>

    <!-- User Activity Breakdown -->
    <section class="report-section">
      <div class="section-header">
        <div>
          <h2>User Activity Breakdown</h2>
          <p>
            Detailed audit activity for each identified application user.
          </p>
        </div>

        <div class="section-meta">
          <span class="section-label">
            {{
              filtersActive
                ? 'Selected period activity'
                : 'All-time activity'
            }}
          </span>

          <span class="count-badge">
            {{ userActivity.length }}
            {{ userActivity.length === 1 ? 'user' : 'users' }}
          </span>
        </div>
      </div>

      <div
        v-if="userActivity.length === 0"
        class="empty-state"
      >
        No identified user activity was found for this reporting period.
      </div>

      <div
        v-else
        class="table-wrapper"
      >
        <table class="report-table">
          <thead>
          <tr>
            <th>User</th>
            <th>Email</th>
            <th>Role</th>
            <th>Created</th>
            <th>Updated</th>
            <th>Status Changes</th>
            <th>Deleted</th>
            <th>Total</th>
          </tr>
          </thead>

          <tbody>
          <tr
            v-for="user in userActivity"
            :key="user.userId"
          >
            <td>
              <div class="user-cell">
                <strong>
                  {{ user.userName || '-' }}
                </strong>

                <span>
                    #{{ user.userId }}
                  </span>
              </div>
            </td>

            <td>
              {{ user.userEmail || '-' }}
            </td>

            <td>
                <span
                  class="role-badge"
                  :class="{
                    'role-admin': user.userRole === 'ADMIN',
                    'role-user': user.userRole === 'USER'
                  }"
                >
                  {{ user.userRole || '-' }}
                </span>
            </td>

            <td>
              {{ user.created }}
            </td>

            <td>
              {{ user.updated }}
            </td>

            <td>
              {{ user.statusChanged }}
            </td>

            <td>
              {{ user.deleted }}
            </td>

            <td>
              <strong>
                {{ user.total }}
              </strong>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Recent Operations -->
    <section class="report-section">
      <div class="section-header">
        <div>
          <h2>Recent Operations</h2>
          <p>
            Latest operations for the selected reporting scope.
          </p>
        </div>

        <span class="section-label">
          {{ filtersActive ? 'Selected period' : 'Latest records' }}
        </span>
      </div>

      <div
        v-if="recentOperations.length === 0"
        class="empty-state"
      >
        No operations were found for this reporting period.
      </div>

      <div
        v-else
        class="table-wrapper"
      >
        <table class="report-table recent-table">
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
            v-for="operation in recentOperations"
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
                  :class="statusClass(operation.status)"
                >
                  {{ formatStatus(operation.status) }}
                </span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import {
  computed,
  onMounted,
  ref
} from 'vue'

import {
  API_BASE_URL,
  OPERATIONS_API
} from '../config/api'

import {
  apiFetch,
  ApiError
} from '../services/api'

const report = ref({
  totalOperations: 0,
  planned: 0,
  inProgress: 0,
  completed: 0,
  completionRate: 0,

  totalAuditEvents: 0,
  createdEvents: 0,
  updatedEvents: 0,
  statusChangedEvents: 0,
  deletedEvents: 0,

  trackedUserActions: 0,
  userActions: 0,
  adminActions: 0,
  unattributedActions: 0,
})

const filteredSummary = ref({
  total: 0,
  planned: 0,
  inProgress: 0,
  completed: 0,
})

const recentOperations = ref([])
const userActivity = ref([])

const loading = ref(true)
const error = ref('')
const filterError = ref('')

const startDate = ref('')
const endDate = ref('')
const filtersActive = ref(false)

const completionRate = computed(() => {
  if (filtersActive.value) {
    if (filteredSummary.value.total === 0) {
      return 0
    }

    return Math.round(
      (
        filteredSummary.value.completed /
        filteredSummary.value.total
      ) * 100
    )
  }

  return Math.round(
    report.value.completionRate
  )
})

const displayedSummary = computed(() => {
  if (filtersActive.value) {
    return {
      total:
      filteredSummary.value.total,

      planned:
      filteredSummary.value.planned,

      inProgress:
      filteredSummary.value.inProgress,

      completed:
      filteredSummary.value.completed,
    }
  }

  return {
    total:
    report.value.totalOperations,

    planned:
    report.value.planned,

    inProgress:
    report.value.inProgress,

    completed:
    report.value.completed,
  }
})

const userActivityPercentage =
  computed(() => {
    if (
      report.value.trackedUserActions === 0
    ) {
      return 0
    }

    return Math.round(
      (
        report.value.userActions /
        report.value.trackedUserActions
      ) * 100
    )
  })

const adminActivityPercentage =
  computed(() => {
    if (
      report.value.trackedUserActions === 0
    ) {
      return 0
    }

    return Math.round(
      (
        report.value.adminActions /
        report.value.trackedUserActions
      ) * 100
    )
  })

function percentage(
  value,
  total
) {
  if (!total) {
    return 0
  }

  return Math.round(
    (value / total) * 100
  )
}

function formatStatus(status) {
  if (!status) {
    return '-'
  }

  return status
    .toLowerCase()
    .split('_')
    .map(
      word =>
        word.charAt(0).toUpperCase() +
        word.slice(1)
    )
    .join(' ')
}

function statusClass(status) {
  return {
    'status-planned':
      status === 'PLANNED',

    'status-in-progress':
      status === 'IN_PROGRESS',

    'status-completed':
      status === 'COMPLETED',
  }
}

function getErrorMessage(
  err,
  fallbackMessage
) {
  if (err instanceof ApiError) {
    if (err.validationErrors) {
      const messages =
        Object.values(
          err.validationErrors
        )

      if (messages.length > 0) {
        return messages.join(', ')
      }
    }

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

async function loadReport() {
  loading.value = true
  error.value = ''
  filterError.value = ''

  try {
    const recentParams =
      new URLSearchParams()

    recentParams.append(
      'page',
      '0'
    )

    recentParams.append(
      'size',
      '5'
    )

    recentParams.append(
      'sortBy',
      'operationDate'
    )

    recentParams.append(
      'direction',
      'desc'
    )

    const [
      reportResponse,
      summaryResponse,
      recentResponse,
      userActivityResponse,
    ] = await Promise.all([
      apiFetch(
        `${API_BASE_URL}/api/reports/summary`
      ),

      apiFetch(
        `${OPERATIONS_API}/summary`
      ),

      apiFetch(
        `${OPERATIONS_API}/search?${recentParams.toString()}`
      ),

      apiFetch(
        `${API_BASE_URL}/api/reports/user-activity`
      ),
    ])

    report.value =
      await reportResponse.json()

    filteredSummary.value =
      await summaryResponse.json()

    const recentData =
      await recentResponse.json()

    recentOperations.value =
      recentData.content || []

    userActivity.value =
      await userActivityResponse.json()

    filtersActive.value = false
  } catch (err) {
    error.value =
      getErrorMessage(
        err,
        'Failed to load reports'
      )
  } finally {
    loading.value = false
  }
}

async function applyDateFilter() {
  filterError.value = ''
  error.value = ''

  if (
    !startDate.value &&
    !endDate.value
  ) {
    await loadReport()
    return
  }

  if (
    !startDate.value ||
    !endDate.value
  ) {
    filterError.value =
      'Start date and end date must be provided together'

    return
  }

  if (
    startDate.value >
    endDate.value
  ) {
    filterError.value =
      'Start date must not be after end date'

    return
  }

  loading.value = true

  try {
    const dateParams =
      new URLSearchParams()

    dateParams.append(
      'startDate',
      startDate.value
    )

    dateParams.append(
      'endDate',
      endDate.value
    )

    const recentParams =
      new URLSearchParams(
        dateParams
      )

    recentParams.append(
      'page',
      '0'
    )

    recentParams.append(
      'size',
      '5'
    )

    recentParams.append(
      'sortBy',
      'operationDate'
    )

    recentParams.append(
      'direction',
      'desc'
    )

    const [
      reportResponse,
      summaryResponse,
      recentResponse,
      userActivityResponse,
    ] = await Promise.all([
      apiFetch(
        `${API_BASE_URL}/api/reports/summary?${dateParams.toString()}`
      ),

      apiFetch(
        `${OPERATIONS_API}/summary?${dateParams.toString()}`
      ),

      apiFetch(
        `${OPERATIONS_API}/search?${recentParams.toString()}`
      ),

      apiFetch(
        `${API_BASE_URL}/api/reports/user-activity?${dateParams.toString()}`
      ),
    ])

    report.value =
      await reportResponse.json()

    filteredSummary.value =
      await summaryResponse.json()

    const recentData =
      await recentResponse.json()

    recentOperations.value =
      recentData.content || []

    userActivity.value =
      await userActivityResponse.json()

    filtersActive.value = true
  } catch (err) {
    error.value =
      getErrorMessage(
        err,
        'Failed to apply report filter'
      )
  } finally {
    loading.value = false
  }
}

async function clearFilter() {
  startDate.value = ''
  endDate.value = ''
  filterError.value = ''
  error.value = ''

  await loadReport()
}

async function retryReport() {
  if (
    filtersActive.value &&
    startDate.value &&
    endDate.value
  ) {
    await applyDateFilter()
    return
  }

  await loadReport()
}

onMounted(
  loadReport
)
</script>

<style scoped>
.reports-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 30px;
}

.page-description {
  margin: 6px 0 0;
  color: #64748b;
}

.refresh-button,
.primary-button,
.secondary-button {
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  font-weight: 600;
  cursor: pointer;
}

.refresh-button,
.primary-button {
  background: #111827;
  color: white;
}

.secondary-button {
  background: #e5e7eb;
  color: #111827;
}

.refresh-button:disabled,
.primary-button:disabled,
.secondary-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.state-message {
  padding: 24px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  text-align: center;
  color: #64748b;
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

.filter-error {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #fee2e2;
  color: #991b1b;
  font-size: 14px;
}

.report-section {
  padding: 22px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
}

.section-header p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 14px;
}

.section-label,
.count-badge,
.active-filter-badge {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
}

.section-label {
  background: #f1f5f9;
  color: #475569;
}

.count-badge {
  background: #e0f2fe;
  color: #0369a1;
}

.active-filter-badge {
  background: #dcfce7;
  color: #166534;
}

.section-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-field label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.filter-field input {
  min-width: 180px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  background: white;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.filter-summary {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f8fafc;
  color: #475569;
  font-size: 14px;
}

.metric-grid {
  display: grid;
  grid-template-columns:
    repeat(
      auto-fit,
      minmax(160px, 1fr)
    );
  gap: 14px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 7px;
  min-height: 100px;
  padding: 18px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.metric-label {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
}

.metric-value {
  color: #0f172a;
  font-size: 28px;
}

.metric-detail {
  color: #64748b;
  font-size: 12px;
}

.percentage-value {
  font-size: 28px;
}

.progress-track {
  width: 100%;
  height: 9px;
  overflow: hidden;
  background: #e5e7eb;
  border-radius: 999px;
}

.progress-fill {
  height: 100%;
  background: #334155;
  border-radius: inherit;
  transition: width 0.25s ease;
}

.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.distribution-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.distribution-heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.activity-distribution {
  margin-top: 24px;
}

.activity-distribution h3 {
  margin: 0 0 16px;
  font-size: 16px;
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

.report-table {
  width: 100%;
  min-width: 850px;
  border-collapse: collapse;
}

.report-table th,
.report-table td {
  padding: 13px 14px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.report-table th {
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.report-table td {
  color: #334155;
  font-size: 14px;
}

.report-table tbody tr:last-child td {
  border-bottom: none;
}

.user-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.user-cell span {
  color: #94a3b8;
  font-size: 12px;
}

.role-badge,
.status-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 5px 9px;
  font-size: 11px;
  font-weight: 700;
}

.role-admin {
  background: #f3e8ff;
  color: #7e22ce;
}

.role-user {
  background: #dbeafe;
  color: #1d4ed8;
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
  padding: 24px;
  text-align: center;
  background: #f8fafc;
  border-radius: 8px;
  color: #64748b;
}

@media (max-width: 768px) {
  .page-header,
  .section-header {
    flex-direction: column;
  }

  .section-meta {
    flex-wrap: wrap;
  }

  .filter-row {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-field input {
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
  }

  .filter-actions {
    width: 100%;
  }

  .filter-actions button {
    flex: 1;
  }
}
</style>
