<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  API_BASE_URL,
  OPERATIONS_API
} from '../config/api'

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
})

const filteredSummary = ref({
  total: 0,
  planned: 0,
  inProgress: 0,
  completed: 0,
})

const recentOperations = ref([])

const loading = ref(true)
const error = ref('')

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

  return Math.round(report.value.completionRate)
})

const displayedSummary = computed(() => {
  if (filtersActive.value) {
    return {
      total: filteredSummary.value.total,
      planned: filteredSummary.value.planned,
      inProgress: filteredSummary.value.inProgress,
      completed: filteredSummary.value.completed,
    }
  }

  return {
    total: report.value.totalOperations,
    planned: report.value.planned,
    inProgress: report.value.inProgress,
    completed: report.value.completed,
  }
})

function percentage(value, total) {
  if (!total) {
    return 0
  }

  return Math.round((value / total) * 100)
}

async function readError(response, fallback) {
  try {
    const data = await response.json()

    return data.error || data.message || fallback
  } catch {
    return fallback
  }
}

async function loadReport() {
  loading.value = true
  error.value = ''

  try {
    const recentParams = new URLSearchParams()

    recentParams.append('page', '0')
    recentParams.append('size', '5')
    recentParams.append('sortBy', 'operationDate')
    recentParams.append('direction', 'desc')

    const [
      reportResponse,
      summaryResponse,
      recentResponse,
    ] = await Promise.all([
      fetch(`${API_BASE_URL}/api/reports/summary`),
      fetch(`${OPERATIONS_API}/summary`),
      fetch(
        `${OPERATIONS_API}/search?${recentParams.toString()}`
      ),
    ])

    if (!reportResponse.ok) {
      throw new Error(
        await readError(
          reportResponse,
          'Failed to load report analytics'
        )
      )
    }

    if (!summaryResponse.ok) {
      throw new Error(
        await readError(
          summaryResponse,
          'Failed to load operation summary'
        )
      )
    }

    if (!recentResponse.ok) {
      throw new Error(
        await readError(
          recentResponse,
          'Failed to load recent operations'
        )
      )
    }

    report.value = await reportResponse.json()
    filteredSummary.value =
      await summaryResponse.json()

    const recentData =
      await recentResponse.json()

    recentOperations.value =
      recentData.content || []

    filtersActive.value = false
  } catch (err) {
    error.value =
      err instanceof Error
        ? err.message
        : 'Failed to load reports'
  } finally {
    loading.value = false
  }
}

async function applyDateFilter() {
  if (!startDate.value && !endDate.value) {
    await loadReport()
    return
  }

  if (!startDate.value || !endDate.value) {
    error.value =
      'Start date and end date must be provided together'
    return
  }

  if (startDate.value > endDate.value) {
    error.value =
      'Start date must not be after end date'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const summaryParams = new URLSearchParams()

    summaryParams.append(
      'startDate',
      startDate.value
    )

    summaryParams.append(
      'endDate',
      endDate.value
    )

    const recentParams =
      new URLSearchParams(summaryParams)

    recentParams.append('page', '0')
    recentParams.append('size', '5')
    recentParams.append(
      'sortBy',
      'operationDate'
    )
    recentParams.append(
      'direction',
      'desc'
    )

    const [
      summaryResponse,
      recentResponse,
    ] = await Promise.all([
      fetch(
        `${OPERATIONS_API}/summary?${summaryParams.toString()}`
      ),
      fetch(
        `${OPERATIONS_API}/search?${recentParams.toString()}`
      ),
    ])

    if (!summaryResponse.ok) {
      throw new Error(
        await readError(
          summaryResponse,
          'Failed to load filtered summary'
        )
      )
    }

    if (!recentResponse.ok) {
      throw new Error(
        await readError(
          recentResponse,
          'Failed to load filtered operations'
        )
      )
    }

    filteredSummary.value =
      await summaryResponse.json()

    const recentData =
      await recentResponse.json()

    recentOperations.value =
      recentData.content || []

    filtersActive.value = true
  } catch (err) {
    error.value =
      err instanceof Error
        ? err.message
        : 'Failed to apply report filter'
  } finally {
    loading.value = false
  }
}

function clearFilter() {
  startDate.value = ''
  endDate.value = ''

  loadReport()
}

onMounted(loadReport)
</script>

