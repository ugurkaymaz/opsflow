<script setup>
import { computed, onMounted, ref } from 'vue'
import { OPERATIONS_API } from '../config/api'

const summary = ref({
  total: 0,
  planned: 0,
  inProgress: 0,
  completed: 0,
})

const upcomingOperations = ref([])
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

async function loadDashboard() {
  loading.value = true
  error.value = ''

  try {
    const today = new Date()

    const todayString =
      today.getFullYear() +
      '-' +
      String(today.getMonth() + 1).padStart(2, '0') +
      '-' +
      String(today.getDate()).padStart(2, '0')

    const params = new URLSearchParams()

    params.append('startDate', todayString)
    params.append('endDate', '2099-12-31')
    params.append('page', '0')
    params.append('size', '5')
    params.append('sortBy', 'operationDate')
    params.append('direction', 'asc')

    const [summaryResponse, upcomingResponse] =
      await Promise.all([
        fetch(
          `${OPERATIONS_API}/summary`
        ),
        fetch(
          `${OPERATIONS_API}/search?${params.toString()}`
        ),
      ])

    if (!summaryResponse.ok) {
      const data = await summaryResponse.json()

      throw new Error(
        data.error ||
        'Failed to load dashboard summary'
      )
    }

    if (!upcomingResponse.ok) {
      const data = await upcomingResponse.json()

      throw new Error(
        data.error ||
        'Failed to load upcoming operations'
      )
    }

    summary.value =
      await summaryResponse.json()

    const upcomingData =
      await upcomingResponse.json()

    upcomingOperations.value =
      upcomingData.content
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <main>
    <div class="page-header">
      <div>
        <h1>Dashboard</h1>

        <p>
          Overview of your operation workflow.
        </p>
      </div>

      <button
        class="refresh-button"
        @click="loadDashboard"
      >
        Refresh
      </button>
    </div>

    <p v-if="loading">
      Loading dashboard...
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
      </section>

      <section class="dashboard-grid">
        <div class="panel">
          <div class="panel-header">
            <div>
              <h2>Workflow Progress</h2>

              <p>
                Overall completion rate.
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
        </div>

        <div class="panel">
          <h2>Status Overview</h2>

          <div class="status-list">
            <div class="status-item">
              <span>
                Planned
              </span>

              <strong>
                {{ summary.planned }}
              </strong>
            </div>

            <div class="status-item">
              <span>
                In Progress
              </span>

              <strong>
                {{ summary.inProgress }}
              </strong>
            </div>

            <div class="status-item">
              <span>
                Completed
              </span>

              <strong>
                {{ summary.completed }}
              </strong>
            </div>
          </div>
        </div>
      </section>

      <section class="panel upcoming-panel">
        <div class="panel-header">
          <div>
            <h2>
              Upcoming Operations
            </h2>

            <p>
              Next scheduled operations.
            </p>
          </div>
        </div>

        <table v-if="upcomingOperations.length">
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
            No upcoming operations
          </strong>

          <p>
            There are currently no future
            operations scheduled.
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

.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
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

.panel p {
  color: #666;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.rate {
  font-size: 28px;
  color: #1f2937;
}

.progress-track {
  height: 14px;
  margin-top: 24px;
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

.progress-details {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 20px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.status-item:last-child {
  border-bottom: none;
}

.upcoming-panel {
  margin-top: 24px;
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

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 650px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
