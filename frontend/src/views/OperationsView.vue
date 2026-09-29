<script setup>
import { onMounted, ref } from 'vue'
import { OPERATIONS_API } from '../config/api'

const operations = ref([])
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

async function getOperations(page = 0, useFilters = false) {
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
        data.error ||
        'Failed to load operations'
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

function clearFilters() {
  startDate.value = ''
  endDate.value = ''
  startTime.value = ''
  endTime.value = ''

  sortBy.value = 'operationDate'
  sortDirection.value = 'desc'

  filtersActive.value = false

  getOperations(0, false)
}

function changePage(page) {
  getOperations(
    page,
    filtersActive.value
  )
}

function startEdit() {
  editOperation.value = {
    title:
    selectedOperation.value.title,

    description:
      selectedOperation.value.description || '',

    operationDate:
    selectedOperation.value.operationDate,

    operationTime:
    selectedOperation.value.operationTime,

    status:
    selectedOperation.value.status,
  }

  editMode.value = true
}

async function updateOperation() {
  error.value = ''

  try {
    const response = await fetch(
      `${OPERATIONS_API}/${selectedOperation.value.id}`,
      {
        method: 'PUT',

        headers: {
          'Content-Type': 'application/json',
        },

        body: JSON.stringify(
          editOperation.value
        ),
      }
    )

    if (!response.ok) {
      const data = await response.json()

      const message =
        data.error ||
        Object.values(data).join(', ') ||
        'Failed to update operation'

      throw new Error(message)
    }

    const updatedOperation =
      await response.json()

    selectedOperation.value =
      updatedOperation

    editMode.value = false

    await getOperations(
      currentPage.value,
      filtersActive.value
    )
  } catch (err) {
    error.value = err.message
  }
}

async function deleteOperation() {
  const confirmed = window.confirm(
    `Delete "${selectedOperation.value.title}"?`
  )

  if (!confirmed) {
    return
  }

  error.value = ''

  try {
    const response = await fetch(
      `${OPERATIONS_API}/${selectedOperation.value.id}`,
      {
        method: 'DELETE',
      }
    )

    if (!response.ok) {
      throw new Error(
        'Failed to delete operation'
      )
    }

    selectedOperation.value = null
    editMode.value = false

    await getOperations(
      0,
      filtersActive.value
    )
  } catch (err) {
    error.value = err.message
  }
}

async function createOperation() {
  error.value = ''

  try {
    const response = await fetch(
      OPERATIONS_API,
      {
        method: 'POST',

        headers: {
          'Content-Type': 'application/json',
        },

        body: JSON.stringify(
          newOperation.value
        ),
      }
    )

    if (!response.ok) {
      const data = await response.json()

      const message =
        data.error ||
        Object.values(data).join(', ') ||
        'Failed to create operation'

      throw new Error(message)
    }

    newOperation.value = {
      title: '',
      description: '',
      operationDate: '',
      operationTime: '',
      status: 'PLANNED',
    }

    showCreateForm.value = false

    await getOperations(
      0,
      filtersActive.value
    )
  } catch (err) {
    error.value = err.message
  }
}

onMounted(() => {
  getOperations(0, false)
})
</script>
<template>
  <main>
    <div class="page-header">
      <div>
        <h1>Operations</h1>
        <p>Manage operation records.</p>
      </div>

      <button
        class="add-button"
        @click="showCreateForm = !showCreateForm"
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
          v-model="newOperation.description"
          type="text"
          placeholder="Description"
        />

        <input
          v-model="newOperation.operationDate"
          type="date"
        />

        <input
          v-model="newOperation.operationTime"
          type="time"
        />

        <select v-model="newOperation.status">
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
        @click="filtersActive = true; getOperations(0, true)"
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
      Loading operations...
    </p>

    <p
      v-else-if="error"
      class="error-message"
    >
      {{ error }}
    </p>

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
        v-for="operation in operations"
        :key="operation.id"
        class="operation-row"
        @click="selectedOperation = operation"
      >
        <td>{{ operation.id }}</td>

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
          {{ operation.status }}
        </td>
      </tr>
      </tbody>
    </table>

    <div
      v-if="selectedOperation"
      class="modal-overlay"
      @click.self="selectedOperation = null"
    >
      <div class="modal">
        <div class="modal-header">
          <h2>Operation Details</h2>

          <button
            class="close-button"
            @click="selectedOperation = null"
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
              v-model="editOperation.title"
              type="text"
            />
          </label>

          <label>
            Description

            <input
              v-model="editOperation.description"
              type="text"
            />
          </label>

          <label>
            Date

            <input
              v-model="editOperation.operationDate"
              type="date"
            />
          </label>

          <label>
            Time

            <input
              v-model="editOperation.operationTime"
              type="time"
            />
          </label>

          <label>
            Status

            <select
              v-model="editOperation.status"
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
          </label>

          <button
            class="save-button"
            @click="updateOperation"
          >
            Save Changes
          </button>

          <button
            class="clear-button"
            @click="editMode = false"
          >
            Cancel
          </button>
        </div>

        <div v-else>
          <div class="detail-row">
            <strong>ID</strong>
            <span>
              {{ selectedOperation.id }}
            </span>
          </div>

          <div class="detail-row">
            <strong>Title</strong>
            <span>
              {{ selectedOperation.title }}
            </span>
          </div>

          <div class="detail-row">
            <strong>Description</strong>
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
              {{ selectedOperation.status }}
            </span>
          </div>

          <div class="modal-actions">
            <button
              class="edit-button"
              @click="startEdit"
            >
              Edit
            </button>

            <button
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
          :disabled="currentPage === 0"
          @click="
            changePage(currentPage - 1)
          "
        >
          Previous
        </button>

        <span>
          Page {{ currentPage + 1 }}
          of {{ totalPages || 1 }}
        </span>

        <button
          :disabled="
            currentPage >= totalPages - 1
          "
          @click="
            changePage(currentPage + 1)
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
  grid-template-columns: repeat(5, 1fr);
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
  background: rgba(0, 0, 0, 0.45);
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
  grid-template-columns: 130px 1fr;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
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

.error-message {
  color: #b91c1c;
}
</style>