<template>
  <main>
    <div class="page-header">
      <div>
        <h1>Reports</h1>

        <p>
          Operation performance and audit analytics.
        </p>
      </div>

      <button
        class="refresh-button"
        :disabled="loading"
        @click="loadReport"
      >
        Refresh
      </button>
    </div>

    <section class="filters">
      <div class="filter-field">
        <label>Start Date</label>

        <input
          v-model="startDate"
          type="date"
        />
      </div>

      <div class="filter-field">
        <label>End Date</label>

        <input
          v-model="endDate"
          type="date"
        />
      </div>

      <button
        class="apply-button"
        :disabled="loading"
        @click="applyDateFilter"
      >
        Apply
      </button>

      <button
        class="clear-button"
        :disabled="loading"
        @click="clearFilter"
      >
        Clear
      </button>

      <span
        v-if="filtersActive"
        class="filter-status"
      >
        Operation data filtered by date
      </span>
    </section>

    <p
      v-if="loading"
      class="state-message"
    >
      Loading reports...
    </p>

    <p
      v-else-if="error"
      class="error-message"
    >
      {{ error }}
    </p>

    <template v-else>
      <section>
        <div class="section-heading">
          <div>
            <h2>Current Operations</h2>

            <p>
              Current operation status overview.
            </p>
          </div>
        </div>

        <div class="summary-grid">
          <div class="summary-card">
            <span class="card-label">
              Total Operations
            </span>

            <strong class="card-value">
              {{ displayedSummary.total }}
            </strong>
          </div>

          <div class="summary-card">
            <span class="card-label">
              Planned
            </span>

            <strong class="card-value">
              {{ displayedSummary.planned }}
            </strong>
          </div>

          <div class="summary-card">
            <span class="card-label">
              In Progress
            </span>

            <strong class="card-value">
              {{ displayedSummary.inProgress }}
            </strong>
          </div>

          <div class="summary-card">
            <span class="card-label">
              Completed
            </span>

            <strong class="card-value">
              {{ displayedSummary.completed }}
            </strong>
          </div>
        </div>
      </section>

      <section class="report-grid">
        <div class="panel">
          <div class="panel-header">
            <div>
              <h2>Completion Rate</h2>

              <p>
                Percentage of operations completed.
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
              {{ displayedSummary.completed }}
              completed
            </span>

            <span>
              {{ displayedSummary.total }}
              total
            </span>
          </div>
        </div>

        <div class="panel">
          <h2>Status Distribution</h2>

          <div class="distribution-list">
            <div class="distribution-item">
              <div class="distribution-header">
                <span>Planned</span>

                <strong>
                  {{
                    percentage(
                      displayedSummary.planned,
                      displayedSummary.total
                    )
                  }}%
                </strong>
              </div>

              <div class="small-track">
                <div
                  class="small-fill"
                  :style="{
                    width:
                      percentage(
                        displayedSummary.planned,
                        displayedSummary.total
                      ) + '%'
                  }"
                ></div>
              </div>
            </div>

            <div class="distribution-item">
              <div class="distribution-header">
                <span>In Progress</span>

                <strong>
                  {{
                    percentage(
                      displayedSummary.inProgress,
                      displayedSummary.total
                    )
                  }}%
                </strong>
              </div>

              <div class="small-track">
                <div
                  class="small-fill"
                  :style="{
                    width:
                      percentage(
                        displayedSummary.inProgress,
                        displayedSummary.total
                      ) + '%'
                  }"
                ></div>
              </div>
            </div>

            <div class="distribution-item">
              <div class="distribution-header">
                <span>Completed</span>

                <strong>
                  {{
                    percentage(
                      displayedSummary.completed,
                      displayedSummary.total
                    )
                  }}%
                </strong>
              </div>

              <div class="small-track">
                <div
                  class="small-fill"
                  :style="{
                    width:
                      percentage(
                        displayedSummary.completed,
                        displayedSummary.total
                      ) + '%'
                  }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="audit-section">
        <div class="section-heading">
          <div>
            <h2>Audit Activity</h2>

            <p>
              Historical operation activity
              recorded by the audit system.
            </p>
          </div>

          <span class="global-label">
            All-time activity
          </span>
        </div>

        <div class="audit-grid">
          <div class="audit-card">
            <span class="card-label">
              Total Events
            </span>

            <strong class="audit-value">
              {{ report.totalAuditEvents }}
            </strong>
          </div>

          <div class="audit-card">
            <span class="card-label">
              Created
            </span>

            <strong class="audit-value">
              {{ report.createdEvents }}
            </strong>
          </div>

          <div class="audit-card">
            <span class="card-label">
              Updated
            </span>

            <strong class="audit-value">
              {{ report.updatedEvents }}
            </strong>
          </div>

          <div class="audit-card">
            <span class="card-label">
              Status Changes
            </span>

            <strong class="audit-value">
              {{ report.statusChangedEvents }}
            </strong>
          </div>

          <div class="audit-card">
            <span class="card-label">
              Deleted
            </span>

            <strong class="audit-value">
              {{ report.deletedEvents }}
            </strong>
          </div>
        </div>
      </section>

      <section class="panel recent-panel">
        <div class="panel-header">
          <div>
            <h2>Recent Operations</h2>

            <p>
              Most recent operation records
              {{
                filtersActive
                  ? 'within the selected date range.'
                  : 'currently stored.'
              }}
            </p>
          </div>
        </div>

        <div class="table-container">
          <table v-if="recentOperations.length">
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
                {{ operation.title }}
              </td>

              <td>
                {{ operation.operationDate }}
              </td>

              <td>
                {{ operation.operationTime }}
              </td>

              <td>
                  <span class="status-badge">
                    {{ operation.status }}
                  </span>
              </td>
            </tr>
            </tbody>
          </table>

          <div
            v-else
            class="empty-state"
          >
            <strong>
              No operations found
            </strong>

            <p>
              No operation records match
              the current report criteria.
            </p>
          </div>
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.page-header h1 {
  margin-bottom: 8px;
}

