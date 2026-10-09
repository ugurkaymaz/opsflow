import { LOGIN_API } from '../config/api'

const TOKEN_KEY = 'opsflow_token'

export function saveToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function isAuthenticated() {
  return Boolean(getToken())
}

export function getCurrentUser() {
  const token = getToken()

  if (!token) {
    return null
  }

  try {
    const parts = token.split('.')

    if (parts.length !== 3) {
      return null
    }

    const base64Url = parts[1]

    const base64 = base64Url
      .replace(/-/g, '+')
      .replace(/_/g, '/')
      .padEnd(
        base64Url.length + (4 - (base64Url.length % 4)) % 4,
        '='
      )

    const claims = JSON.parse(atob(base64))

    return {
      id: claims.userId ?? null,
      name: claims.name ?? '',
      email: claims.sub ?? '',
      role: claims.role ?? '',
    }
  } catch (error) {
    console.error('Failed to decode authentication token', error)

    return null
  }
}

export async function login(email, password) {
  const response = await fetch(LOGIN_API, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email,
      password,
    }),
  })

  if (!response.ok) {
    throw new Error('Invalid email or password')
  }

  const data = await response.json()

  if (!data.token) {
    throw new Error('Authentication token was not returned')
  }

  saveToken(data.token)

  return data
}
