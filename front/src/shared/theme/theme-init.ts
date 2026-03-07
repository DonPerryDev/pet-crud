import { useEffect, useSyncExternalStore } from 'react'
import { useThemeStore } from './theme-store'
import type { ResolvedTheme } from './types'

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

export function ThemeInit(): null {
  const theme = useThemeStore((s) => s.theme)
  const prefersDark = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot)

  const resolved: ResolvedTheme = theme === 'system'
    ? (prefersDark ? 'dark' : 'light')
    : theme

  useEffect(() => {
    const root = document.documentElement
    if (resolved === 'dark') {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
  }, [resolved])

  return null
}
