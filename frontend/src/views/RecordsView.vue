<script setup>
import { onMounted, ref } from 'vue'
import { OPERATIONS_API } from '../config/api'
import { apiFetch, ApiError } from '../services/api'

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

function getErrorMessage(
  err,
  fallbackMessage
) {
  if (err instanceof ApiError) {
    return err.message || fallbackMessage
  }

  return err?.message || fallbackMessage
}

async function loadRecords(page = 0) {
  loading.value = true
  error.value = ''

  try {
    const params =
      new URLSearchParams()

    params.append(
      'page',
      page
    )

    params.append(
      'size',
      pageSize.value
    )

    const response =
      await apiFetch(
        `${OPERATIONS_API}/history?${params.toString()}`
      )

    const data =
      await response.json()

    applyPageData(data)
  } catch (err) {
    error.value =
      getErrorMessage(
        err,
        'Failed to load operation history'
      )
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  if (
    page < 0 ||
    page >= totalPages.value
  ) {
    return
  }

  loadRecords(page)
}

function refreshRecords() {
  loadRecords(
    currentPage.value
  )
}

function formatEventType(eventType) {
  if (!eventType) {
    return '-'
  }

  switch (eventType) {
    case 'CREATED':
      return 'Create'

    case 'UPDATED':
      return 'Update'

    case 'STATUS_CHANGED':
      return 'Status Change'

    case 'DELETED':
      return 'Delete'

    default:
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
    formatStatus(
      record.oldStatus
    )

  const newStatus =
    formatStatus(
      record.newStatus
    )

  if (
    record.eventType === 'CREATED'
  ) {
    return `- → ${newStatus}`
  }

  if (
    record.eventType === 'DELETED'
  ) {
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

function roleClass(role) {
  switch (role) {
    case 'ADMIN':
      return 'role-admin'

    case 'USER':
      return 'role-user'

    default:
      return 'role-unknown'
  }
}

function displayValue(value) {
  if (
    value === null ||
    value === undefined ||
    value === ''
  ) {
    return '-'
  }

  return value
}

function openRecord(record) {
  selectedRecord.value = record
}

function closeRecord() {
  selectedRecord.value = null
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
          {{
            totalPages
              ? currentPage + 1
              : 0
          }}
          / {{ totalPages }}
        </strong>
      </div>
    </section>

    <div
      v-if="loading"
      class="state-message"
    >
      Loading operation history...
    </div>

    <div
      v-else-if="error"
      class="error-state"
    >
      <strong>
        Unable to load audit records
      </strong>

      <p>
        {{ error }}
      </p>

      <button
        class="retry-button"
        @click="
          loadRecords(currentPage)
        "
      >
        Try Again
      </button>
    </div>

    <template v-else>
      <div class="table-container">
        <table
          v-if="records.length"
        >
          <thead>
          <tr>
            <th>Action</th>
            <th>Operation ID</th>
            <th>Operation Title</th>
            <th>Performed By</th>
            <th>User ID</th>
            <th>Email</th>
            <th>Role</th>
            <th>Timestamp</th>
          </tr>
          </thead>

          <tbody>
          <tr
            v-for="record in records"
            :key="record.id"
            class="record-row"
            @click="
                openRecord(record)
              "
          >
            <td>
                <span
                  class="event-badge"
                  :class="
                    eventClass(
                      record.eventType
                    )
                  "
                >
                  {{
                    formatEventType(
                      record.eventType
                    )
                  }}
                </span>
            </td>

            <td class="id-cell">
              #{{ record.operationId }}
            </td>

            <td class="title-cell">
              {{
                record.operationTitle ||
                '-'
              }}
            </td>

            <td class="user-name-cell">
              {{
                displayValue(
                  record.userName
                )
              }}
            </td>

            <td class="id-cell">
              <template
                v-if="
                    record.userId !== null &&
                    record.userId !== undefined
                  "
              >
                #{{ record.userId }}
              </template>

              <template v-else>
                -
              </template>
            </td>

            <td class="email-cell">
              {{
                displayValue(
                  record.userEmail
                )
              }}
            </td>

            <td>
                <span
                  v-if="record.userRole"
                  class="role-badge"
                  :class="
                    roleClass(
                      record.userRole
                    )
                  "
                >
                  {{ record.userRole }}
                </span>

              <span v-else>
                  -
                </span>
            </td>

            <td class="date-cell">
              {{
                formatDateTime(
                  record.createdAt
                )
              }}
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
          {{ totalElements }}
          audit records
        </div>

        <div class="page-controls">
          <button
            :disabled="
              currentPage === 0
            "
            @click="
              changePage(
                currentPage - 1
              )
            "
          >
            Previous
          </button>

          <span>
            Page
            {{
              totalPages
                ? currentPage + 1
                : 0
            }}
            of {{ totalPages }}
          </span>

          <button
            :disabled="
              totalPages === 0 ||
              currentPage >=
                totalPages - 1
            "
            @click="
              changePage(
                currentPage + 1
              )
            "
          >
            Next
          </button>
        </div>
      </div>
    </template>

    <div
      v-if="selectedRecord"
      class="modal-overlay"
      @click.self="closeRecord"
    >
      <div class="modal">
        <div class="modal-header">
          <div>
            <h2>Audit Record</h2>

            <p>
              History entry
              #{{ selectedRecord.id }}
            </p>
          </div>

          <button
            class="close-button"
            @click="closeRecord"
          >
            ×
          </button>
        </div>

        <section class="detail-section">
          <h3>Activity</h3>

          <div class="detail-row">
            <strong>
              History ID
            </strong>

            <span>
              #{{ selectedRecord.id }}
            </span>
          </div>

          <div class="detail-row">
            <strong>Action</strong>

            <span>
              <span
                class="event-badge"
                :class="
                  eventClass(
                    selectedRecord.eventType
                  )
                "
              >
                {{
                  formatEventType(
                    selectedRecord.eventType
                  )
                }}
              </span>
            </span>
          </div>

          <div class="detail-row">
            <strong>
              Operation ID
            </strong>

            <span>
              #{{
                selectedRecord.operationId
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>
              Operation Title
            </strong>

            <span>
              {{
                selectedRecord.operationTitle ||
                '-'
              }}
            </span>
          </div>
        </section>

        <section class="detail-section">
          <h3>Performed By</h3>

          <div class="detail-row">
            <strong>Name</strong>

            <span>
              {{
                displayValue(
                  selectedRecord.userName
                )
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>User ID</strong>

            <span>
              <template
                v-if="
                  selectedRecord.userId !== null &&
                  selectedRecord.userId !== undefined
                "
              >
                #{{
                  selectedRecord.userId
                }}
              </template>

              <template v-else>
                -
              </template>
            </span>
          </div>

          <div class="detail-row">
            <strong>Email</strong>

            <span>
              {{
                displayValue(
                  selectedRecord.userEmail
                )
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>Role</strong>

            <span>
              <span
                v-if="
                  selectedRecord.userRole
                "
                class="role-badge"
                :class="
                  roleClass(
                    selectedRecord.userRole
                  )
                "
              >
                {{
                  selectedRecord.userRole
                }}
              </span>

              <span v-else>
                -
              </span>
            </span>
          </div>
        </section>

        <section class="detail-section">
          <h3>Status</h3>

          <div class="detail-row">
            <strong>
              Old Status
            </strong>

            <span>
              {{
                formatStatus(
                  selectedRecord.oldStatus
                )
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>
              New Status
            </strong>

            <span>
              {{
                formatStatus(
                  selectedRecord.newStatus
                )
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>
              Status Change
            </strong>

            <span class="status-change">
              {{
                getStatusChange(
                  selectedRecord
                )
              }}
            </span>
          </div>
        </section>

        <section class="detail-section">
          <h3>Timestamp</h3>

          <div class="detail-row">
            <strong>
              Recorded At
            </strong>

            <span>
              {{
                formatDateTime(
                  selectedRecord.createdAt
                )
              }}
            </span>
          </div>
        </section>
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
  min-width: 1150px;
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
  white-space: nowrap;
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

.id-cell {
  white-space: nowrap;
  font-weight: 600;
}

.title-cell {
  min-width: 170px;
}

.user-name-cell {
  min-width: 130px;
  font-weight: 600;
}

.email-cell {
  min-width: 180px;
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

.role-badge {
  display: inline-block;
  padding: 5px 9px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.role-admin {
  background: #f3e8ff;
  color: #7e22ce;
}

.role-user {
  background: #e0f2fe;
  color: #0369a1;
}

.role-unknown {
  background: #f3f4f6;
  color: #4b5563;
}

.status-change {
  font-weight: 600;
  color: #374151;
  white-space: nowrap;
}

.state-message {
  margin-top: 24px;
  padding: 20px;
  text-align: center;
  color: #4b5563;
}

.error-state {
  margin-top: 24px;
  padding: 24px;
  border: 1px solid #fecaca;
  border-radius: 8px;
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
  border-radius: 5px;
  background: #1f2937;
  color: white;
  cursor: pointer;
}

.retry-button:hover {
  background: #374151;
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
  width: 620px;
  max-width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow:
    0 10px 30px
    rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 22px;
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

.detail-section {
  margin-top: 22px;
}

.detail-section:first-of-type {
  margin-top: 0;
}

.detail-section h3 {
  margin: 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #e5e7eb;
  color: #1f2937;
  font-size: 15px;
}

.detail-row {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row strong {
  color: #374151;
}

.detail-row span {
  overflow-wrap: anywhere;
}

@media (max-width: 900px) {
  .table-container {
    overflow-x: auto;
  }
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
