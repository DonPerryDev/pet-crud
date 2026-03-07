import { create } from 'zustand'
import type { Theme } from './types'

const STORAGE_KEY = 'pet-app-theme'

function readStoredTheme(): Theme {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'light' || stored === 'dark' || stored === 'system') {
    return stored
  }
  return 'system'
}

interface ThemeState {
  theme: Theme
  setTheme: (theme: Theme) => void
}

export const useThemeStore = create<ThemeState>()((set) => ({
  theme: readStoredTheme(),
  setTheme: (theme): void => {
    localStorage.setItem(STORAGE_KEY, theme)
    set({ theme })
  },
}))
