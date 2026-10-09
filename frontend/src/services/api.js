import {
  getToken,
  removeToken,
} from './auth'

export class ApiError extends Error {
  constructor({
                status,
                error,
                message,
                path,
                timestamp,
                validationErrors,
              }) {
    super(
      message ||
      'An unexpected error occurred'
    )

    this.name = 'ApiError'
    this.status = status || 500
    this.error = error || 'Error'
    this.path = path || ''
    this.timestamp = timestamp || null
    this.validationErrors =
      validationErrors || null
  }
}

export async function apiFetch(
  url,
  options = {}
) {
  const token = getToken()

  const headers =
    new Headers(
      options.headers || {}
    )

  if (token) {
    headers.set(
      'Authorization',
      `Bearer ${token}`
    )
  }

  const response =
    await fetch(
      url,
      {
        ...options,
        headers,
      }
    )

  if (response.status === 401) {
    removeToken()

    if (
      window.location.pathname !==
      '/login'
    ) {
      window.location.replace(
        '/login'
      )
    }

    return response
  }

  if (!response.ok) {
    let errorData = null

    try {
      errorData =
        await response.json()
    } catch {
      errorData = null
    }

    throw new ApiError({
      status:
      response.status,

      error:
        errorData?.error ||
        response.statusText,

      message:
        errorData?.message ||
        `Request failed with status ${response.status}`,

      path:
        errorData?.path || '',

      timestamp:
        errorData?.timestamp || null,

      validationErrors:
        errorData?.validationErrors ||
        null,
    })
  }

  return response
}
