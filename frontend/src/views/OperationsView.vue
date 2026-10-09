<script setup>
import { computed, onMounted, ref } from 'vue'
import { OPERATIONS_API } from '../config/api'
import { apiFetch, ApiError } from '../services/api'
import { getCurrentUser } from '../services/auth'

const currentUser = getCurrentUser()

const isAdmin = computed(
  () => currentUser?.role === 'ADMIN'
)

const operations = ref([])
const loading = ref(true)

const pageError = ref('')
const createError = ref('')
const editError = ref('')
const deleteError = ref('')

const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)

const startDate = ref('')
const endDate = ref('')
const startTime = ref('')
const endTime = ref('')
const status = ref('')

const filtersActive = ref(false)

const showCreateForm = ref(false)
const selectedOperation = ref(null)
const editMode = ref(false)

const sortBy = ref('operationDate')
const sortDirection = ref('desc')

const editOperation = ref({
  title: '',
  description: '',
  operationDate: '',
  operationTime: '',
  status: '',
})

const newOperation = ref({
  title: '',
  description: '',
  operationDate: '',
  operationTime: '',
  status: 'PLANNED',
})

function applyPageData(data) {
  operations.value = data.content
  currentPage.value = data.number
  totalPages.value = data.totalPages
  totalElements.value = data.totalElements
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

    return err.message || fallbackMessage
  }

  return err?.message || fallbackMessage
}

async function getOperations(
  page = 0,
  useFilters = false
) {
  loading.value = true
  pageError.value = ''

  try {
    const params =
      new URLSearchParams()

    if (useFilters) {
      if (startDate.value) {
        params.append(
          'startDate',
          startDate.value
        )
      }

      if (endDate.value) {
        params.append(
          'endDate',
          endDate.value
        )
      }

      if (startTime.value) {
        params.append(
          'startTime',
          startTime.value
        )
      }

      if (endTime.value) {
        params.append(
          'endTime',
          endTime.value
        )
      }

      if (status.value) {
        params.append(
          'status',
          status.value
        )
      }
    }

    params.append(
      'page',
      page
    )

    params.append(
      'size',
      pageSize.value
    )

    params.append(
      'sortBy',
      sortBy.value
    )

    params.append(
      'direction',
      sortDirection.value
    )

    const response =
      await apiFetch(
        `${OPERATIONS_API}/search?${params.toString()}`
      )

    const data =
      await response.json()

    applyPageData(data)
  } catch (err) {
    pageError.value =
      getErrorMessage(
        err,
        'Failed to load operations'
      )
  } finally {
    loading.value = false
  }
}

function toggleCreateForm() {
  showCreateForm.value =
    !showCreateForm.value

  createError.value = ''
}

function clearFilters() {
  startDate.value = ''
  endDate.value = ''
  startTime.value = ''
  endTime.value = ''
  status.value = ''

  sortBy.value = 'operationDate'
  sortDirection.value = 'desc'

  filtersActive.value = false

  getOperations(
    0,
    false
  )
}

function searchOperations() {
  filtersActive.value = true

  getOperations(
    0,
    true
  )
}

function changePage(page) {
  if (
    page < 0 ||
    page >= totalPages.value
  ) {
    return
  }

  getOperations(
    page,
    filtersActive.value
  )
}

function openOperation(operation) {
  selectedOperation.value =
    operation

  editMode.value = false
  editError.value = ''
  deleteError.value = ''
}

function closeOperation() {
  selectedOperation.value = null
  editMode.value = false
  editError.value = ''
  deleteError.value = ''
}

function startEdit() {
  editOperation.value = {
    title:
    selectedOperation.value.title,

    description:
      selectedOperation.value
        .description || '',

    operationDate:
    selectedOperation.value
      .operationDate,

    operationTime:
    selectedOperation.value
      .operationTime,

    status:
    selectedOperation.value.status,
  }

  editMode.value = true
  editError.value = ''
  deleteError.value = ''
}

function cancelEdit() {
  editMode.value = false
  editError.value = ''
}

async function updateOperation() {
  editError.value = ''

  try {
    const response =
      await apiFetch(
        `${OPERATIONS_API}/${selectedOperation.value.id}`,
        {
          method: 'PUT',

          headers: {
            'Content-Type':
              'application/json',
          },

          body: JSON.stringify(
            editOperation.value
          ),
        }
      )

    const updatedOperation =
      await response.json()

    selectedOperation.value =
      updatedOperation

    editMode.value = false
    editError.value = ''

    await getOperations(
      currentPage.value,
      filtersActive.value
    )
  } catch (err) {
    editError.value =
      getErrorMessage(
        err,
        'Failed to update operation'
      )
  }
}

async function deleteOperation() {
  const confirmed =
    window.confirm(
      `Delete "${selectedOperation.value.title}"?`
    )

  if (!confirmed) {
    return
  }

  deleteError.value = ''

  try {
    await apiFetch(
      `${OPERATIONS_API}/${selectedOperation.value.id}`,
      {
        method: 'DELETE',
      }
    )

    selectedOperation.value = null
    editMode.value = false
    deleteError.value = ''

    await getOperations(
      0,
      filtersActive.value
    )
  } catch (err) {
    deleteError.value =
      getErrorMessage(
        err,
        'Failed to delete operation'
      )
  }
}

