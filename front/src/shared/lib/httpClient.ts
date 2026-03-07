const BASE_URL = (import.meta.env['VITE_API_URL'] as string | undefined) ?? 'http://localhost:8080'

let _token: string | null = null

export function setAuthToken(token: string | null): void {
  _token = token
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }

  if (_token !== null) {
    headers['Authorization'] = `Bearer ${_token}`
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    ...init,
    headers: { ...headers, ...(init?.headers as Record<string, string> | undefined) },
  })

  if (!res.ok) {
    throw new Error(`HTTP ${res.status}: ${res.statusText}`)
  }

  if (res.status === 204) {
    return undefined as unknown as T
  }

  const data: unknown = await res.json()
  return data as T
}

export const httpClient = {
  get<T>(path: string): Promise<T> {
    return request<T>(path)
  },

  post<T>(path: string, body: unknown): Promise<T> {
    return request<T>(path, { method: 'POST', body: JSON.stringify(body) })
  },

  put<T>(path: string, body: unknown): Promise<T> {
    return request<T>(path, { method: 'PUT', body: JSON.stringify(body) })
  },

  delete(path: string): Promise<void> {
    return request<void>(path, { method: 'DELETE' })
  },
}
