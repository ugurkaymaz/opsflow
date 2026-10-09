
export const API_BASE_URL =
  (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')

export const AUTH_API =
  `${API_BASE_URL}/api/auth`

export const LOGIN_API =
  `${AUTH_API}/login`

export const OPERATIONS_API =
  `${API_BASE_URL}/api/operations`

export const HISTORY_API =
  `${OPERATIONS_API}/history`

export const REPORTS_API =
  `${API_BASE_URL}/api/reports`
