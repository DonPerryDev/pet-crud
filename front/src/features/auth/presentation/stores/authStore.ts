import { create } from 'zustand'
import { setAuthToken } from '@/shared/lib/httpClient'
import { tokenStorage } from '../../infrastructure/token-storage'
import type { AuthUser } from '../../domain/entities'

// DEV ONLY — remove before connecting a real identity provider
const DEV_TOKEN = (import.meta.env['VITE_DEV_TOKEN'] as string | undefined) ?? null

function decodeUser(token: string): AuthUser | null {
  try {
    const payload = token.split('.')[1]
    if (payload === undefined) return null
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/'))) as Record<string, unknown>
    const userId = decoded['user_id']
    if (typeof userId !== 'string') return null
    return { id: userId }
  } catch {
    return null
  }
}

interface AuthState {
  token: string | null
  user: AuthUser | null
  isAuthenticated: boolean
  login: (token: string) => void
  logout: () => void
}

function initFromStorage(): Pick<AuthState, 'token' | 'user' | 'isAuthenticated'> {
  const token = tokenStorage.get() ?? DEV_TOKEN
  if (token === null) return { token: null, user: null, isAuthenticated: false }

  const user = decodeUser(token)
  if (user === null) {
    tokenStorage.clear()
    return { token: null, user: null, isAuthenticated: false }
  }

  setAuthToken(token)
  return { token, user, isAuthenticated: true }
}

export const useAuthStore = create<AuthState>((set) => ({
  ...initFromStorage(),

  login(token: string): void {
    const user = decodeUser(token)
    tokenStorage.set(token)
    setAuthToken(token)
    set({ token, user, isAuthenticated: user !== null })
  },

  logout(): void {
    tokenStorage.clear()
    setAuthToken(null)
    set({ token: null, user: null, isAuthenticated: false })
  },
}))
