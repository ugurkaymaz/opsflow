<script setup>
import { onMounted, ref } from 'vue'
import { OPERATIONS_API } from '../config/api'

const summary = ref({
  total: 0,
  planned: 0,
  inProgress: 0,
  completed: 0,
})

const recentOperations = ref([])

const startDate = ref('')
const endDate = ref('')

const loading = ref(true)
const error = ref('')

const reportFiltered = ref(false)

function percentage(count) {
  if (summary.value.total === 0) {
    return 0
  }

  return Math.round(
    (count / summary.value.total) * 100
  )
}

async function loadReports(useDateFilter = false) {
  loading.value = true
  error.value = ''

  try {
    const summaryParams = new URLSearchParams()
    const recentParams = new URLSearchParams()

    if (useDateFilter) {
      if (!startDate.value || !endDate.value) {
        throw new Error(
          'Start Date and End Date are required'
        )
      }

      if (startDate.value > endDate.value) {
        throw new Error(
          'Start Date must not be after End Date'
        )
      }

      summaryParams.append(
        'startDate',
        startDate.value
      )

      summaryParams.append(
        'endDate',
        endDate.value
      )

      recentParams.append(
        'startDate',
        startDate.value
      )

      recentParams.append(
        'endDate',
        endDate.value
      )
    }

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

    const summaryUrl =
      summaryParams.toString()
        ? `${OPERATIONS_API}/summary?${summaryParams.toString()}`
        : `${OPERATIONS_API}/summary`

    const recentUrl =
      `${OPERATIONS_API}/search?${recentParams.toString()}`

    const [
      summaryResponse,
      recentResponse,
    ] = await Promise.all([
      fetch(summaryUrl),
      fetch(recentUrl),
    ])

    if (!summaryResponse.ok) {
      const data = await summaryResponse.json()

      throw new Error(
        data.error ||
        'Failed to load operation summary'
      )
    }

    if (!recentResponse.ok) {
      const data = await recentResponse.json()

      throw new Error(
        data.error ||
        'Failed to load recent operations'
      )
    }

    summary.value =
      await summaryResponse.json()

    const recentData =
      await recentResponse.json()

    recentOperations.value =
      recentData.content

    reportFiltered.value = useDateFilter
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

function generateReport() {
  loadReports(true)
}

function clearReport() {
  startDate.value = ''
  endDate.value = ''

  reportFiltered.value = false

  loadReports(false)
}

onMounted(() => {
  loadReports(false)
})
</script>

<template>
  <main>
    <div class="page-header">
      <div>
        <h1>Reports</h1>

        <p>
          Operation summary and status overview.
        </p>
      </div>

      <button
        class="refresh-button"
        @click="loadReports(reportFiltered)"
      >
        Refresh
      </button>
    </div>

    <section class="report-filter">
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
        class="generate-button"
        @click="generateReport"
      >
        Generate Report
      </button>

      <button
        class="clear-button"
        @click="clearReport"
      >
        Clear
      </button>
    </section>

    <p
      v-if="reportFiltered"
      class="filter-message"
    >
      Showing report from
      <strong>{{ startDate }}</strong>
      to
      <strong>{{ endDate }}</strong>
    </p>

    <p v-if="loading">
      Loading report...
    </p>

    <p
      v-else-if="error"
      class="error-message"
    >
      {{ error }}
    </p>

    <template v-else>
      <section class="summary-grid">
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

          <span class="card-percentage">
            {{ percentage(summary.planned) }}%
          </span>
        </div>

        <div class="summary-card">
          <span class="card-label">
            In Progress
          </span>

          <strong class="card-value">
            {{ summary.inProgress }}
          </strong>

          <span class="card-percentage">
            {{ percentage(summary.inProgress) }}%
          </span>
        </div>

        <div class="summary-card">
          <span class="card-label">
            Completed
          </span>

          <strong class="card-value">
            {{ summary.completed }}
          </strong>

          <span class="card-percentage">
            {{ percentage(summary.completed) }}%
          </span>
        </div>
      </section>

      <section class="report-panel">
        <h2>Status Distribution</h2>

        <div class="status-row">
          <div class="status-info">
            <span>Planned</span>

            <strong>
              {{ summary.planned }}
              /
              {{ summary.total }}
            </strong>
          </div>

          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{
                width:
                  percentage(summary.planned) + '%'
              }"
            ></div>
          </div>

          <span class="percentage">
            {{ percentage(summary.planned) }}%
          </span>
        </div>

        <div class="status-row">
          <div class="status-info">
            <span>In Progress</span>

            <strong>
              {{ summary.inProgress }}
              /
              {{ summary.total }}
            </strong>
          </div>

          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{
                width:
                  percentage(summary.inProgress) + '%'
              }"
            ></div>
          </div>

          <span class="percentage">
            {{ percentage(summary.inProgress) }}%
          </span>
        </div>

        <div class="status-row">
          <div class="status-info">
            <span>Completed</span>

            <strong>
              {{ summary.completed }}
              /
              {{ summary.total }}
            </strong>
          </div>

          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{
                width:
                  percentage(summary.completed) + '%'
              }"
            ></div>
          </div>

          <span class="percentage">
            {{ percentage(summary.completed) }}%
          </span>
        </div>
      </section>

      <section class="report-panel">
        <div class="panel-header">
          <div>
            <h2>Recent Operations</h2>

            <p v-if="reportFiltered">
              Latest operations within the
              selected date range.
            </p>

            <p v-else>
              Latest 5 operation records.
            </p>
          </div>
        </div>

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
              {{ operation.id }}
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
            There are no operations for the
            selected report period.
          </p>
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
}

.page-header h1 {
  margin-bottom: 8px;
}

.page-header p {
  margin-top: 0;
  color: #666;
}

.refresh-button {
  padding: 10px 18px;
  border: none;
  border-radius: 5px;
  background: #1f2937;
  color: white;
  cursor: pointer;
}

.refresh-button:hover {
  background: #374151;
}

.report-filter {
  display: flex;
  align-items: end;
  gap: 12px;
  margin-top: 24px;
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
}

.generate-button,
.clear-button {
  padding: 10px 16px;
  border-radius: 5px;
  cursor: pointer;
}

.generate-button {
  border: none;
  background: #1f2937;
  color: white;
}

.generate-button:hover {
  background: #374151;
}

.clear-button {
  border: 1px solid #ccc;
  background: white;
}

.clear-button:hover {
  background: #f3f4f6;
}

.filter-message {
  margin-top: 14px;
  color: #555;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-top: 28px;
}

.summary-card {
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

.card-percentage {
  margin-top: 6px;
  color: #666;
  font-size: 14px;
}

.report-panel {
  margin-top: 24px;
  padding: 22px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.report-panel h2 {
  margin-top: 0;
}

.status-row {
  display: grid;
  grid-template-columns: 170px 1fr 60px;
  align-items: center;
  gap: 18px;
  margin-top: 22px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-info span {
  color: #555;
}

.progress-track {
  height: 12px;
  overflow: hidden;
  background: #e5e7eb;
  border-radius: 20px;
}

.progress-fill {
  height: 100%;
  background: #374151;
  border-radius: 20px;
  transition: width 0.3s ease;
}

.percentage {
  text-align: right;
  font-weight: 600;
}

.panel-header p {
  margin-top: -8px;
  color: #666;
}

table {
  width: 100%;
  margin-top: 20px;
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
  background: #f3f4f6;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.error-message {
  margin-top: 18px;
  padding: 12px;
  border: 1px solid #fecaca;
  border-radius: 5px;
  background: #fef2f2;
  color: #b91c1c;
}

.empty-state {
  padding: 32px;
  text-align: center;
  color: #666;
}

.empty-state strong {
  display: block;
  margin-bottom: 6px;
  color: #1f2937;
}

@media (max-width: 1000px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .report-filter {
    flex-wrap: wrap;
  }
}

@media (max-width: 650px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .status-row {
    grid-template-columns: 1fr;
  }

  .percentage {
    text-align: left;
  }
}
</style>