.page-header p,
.section-heading p,
.panel p {
  margin-top: 0;
  color: #666;
}

.refresh-button,
.apply-button {
  padding: 10px 18px;
  border: none;
  border-radius: 5px;
  background: #1f2937;
  color: white;
  cursor: pointer;
}

.refresh-button:hover:not(:disabled),
.apply-button:hover:not(:disabled) {
  background: #374151;
}

.refresh-button:disabled,
.apply-button:disabled,
.clear-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.filters {
  display: flex;
  align-items: end;
  gap: 12px;
  margin: 24px 0;
  padding: 18px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-field label {
  font-size: 14px;
  font-weight: 600;
}

.filter-field input {
  padding: 9px 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background: white;
}

.clear-button {
  padding: 9px 16px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background: white;
  cursor: pointer;
}

.filter-status,
.global-label {
  padding: 6px 10px;
  border-radius: 12px;
  background: #f3f4f6;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}

.filter-status {
  margin-left: auto;
  align-self: center;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.section-heading h2 {
  margin-bottom: 6px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-top: 16px;
}

.summary-card,
.audit-card {
  display: flex;
  flex-direction: column;
  padding: 22px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.card-label {
  color: #666;
  font-size: 14px;
}

.card-value {
  margin-top: 10px;
  font-size: 32px;
  color: #1f2937;
}

.report-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  margin-top: 24px;
}

.panel {
  padding: 22px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.panel h2 {
  margin-top: 0;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.rate {
  font-size: 30px;
  color: #1f2937;
}

.progress-track,
.small-track {
  overflow: hidden;
  background: #e5e7eb;
  border-radius: 20px;
}

.progress-track {
  height: 14px;
  margin-top: 24px;
}

.progress-fill,
.small-fill {
  height: 100%;
  background: #374151;
  border-radius: 20px;
  transition: width 0.3s ease;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}

.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-top: 20px;
}

.distribution-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 7px;
}

.small-track {
  height: 9px;
}

.audit-section {
  margin-top: 30px;
}

.audit-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 18px;
  margin-top: 16px;
}

.audit-value {
  margin-top: 10px;
  font-size: 28px;
  color: #1f2937;
}

.recent-panel {
  margin-top: 30px;
}

.table-container {
  margin-top: 18px;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  border-bottom: 1px solid #ddd;
  text-align: left;
}

th {
  background: #e9edf2;
}

.status-badge {
  display: inline-block;
  padding: 5px 9px;
  border-radius: 12px;
  background: #f3f4f6;
  font-size: 12px;
  font-weight: 600;
}

.empty-state {
  padding: 36px;
  text-align: center;
  color: #666;
}

.empty-state strong {
  display: block;
  margin-bottom: 7px;
  color: #1f2937;
}

.empty-state p {
  margin: 0;
}

.state-message {
  margin-top: 24px;
}

.error-message {
  margin-top: 24px;
  padding: 12px;
  border: 1px solid #fecaca;
  border-radius: 5px;
  background: #fef2f2;
  color: #b91c1c;
}

@media (max-width: 1100px) {
  .audit-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .report-grid {
    grid-template-columns: 1fr;
  }

  .filters {
    flex-wrap: wrap;
  }

  .filter-status {
    margin-left: 0;
  }
}

@media (max-width: 650px) {
  .page-header,
  .section-heading {
    align-items: flex-start;
  }

  .summary-grid,
  .audit-grid {
    grid-template-columns: 1fr;
  }

  .filters {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-field input {
    width: auto;
  }
}
</style>
