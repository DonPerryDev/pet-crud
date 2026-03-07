import { useSyncExternalStore } from 'react'
import { useThemeStore } from './theme-store'
import type { ResolvedTheme, Theme } from './types'

const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

function subscribe(callback: () => void): () => void {
  mediaQuery.addEventListener('change', callback)
  return (): void => {
    mediaQuery.removeEventListener('change', callback)
  }
}

function getSnapshot(): boolean {
  return mediaQuery.matches
}

function getServerSnapshot(): boolean {
  return false
}

interface UseThemeReturn {
  theme: Theme
  resolvedTheme: ResolvedTheme
  setTheme: (theme: Theme) => void
}

export function useTheme(): UseThemeReturn {
  const theme = useThemeStore((s) => s.theme)
  const setTheme = useThemeStore((s) => s.setTheme)
  const prefersDark = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot)

  const resolvedTheme: ResolvedTheme = theme === 'system'
    ? (prefersDark ? 'dark' : 'light')
    : theme

  return { theme, resolvedTheme, setTheme }
}
