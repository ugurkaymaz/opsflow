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

const selectedRecord = ref(null)

function applyPageData(data) {
  records.value = data.content || []
  currentPage.value = data.number ?? 0
  totalPages.value = data.totalPages ?? 0
  totalElements.value = data.totalElements ?? 0
}

async function loadRecords(page = 0) {
  loading.value = true
  error.value = ''

  try {
    const params = new URLSearchParams()

    params.append('page', page)
    params.append('size', pageSize.value)

    const response = await fetch(
      `${OPERATIONS_API}/history?${params.toString()}`
    )

    if (!response.ok) {
      let message = 'Failed to load operation history'

      try {
        const data = await response.json()

        if (data.error) {
          message = data.error
        }
      } catch {
        // Keep the default message.
      }

      throw new Error(message)
    }

    const data = await response.json()

    applyPageData(data)
  } catch (err) {
    error.value =
      err instanceof Error
        ? err.message
        : 'Failed to load operation history'
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  if (page < 0 || page >= totalPages.value) {
    return
  }

  loadRecords(page)
}

function refreshRecords() {
  loadRecords(currentPage.value)
}

function formatEventType(eventType) {
  if (!eventType) {
    return '-'
  }

  return eventType
    .toLowerCase()
    .split('_')
    .map(
      word =>
        word.charAt(0).toUpperCase() +
        word.slice(1)
    )
    .join(' ')
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

function formatDateTime(value) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat(
    undefined,
    {
      year: 'numeric',
      month: 'short',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }
  ).format(date)
}

function getStatusChange(record) {
  const oldStatus =
    formatStatus(record.oldStatus)

  const newStatus =
    formatStatus(record.newStatus)

  if (record.eventType === 'CREATED') {
    return `- → ${newStatus}`
  }

  if (record.eventType === 'DELETED') {
    return `${oldStatus} → -`
  }

  return `${oldStatus} → ${newStatus}`
}

function eventClass(eventType) {
  switch (eventType) {
    case 'CREATED':
      return 'event-created'

    case 'UPDATED':
      return 'event-updated'

    case 'STATUS_CHANGED':
      return 'event-status-changed'

    case 'DELETED':
      return 'event-deleted'

    default:
      return ''
  }
}

onMounted(() => {
  loadRecords(0)
})
</script>

<template>
  <main>
    <div class="page-header">
      <div>
        <h1>Records</h1>

        <p>
          Audit history of operation activity.
        </p>
      </div>

      <button
        class="refresh-button"
        :disabled="loading"
        @click="refreshRecords"
      >
        Refresh
      </button>
    </div>

    <section class="summary-bar">
      <div>
        <span class="summary-label">
          Audit Records
        </span>

        <strong>
          {{ totalElements }}
        </strong>
      </div>

      <div>
        <span class="summary-label">
          Page
        </span>

        <strong>
          {{ totalPages ? currentPage + 1 : 0 }}
          / {{ totalPages }}
        </strong>
      </div>
    </section>

    <p
      v-if="loading"
      class="state-message"
    >
      Loading operation history...
    </p>

    <p
      v-else-if="error"
      class="error-message"
    >
      {{ error }}
    </p>

    <template v-else>
      <div class="table-container">
        <table v-if="records.length">
          <thead>
          <tr>
            <th>Time</th>
            <th>Operation</th>
            <th>Title</th>
            <th>Event</th>
            <th>Status Change</th>
          </tr>
          </thead>

          <tbody>
          <tr
            v-for="record in records"
            :key="record.id"
            class="record-row"
            @click="selectedRecord = record"
          >
            <td class="date-cell">
              {{ formatDateTime(record.createdAt) }}
            </td>

            <td>
              #{{ record.operationId }}
            </td>

            <td>
              {{ record.operationTitle || '-' }}
            </td>

            <td>
                <span
                  class="event-badge"
                  :class="eventClass(record.eventType)"
                >
                  {{ formatEventType(record.eventType) }}
                </span>
            </td>

            <td>
                <span class="status-change">
                  {{ getStatusChange(record) }}
                </span>
            </td>
          </tr>
          </tbody>
        </table>

        <div
          v-else
          class="no-records"
        >
          <strong>
            No audit records found
          </strong>

          <p>
            Operation activity will appear here
            when records are created, updated,
            changed or deleted.
          </p>
        </div>
      </div>

      <div class="pagination">
        <div class="record-count">
          {{ totalElements }} audit records
        </div>

        <div class="page-controls">
          <button
            :disabled="currentPage === 0"
            @click="changePage(currentPage - 1)"
          >
            Previous
          </button>

          <span>
            Page {{ totalPages ? currentPage + 1 : 0 }}
            of {{ totalPages }}
          </span>

          <button
            :disabled="
              totalPages === 0 ||
              currentPage >= totalPages - 1
            "
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
          <div>
            <h2>Audit Record</h2>

            <p>
              History entry #{{ selectedRecord.id }}
            </p>
          </div>

          <button
            class="close-button"
            @click="selectedRecord = null"
          >
            ×
          </button>
        </div>

        <div class="detail-row">
          <strong>History ID</strong>

          <span>
            #{{ selectedRecord.id }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Operation ID</strong>

          <span>
            #{{ selectedRecord.operationId }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Title</strong>

          <span>
            {{ selectedRecord.operationTitle || '-' }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Event</strong>

          <span
            class="event-badge"
            :class="
              eventClass(selectedRecord.eventType)
            "
          >
            {{
              formatEventType(
                selectedRecord.eventType
              )
            }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Old Status</strong>

          <span>
            {{
              formatStatus(
                selectedRecord.oldStatus
              )
            }}
          </span>
        </div>

        <div class="detail-row">
          <strong>New Status</strong>

          <span>
            {{
              formatStatus(
                selectedRecord.newStatus
              )
            }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Status Change</strong>

          <span>
            {{ getStatusChange(selectedRecord) }}
          </span>
        </div>

        <div class="detail-row">
          <strong>Recorded At</strong>

          <span>
            {{
              formatDateTime(
                selectedRecord.createdAt
              )
            }}
          </span>
        </div>
      </div>
    </div>
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

.page-header p {
  margin: 0;
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

.refresh-button:hover:not(:disabled) {
  background: #374151;
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.summary-bar {
  display: flex;
  gap: 18px;
  margin-top: 24px;
}

.summary-bar > div {
  min-width: 150px;
  padding: 16px 20px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.summary-bar strong {
  display: block;
  margin-top: 6px;
  font-size: 22px;
  color: #1f2937;
}

.summary-label {
  color: #666;
  font-size: 13px;
}

.table-container {
  margin-top: 24px;
  overflow-x: auto;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 14px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  vertical-align: middle;
}

th {
  background: #e9edf2;
  color: #374151;
  font-size: 13px;
}

tbody tr:last-child td {
  border-bottom: none;
}

.record-row {
  cursor: pointer;
}

.record-row:hover {
  background: #f8fafc;
}

.date-cell {
  white-space: nowrap;
}

.event-badge {
  display: inline-block;
  padding: 5px 9px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
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

.status-change {
  font-weight: 600;
  color: #374151;
  white-space: nowrap;
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

.no-records {
  padding: 48px 24px;
  text-align: center;
  color: #666;
}

.no-records strong {
  display: block;
  margin-bottom: 8px;
  color: #1f2937;
}

.no-records p {
  margin: 0;
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

.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
}

.modal {
  width: 540px;
  max-width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.modal-header h2 {
  margin: 0 0 5px;
}

.modal-header p {
  margin: 0;
  color: #666;
}

.close-button {
  border: none;
  background: transparent;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.detail-row {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.detail-row:last-child {
  border-bottom: none;
}

@media (max-width: 700px) {
  .page-header {
    align-items: flex-start;
  }

  .summary-bar {
    flex-direction: column;
  }

  .summary-bar > div {
    width: auto;
  }

  .pagination {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .detail-row {
    grid-template-columns: 1fr;
    gap: 5px;
  }
}
</style>