async function createOperation() {
  createError.value = ''

  try {
    await apiFetch(
      OPERATIONS_API,
      {
        method: 'POST',

        headers: {
          'Content-Type':
            'application/json',
        },

        body: JSON.stringify(
          newOperation.value
        ),
      }
    )

    newOperation.value = {
      title: '',
      description: '',
      operationDate: '',
      operationTime: '',
      status: 'PLANNED',
    }

    createError.value = ''
    showCreateForm.value = false

    await getOperations(
      0,
      filtersActive.value
    )
  } catch (err) {
    createError.value =
      getErrorMessage(
        err,
        'Failed to create operation'
      )
  }
}

onMounted(() => {
  getOperations(
    0,
    false
  )
})
</script>

<template>
  <main>
    <div class="page-header">
      <div>
        <h1>Operations</h1>

        <p>
          Manage operation records.
        </p>
      </div>

      <button
        class="add-button"
        @click="toggleCreateForm"
      >
        + Add Operation
      </button>
    </div>

    <div
      v-if="showCreateForm"
      class="create-form"
    >
      <h3>New Operation</h3>

      <div class="create-grid">
        <input
          v-model="newOperation.title"
          type="text"
          placeholder="Title"
        />

        <input
          v-model="
            newOperation.description
          "
          type="text"
          placeholder="Description"
        />

        <input
          v-model="
            newOperation.operationDate
          "
          type="date"
        />

        <input
          v-model="
            newOperation.operationTime
          "
          type="time"
        />

        <select
          v-model="newOperation.status"
        >
          <option value="PLANNED">
            PLANNED
          </option>

          <option value="IN_PROGRESS">
            IN_PROGRESS
          </option>

          <option value="COMPLETED">
            COMPLETED
          </option>
        </select>
      </div>

      <div
        v-if="createError"
        class="form-error"
      >
        <strong>
          Unable to create operation
        </strong>

        <p>
          {{ createError }}
        </p>
      </div>

      <button
        class="create-button"
        @click="createOperation"
      >
        Create Operation
      </button>
    </div>

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
        <label>Status</label>

        <select v-model="status">
          <option value="">
            All
          </option>

          <option value="PLANNED">
            Planned
          </option>

          <option value="IN_PROGRESS">
            In Progress
          </option>

          <option value="COMPLETED">
            Completed
          </option>
        </select>
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

        <select
          v-model="sortDirection"
        >
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
        @click="searchOperations"
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

    <div
      v-if="loading"
      class="state-message"
    >
      Loading operations...
    </div>

    <div
      v-else-if="pageError"
      class="error-state"
    >
      <strong>
        Unable to load operations
      </strong>

      <p>
        {{ pageError }}
      </p>

      <button
        class="retry-button"
        @click="
          getOperations(
            currentPage,
            filtersActive
          )
        "
      >
        Try Again
      </button>
    </div>

    <div
      v-else-if="
        operations.length === 0
      "
      class="empty-state"
    >
      <strong>
        No operations found
      </strong>

      <p>
        {{
          filtersActive
            ? 'No operations match the selected filters.'
            : 'There are no operations to display yet.'
        }}
      </p>
    </div>

    <table v-else>
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
        v-for="
            operation in operations
          "
        :key="operation.id"
        class="operation-row"
        @click="
            openOperation(operation)
          "
      >
        <td>
          {{ operation.id }}
        </td>

        <td>
          {{ operation.title }}
        </td>

        <td>
          {{
            operation.operationDate
          }}
        </td>

        <td>
          {{
            operation.operationTime
          }}
        </td>

        <td>
          {{ operation.status }}
        </td>
      </tr>
      </tbody>
    </table>

    <div
      v-if="selectedOperation"
      class="modal-overlay"
      @click.self="closeOperation"
    >
      <div class="modal">
        <div class="modal-header">
          <h2>
            Operation Details
          </h2>

          <button
            class="close-button"
            @click="closeOperation"
          >
            ×
          </button>
        </div>

        <div
          v-if="editMode"
          class="edit-form"
        >
          <label>
            Title

            <input
              v-model="
                editOperation.title
              "
              type="text"
            />
          </label>

          <label>
            Description

            <input
              v-model="
                editOperation.description
              "
              type="text"
            />
          </label>

          <label>
            Date

            <input
              v-model="
                editOperation.operationDate
              "
              type="date"
            />
          </label>

          <label>
            Time

            <input
              v-model="
                editOperation.operationTime
              "
              type="time"
            />
          </label>

          <label>
            Status

            <select
              v-model="
                editOperation.status
              "
            >
              <option value="PLANNED">
                PLANNED
              </option>

              <option
                value="IN_PROGRESS"
              >
                IN_PROGRESS
              </option>

              <option
                value="COMPLETED"
              >
                COMPLETED
              </option>
            </select>
          </label>

          <div
            v-if="editError"
            class="form-error"
          >
            <strong>
              Unable to update operation
            </strong>

            <p>
              {{ editError }}
            </p>
          </div>

          <div class="edit-actions">
            <button
              class="save-button"
              @click="updateOperation"
            >
              Save Changes
            </button>

            <button
              class="clear-button"
              @click="cancelEdit"
            >
              Cancel
            </button>
          </div>
        </div>

        <div v-else>
          <div class="detail-row">
            <strong>ID</strong>

            <span>
              {{
                selectedOperation.id
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>Title</strong>

            <span>
              {{
                selectedOperation.title
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>
              Description
            </strong>

            <span>
              {{
                selectedOperation.description ||
                '-'
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>Date</strong>

            <span>
              {{
                selectedOperation.operationDate
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>Time</strong>

            <span>
              {{
                selectedOperation.operationTime
              }}
            </span>
          </div>

          <div class="detail-row">
            <strong>Status</strong>

            <span>
              {{
                selectedOperation.status
              }}
            </span>
          </div>

          <div
            v-if="deleteError"
            class="form-error"
          >
            <strong>
              Unable to delete operation
            </strong>

            <p>
              {{ deleteError }}
            </p>
          </div>

          <div class="modal-actions">
            <button
              class="edit-button"
              @click="startEdit"
            >
              Edit
            </button>

            <button
              v-if="isAdmin"
              class="delete-button"
              @click="deleteOperation"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination">
      <div class="record-count">
        {{ totalElements }} records
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
          {{ currentPage + 1 }}
          of
          {{ totalPages || 1 }}
        </span>

        <button
          :disabled="
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
  </main>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-button {
  padding: 10px 16px;
  border: none;
  border-radius: 5px;
  background: #1f2937;
  color: white;
  cursor: pointer;
}

.create-form {
  margin: 20px 0;
  padding: 20px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
}

.create-form h3 {
  margin-top: 0;
}

.create-grid {
  display: grid;
  grid-template-columns:
    repeat(5, 1fr);
  gap: 12px;
}

.create-grid input,
.create-grid select {
  padding: 9px 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.create-button {
  margin-top: 16px;
  padding: 10px 18px;
  border: none;
  border-radius: 5px;
  background: #1f2937;
  color: white;
  cursor: pointer;
}

.create-button:hover {
  background: #374151;
}

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

.operation-row {
  cursor: pointer;
}

.operation-row:hover {
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

.page-controls button:hover:not(
  :disabled
) {
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
  background:
    rgba(0, 0, 0, 0.45);
  z-index: 1000;
}

.modal {
  width: 500px;
  max-width: 90%;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow:
    0 10px 30px
    rgba(0, 0, 0, 0.2);
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
  grid-template-columns:
    130px 1fr;
  padding: 12px 0;
  border-bottom:
    1px solid #eee;
}

.detail-row:last-child {
  border-bottom: none;
}

.edit-button {
  margin-top: 18px;
  padding: 9px 18px;
  border: none;
  border-radius: 5px;
  background: #1f2937;
  color: white;
  cursor: pointer;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.edit-form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
}

.edit-form input,
.edit-form select {
  padding: 9px 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.edit-actions {
  display: flex;
  gap: 10px;
}

.save-button {
  padding: 10px 18px;
  border: none;
  border-radius: 5px;
  background: #1f2937;
  color: white;
  cursor: pointer;
}

.save-button:hover {
  background: #374151;
}

.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 18px;
}

.modal-actions .edit-button {
  margin-top: 0;
}

.delete-button {
  padding: 9px 18px;
  border: 1px solid #dc2626;
  border-radius: 5px;
  background: white;
  color: #dc2626;
  cursor: pointer;
}

.delete-button:hover {
  background: #fef2f2;
}

.state-message {
  margin-top: 24px;
  padding: 20px;
  text-align: center;
  color: #4b5563;
}

.error-state,
.empty-state {
  margin-top: 24px;
  padding: 24px;
  border:
    1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  text-align: center;
}

.error-state {
  border-color: #fecaca;
}

.error-state strong {
  color: #b91c1c;
}

.error-state p,
.empty-state p {
  margin: 8px 0 0;
  color: #6b7280;
}

.form-error {
  margin-top: 16px;
  padding: 12px 14px;
  border: 1px solid #fecaca;
  border-radius: 6px;
  background: #fef2f2;
  color: #991b1b;
}

.form-error strong {
  display: block;
}

.form-error p {
  margin:
    5px 0 0;
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

@media (max-width: 1100px) {
  .filters {
    flex-wrap: wrap;
  }

  .create-grid {
    grid-template-columns:
      repeat(2, 1fr);
  }
}

@media (max-width: 700px) {
  .page-header {
    align-items: flex-start;
    gap: 16px;
  }

  .create-grid {
    grid-template-columns: 1fr;
  }

  .filters {
    align-items: stretch;
    flex-direction: column;
  }

  .pagination {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  table {
    display: block;
    overflow-x: auto;
  }
}
</style>
