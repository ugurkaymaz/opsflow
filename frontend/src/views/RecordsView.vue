<script setup>
import { onMounted, ref } from 'vue'
import { OPERATIONS_API } from '../config/api'

const records = ref([])
const loading = ref(true)
const error = ref('')

const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)

const startDate = ref('')
const endDate = ref('')
const startTime = ref('')
const endTime = ref('')

const sortBy = ref('operationDate')
const sortDirection = ref('desc')

const filtersActive = ref(false)
const selectedRecord = ref(null)

function applyPageData(data) {
  records.value = data.content
  currentPage.value = data.number
  totalPages.value = data.totalPages
  totalElements.value = data.totalElements
}

async function loadRecords(page = 0, useFilters = false) {
  loading.value = true
  error.value = ''

  try {
    const params = new URLSearchParams()

    if (useFilters) {
      if (startDate.value) {
        params.append('startDate', startDate.value)
      }

      if (endDate.value) {
        params.append('endDate', endDate.value)
      }

      if (startTime.value) {
        params.append('startTime', startTime.value)
      }

      if (endTime.value) {
        params.append('endTime', endTime.value)
      }
    }

    params.append('page', page)
    params.append('size', pageSize.value)
    params.append('sortBy', sortBy.value)
    params.append('direction', sortDirection.value)

    const response = await fetch(
      `${OPERATIONS_API}/search?${params.toString()}`
    )

    if (!response.ok) {
      const data = await response.json()

      throw new Error(
        data.error || 'Failed to load records'
      )
    }

    const data = await response.json()

    applyPageData(data)
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

function searchRecords() {
  filtersActive.value = true
  loadRecords(0, true)
}

function clearFilters() {
  startDate.value = ''
  endDate.value = ''
  startTime.value = ''
  endTime.value = ''

  sortBy.value = 'operationDate'
  sortDirection.value = 'desc'

  filtersActive.value = false

  loadRecords(0, false)
}

function changePage(page) {
  loadRecords(page, filtersActive.value)
}

onMounted(() => {
  loadRecords(0, false)
})
</script>

<template>
  <main>
    <h1>Records</h1>

    <p>Browse and filter operation records.</p>

    <div class="filters">
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

      <div class="filter-field">
        <label>Start Time</label>

        <input
          v-model="startTime"
          type="time"
        />
      </div>

      <div class="filter-field">
        <label>End Time</label>

        <input
          v-model="endTime"
          type="time"
        />
      </div>

      <div class="filter-field">
        <label>Sort By</label>

        <select v-model="sortBy">
          <option value="operationDate">
            Date
          </option>

          <option value="operationTime">
            Time
          </option>

          <option value="title">
            Title
          </option>

          <option value="status">
            Status
          </option>

          <option value="id">
            ID
          </option>
        </select>
      </div>

      <div class="filter-field">
        <label>Direction</label>

        <select v-model="sortDirection">
          <option value="desc">
            Descending
          </option>

          <option value="asc">
            Ascending
          </option>
        </select>
      </div>

      <button
        class="search-button"
        @click="searchRecords"
      >
        Search
      </button>

      <button
        class="clear-button"
        @click="clearFilters"
      >
        Clear
      </button>
    </div>

    <p v-if="loading">
      Loading records...
    </p>

    <p
      v-else-if="error"
      class="error-message"
    >
      {{ error }}
    </p>

    <template v-else>
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
          v-for="record in records"
          :key="record.id"
          class="record-row"
          @click="selectedRecord = record"
        >
          <td>{{ record.id }}</td>

          <td>{{ record.title }}</td>

          <td>
            {{ record.operationDate }}
          </td>

          <td>
            {{ record.operationTime }}
          </td>

          <td>{{ record.status }}</td>
        </tr>
        </tbody>
      </table>

      <div
        v-if="records.length === 0"
        class="no-records"
      >
        No records found.
      </div>

      <div class="pagination">
        <div class="record-count">
          {{ totalElements }} records
        </div>

        <div class="page-controls">
          <button
            :disabled="currentPage === 0"
            @click="changePage(currentPage - 1)"
          >
            Previous
          </button>

          <span>
            Page {{ currentPage + 1 }}
            of {{ totalPages || 1 }}
          </span>

          <button
            :disabled="currentPage >= totalPages - 1"
            @click="changePage(currentPage + 1)"
          >
            Next
          </button>
        </div>
      </div>
    </template>

    <div
      v-if="selectedRecord"
      class="modal-overlay"
      @click.self="selectedRecord = null"
    >
      <div class="modal">
        <div class="modal-header">
          <h2>Record Details</h2>

          <button
            class="close-button"
            @click="selectedRecord = null"
          >
            ×
          </button>
        </div>

        <div class="detail-row">
          <strong>ID</strong>

          <span>
            {{ selectedRecord.id }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Title</strong>

          <span>
            {{ selectedRecord.title }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Description</strong>

          <span>
            {{ selectedRecord.description || '-' }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Date</strong>

          <span>
            {{ selectedRecord.operationDate }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Time</strong>

          <span>
            {{ selectedRecord.operationTime }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Status</strong>

          <span>
            {{ selectedRecord.status }}
          </span>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.filters {
  display: flex;
  align-items: end;
  gap: 12px;
  margin: 24px 0;
  padding: 18px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
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

.filter-field input,
.filter-field select {
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background: white;
}

.search-button,
.clear-button {
  padding: 9px 16px;
  border-radius: 5px;
  cursor: pointer;
}

.search-button {
  border: none;
  background: #1f2937;
  color: white;
}

.clear-button {
  border: 1px solid #ccc;
  background: white;
}

table {
  width: 100%;
  margin-top: 24px;
  border-collapse: collapse;
  background: white;
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

.record-row {
  cursor: pointer;
}

.record-row:hover {
  background: #f3f4f6;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.record-count {
  color: #555;
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-controls button {
  padding: 8px 14px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background: white;
  cursor: pointer;
}

.page-controls button:hover:not(:disabled) {
  background: #f0f0f0;
}

.page-controls button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error-message {
  color: #b91c1c;
}

.no-records {
  padding: 24px;
  text-align: center;
  background: white;
  color: #666;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
}

.modal {
  width: 500px;
  max-width: 90%;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.modal-header h2 {
  margin: 0;
}

.close-button {
  border: none;
  background: transparent;
  font-size: 28px;
  cursor: pointer;
}

.detail-row {
  display: grid;
  grid-template-columns: 130px 1fr;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.detail-row:last-child {
  border-bottom: none;
}
</style>
